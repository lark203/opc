package com.atlantafx.core.view;

import atlantafx.base.controls.RingProgressIndicator;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXBorderPane;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXStackPane;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.components.layout.HeaderBar;
import com.atlantafx.components.layout.SidebarNavContainer;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.event.EventBus;
import com.atlantafx.core.event.NavEvent;
import com.atlantafx.core.event.NotificationEvent;
import com.atlantafx.core.manager.ViewManager;
import com.atlantafx.util.ViewUtils;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainLayout extends StackPane {
    private static final Logger log = LoggerFactory.getLogger(MainLayout.class); // 修改为继承 StackPane

    // 整体布局
    private final FXBorderPane innerLayout = FXBorderPane.create();
    // 右侧 - 顶部工具栏
    private final HeaderBar header = new HeaderBar();
    // 右侧 - 中间内容区 - 页面
    private final FXStackPane contentArea = FXStackPane.create();
    // 专门存放通知的容器
    private final FXVBox notificationHolder = FXVBox.create(10);

    // 创建全局遮罩层
    private final FXStackPane globalMask = FXStackPane.create();
    // 创建全局遮罩层内容
    private final FXVBox maskContent = FXVBox.create(15);

    // 侧边栏菜单容器（用于非菜单点击时激活对应菜单项）
    private SidebarNavContainer sidebarMenu;
    private VBox rightPane;

    public MainLayout() {
        // 1. 初始化 页面整体布局 BorderPane
        setupInnerLayout();

        // 初始化 页面管理器 ViewManager
        ViewManager.setContentArea(contentArea);

        // 2. 初始化通知容器 (置于右上角，不占位), pickOnBounds=false 点击空隙可以穿透到下方界面
        notificationHolder.align(Pos.TOP_RIGHT).padding(20).mxWidth(Region.USE_PREF_SIZE).mxHeight(Region.USE_PREF_SIZE).setPickOnBounds(false);
        StackPane.setAlignment(notificationHolder, Pos.TOP_RIGHT);

        // 3. 将所有层加入 StackPane
        this.getChildren().addAll(innerLayout, notificationHolder);

        // 5. 创建全局遮罩层
        setupGlobalMask();

        // 6. 订阅事件总线（使用类型安全API）
        EventBus.subscribe(NotificationEvent.class, event -> {
            // 执行通知
            AppContext.showNotification(event.message(), event.level());
        });

        EventBus.subscribe(NavEvent.class, event -> {
            // 执行页面跳转逻辑
            AppContext.navigateFromPage(event.viewId());
        });
    }

    private void setupInnerLayout() {
        // 2. 实例化封装后的纯净菜单大容器
        sidebarMenu = new SidebarNavContainer();
        // 3. 业务层监听全局路由变化切换面板
        sidebarMenu.setOnRouteChanged(AppContext::navigateTo);

        // 临时存储已经创建好的大组容器，防止重复创建
        Map<String, SidebarNavContainer.NavSubMenuFolder> folderContainers = new HashMap<>();
        ViewFactory.getMenuItems().forEach(meta -> {
            if (meta.isHidden()) return;
            // 【核心分流逻辑】假设通过 meta.getParentName() 判断是否是二级菜单
            String parentName = meta.parentName();

            if (StringUtils.isBlank(parentName)) {
                // 情况 A：这是一级菜单（或者是一个普通的无子集的独立页面）
                // 使用 O(1) 的索引判断是否为文件夹
                boolean isFolder = ViewFactory.isFolder(meta.name());

                if (isFolder) {
                    SidebarNavContainer.NavSubMenuFolder folder = new SidebarNavContainer.NavSubMenuFolder(meta.name(), meta.icon());
                    sidebarMenu.addFolder(folder);
                    folderContainers.put(meta.name(), folder);
                } else {
                    // 独立的普通一级菜单
                    SidebarNavContainer.NavMenuItem item = new SidebarNavContainer.NavMenuItem(meta.name(), meta.icon(), meta.id());
                    sidebarMenu.addMenuItem(item);
                    if (meta.id().equals(ViewFactory.getDefaultPageId())) {
                        sidebarMenu.requestActivation(item);
                    }
                }
            } else {
                // 使用 O(1) 的索引判断是否为文件夹
                boolean isFolder = ViewFactory.isFolder(meta.name());
                SidebarNavContainer.NavSubMenuFolder parentContainer = folderContainers.get(parentName);

                // 情况 B：这是二级菜单，找到它的父级容器塞进去
                if (parentContainer != null) {
                    if (isFolder) {
                        SidebarNavContainer.NavSubMenuFolder folder = new SidebarNavContainer.NavSubMenuFolder(meta.name(), meta.icon());
                        parentContainer.addSubFolder(folder);
                        folderContainers.put(meta.name(), folder);
                    } else {
                        // 作为二级子菜单加入
                        SidebarNavContainer.NavMenuItem navMenuItem = new SidebarNavContainer.NavMenuItem(meta.name(), meta.icon(), meta.id());
                        parentContainer.addMenuItem(navMenuItem);
                        if (meta.id().equals(ViewFactory.getDefaultPageId())) {
                            sidebarMenu.requestActivation(navMenuItem);
                        }
                    }
                }
            }
        });

        // 强制固定 Header 的高度范围
        header.setMinHeight(Region.USE_PREF_SIZE);
        header.setMaxHeight(Region.USE_PREF_SIZE);
        // 中间内容区
        contentArea.stylesClass("main-content-area");

        // 右侧主体容器 - 顶部工具栏 + 中间内容
        rightPane = FXVBox.create().vgrowN(header).vgrow(contentArea).add(header, contentArea);

        if (StringUtils.isNotBlank(AppState.getInstance().getBackgroundImageUrl())) {
            rightPane.setOpacity(AppState.getInstance().getContentOpacity());
            changeGlobalBackgroundStyle(AppState.getInstance().getBackgroundImageUrl());
            // 透明度设置 - 背景图片展示
            sidebarMenu.opacity(AppState.getInstance().getSideBarOpacity());
        }

        AppState.getInstance().contentOpacityProperty().addListener(e -> rightPane.setOpacity(AppState.getInstance().getContentOpacity()));
        AppState.getInstance().backgroundImageUrlProperty().addListener((observable, oldValue, newValue) -> changeGlobalBackgroundStyle(newValue));
        AppState.getInstance().sideBarOpacityProperty().addListener(e -> sidebarMenu.opacity(AppState.getInstance().getSideBarOpacity()));

        innerLayout.left(sidebarMenu).center(rightPane);
    }

    /**
     * 统一入口，用于切换页面
     */
    public void switchPage(String pageId, Node newView) {
        // 1. 设置 Header 标题
        header.setTitle(ViewFactory.getPageTitleById(pageId));

        // 2. 动态检测接口并注入工具栏
        if (newView instanceof PageHeaderSupport headerSupport) {
            header.setCustomTools(headerSupport.getHeaderTools());
        } else {
            header.setCustomTools(null); // 清空上一个页面的工具
        }

        // 3. 更新返回按钮可见性
        header.updateBackButtonVisibility();

        // 4. 使用统一控制器切换页面
        ViewManager.switchPage(newView);
    }

    /**
     * 显示通知
     */
    public void showNotification(String message, NotificationLevel level) {
        ViewUtils.showNotification(notificationHolder, message, level);
    }

    /**
     * 设置进度条可见性
     */
    public void setProgressBarVisible(boolean visible) {
        ProgressBar progressBar = header.getProgressBar();
        if (visible) {
            progressBar.setVisible(true);
            // 如果想要更灵动的渐显效果
            progressBar.setOpacity(0);
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), progressBar);
            ft.setToValue(1.0);
            ft.play();
        } else {
            // 渐隐后隐藏
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), progressBar);
            ft.setToValue(0);
            ft.setOnFinished(e -> progressBar.setVisible(false));
            ft.play();
        }
    }

    /**
     * 创建全局遮罩层
     */
    private void setupGlobalMask() {
        globalMask.getStyleClass().add("global-mask");
        globalMask.setVisible(false);
        globalMask.setOpacity(0);

        // 遮罩中间可以加个旋转进度环和提示文字
        RingProgressIndicator ringProgress = new RingProgressIndicator(0, false);
        ringProgress.setPrefSize(50, 50);

        Label maskLabel = FXLabel.create("关键任务处理中，请稍候...").bold();

        // 建立属性绑定 (关键步骤)
        // 进度环绑定：RingProgressIndicator 接受 0-100 的整数或 0-1 的双精度
        ringProgress.progressProperty().bind(AppContext.globalProgressProperty());

        // 文字显示绑定：实时显示 "处理中... (85%)"
        maskLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("%s (%.0f%%)",
                                AppContext.globalStatusTextProperty().get(),
                                AppContext.globalProgressProperty().get() * 100),
                        AppContext.globalStatusTextProperty(),
                        AppContext.globalProgressProperty()
                )
        );

        maskContent.align(Pos.CENTER).add(ringProgress, maskLabel);
        globalMask.getChildren().add(maskContent);

        // 将遮罩添加到 MainLayout (StackPane) 的最顶层
        this.getChildren().add(globalMask);
    }

    /**
     * 暴露遮罩控制开关
     */
    public void setMaskVisible(boolean visible) {
        if (visible) {
            globalMask.setVisible(true);
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), globalMask);
            ft.setToValue(1.0);
            ft.play();
        } else {
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), globalMask);
            ft.setToValue(0);
            ft.setOnFinished(e -> globalMask.setVisible(false));
            ft.play();
        }
    }

    /**
     * 获取侧边栏菜单容器
     * 用于非菜单点击方式的页面跳转时激活对应菜单项
     */
    public SidebarNavContainer getSidebarMenu() {
        return sidebarMenu;
    }

    /**
     * 统一动态切换背景图片管线门面事实
     *
     * @param targetPath 支持 Classpath 路径 (如 "/assets/images/default_bg.png") 或系统绝对路径 (如 "D:\\images\\wallpaper.jpg")
     */
    public void changeGlobalBackgroundStyle(String targetPath) {
        if (StringUtils.isBlank(targetPath)) {
            sidebarMenu.setOpacity(1.0);
            rightPane.setOpacity(1.0);
            this.setStyle("-fx-background-image: null;");
            return;
        }

        try {
            String cleanUrl;
            File file = new File(targetPath);

            // 1. 分支判定事实：判断是否为操作系统的物理绝对路径
            if (file.isAbsolute() && file.exists()) {
                // 核心事实：外部磁盘绝对路径必须包装成标准的 file:/ 协议 URL
                cleanUrl = file.toURI().toURL().toExternalForm();
                log.info("成功截获系统磁盘外部绝对路径 facts: {}", targetPath);
            } else {
                // 2. 降级判定事实：按程序内部包 Classpath 资产提取
                var resource = getClass().getResource(targetPath);
                if (resource == null) {
                    log.error("在系统磁盘和内部程序包内均未找到指定的图片资源事实: {}", targetPath);
                    sidebarMenu.setOpacity(1.0);
                    rightPane.setOpacity(1.0);
                    this.setStyle("-fx-background-image: null;");
                    return;
                }
                cleanUrl = resource.toExternalForm();
            }

            // 5. 纯 Java 拼接高层级 CSS 规则矩阵事实
            // 如果希望背景变暗，用 rgba(0,0,0)；如果希望背景变亮，用 rgba(255,255,255)
            String cssStyle =
                    "-fx-background-image: url('" + cleanUrl + "'), url('" + cleanUrl + "');" +
                            "-fx-background-repeat: no-repeat, no-repeat;" +
                            "-fx-background-size: cover, cover;" +
                            "-fx-background-position: center center, center center;";

            // 6. 覆写样式事实，触发图形硬件刷新
            sidebarMenu.setOpacity(AppState.getInstance().getSideBarOpacity());
            rightPane.setOpacity(AppState.getInstance().getContentOpacity());
            this.setStyle(cssStyle);
            log.info("全局背景样式更换成功 facts. 路径: {}", targetPath);

        } catch (Exception e) {
            log.error("执行动态背景样式切换时发生未预期异常 facts: ", e);
        }
    }
}
