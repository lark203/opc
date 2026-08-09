package com.atlantafx.core.view;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXButton;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.manager.DIContainer;
import com.atlantafx.core.util.AppClassScanner;
import io.github.classgraph.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ViewFactory {
    private static final Logger log = LoggerFactory.getLogger(ViewFactory.class);

    // 核心仓库：页面ID -> 类定义
    private static final Map<String, String> PAGE_REGISTRY_ID = new HashMap<>();
    // 菜单项元数据：页面ID -> 页面元数据
    private static final Map<String, PageMeta> PAGE_ID_TO_TITLE = new HashMap<>();
    // 文件夹名称索引：所有作为父级菜单的名称集合（用于 O(1) 判断是否为文件夹）
    private static final Set<String> FOLDER_NAMES = new HashSet<>();
    // 菜单项列表（已排序）
    private static final List<PageMeta> MENU_ITEMS = new ArrayList<>();
    // 页面实例缓存：桌面应用页面数量有限，使用 HashMap 保持页面状态稳定
    // （WeakHashMap 在内存紧张时可能回收页面导致用户状态丢失）
    private static final Map<String, Node> INSTANCE_CACHE = new HashMap<>();
    // 默认首页的 ID
    private static String defaultPageId = null;

    static {
        // 第一次用到这个类的地方会触发类初始化，此时会调用 initAndScan() 方法。（Sidebar中第一次用到）
        initAndScan();
    }

    private static void initAndScan() {
        // 复用全局 ClassGraph 扫描结果（与 DB 扫描合并为一次），避免重复全量扫描
        ScanResult scanResult = AppClassScanner.get();

        ClassInfoList pages = scanResult.getClassesWithAnnotation(Page.class.getName());

        for (ClassInfo info : pages) {
            // 核心优化：直接从元数据读取注解参数，不 loadClass
            AnnotationInfo routeAnn = info.getAnnotationInfo(Page.class.getName());
            AnnotationParameterValueList params = routeAnn.getParameterValues();

            // 获取注解值
            String id = (String) params.getValue("id");
            String name = (String) params.getValue("name");
            String icon = (String) params.getValue("icon");
            int order = (Integer) params.getValue("order");
            String titleParam = (String) params.getValue("title");
            boolean isDefault = (Boolean) params.getValue("isDefault");
            String parentName = (String) params.getValue("parentName");
            int level = (Integer) params.getValue("level");
            boolean isHidden = (Boolean) params.getValue("isHidden");
            boolean lazyLoad = (Boolean) params.getValue("lazyLoad");

            // 如果 title 为空，使用 name 作为标题
            String title = StringUtils.isNotBlank(titleParam) ? titleParam : name;

            // 存储类名字符串，使用 ID 作为 key
            PAGE_REGISTRY_ID.put(id, info.getName());

            PageMeta meta = new PageMeta(id, name, title, icon, order, parentName, level, isHidden, lazyLoad);
            MENU_ITEMS.add(meta);
            PAGE_ID_TO_TITLE.put(id, meta);

            // 构建文件夹索引：收集所有作为父级菜单的名称
            if (StringUtils.isNotBlank(parentName)) {
                FOLDER_NAMES.add(parentName);
            }

            if (isDefault) {
                defaultPageId = id;
            }
            log.debug("发现页面元数据: id={}, name={}", id, name);
        }
        MENU_ITEMS.sort(Comparator.comparing(PageMeta::level).thenComparingInt(PageMeta::order).thenComparing(PageMeta::id));
        log.debug("已加载 {} 个页面元数据", MENU_ITEMS.size());
    }

    /**
     * 当前活动页面 ID
     */
    private static String currentPageId = null;

    /**
     * 根据页面 ID 或名称创建视图实例
     * <p>
     * 支持两种调用方式：
     * 1. 使用页面 ID（推荐）：createView("dashboard")
     * 2. 使用菜单名称（兼容旧代码）：createView("数据大屏")
     *
     * @param pageId 页面 ID 或菜单名称
     * @return 视图节点，如果加载失败返回错误页面
     */
    public static Node createView(String pageId) {
        // 1. 先看缓存
        if (INSTANCE_CACHE.containsKey(pageId)) {
            return INSTANCE_CACHE.get(pageId);
        }

        // 2. 缓存没有则按需加载类并实例化
        String className = PAGE_REGISTRY_ID.get(pageId);
        try {
            Class<? extends Node> clazz = (Class<? extends Node>) Class.forName(className);
            Node instance = DIContainer.get(clazz);

            // 触发 onCreated 生命周期
            if (instance instanceof PageLifecycle lifecycle) {
                lifecycle.onCreated();
            }

            // 存入缓存（使用 ID 作为 key）
            INSTANCE_CACHE.put(pageId, instance);
            return instance;
        } catch (Exception e) {
            log.error("动态加载并创建视图失败: pageId={}, className={}", pageId, className, e);
            return createErrorPage(pageId, "页面加载失败: " + e.getMessage());
        }
    }

    /**
     * 显示指定页面并触发生命周期
     *
     * @param pageId 页面 ID
     * @return 视图节点
     */
    public static Node showPage(String pageId) {
        // 取消闲置定时器
        cancelIdleTimer(pageId);

        Node view = createView(pageId);

        // 触发生命周期：先隐藏当前页面，再显示新页面
        if (currentPageId != null && !currentPageId.equals(pageId)) {
            hidePage(currentPageId);
        }

        // 触发 onShow 生命周期
        if (view instanceof PageLifecycle lifecycle) {
            // 触发 onInit 生命周期
            if (!lifecycle.isInitialized() && !lifecycle.isDisposed()) {
                lifecycle.onInit();
            }
            lifecycle.onShow();
        }

        currentPageId = pageId;
        return view;
    }

    /**
     * 隐藏指定页面（触发 onHide 生命周期）
     *
     * @param pageId 页面 ID
     */
    public static void hidePage(String pageId) {
        Node view = INSTANCE_CACHE.get(pageId);
        if (view instanceof PageLifecycle lifecycle) {
            lifecycle.onHide();
            schedulePageEviction(pageId);
        }
    }

    /**
     * 销毁指定页面（从缓存移除并触发 onDispose 生命周期）
     *
     * @param pageId 页面 ID
     */
    public static void disposePage(String pageId) {
        Node view = INSTANCE_CACHE.remove(pageId);
        if (view instanceof PageLifecycle lifecycle) {
            lifecycle.onDispose();
        }
        log.debug("页面已销毁: {}", pageId);
    }

    /**
     * 销毁所有页面
     */
    public static void disposeAllPages() {
        for (String pageId : new ArrayList<>(INSTANCE_CACHE.keySet())) {
            disposePage(pageId);
        }
        currentPageId = null;
    }

    /**
     * 获取当前活动页面 ID
     */
    public static String getCurrentPageId() {
        return currentPageId;
    }

    /**
     * 创建错误页面
     */
    private static Node createErrorPage(String pageId, String message) {
        return FXVBox.create()
                .align(Pos.CENTER)
                .spacing(16)
                .padding(40)
                .add(
                        FXLabel.create("页面加载失败").h2().styleCss("-fx-text-fill: -color-danger-emphasis;"),
                        FXLabel.create(message).bold().wrapText(true),
                        FXLabel.create("页面: " + pageId).styleCss("-fx-text-fill: -color-fg-muted;"),
                        FXButton.create("重试").onAction(e -> AppContext.navigateTo(pageId))
                );
    }

    public static List<PageMeta> getMenuItems() {
        return Collections.unmodifiableList(MENU_ITEMS);
    }

    /**
     * 获取默认页面 ID
     */
    public static String getDefaultPageId() {
        return defaultPageId != null ? defaultPageId :
                (MENU_ITEMS.isEmpty() ? null : MENU_ITEMS.getFirst().id());
    }

    /**
     * 菜单元数据模型
     */
    public record PageMeta(String id, String name, String title, String icon, int order, String parentName, int level,
                           boolean isHidden, boolean lazyLoad) {
    }

    public static String getPageTitleById(String pageId) {
        PageMeta meta = PAGE_ID_TO_TITLE.get(pageId);
        return meta != null ? meta.title() : "";
    }

    /**
     * 判断指定名称是否为文件夹（即是否有子菜单）
     * <p>
     * 时间复杂度：O(1)，通过 HashSet 索引实现
     *
     * @param name 菜单名称
     * @return true 表示是文件夹，false 表示是叶子节点
     */
    public static boolean isFolder(String name) {
        return FOLDER_NAMES.contains(name);
    }

    /**
     * 预加载所有非懒加载页面
     * <p>
     * 在应用启动时调用，将高频使用的核心页面提前实例化到缓存中
     * 可以显著提升用户首次访问这些页面时的响应速度
     */
    public static void preloadEagerPages() {
        if (!AppState.getInstance().isPreloadMessages()) {
            log.info("已禁用预加载消息，跳过预加载...");
            return;
        }
        log.info("开始预加载非懒加载页面...");
        int count = 0;
        for (PageMeta meta : MENU_ITEMS) {
            if (!meta.lazyLoad() && !meta.isHidden()) {
                try {
                    // 触发页面创建并缓存
                    createView(meta.id());
                    count++;
                    log.debug("已预加载页面: id={}, name={}", meta.id(), meta.name());
                } catch (Exception e) {
                    log.error("预加载页面失败: id={}, name={}", meta.id(), meta.name(), e);
                }
            }
        }
        log.info("预加载完成，共加载 {} 个页面", count);
    }

    // ****************************************************************************************************************
    // 页面销毁逻辑
    // ****************************************************************************************************************

    // 为每个离开的页面托管一个专属的销毁时钟
    private static final Map<String, Timeline> IDLE_TIMERS = new HashMap<>();

    /**
     * 核心 facts：为离开的页面挂载 15 分钟物理倒计时
     */
    private static void schedulePageEviction(String pageId) {
        // 绝不回收首页/默认大屏，防止频繁重构主外壳
        PageMeta pageMeta = PAGE_ID_TO_TITLE.get(pageId);
        int idleTime = AppState.getInstance().getIdleTime();
        // 非懒加载页面不销毁，销毁时间 小于等于5 表示不销毁
        if (!pageMeta.lazyLoad || idleTime <= 5) {
            return;
        }

        cancelIdleTimer(pageId); // 规避重复挂载

        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(idleTime), event -> {
            // 时间到，触发彻底解耦卸载
            evictPageFromMemory(pageId);
        }));
        timeline.setCycleCount(1);

        IDLE_TIMERS.put(pageId, timeline);
        timeline.play();
        log.info("页面 [{}] 已转入后台不活跃状态，{}分钟倒计时启动...", pageId, idleTime);
    }

    /**
     * 核心 facts：取消销毁时钟
     */
    private static void cancelIdleTimer(String pageId) {
        Timeline timer = IDLE_TIMERS.remove(pageId);
        if (timer != null) {
            timer.stop();
            log.debug("页面 [{}] 重新被激活，后台闲置倒计时已被粉碎释放。", pageId);
        }
    }

    /**
     * 核心回收：断开强引用，触发垃圾回收（GC Memory Eviction）
     */
    private static void evictPageFromMemory(String pageId) {
        IDLE_TIMERS.remove(pageId);
        Node viewToRemove = INSTANCE_CACHE.remove(pageId);

        if (viewToRemove != null) {
            // 1. 触发你架构原有的生命周期解耦方法（如释放数据库、断开 WebSocket）
            if (viewToRemove instanceof PageLifecycle lifecycle) {
                try {
                    lifecycle.onDispose();
                } catch (Exception e) {
                    log.error("页面注销声明生命周期回调失败: ", e);
                }
            }

            // 2. 清空该节点下所有的子组件，协助 OS 加速解除底层图形上下文
            if (viewToRemove instanceof Pane pane) {
                pane.getChildren().clear();
            }

            log.warn("🚨 核心页面 [{}] 长时间未操作，已自动从内存卸载。强引用彻底解除。", pageId);

            // 3. 显式告知虚拟机进行轻量级资产整理
            if (IDLE_TIMERS.isEmpty()) {
                log.warn("已无其他页面处于后台闲置状态，已触发虚拟机进行轻量级资产整理...");
                System.gc();
            }
        }
    }
}
