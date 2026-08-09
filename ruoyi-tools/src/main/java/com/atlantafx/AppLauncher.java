package com.atlantafx;

import atlantafx.base.util.Animations;
import com.atlantafx.components.base.FXBorderPane;
import com.atlantafx.components.layout.CustomTitleBar;
import com.atlantafx.components.layout.ToolbarInitializer;
import com.atlantafx.components.splash.SplashView;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.config.ConfigStore;
import com.atlantafx.core.db.DatabaseManager;
import com.atlantafx.core.error.GlobalExceptionHandler;
import com.atlantafx.core.service.MessageService;
import com.atlantafx.core.theme.Styles;
import com.atlantafx.core.theme.ThemeManager;
import com.atlantafx.core.view.MainLayout;
import com.atlantafx.core.view.ViewFactory;
import com.atlantafx.util.ResizeHelper;
import com.atlantafx.util.TaskRunner;
import com.atlantafx.util.ViewUtils;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 程序入口，负责启动 JavaFX Application
 */
public class AppLauncher extends Application {

    private static final Logger log = LoggerFactory.getLogger(AppLauncher.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 关键：第一时间注册全局异常处理
        GlobalExceptionHandler.register();

        // 1. 立即显示启动图
        AtomicReference<SplashView> splash = new AtomicReference<>(new SplashView());
        splash.get().show();

        // 2. 使用 Task 执行初始化任务
        Task<MainLayout> initTask = new Task<>() {
            @Override
            protected MainLayout call() throws Exception {
                // 这里的进度更新会实时反映到闪屏的 statusLabel 上
                splash.get().updateProgress(0.1, "正在唤醒内核...");
                Thread.sleep(200); // 留出时间欣赏动画

                splash.get().updateProgress(0.2, "正在加载资源...");
                // 资源校验，防止加载不到资源
                var resource = getClass().getResource("/css/app.css");
                if (ObjectUtils.anyNull(resource)) {
                    throw new RuntimeException("关键资源缺失: default-avatar.png");
                }
                resource = null;

                splash.get().updateProgress(0.3, "正在加载配置...");

                splash.get().updateProgress(0.4, "正在加载数据...");
                try {
                    // 调用数据库管理类进行初始化
                    DatabaseManager.init();
                } catch (Exception e) {
                    // 如果数据库初始化失败，抛出异常以触发 initTask.setOnFailed
                    throw new RuntimeException("数据库初始化失败: " + e.getMessage(), e);
                }

                splash.get().updateProgress(0.5, "正在加载主题...");
                Styles.applyTheme(Objects.requireNonNull(ThemeManager.getTheme()));

                splash.get().updateProgress(0.6, "正在加载页面...");
                MainLayout mainLayout = new MainLayout();

                splash.get().updateProgress(0.65, "正在预加载常用页面...");
                ViewFactory.preloadEagerPages();

                splash.get().updateProgress(0.7, "正在唤醒内核...");

                splash.get().updateProgress(0.8, "正在唤醒内核...");

                splash.get().updateProgress(0.9, "内核准备就绪...");

                splash.get().updateProgress(1.0, "内核准备就绪...");
                Thread.sleep(200);
                return mainLayout;
            }
        };

        // 3. 任务完成后切换到主界面
        initTask.setOnSucceeded(e -> {

            primaryStage.initStyle(StageStyle.UNDECORATED); // 彻底去掉原生边框
            // 顶部窗体标题栏
            CustomTitleBar titleBar = new CustomTitleBar(primaryStage, AppState.getInstance().getProjectName());
            // 主界面 展示菜单+页面
            MainLayout mainLayout = initTask.getValue();
            // 关键：给整个窗口加一个极细的边框，防止在白色背景下找不到窗口边缘
            BorderPane borderPane = FXBorderPane.create().styleCss("-fx-border-color: -color-border-default; -fx-border-width: 1;").top(titleBar).center(mainLayout);
            // 场景的根节点是一个 StackPane，方便添加自定义样式和节点
            Scene scene = new Scene(borderPane, 1200, 800);

            // 加载自定义 CSS
            String css = getClass().getResource("/css/app.css").toExternalForm();
            scene.getStylesheets().add(css);

            // 初始化全局上下文
            AppContext.init(primaryStage, mainLayout, scene);
            // 应用持久化的字体大小设置
            AppContext.applyFontSize();
            // 注册快捷键
            AppContext.setupGlobalHotkeys();
            // 初始化工具栏按钮（通过 EventBus 注册）
            ToolbarInitializer.init();
            // 跳转到首页
            AppContext.navigateTo(ViewFactory.getDefaultPageId());

            // 限制程序最小缩放大小
            primaryStage.setScene(scene);
            ViewUtils.setStageIcon(primaryStage);
            primaryStage.setTitle(AppState.getInstance().getProjectName());
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(750);
            // 入场前先让内容不可见，但舞台要准备好
            mainLayout.setOpacity(0);

            // 触发主界面的入场
            splash.get().hide(() -> {
                // 启动心跳服务
                /*HeartbeatService heartbeatService = new HeartbeatService();
                heartbeatService.start();*/

                // 启动时异步加载未读消息（DB 查询放到虚拟线程，避免阻塞 FX 线程）
                TaskRunner.runAsync(MessageService::loadMessages);

                primaryStage.show();
                splash.set(null);
//                attachGlobalUserActivityMonitor(scene, 15);
                TaskRunner.runInFx(System::gc);
            });

            ResizeHelper.addResizeListener(primaryStage);

            // 显示主界面
            PauseTransition delay = new PauseTransition(Duration.millis(100));
            delay.setOnFinished(event -> Animations.fadeIn(mainLayout, Duration.millis(600)).play());
            delay.play();
        });

        // 4. 监听任务失败
        initTask.setOnFailed(event -> {
            log.error("初始化任务失败", event.getSource().getException());
            AppContext.exitApp();
        });

        // javafx.concurrent.Task 本身实现了 Runnable，可在任意线程（含虚拟线程）上运行；
        // 其 onSucceeded/onFailed 回调由 JavaFX 内部切回 FX 线程执行，因此用虚拟线程替换裸 Thread 是安全的
        TaskRunner.runAsync(initTask);
    }

    public static void main(String[] args) {

        try {
            ConfigStore.load();
            syncConfigToAppState();
        } catch (Exception e) {
            log.info("初始化失败", e);
        }

        // 强制开启 Prism（JavaFX 渲染引擎）的硬件加速
        System.setProperty("prism.forceGPU", String.valueOf(AppState.getInstance().isHardwareAcceleration()));
        // 开启脏区优化，减少重复渲染
        System.setProperty("prism.dirtyopts", String.valueOf(AppState.getInstance().isShowDirtyOpts()));
        launch(args);
    }

    /**
     * 自动同步：ConfigStore -> AppState 持久化属性
     */
    private static void syncConfigToAppState() {
        for (var entry : AppState.getInstance().getPersistedProperties().entrySet()) {
            String value = ConfigStore.get(entry.getKey());
            if (StringUtils.isNotBlank(value)) {
                entry.getValue().set(value);
            }
        }
    }

    /**
     * 监听全局用户活动，并触发内存清理
     *
     * @note 默认不启用
     */
    public static void attachGlobalUserActivityMonitor(Scene scene, int min) {
        // 建立一个全屏无操作直接触发当前可见页面深度清理的定时器
        Timeline globalIdleTimer = new Timeline(new KeyFrame(Duration.minutes(min), event -> {
            log.warn("检测到用户长达 15 分钟未触碰鼠标与键盘，执行全量内存空间大清洗...");

            // 显式让当前正在显示的异步页面释放高能耗非必要数据
            // 触发主动 GC 强制离盘，甚至可以强制让应用切回默认 dashboard 首页，把其余所有缓存一洗而空
            TaskRunner.runInFx(System::gc);
        }));
        globalIdleTimer.setCycleCount(1);
        globalIdleTimer.play();

        // 拦截全窗体最高层级的物理事件：只要乱动鼠标或打字，重置 15 分钟时钟
        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> {
            globalIdleTimer.playFromStart(); // 只要鼠标在晃动，不断把时钟推回原点
        });

        scene.addEventFilter(javafx.scene.input.KeyEvent.ANY, e -> {
            globalIdleTimer.playFromStart(); // 只要在敲击键盘，重置时钟
        });
    }
}
