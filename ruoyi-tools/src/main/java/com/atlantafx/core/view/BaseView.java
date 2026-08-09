package com.atlantafx.core.view;

import com.atlantafx.components.base.FXScrollPane;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 所有业务视图的基类
 * 实现页面生命周期管理
 */
public abstract class BaseView extends FXScrollPane implements PageHeaderSupport, PageLifecycle {

    protected static final Logger log = LoggerFactory.getLogger(BaseView.class);

    private boolean initialized = false;
    private boolean disposed = false;

    public BaseView() {
        // 1. 配置滚动面板 2. 彻底透明化 ScrollPane 样式，防止出现多余边框
        this.fitToWidth().fitToHeight().hbar(ScrollPane.ScrollBarPolicy.NEVER)
                .padding(20).background("transparent").styleCss("-fx-background-insets:0;");
    }

    // =========================================================================
    // 1. 核心生命周期控制（由 ViewFactory 调用，声明为 final，禁止子类重写，确保状态绝对安全）
    // =========================================================================

    @Override
    public final void onCreated() {
        log.debug("Page created: {}", getClass().getSimpleName());
        onPageCreated(); // 调用子类钩子
    }

    @Override
    public final void onInit() {
        log.debug("Page init: {}", getClass().getSimpleName());
        initialized = true;
        setContent(onPageInit());       // 调用子类钩子
    }

    @Override
    public void onShow() {
        log.debug("Page shown: {}", getClass().getSimpleName());
    }

    @Override
    public void onHide() {
        log.debug("Page hidden: {}", getClass().getSimpleName());
    }

    @Override
    public final void onDispose() {
        log.debug("Page disposed: {}", getClass().getSimpleName());
        disposed = true;
        onPageDispose();    // 调用子类钩子
    }

    // =========================================================================
    // 2. 对子类开放的保护钩子方法（子类按需重写，不再需要关心 super.xxx()）
    // =========================================================================

    /**
     * 子类重写：组件成员变量初始化
     */
    protected abstract void onPageCreated();

    /**
     * 子类重写：只执行一次的 UI/数据 树构建与事件绑定
     */
    protected abstract Node onPageInit();

    /**
     * 子类重写：资源释放与取消订阅
     */
    protected abstract void onPageDispose();

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * 获取页面名称（用于日志和调试）
     */
    public String getPageName() {
        return getClass().getSimpleName().replace("View", "");
    }

    // =========================================================================
    // 用于异步 AsyncView 类加载数据
    // =========================================================================

    private boolean dataLoaded = false;

    /**
     * 检查数据是否已经加载完成
     *
     * @return true 表示数据已加载，false 表示需要加载
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * 设置数据加载完成状态
     *
     * @param loaded true 表示数据已加载完成
     */
    public void setDataLoaded(boolean loaded) {
        // 默认实现不做任何操作，子类可以重写
        this.dataLoaded = loaded;
    }
}
