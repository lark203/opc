package com.atlantafx.components.base;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;

import java.util.concurrent.CompletableFuture;

/**
 * FXDialog - 通用悬浮对话框组件（去遮罩轻量动画版）
 * 升级特性：
 * 1. 净化视觉：完全剔除外部全屏模态遮罩层，保持背景透气性。
 * 2. 严丝合缝：采用轻量级单 Stage 架构，舞台物理边界与卡片完全贴合，拖拽绝不露白。
 * 3. 动效保留：完美保留渐显(Fade)与缩放(Scale)交织的微动效。
 */
public class FXDialog {

    /**
     * 对话框类型枚举
     */
    public enum DialogType {
        INFO, CONFIRM, WARNING, ERROR, SUCCESS
    }

    private final Stage dialogStage;
    private final Card card;
    private final FXVBox contentArea;
    private final FXHBox buttonBar;
    private final FXLabel titleLabel;
    private final MessageHolder messageHolder; // 提取出来的文本/自定义内容持有结构
    private final FXFontIcon iconView;

    private DialogType type = DialogType.INFO;
    private double minWidth = 400; // 紧凑型黄金宽度限制
    private double maxWidth = 480;
    private boolean draggable = true;
    private Runnable onClose;

    private double dragStartX;
    private double dragStartY;

    /**
     * 创建对话框实例
     */
    private FXDialog(DialogType type) {
        this.type = type;

        this.dialogStage = new Stage();
        this.dialogStage.initStyle(StageStyle.TRANSPARENT); // 保持物理舞台背景透明
        this.dialogStage.initModality(Modality.APPLICATION_MODAL);
        this.dialogStage.setAlwaysOnTop(true);

        // 创建 Card 作为对话框主体
        card = new Card();
        card.setMinWidth(minWidth);
        card.setMaxWidth(maxWidth);
        card.getStyleClass().addAll(Styles.INTERACTIVE);

        // 注入现代大扩散漫反射立体悬浮阴影（重写 CSS 边框避免暗色模式下边缘死白）
        card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.18), 16, 0, 0, 4); " +
                "-fx-border-color: -color-border-muted; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px;");

        // 1. 头部区域 (Header) - 精确控制内边距防止对话框过高
        FXHBox header = FXHBox.create(12).align(Pos.CENTER_LEFT).padding(12, 16, 8, 16);
        iconView = createHeaderIcon(type);
        titleLabel = FXLabel.create().h4().bold().hgrow(); // 紧凑级 h4 字号

        Button closeBtn = FXButton.create("")
                .icon(MaterialDesignC.CLOSE)
                .circle()
                .flat()
                .stylesClass(Styles.TEXT_MUTED)
                .onAction(e -> close());

        header.add(iconView, titleLabel, FXRegion.create().hSpacer(), closeBtn);
        card.setHeader(header);

        // 2. 内容轴区域 (Body) - 设定紧凑安全的防坍塌高度限制
        contentArea = FXVBox.create(10).padding(4, 16, 12, 16).fillWidth(true);
        messageHolder = new MessageHolder();
        contentArea.add(messageHolder.getLabel());

        // 引入安全内卷滚动包裹轴，当内容稍长时自动转为局部滚动，决不撑高外层 Stage
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(220); // 进一步下压安全高度限制至 220px
        scrollPane.getStyleClass().add(Tweaks.EDGE_TO_EDGE); // 净化滚动组件多余白边
        card.setBody(scrollPane);

        // 3. 底部按钮操作区 (Footer)
        buttonBar = FXHBox.create(10).align(Pos.CENTER_RIGHT).padding(10, 16, 14, 16);
        card.setFooter(buttonBar);

        Scene scene = new Scene(card);
        scene.setFill(Color.TRANSPARENT); // 保持场景透明，彻底解决露白与黑边痛点
        dialogStage.setScene(scene);

        setupDraggable();
    }

    /**
     * 启用卡片区域拖拽
     */
    private void setupDraggable() {
        if (card.getHeader() != null) {
            card.getHeader().setOnMousePressed(e -> {
                if (draggable) {
                    dragStartX = e.getScreenX() - dialogStage.getX();
                    dragStartY = e.getScreenY() - dialogStage.getY();
                }
            });
            card.getHeader().setOnMouseDragged(e -> {
                if (draggable) {
                    dialogStage.setX(e.getScreenX() - dragStartX);
                    dialogStage.setY(e.getScreenY() - dragStartY);
                }
            });
        }
    }

    /**
     * 创建对话框实例
     */
    public static FXDialog create(DialogType type) {
        return new FXDialog(type);
    }

    public static FXDialog create() {
        return create(DialogType.INFO);
    }

    /**
     * 创建信息对话框
     */
    public static FXDialog info(String title, String message) {
        return create(DialogType.INFO).title(title).message(message);
    }

    /**
     * 创建确认对话框
     */
    public static FXDialog confirm(String title, String message) {
        return create(DialogType.CONFIRM).title(title).message(message);
    }

    /**
     * 创建警告对话框
     */
    public static FXDialog warning(String title, String message) {
        return create(DialogType.WARNING).title(title).message(message);
    }

    /**
     * 创建错误对话框
     */
    public static FXDialog error(String title, String message) {
        return create(DialogType.ERROR).title(title).message(message);
    }

    /**
     * 创建成功对话框
     */
    public static FXDialog success(String title, String message) {
        return create(DialogType.SUCCESS).title(title).message(message);
    }

    /**
     * 创建对话框图标（内部）
     */
    private FXFontIcon createHeaderIcon(DialogType type) {
        switch (type) {
            case INFO -> {
                return FXFontIcon.create(MaterialDesignI.INFORMATION).accent().size(22);
            }
            case CONFIRM -> {
                return FXFontIcon.create(MaterialDesignH.HELP_CIRCLE_OUTLINE).accent().size(22);
            }
            case WARNING -> {
                return FXFontIcon.create(MaterialDesignA.ALERT).warning().size(22);
            }
            case ERROR -> {
                return FXFontIcon.create(MaterialDesignC.CLOSE_CIRCLE).danger().size(22);
            }
            case SUCCESS -> {
                return FXFontIcon.create(MaterialDesignC.CHECK_CIRCLE).success().size(22);
            }
        }
        return FXFontIcon.create(MaterialDesignI.INFORMATION).accent().size(22);
    }

    /**
     * 设置标题
     */
    public FXDialog title(String title) {
        titleLabel.setText(title);
        return this;
    }

    /**
     * 设置消息内容
     */
    public FXDialog message(String message) {
        messageHolder.showText(message);
        return this;
    }

    /**
     * 设置自定义内容节点
     */
    public FXDialog content(Node node) {
        messageHolder.showCustomNode(contentArea, node);
        return this;
    }

    /**
     * 设置宽度
     */
    public FXDialog width(double width) {
        this.minWidth = width;
        this.maxWidth = width;
        card.setMinWidth(width);
        card.setMaxWidth(width);
        return this;
    }

    /**
     * 设置是否可拖拽
     */
    public FXDialog draggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    /**
     * 设置父窗口
     */
    public FXDialog owner(Window owner) {
        dialogStage.initOwner(owner);
        return this;
    }

    /**
     * 设置关闭回调
     */
    public FXDialog onClose(Runnable callback) {
        this.onClose = callback;
        return this;
    }

    /**
     * 添加按钮
     *
     * @param text     按钮文本
     * @param primary  是否为主按钮
     * @param callback 点击回调
     */
    public FXDialog addButton(String text, boolean primary, Runnable callback) {
        FXButton button = FXButton.create(text);
        if (primary) {
            switch (this.type) {
                case ERROR -> button.danger();
                case SUCCESS -> button.success();
                default -> button.accent();
            }
        } else {
            button.outline();
        }
        button.setOnAction(e -> {
            if (callback != null) callback.run();
            close();
        });
        buttonBar.getChildren().add(button);
        return this;
    }

    public FXDialog confirmButton(Runnable callback) {
        return addButton("确定", true, callback);
    }

    /**
     * 添加取消按钮
     */
    public FXDialog cancelButton(Runnable callback) {
        return addButton("取消", false, callback);
    }

    /**
     * 添加确定和取消按钮
     */
    public FXDialog confirmCancel(Runnable onConfirm, Runnable onCancel) {
        cancelButton(onCancel);
        confirmButton(onConfirm);
        return this;
    }

    // ==================== 动效控制与舞台绝对中心漂移挂载 ====================

    /**
     * 唤醒轻量级对话框 - 无全屏遮罩干扰，卡片独立伴随渐变与微缩放弹性进场
     */
    public void show() {
        // 如果没有自定义按钮，添加默认按钮
        if (buttonBar.getChildren().isEmpty()) {
            if (type == DialogType.CONFIRM) {
                confirmCancel(null, null);
            } else {
                addButton("确定", true, null);
            }
        }

        // 自动探测并检索主激活视窗
        if (dialogStage.getOwner() == null) {
            Stage.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(w -> w != dialogStage)
                    .findFirst()
                    .ifPresent(dialogStage::initOwner);
        }

        Window owner = dialogStage.getOwner();
        if (owner != null) {
            // 核心演进：因为移除了全屏遮罩，必须在物理舞台 show 之前，通过严密的窗口几何算力锁死绝对中央原点
            // 考虑包裹容器的外补丁 margin (16 * 2)
            double predictedStageW = minWidth + 32;
            double predictedStageH = 190; // 优化后的高度预估线

            dialogStage.setX(owner.getX() + (owner.getWidth() - predictedStageW) / 2);
            dialogStage.setY(owner.getY() + (owner.getHeight() - predictedStageH) / 2);
        }

        // 仅对局部卡片区域装配轻缓进场流
        card.setScaleX(0.92);
        card.setScaleY(0.92);

        dialogStage.show();

        FadeTransition fade = new FadeTransition(Duration.millis(160), card);
        fade.setToValue(1.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), card);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    public CompletableFuture<Boolean> showAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        confirmCancel(() -> future.complete(true), () -> future.complete(false));
        show();
        return future;
    }

    /**
     * 轻量化退场动效
     */
    public void close() {
        FadeTransition fade = new FadeTransition(Duration.millis(120), card);
        fade.setToValue(0.0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(120), card);
        scale.setToX(0.94);
        scale.setToY(0.94);

        fade.setOnFinished(e -> {
            dialogStage.close();
            if (onClose != null) {
                onClose.run();
            }
        });

        fade.play();
        scale.play();
    }

    public Stage getStage() {
        return dialogStage;
    }

    // ==================== 内部高度控制辅助类 ====================
    private static class MessageHolder {
        private final FXLabel label;

        public MessageHolder() {
            this.label = FXLabel.create().wrapText(true).stylesClass(Styles.TEXT_SUBTLE);
            this.label.setLineSpacing(2.0); // 优化行距压缩高度占比
        }

        public FXLabel getLabel() {
            return label;
        }

        public void showText(String text) {
            label.setText(text);
            label.setVisible(true);
            label.setManaged(true);
        }

        public void showCustomNode(FXVBox container, Node node) {
            container.getChildren().clear();
            container.getChildren().add(node);
        }
    }

    // ==================== 静态便捷方法 ====================
    public static void showInfo(String title, String message) {
        info(title, message).show();
    }

    /**
     * 显示简单确认对话框
     */
    public static void showConfirm(String title, String message, Runnable onConfirm) {
        confirm(title, message).confirmCancel(onConfirm, null).show();
    }

    /**
     * 显示简单警告对话框
     */
    public static void showWarning(String title, String message) {
        warning(title, message).show();
    }

    /**
     * 显示简单错误对话框
     */
    public static void showError(String title, String message) {
        error(title, message).show();
    }

    /**
     * 显示简单成功对话框
     */
    public static void showSuccess(String title, String message) {
        success(title, message).show();
    }
}