package com.atlantafx.components.layout;

import atlantafx.base.controls.Popover;
import atlantafx.base.theme.Styles;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.*;
import com.atlantafx.components.theme.ThemeSelectorPopup;
import com.atlantafx.core.event.EventBus;
import com.atlantafx.core.event.ToolbarButtonEvent;
import com.atlantafx.util.TaskRunner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * CustomTitleBar - 自定义标题栏组件
 *
 * <p>提供跨平台的自定义窗口标题栏，支持以下功能：</p>
 * <ul>
 *     <li><strong>窗口控制</strong> - 最小化、最大化/还原、关闭按钮</li>
 *     <li><strong>拖拽移动</strong> - 支持鼠标拖拽窗口（双击最大化/还原）</li>
 *     <li><strong>系统菜单</strong> - 左键点击图标弹出系统菜单（还原、移动、大小、最小化、最大化、关闭）</li>
 *     <li><strong>主题切换</strong> - 集成主题选择器，支持快速切换应用主题</li>
 *     <li><strong>消息通知</strong> - 显示未读消息数量，点击可查看消息列表并跳转</li>
 *     <li><strong>自定义工具栏</strong> - 支持注入自定义工具按钮，自动限制高度防止撑开标题栏</li>
 *     <li><strong>跨平台适配</strong> - 自动识别 macOS/Windows 系统，调整按钮顺序和布局风格</li>
 * </ul>
 *
 * <h3>平台差异处理：</h3>
 * <ul>
 *     <li><strong>macOS</strong> - 按钮顺序：关闭 → 最小化 → 最大化，标题居中显示</li>
 *     <li><strong>Windows</strong> - 按钮顺序：最小化 → 最大化 → 关闭，标题居左显示</li>
 * </ul>
 *
 * <h3>样式特性：</h3>
 * <ul>
 *     <li>使用 AtlantaFX 的颜色变量（-color-bg-subtle、-color-fg-default），支持主题自动切换</li>
 *     <li>符合 Win11 标准的 32px 高度设计</li>
 *     <li>集成 Ikonli 图标库，使用 Material Design 风格图标</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * Stage stage = new Stage();
 * CustomTitleBar titleBar = new CustomTitleBar(stage, "我的应用");
 *
 * // 设置自定义工具栏按钮
 * titleBar.setCustomTools(List.of(
 *     new FXButton("保存"),
 *     new FXButton("设置")
 * ));
 *
 * // 添加到场景
 * BorderPane root = new BorderPane();
 * root.setTop(titleBar);
 * Scene scene = new Scene(root);
 * stage.setScene(scene);
 * }</pre>
 *
 * @author AtlantisFX Team
 * @version 1.0
 * @see Stage
 * @see Popover
 * @see ThemeSelectorPopup
 */
public class CustomTitleBar extends FXHBox {

    private static final Logger log = LoggerFactory.getLogger(CustomTitleBar.class);
    /**
     * 鼠标拖拽时的 X 轴偏移量
     */
    private double xOffset = 0;
    /**
     * 鼠标拖拽时的 Y 轴偏移量
     */
    private double yOffset = 0;
    /**
     * 是否为 macOS 系统
     */
    private final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
    /**
     * 最大化/还原图标
     */
    private final FXFontIcon maxIcon = FXFontIcon.create(MaterialDesignW.WINDOW_MAXIMIZE);
    /**
     * 自定义工具栏容器
     */
    private final FXHBox customToolBar = FXHBox.create(10);
    /**
     * 横向滚动面板（用于容纳超出宽度的工具栏按钮）
     */
    private final FXScrollPane scrollPane = FXScrollPane.create();
    /**
     * 更新最大化图标显示的 Runnable
     */
    private Runnable updateMaxIconRunnable;

    /**
     * 创建自定义标题栏
     *
     * @param stage 关联的窗口舞台
     * @param title 标题文字
     */
    public CustomTitleBar(Stage stage, String title) {
        // --- 1. 基础布局与样式 ---
        // Win11 标准高度;使用 AtlantaFX 的背景变量，确保主题切换时颜色自动改变
        this.height(32).align(Pos.CENTER_LEFT).background("-color-bg-subtle");

        // --- 绑定系统菜单 ---
//        ContextMenu systemMenu = createSystemMenu(stage);

        // 左键单击弹出菜单
        /*iconContainer.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                // 在图标正下方弹出
                systemMenu.show(iconContainer, e.getScreenX(), e.getScreenY());
            }
        });*/

        // --- 2. 标题文字 ---
        Label titleLabel = FXLabel.create(title).fontSize(12).fontColor("-color-fg-default");

        // 给图标和文字之间加一点间距
        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 8));

        // 允许标题栏拖拽窗口
        setupDragHandlers(stage);

        // 中间占位符
        Region spacerLeft = FXRegion.create().hSpacer();
        Region spacerRight = FXRegion.create().hSpacer();

        // 自定义工具栏
        customToolBar.align(Pos.CENTER_LEFT);

        // 配置 ScrollPane,支持鼠标拖拽滚动;移除 ScrollPane 默认的边框和背景，使其隐身
        scrollPane.content(customToolBar).noScrollBars().fitToHeight().pannable(true).stylesClass("no-scrollbar-scroll-pane");

        // 滚轮转横向滚动 (体验优化)
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() != 0) {
                scrollPane.setHvalue(scrollPane.getHvalue() - e.getDeltaY() / customToolBar.getWidth());
                e.consume();
            }
        });

        // 订阅工具栏按钮事件
        setupToolbarButtonListener();

        // --- 3. 窗口控制按钮组 ---
        HBox controls = createControlButtons(stage);

        // --- 组装布局 ---
        if (isMac) {
            this.padding(0, 0, 0, 0);
            // macOS 逻辑：控制按钮 -> 标题 -> 占位 -> 工具栏
            this.add(controls, spacerLeft, titleLabel, spacerRight, scrollPane);
            // Mac 习惯标题居中，可以将 spacer 分成左右两个
            HBox.setMargin(controls, new Insets(0, 10, 0, 5));
        } else {
            this.padding(0, 0, 0, 12);
            // 软件图标
            ImageView appIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/icons/icon-16.png"))));
            appIcon.setFitWidth(16); // 原生图标通常是 16x16
            appIcon.setFitHeight(16);
            appIcon.setPreserveRatio(true);

            // 包装图标以增加点击感（热区）
            StackPane iconContainer = FXStackPane.create(appIcon).padding(0, 5, 0, 5);
            iconContainer.setCursor(Cursor.HAND);
            HBox.setMargin(scrollPane, new Insets(0, 20, 0, 0));
            // Windows 逻辑：图标 -> 标题 -> 占位 -> 工具栏 -> 控制按钮
            this.add(iconContainer, titleLabel, spacerRight, scrollPane, controls);
        }

        // 监听最大化状态切换图标（需要同时监听 stage.maximizedProperty 和 isManuallyMaximized）
        updateMaxIconRunnable = () -> {
            boolean isMax = stage.isMaximized() || isManuallyMaximized;
            maxIcon.setIconCode(isMax ? MaterialDesignW.WINDOW_RESTORE : MaterialDesignW.WINDOW_MAXIMIZE);
        };

        stage.maximizedProperty().addListener((obs, old, isMax) -> updateMaxIconRunnable.run());
    }

    private Button createBtn(FontIcon icon, String styleClass) {
        Button btn = FXButton.create("");
        btn.setGraphic(icon);
        btn.setFocusTraversable(false);
        btn.setPrefSize(46, 32); // Windows 标准控制按钮尺寸

        // 基础样式：去除背景，设置图标大小
        icon.setIconSize(16);
        btn.getStyleClass().add("button-clean"); // AtlantaFX 清爽按钮基类
        btn.getStyleClass().add(styleClass);
        return btn;
    }

    /**
     * 窗口最大化前的状态保存
     */
    private double preMaximizeX = 0;
    private double preMaximizeY = 0;
    private double preMaximizeWidth = 0;
    private double preMaximizeHeight = 0;
    private boolean isManuallyMaximized = false;

    private void toggleMaximize(Stage stage) {
        if (isMac) {
            // macOS 直接使用内置的最大化功能
            stage.setMaximized(!stage.isMaximized());
        } else {
            // Windows 需要特殊处理，避免覆盖任务栏
            if (isManuallyMaximized) {
                // 还原窗口到之前的大小和位置
                stage.setX(preMaximizeX);
                stage.setY(preMaximizeY);
                stage.setWidth(preMaximizeWidth);
                stage.setHeight(preMaximizeHeight);
                isManuallyMaximized = false;
            } else {
                // 保存当前窗口状态
                preMaximizeX = stage.getX();
                preMaximizeY = stage.getY();
                preMaximizeWidth = stage.getWidth();
                preMaximizeHeight = stage.getHeight();

                // 获取工作区域（排除任务栏）
                Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

                // 手动设置窗口位置和大小来模拟最大化
                stage.setX(visualBounds.getMinX());
                stage.setY(visualBounds.getMinY());
                stage.setWidth(visualBounds.getWidth());
                stage.setHeight(visualBounds.getHeight());
                isManuallyMaximized = true;
            }
            // 更新图标
            if (updateMaxIconRunnable != null) {
                updateMaxIconRunnable.run();
            }
        }
    }

    private void setupDragHandlers(Stage stage) {
        this.setOnMousePressed(e -> {
            // 如果是手动最大化状态，先还原窗口
            if (isManuallyMaximized) {
                // 计算还原后的窗口位置（鼠标当前位置作为窗口左上角）
                double restoreWidth = preMaximizeWidth;
                double restoreHeight = preMaximizeHeight;
                double newX = e.getScreenX() - (restoreWidth / 2); // 居中
                double newY = e.getScreenY() - 16; // 标题栏高度的一半

                stage.setX(newX);
                stage.setY(newY);
                stage.setWidth(restoreWidth);
                stage.setHeight(restoreHeight);
                isManuallyMaximized = false;

                // 更新图标
                if (updateMaxIconRunnable != null) {
                    updateMaxIconRunnable.run();
                }

                // 更新偏移量
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            } else {
                xOffset = e.getSceneX();
                yOffset = e.getSceneY();
            }
        });
        this.setOnMouseDragged(e -> {
            if (!stage.isMaximized() && !isManuallyMaximized) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });
        this.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) toggleMaximize(stage);
        });
    }

    private HBox createControlButtons(Stage stage) {
        FXHBox container = FXHBox.create(isMac ? 8 : 0).align(Pos.CENTER); // Mac 按钮之间有间距

        // 创建三个基础按钮
        Button closeBtn = createBtn(new FontIcon(MaterialDesignW.WINDOW_CLOSE), isMac ? "mac-close" : "control-close-btn");
        Button minBtn = createBtn(new FontIcon(MaterialDesignM.MINUS), isMac ? "mac-min" : "control-btn");
        Button maxBtn = createBtn(maxIcon, isMac ? "mac-max" : "control-btn");

        // 绑定动作
        closeBtn.setOnAction(e -> AppContext.exitApp());
        minBtn.setOnAction(e -> stage.setIconified(true));
        maxBtn.setOnAction(e -> toggleMaximize(stage));

        if (isMac) {
            // macOS 顺序：红(关) -> 黄(最小) -> 绿(最大);Mac 下隐藏图标（通常 Mac 标题栏不放图标，或者只放文字）
            container.add(closeBtn, minBtn, maxBtn).padding(0, 10, 0, 10);
        } else {
            // Windows 顺序：最小 -> 最大 -> 关
            container.add(minBtn, maxBtn, closeBtn);
        }

        return container;
    }

    /**
     * 创建模拟的系统窗口菜单
     */
    private ContextMenu createSystemMenu(Stage stage) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("system-context-menu"); // 自定义样式类

        MenuItem restoreItem = new MenuItem("还原");
        restoreItem.setDisable(!isManuallyMaximized && !stage.isMaximized());
        restoreItem.setOnAction(e -> {
            if (isManuallyMaximized) {
                // 还原手动最大化的窗口
                stage.setX(preMaximizeX);
                stage.setY(preMaximizeY);
                stage.setWidth(preMaximizeWidth);
                stage.setHeight(preMaximizeHeight);
                isManuallyMaximized = false;
                // 更新图标
                if (updateMaxIconRunnable != null) {
                    updateMaxIconRunnable.run();
                }
            } else {
                stage.setMaximized(false);
            }
        });

        MenuItem moveItem = new MenuItem("移动");
        moveItem.setDisable(stage.isMaximized() || isManuallyMaximized); // 最大化时不可移动

        MenuItem sizeItem = new MenuItem("大小");
        sizeItem.setDisable(stage.isMaximized() || isManuallyMaximized);

        MenuItem minItem = new MenuItem("最小化");
        minItem.setOnAction(e -> stage.setIconified(true));

        MenuItem maxItem = new MenuItem("最大化");
        maxItem.setDisable(stage.isMaximized() || isManuallyMaximized);
        maxItem.setOnAction(e -> {
            if (!stage.isMaximized() && !isManuallyMaximized) {
                toggleMaximize(stage);
            }
        });

        MenuItem closeItem = new MenuItem("关闭");
        closeItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN));
        closeItem.setOnAction(e -> AppContext.exitApp());

        menu.getItems().addAll(
                restoreItem, moveItem, sizeItem,
                new SeparatorMenuItem(),
                minItem, maxItem,
                new SeparatorMenuItem(),
                closeItem
        );

        // 每次显示前根据窗口状态更新菜单项的启用情况
        menu.setOnShowing(e -> {
            boolean isMax = stage.isMaximized() || isManuallyMaximized;
            restoreItem.setDisable(!isMax);
            moveItem.setDisable(isMax);
            sizeItem.setDisable(isMax);
            maxItem.setDisable(isMax);
        });

        return menu;
    }

    /**
     * 设置工具栏按钮事件监听器
     * 允许其他类通过 EventBus 动态添加/移除工具栏按钮
     */
    private void setupToolbarButtonListener() {
        EventBus.subscribe(ToolbarButtonEvent.class, this::handleToolbarButtonEvent);
    }

    /**
     * 处理工具栏按钮事件
     */
    private void handleToolbarButtonEvent(ToolbarButtonEvent event) {
        TaskRunner.runInFx(() -> {
            switch (event.actionType()) {
                case ADD -> {
                    Node button = event.button();
                    if (button != null) {
                        // 限制按钮高度
                        styleToolbarButton(button);
                        int pos = event.position();
                        if (pos >= 0 && pos < customToolBar.getChildren().size()) {
                            customToolBar.getChildren().add(pos, button);
                        } else {
                            customToolBar.getChildren().add(button);
                        }
                    }
                }
                case REMOVE -> {
                    Node button = event.button();
                    if (button != null) {
                        customToolBar.getChildren().remove(button);
                    }
                }
                case CLEAR -> {
                    // 清空所有自定义按钮，但保留系统按钮（取消任务按钮）
                    customToolBar.getChildren().clear();
                    // 重新初始化主题和消息按钮（通过 EventBus）
                    ToolbarInitializer.init();
                }
                case SET_ALL -> {
                    // SET_ALL 事件暂不实现，因为 record 中无法传递 List
                    // 如需使用，可以扩展 ToolbarButtonEvent
                    log.warn("SET_ALL action type is not implemented yet");
                }
            }
        });
    }

    /**
     * 为工具栏按钮应用统一样式
     */
    private void styleToolbarButton(Node button) {
        if (button instanceof Button btn) {
            btn.setMaxHeight(28);
            btn.getStyleClass().add(Styles.SMALL);
        } else if (button instanceof Region r) {
            r.setMaxHeight(26);
        }
    }
}
