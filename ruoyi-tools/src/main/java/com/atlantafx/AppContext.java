package com.atlantafx;

import com.atlantafx.components.functional.FXMenuSearchDialog;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.db.DatabaseManager;
import com.atlantafx.core.manager.NavigationService;
import com.atlantafx.core.manager.NotificationService;
import com.atlantafx.core.manager.TaskStateService;
import com.atlantafx.core.service.MessageService;
import com.atlantafx.core.view.MainLayout;
import com.atlantafx.core.view.ViewFactory;
import com.atlantafx.util.HttpUtils;
import com.atlantafx.util.TaskRunner;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 全局上下文，用于跨层级访问核心组件
 * <p>
 * 作为静态门面，将职责委托给专门的服务类：
 * - {@link NavigationService} 页面导航
 * - {@link NotificationService} 消息通知
 * - {@link TaskStateService} 任务状态（进度条、遮罩层）
 */
public final class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    // -------------------------- 全局组件引用 --------------------------
    private static Stage primaryStage;
    private static Scene mainScene;

    // -------------------------- 导航历史栈（用于方向感知动画） --------------------------
    private static final Deque<String> NAVIGATION_STACK = new ArrayDeque<>();
    private static String currentPageId = null;
    private static boolean isForwardNavigation = true;

    public static void init(Stage stage, MainLayout layout, Scene scene) {
        primaryStage = stage;
        mainScene = scene;

        // 初始化各服务
        NavigationService.init(layout);
        NotificationService.init(layout);
        TaskStateService.init(layout);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static MainLayout getMainLayout() {
        // 委托给 NavigationService 获取 layout 引用
        return NavigationService.getMainLayout();
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    /**
     * 退出应用
     */
    public static void exitApp() {
        log.info("正在关闭应用，执行清理任务...");
        ViewFactory.disposeAllPages();
        HttpUtils.shutdown();
        try {
            DatabaseManager.close();
        } catch (Exception e) {
            log.error("关闭数据库时发生错误: {}", e.getMessage());
        }

        Platform.exit();
        System.exit(0);
    }

    // -------------------------- 全局快捷键 --------------------------

    public static void setupGlobalHotkeys() {
        if (mainScene == null) return;

        // 实例化常驻单例搜索模态弹窗 facts
        final FXMenuSearchDialog menuSearchDialog = new FXMenuSearchDialog(primaryStage);

        mainScene.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                log.info("触发全局搜索");
                // 如果当前没在显示，则安全唤醒
                if (!menuSearchDialog.isShowing()) {
                    // 动态锚定在主视窗的几何中央
                    menuSearchDialog.setX(primaryStage.getX() + (primaryStage.getWidth() - menuSearchDialog.getWidth()) / 2);
                    menuSearchDialog.setY(primaryStage.getY() + (primaryStage.getHeight() - menuSearchDialog.getHeight()) / 4);

                    menuSearchDialog.showAndReset();
                }

                event.consume(); // 强行吞噬事件，阻止事件向更深层 UI 节点传递引发冲突
            }
            if (event.isControlDown() && event.getCode() == KeyCode.T) {
                // toggleGlobalTheme();
            }
            if (event.getCode() == KeyCode.F11) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });
    }

    // -------------------------- 委托：导航 --------------------------

    /**
     * 切换到指定页面（从菜单点击，不显示返回按钮）
     *
     * @param pageId 目标页面 ID
     */
    public static void navigateTo(String pageId) {
        navigateTo(pageId, true);
    }

    public static void navigateTo(String pageId, boolean fromMenuClick) {
        currentPageId = pageId;
        NAVIGATION_STACK.clear();
        isForwardNavigation = true;
        NavigationService.navigateTo(pageId, fromMenuClick); // 菜单导航，不显示返回按钮
    }

    /**
     * 切换到指定页面（从页面内跳转，显示返回按钮）
     *
     * @param pageId 目标页面 ID
     */
    public static void navigateFromPage(String pageId) {
        recordNavigation(pageId);
        isForwardNavigation = true;
        // 页面内导航，非菜单点击方式，需要激活对应菜单项
        NavigationService.navigateTo(pageId, false);
    }

    public static void navigateBackPage() {
        if (!NAVIGATION_STACK.isEmpty()) {
            isForwardNavigation = false;
            String previousPageId = NAVIGATION_STACK.pop();
            currentPageId = previousPageId;
            // 返回导航，非菜单点击方式，需要激活对应菜单项
            NavigationService.navigateTo(previousPageId, false);
        }
    }

    /**
     * 获取是否应该显示返回按钮
     *
     * @return true=显示，false=隐藏
     */
    public static boolean shouldShowBackButton() {
        return !NAVIGATION_STACK.isEmpty();
    }

    /**
     * 获取当前页面 ID
     *
     * @return 当前页面 ID
     */
    public static String getCurrentPageId() {
        return currentPageId;
    }

    /**
     * 记录导航历史（由 NavigationService 调用）
     *
     * @param pageId 当前页面 ID
     */
    public static void recordNavigation(String pageId) {
        if (currentPageId != null) {
            NAVIGATION_STACK.push(currentPageId);
        }
        currentPageId = pageId;
    }

    /**
     * 判断导航方向
     *
     * @return true=前进（新页面），false=后退（返回上一页）
     */
    public static boolean isForwardNavigation() {
        return isForwardNavigation;
    }

    // -------------------------- 委托：通知 --------------------------

    /**
     * 展示通知
     *
     * @param message
     * @param level
     */
    public static void showNotification(String message, NotificationLevel level) {
        NotificationService.showNotification(message, level);
    }

    // -------------------------- 委托：消息 --------------------------

    /**
     * 添加导航消息
     *
     * @param title
     * @param content
     * @param targetViewId
     */
    public static void addNavigateMessage(String title, String content, String targetViewId) {
        MessageService.addNavigateMessage(title, content, targetViewId);
    }

    /**
     * 添加警告消息
     *
     * @param title
     * @param content
     */
    public static void addAlertMessage(String title, String content) {
        MessageService.addAlertMessage(title, content);
    }

    /**
     * 添加链接消息
     *
     * @param title
     * @param content
     * @param url
     */
    public static void addLinkMessage(String title, String content, String url) {
        MessageService.addLinkMessage(title, content, url);
    }

    /**
     * 添加任务消息
     *
     * @param title
     * @param content
     * @param taskId
     */
    public static void addTaskMessage(String title, String content, String taskId) {
        MessageService.addTaskMessage(title, content, taskId);
    }

    /**
     * 添加通知消息
     *
     * @param title
     * @param content
     */
    public static void addNotificationMessage(String title, String content) {
        MessageService.addNotificationMessage(title, content);
    }

    // -------------------------- 委托：任务状态 --------------------------

    public static void startLoading() {
        TaskStateService.startLoading();
    }

    public static void startLoading(String message) {
        TaskStateService.startLoading(message);
    }

    public static void stopLoading() {
        TaskStateService.stopLoading();
    }

    public static void stopLoading(String message) {
        TaskStateService.stopLoading(message);
    }

    public static void runTask(String startMessage, String endMessage, Runnable task) {
        TaskStateService.runTask(startMessage, endMessage, task);
    }

    public static void updateMaskStatus(long taskId, double progress, String status) {
        TaskStateService.updateMaskStatus(taskId, progress, status);
    }

    public static void startCriticalTask(long taskId, String initialStatus) {
        TaskStateService.startCriticalTask(taskId, initialStatus);
    }

    public static void stopCriticalTask(long taskId) {
        TaskStateService.stopCriticalTask(taskId);
    }

    public static DoubleProperty globalProgressProperty() {
        return TaskStateService.globalProgressProperty();
    }

    public static StringProperty globalStatusTextProperty() {
        return TaskStateService.globalStatusTextProperty();
    }

    // -------------------------- 全局字体大小 --------------------------

    /**
     * 应用全局字体大小设置
     * 修改 Scene 根节点的 -fx-font-size，所有使用 em 单位的子节点自动跟随缩放
     */
    public static void applyFontSize() {
        if (mainScene == null || mainScene.getRoot() == null) return;
        int fontSize = AppState.getInstance().getFontSize();
        TaskRunner.runInFx(() -> mainScene.getRoot().setStyle("-fx-font-size: " + fontSize + "px;"));
    }
}
