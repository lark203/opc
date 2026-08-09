package com.atlantafx.components.layout;

import atlantafx.base.controls.Popover;
import atlantafx.base.theme.Styles;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.*;
import com.atlantafx.components.theme.ThemeSelectorPopup;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.event.EventBus;
import com.atlantafx.core.event.NavEvent;
import com.atlantafx.core.event.ToolbarButtonEvent;
import com.atlantafx.core.service.MessageService;
import com.atlantafx.core.table.Message;
import com.atlantafx.util.TaskRunner;
import com.sun.management.OperatingSystemMXBean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.materialdesign2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 工具栏初始化器：通过 EventBus 动态注册标题栏按钮
 * <p>
 * 使用方式：
 * <pre>{@code
 * // 在应用启动后调用
 * ToolbarInitializer.init();
 * }</pre>
 */
public class ToolbarInitializer {

    private static final Logger log = LoggerFactory.getLogger(ToolbarInitializer.class);

    private static OperatingSystemMXBean osBean;
    private static Timeline monitorTimeline;

    /**
     * 全局监控管线（Timeline + monitoringProperty 监听器）只启动一次的标志。
     * 标题栏 CLEAR 分支会重入 init()，但管线资源不应重复创建，否则每次 CLEAR 都会叠加监听器，
     * 导致切换监控时重复弹出 Toast、旧监听器持有已脱离场景的旧容器引用。
     */
    private static boolean monitoringPipelineStarted = false;
    private static final String MONITOR_CONTAINER_ID = "toolbar-monitor-container";

    /**
     * 初始化工具栏按钮
     * 通过 EventBus 发布 ToolbarButtonEvent 来注册主题切换按钮和消息按钮
     */
    public static void init() {
        // 添加监控
        FXHBox performanceMonitorWidget = createPerformanceMonitorWidget();
        EventBus.publish(ToolbarButtonEvent.add(performanceMonitorWidget));

        // 添加主题切换按钮
        EventBus.publish(ToolbarButtonEvent.add(createThemeBtn()));

        // 添加消息通知按钮
        EventBus.publish(ToolbarButtonEvent.add(createNotificationBtn()));

        TaskRunner.initCancelButton();

        // 全局监控管线（Timeline + monitoringProperty 监听器）只启动一次，
        // 避免每次 CLEAR 重入都叠加监听器导致 Toast 刷屏 / 孤儿监听驻留
        if (!monitoringPipelineStarted) {
            startGlobalMonitoringPipeline();
            monitoringPipelineStarted = true;
        }
    }

    /**
     * 创建主题切换按钮
     */
    private static Node createThemeBtn() {
        FXButton btn = FXButton.create(null).icon(MaterialDesignP.PALETTE).tooltip("切换主题").stylesClass(Styles.BUTTON_CIRCLE, Styles.FLAT);

        var content = new ThemeSelectorPopup(() -> {
        });
        Popover popover = new Popover(content);
        popover.getStyleClass().add("theme-selector-popup");
        popover.setHeaderAlwaysVisible(false);
        popover.setAutoHide(true);
        popover.setDetachable(false);
        popover.setArrowLocation(Popover.ArrowLocation.TOP_RIGHT);
        popover.focusedProperty().addListener(_ -> {
            if (!popover.isFocused()) {
                btn.setDisable(false);
            }
        });

        btn.setOnAction(e -> {
            btn.setDisable(true);
            popover.show(btn);
        });

        return btn;
    }

    /**
     * 创建消息通知按钮
     */
    private static Node createNotificationBtn() {
        FXButton btn = FXButton.create(null).icon(MaterialDesignB.BELL).tooltip("消息通知").stylesClass(Styles.BUTTON_CIRCLE, Styles.FLAT);

        // 创建消息列表容器
        FXVBox messageContainer = FXVBox.create(8).padding(8);
        messageContainer.setMaxWidth(300);
        messageContainer.setMinWidth(300);
        messageContainer.setPrefHeight(320);

        Map<Long, Node> messageNodeMap = new HashMap<>();

        Runnable renderMessages = () -> {
            messageContainer.clear();
            messageNodeMap.clear();

            for (Message msg : MessageService.getData()) {
                Node messageNode = createMessageNode(msg, () -> handleMessageClick(msg));
                messageContainer.add(messageNode);
                messageNodeMap.put(msg.getId(), messageNode);
            }

            if (MessageService.getData().isEmpty()) {
                Label emptyLabel = FXLabel.create("暂无消息").stylesClass(Styles.TEXT_MUTED);
                emptyLabel.setAlignment(Pos.CENTER);
                messageContainer.add(emptyLabel);
            }
        };

        renderMessages.run();

        Button clearAllBtn = FXButton.create("一键忽略").icon(MaterialDesignM.MESSAGE_OFF_OUTLINE).stylesClass(Styles.BUTTON_CIRCLE, Styles.FLAT)
                .onAction(e -> MessageService.markAllAsRead());

        FXHBox header = FXHBox.create().align(Pos.CENTER_LEFT).padding(10, 12, 10, 8).stylesClass("popover-header");
        header.setPrefWidth(300);
        header.setMinWidth(300);
        header.setMaxWidth(300);

        Label titleLabel = FXLabel.create("消息通知").bold();

        Region spacer = FXRegion.create().hSpacer();
        header.add(titleLabel, spacer, clearAllBtn);

        FXScrollPane messageScrollPane = FXScrollPane.create(messageContainer)
                .fitToWidth()
                .noScrollBars()
                .styleCss("-fx-background-color: transparent;");
        messageScrollPane.setPrefHeight(320);
        messageScrollPane.setPrefWidth(300);
        messageScrollPane.setMinWidth(300);
        messageScrollPane.setMaxWidth(300);

        VBox popoverRoot = FXVBox.create().add(header, messageScrollPane).fillWidth(true);
        popoverRoot.setPrefWidth(300);
        popoverRoot.setMinWidth(300);
        popoverRoot.setMaxWidth(300);

        Popover popover = new Popover(popoverRoot);
        popover.setHeaderAlwaysVisible(false);
        popover.getStyleClass().add("notice-selector-popup");
        popover.setArrowLocation(Popover.ArrowLocation.TOP_CENTER);
        popover.setPrefWidth(320);
        popover.setMinWidth(320);
        popover.setMaxWidth(320);

        MessageService.getData().addListener((ListChangeListener<Message>) _ -> {
            TaskRunner.runInFx(renderMessages);
            int size = MessageService.getData().size();
            titleLabel.setText("未读消息 (" + size + ")");
            clearAllBtn.setVisible(size > 0);
        });

        btn.setUserData(popover);
        btn.onAction(_ -> popover.show(btn));

        Region badge = FXRegion.create().stylesClass("notification-badge");
        badge.visibleProperty().bind(Bindings.isNotEmpty(MessageService.getData()));
        badge.setMouseTransparent(true);

        StackPane container = FXStackPane.create(btn, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(5, 5, 0, 0));

        return container;
    }

    /**
     * 创建单个消息节点（根据消息类型显示不同样式）
     */
    private static Node createMessageNode(Message message, Runnable onClick) {
        VBox card = FXVBox.create(4).padding(10).mxWidth(280).mnHeight(100);
        card.setStyle("-fx-background-color: transparent; -fx-border-color: -color-border-subtle; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setCursor(Cursor.HAND);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: -color-bg-subtle; -fx-border-color: -color-border-muted; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: transparent; -fx-border-color: -color-border-subtle; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        });
        card.setOnMouseClicked(e -> onClick.run());

        // 创建消息头部（图标 + 标题）
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FXLabel iconLabel = getMessageIcon(message.getMessageType());

        Label title = new Label(message.getTitle());
        title.setWrapText(true);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -color-fg-default;");

        header.getChildren().addAll(iconLabel, title);

        Label content = new Label(message.getContent());
        content.setWrapText(true);
        content.setMaxWidth(260);
        content.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");

        // 如果有操作按钮文本，显示操作按钮
        if (message.getActionButtonText() != null && !message.getActionButtonText().isEmpty()) {
            HBox actionArea = new HBox(8);
            actionArea.setAlignment(Pos.CENTER_RIGHT);

            FXButton actionBtn = FXButton.create(message.getActionButtonText())
                    .sm()
                    .accent()
                    .onAction(e -> {
                        onClick.run();
                    });
            actionArea.getChildren().add(actionBtn);
            card.getChildren().addAll(header, content, actionArea);
        } else {
            card.getChildren().addAll(header, content);
        }

        return card;
    }

    /**
     * 根据消息类型获取图标代码
     */
    private static FXLabel getMessageIcon(String messageType) {
        if (messageType == null) {
            return FXLabel.create("", FXFontIcon.create(MaterialDesignI.INFORMATION_OUTLINE)).accent(); // DEFAULT: INFORMATION_OUTLINE
        }

        return switch (messageType) {
            case "ALERT" ->
                    FXLabel.create("", FXFontIcon.create(MaterialDesignA.ALERT_BOX_OUTLINE)).danger(); // ALERT_CIRCLE
            case "ACTION" ->
                    FXLabel.create("", FXFontIcon.create(MaterialDesignC.CHEVRON_RIGHT)).accent(); // CHEVRON_RIGHT
            case "LINK" ->
                    FXLabel.create("", FXFontIcon.create(MaterialDesignL.LINK_VARIANT)).warning(); // LINK_VARIANT
            case "TASK" -> FXLabel.create("", FXFontIcon.create(MaterialDesignT.TOOLS)).success(); // TASK
            default ->
                    FXLabel.create("", FXFontIcon.create(MaterialDesignI.INFORMATION_OUTLINE)).accent(); // INFORMATION_OUTLINE
        };
    }

    /**
     * 处理消息点击（根据 actionType 执行不同操作）
     */
    private static void handleMessageClick(Message message) {
        // 标记为已读
        MessageService.markAsRead(message);

        String actionType = message.getActionType();
        String actionParam = message.getActionParam();

        if (actionType == null || actionType.isEmpty() || "NONE".equals(actionType)) {
            // 没有操作类型，仅标记为已读
            return;
        }

        switch (actionType) {
            case "NAVIGATE" -> handleNavigateAction(actionParam);
            case "OPEN_URL" -> handleOpenUrlAction(actionParam);
            case "SHOW_DIALOG" -> handleShowDialogAction(message);
            case "EXECUTE_TASK" -> handleExecuteTaskAction(actionParam);
            default -> {
                // 未知操作类型，尝试旧的 targetViewId 逻辑
                if (message.getTargetViewId() != null) {
                    handleNavigateAction(message.getTargetViewId());
                }
            }
        }
    }

    /**
     * 处理页面跳转操作
     */
    private static void handleNavigateAction(String pageId) {
        if (pageId == null || pageId.isEmpty()) {
            return;
        }
        AppContext.navigateFromPage(pageId);
    }

    /**
     * 处理打开网页操作
     */
    private static void handleOpenUrlAction(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        try {
            URI uri = new URI(url);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
                AppContext.showNotification("正在打开链接...", NotificationLevel.INFO);
            } else {
                AppContext.showNotification("系统不支持打开浏览器", NotificationLevel.ERROR);
            }
        } catch (URISyntaxException | IOException e) {
            AppContext.showNotification("链接无效: " + e.getMessage(), NotificationLevel.ERROR);
        }
    }

    /**
     * 处理显示弹窗操作
     */
    private static void handleShowDialogAction(Message message) {
        String dialogContent = message.getActionParam();
        String title = message.getTitle();

        if (dialogContent == null || dialogContent.isEmpty()) {
            dialogContent = message.getContent();
        }

        FXDialog.showWarning(title, dialogContent);
    }

    /**
     * 处理执行任务操作
     */
    private static void handleExecuteTaskAction(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return;
        }

        // 根据任务ID执行对应的任务
        executeTask(taskId);
    }

    /**
     * 执行任务（根据任务ID）
     * TODO 后续实现
     */
    private static void executeTask(String taskId) {
        // 这里可以根据任务ID执行不同的任务
        switch (taskId) {
            case "refresh_projects" -> {
                AppContext.showNotification("正在刷新项目列表...", NotificationLevel.INFO);
                EventBus.publish(new NavEvent("project-list"));
            }
            case "check_updates" -> {
                AppContext.showNotification("正在检查更新...", NotificationLevel.INFO);
                // 可以在这里添加检查更新的逻辑
            }
            case "clean_cache" -> {
                AppContext.showNotification("正在清理缓存...", NotificationLevel.INFO);
                // 可以在这里添加清理缓存的逻辑
            }
            default -> {
                AppContext.showNotification("未知任务: " + taskId, NotificationLevel.WARNING);
            }
        }
    }

    /**
     * 核心 facts：构建高紧凑度、水平对齐的顶栏专用监控条
     */
    private static FXHBox createPerformanceMonitorWidget() {
        // CPU 微型控制排版
        FXFontIcon cpuIcon = FXFontIcon.create(MaterialDesignC.CPU_64_BIT).accent();
        FXLabel cpuLabel = FXLabel.create("CPU: 0%");
        FXHBox cpuBox = FXHBox.create(4).align(Pos.CENTER_LEFT).add(cpuIcon, cpuLabel);

        // 内存微型控制排版
        FXFontIcon memIcon = FXFontIcon.create(MaterialDesignM.MEMORY).success();
        FXLabel memLabel = FXLabel.create("USE_MEM: 0M");
        FXHBox memBox = FXHBox.create(4).align(Pos.CENTER_LEFT).add(memIcon, memLabel);

        // Windows内存
        FXFontIcon winMemIcon = FXFontIcon.create(MaterialDesignM.MEMORY).success();
        FXLabel winMemLabel = FXLabel.create("TOTAL_MEM: 0M");
        FXHBox winMemBox = FXHBox.create(4).align(Pos.CENTER_LEFT).add(winMemIcon, winMemLabel);

        // 将专属 ID 挂载至节点，以便轮询时安全查找 facts
        cpuLabel.setId("toolbar-monitor-cpu-text");
        memLabel.setId("toolbar-monitor-mem-text");
        cpuIcon.setId("toolbar-monitor-cpu-icon");
        memIcon.setId("toolbar-monitor-mem-icon");
        winMemLabel.setId("toolbar-monitor-win-mem-text");
        winMemIcon.setId("toolbar-monitor-win-mem-icon");

        FXHBox container = FXHBox.create(16).align(Pos.CENTER_LEFT).add(cpuBox, memBox, winMemBox).visible(AppState.getInstance().isMonitoring());
        container.setId(MONITOR_CONTAINER_ID);

        // 装配悬浮提示词
        Tooltip.install(container, new Tooltip("Java 虚拟机 JVM 进程实时硬件开销监控 (5秒刷新)"));
        return container;
    }

    /**
     * 核心 facts：配置独立的系统级低延迟时钟，确保监控不产生内存泄漏
     */
    private static void startGlobalMonitoringPipeline() {
        try {
            osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (Exception e) {
            log.error("操作系统高阶管理核心 MXBean 实例化阻塞。", e);
        }

        // 建立 5.0秒 循环刷新的 Timeline 管线；仅创建一次
        if (monitorTimeline != null) {
            monitorTimeline.stop();
        }

        // 通过静态门面提取当前场景根节点
        Scene mainScene = AppContext.getMainScene();
        if (mainScene == null) return;

        // 监听器只注册一次；按 id 查找当前监控容器，兼容 CLEAR 重建后的新节点（旧节点已脱离场景）
        AppState.getInstance().monitoringProperty().addListener((observable, oldValue, newValue) -> {
            boolean on = Boolean.parseBoolean(newValue);
            Node monitorContainer = mainScene.lookup("#" + MONITOR_CONTAINER_ID);
            if (monitorContainer instanceof FXHBox hbox) {
                hbox.visible(on);
            }
            if (on) {
                if (monitorTimeline != null) monitorTimeline.play();
                AppContext.showNotification("已开启性能监控", NotificationLevel.WARNING);
            } else {
                if (monitorTimeline != null) monitorTimeline.stop();
                AppContext.showNotification("已关闭性能监控", NotificationLevel.INFO);
            }
        });

        monitorTimeline = new Timeline(new KeyFrame(Duration.seconds(5.0), event -> {
            // 逆向核对文本节点是否存在
            FXLabel cpuLabel = (FXLabel) mainScene.lookup("#toolbar-monitor-cpu-text");
            FXLabel memLabel = (FXLabel) mainScene.lookup("#toolbar-monitor-mem-text");
            FXFontIcon cpuIcon = (FXFontIcon) mainScene.lookup("#toolbar-monitor-cpu-icon");
            FXFontIcon memIcon = (FXFontIcon) mainScene.lookup("#toolbar-monitor-mem-icon");
            FXLabel winMemLabel = (FXLabel) mainScene.lookup("#toolbar-monitor-win-mem-text");
            FXFontIcon winMemIcon = (FXFontIcon) mainScene.lookup("#toolbar-monitor-win-mem-icon");
            if (cpuLabel == null || memLabel == null || winMemLabel == null) return;

            // 1. 获取 CPU 使用率
            double cpuLoad = 0;
            if (osBean != null) {
                cpuLoad = osBean.getProcessCpuLoad();
                if (cpuLoad < 0) cpuLoad = 0;
            }
            final double finalCpu = cpuLoad;

            // 2. 获取 JVM 堆栈开销
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            final double usedMb = usedMemory / (1024.0 * 1024.0);

            // =========================================================================
            // 现代化跨平台（Windows / macOS / Linux）物理内存全量清算 pipeline
            // =========================================================================
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

            // 1. 抓取当前 JVM 标准堆内存的【真实已用字节数】（非圈地总大小）
            long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();

            // 2. 抓取当前 JVM 非堆内存的【真实已用字节数】（包含 macOS 渲染缓冲区、MetaSpace 等）
            long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();

            // 3. 跨平台全量求和：堆 + 非堆 = 整个 Java 进程在宿主机上的真实物理物理投影
            long totalActualProcessMemory = heapUsed + nonHeapUsed;

            // 4. 转换口径为兆字节（MB）
            final double crossPlatformMb = totalActualProcessMemory / (1024.0 * 1024.0);

            log.info("CPU: {}, MEM: {}, WIN_MEM: {}", finalCpu, usedMb, crossPlatformMb);

            // 3. 安全更新 JavaFX 渲染主线程 facts
            TaskRunner.runInFx(() -> {
                cpuLabel.setText(String.format("CPU:%2.0f%%", finalCpu * 100));
                memLabel.setText(String.format("USE_MEM:%4.0fM", usedMb));
                winMemLabel.setText(String.format("TOTAL_MEM:%4.0fM", crossPlatformMb));

                cpuIcon.resetState();
                memIcon.resetState();
                winMemIcon.resetState();

                // 极端资源负载健康度颜色断言 facts
                if (finalCpu > 0.80) {
                    cpuIcon.danger();
                } else {
                    cpuIcon.accent();
                }

                if (usedMb > 1024) { // 超过 1G 堆分配时警报
                    memIcon.danger();
                } else if (usedMb > 512) {
                    memIcon.warning();
                } else {
                    memIcon.success();
                }

                if (crossPlatformMb > 2048) {
                    winMemIcon.danger();
                } else if (crossPlatformMb > 1024) {
                    winMemIcon.warning();
                } else {
                    winMemIcon.success();
                }
            });
        }));

        monitorTimeline.setCycleCount(Timeline.INDEFINITE);
        if (AppState.getInstance().isMonitoring()) {
            monitorTimeline.play();
        }
    }
}
