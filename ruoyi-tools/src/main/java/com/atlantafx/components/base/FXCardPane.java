package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * FXCardPane - 现代化高级语义化卡片容器
 * 核心：彻底清洗了原旧代码中硬编码十六进制色值的物理硬伤，全面支持明暗主题热刷新。
 * 引入高级悬浮物理抬升动画管线，支持语义化头部、内容体、尾部平铺。
 */
public class FXCardPane extends StackPane implements IFXNode<FXCardPane> {

    private final VBox layoutBox; // 引入内部核心平铺管线，解决 StackPane 默认层叠隐伤
    private DropShadow currentShadow;
    private boolean isClickable = false;

    private FXCardPane() {
        super();
        this.layoutBox = new VBox(0);
        this.layoutBox.setAlignment(Pos.TOP_LEFT);

        // 将主布局箱挂载到 StackPane 中央
        getChildren().add(layoutBox);

        // 初始化现代生产级扁平样式 facts
        asFlatCard();
    }

    public static FXCardPane create() {
        return new FXCardPane();
    }

    public static FXCardPane create(Node content) {
        FXCardPane card = new FXCardPane();
        card.content(content);
        return card;
    }

    /**
     * 核心：完全剥离硬编码，通过 AtlantaFX 样式Lookup变量热适应深浅模式
     */
    public FXCardPane asFlatCard() {
        setEffect(null);
        setStyle(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-border-default;" +
                        "-fx-border-width: 1px;"
        );
        padding(16);
        return this;
    }

    /**
     * 升级：高级悬浮高显卡片，使用透明黑渐变阴影，适应所有明暗底衬
     */
    public FXCardPane asElevatedCard() {
        this.currentShadow = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.08), 12, 0.2, 0, 4);
        setEffect(currentShadow);
        setStyle(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: transparent;"
        );
        padding(20);
        return this;
    }

    /**
     * 升级：粗线立体描边卡片
     */
    public FXCardPane asOutlinedCard() {
        setEffect(null);
        setStyle(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-border-muted;" +
                        "-fx-border-width: 2px;"
        );
        padding(16);
        return this;
    }

    /**
     * 核心：装配可点击卡片的高级微动升降动画管线 facts
     */
    public FXCardPane clickable(boolean clickable) {
        this.isClickable = clickable;
        if (clickable) {
            setCursor(Cursor.HAND);
            getStyleClass().add(Styles.INTERACTIVE);

            // 弹性升降微动物理补间动画
            ScaleTransition hoverAnim = new ScaleTransition(Duration.millis(120), this);
            hoverAnim.setFromX(1.0);
            hoverAnim.setFromY(1.0);
            hoverAnim.setToX(1.015); // 轻量微幅无损上浮
            hoverAnim.setToY(1.015);

            setOnMouseEntered(e -> {
                hoverAnim.setRate(1.0);
                hoverAnim.play();
                if (currentShadow != null) {
                    // 悬浮时阴影高精深邃化呈现 facts
                    currentShadow.setRadius(20);
                    currentShadow.setColor(Color.rgb(0, 0, 0, 0.14));
                }
            });

            setOnMouseExited(e -> {
                hoverAnim.setRate(-1.0);
                hoverAnim.play();
                if (currentShadow != null) {
                    currentShadow.setRadius(12);
                    currentShadow.setColor(Color.rgb(0, 0, 0, 0.08));
                }
            });
        } else {
            setCursor(Cursor.DEFAULT);
            getStyleClass().remove(Styles.INTERACTIVE);
            setOnMouseEntered(null);
            setOnMouseExited(null);
        }
        return this;
    }

    /* =========================================================================
     * 开箱即用高频语义化排版流式扩展 API
     * ========================================================================= */

    /**
     * 一键注入卡片头部组件（自动附加下沿薄边框隔离 facts）
     */
    public FXCardPane header(Node headerNode) {
        if (headerNode == null) return this;

        StackPane headerWrapper = new StackPane(headerNode);
        headerWrapper.setPadding(new Insets(0, 0, 12, 0));
        headerWrapper.setStyle("-fx-border-color: -color-border-muted; -fx-border-width: 0 0 1px 0;");

        // 始终锚定插入到最顶端
        if (!layoutBox.getChildren().isEmpty() && layoutBox.getChildren().getFirst() instanceof StackPane) {
            layoutBox.getChildren().set(0, headerWrapper);
        } else {
            layoutBox.getChildren().addFirst(headerWrapper);
        }
        return this;
    }

    /**
     * 一键注入正文容器体
     */
    public FXCardPane content(Node contentNode) {
        if (contentNode == null) return this;

        VBox.setVgrow(contentNode, Priority.ALWAYS);
        // 如果存在头部，则留出上内边距间隔
        if (layoutBox.getChildren().size() > 0) {
            VBox.setMargin(contentNode, new Insets(12, 0, 0, 0));
        }
        layoutBox.getChildren().add(contentNode);
        return this;
    }

    /**
     * 一键快捷注入底部动作控制区域（自动附加动作条上沿隔离条）
     */
    public FXCardPane footer(Node footerNode) {
        if (footerNode == null) return this;

        StackPane footerWrapper = new StackPane(footerNode);
        footerWrapper.setPadding(new Insets(12, 0, 0, 0));
        footerWrapper.setStyle("-fx-border-color: -color-border-muted; -fx-border-width: 1px 0 0 0;");

        VBox.setMargin(footerWrapper, new Insets(12, 0, 0, 0));
        layoutBox.getChildren().add(footerWrapper);
        return this;
    }

    public FXCardPane compact() {
        return padding(8);
    }

    public FXCardPane spacious() {
        return padding(24);
    }

    public FXCardPane padding(double padding) {
        layoutBox.setPadding(new Insets(padding));
        return this;
    }

    public FXCardPane spacing(double spacing) {
        layoutBox.setSpacing(spacing);
        return this;
    }

    public FXCardPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    public FXCardPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    public FXCardPane clearAll() {
        layoutBox.getChildren().clear();
        return this;
    }
}