package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.Collection;

/**
 * FXTextFlow - 基于 AtlantaFX 风格的富文本流式排版布局组件
 * 继承自 JavaFX TextFlow，实现 IFXNode 接口支持链式调用
 * 针对多样式富文本无缝拼接、国际化排版、行高微调以及 AtlantaFX 现代化语义字体（Typography）进行了流式封装
 */
public class FXTextFlow extends TextFlow implements IFXNode<FXTextFlow> {

    /**
     * 构造函数私有化，强制通过静态工厂方法 create() 进行流式实例化
     */
    public FXTextFlow() {
        super();
    }

    /**
     * 创建一个全新的、空白的 FXTextFlow 实例
     *
     * @return FXTextFlow 实例（链式调用入口）
     */
    public static FXTextFlow create() {
        return new FXTextFlow();
    }

    /**
     * 创建一个包含初始文本或节点序列的 FXTextFlow 实例
     *
     * @param children 初始注入的文本、超链接或其他底层 Node 节点数组
     * @return FXTextFlow 实例（链式调用入口）
     */
    public static FXTextFlow create(Node... children) {
        FXTextFlow textFlow = new FXTextFlow();
        if (children != null) {
            textFlow.getChildren().addAll(children);
        }
        return textFlow;
    }

    // ==================== 子节点动态追加与富文本拼接 ====================

    /**
     * 向文本流末尾流式追加单个节点（Text, Hyperlink, ImageView等）
     *
     * @param child 目标内容节点
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow add(Node child) {
        if (child != null) {
            getChildren().add(child);
        }
        return this;
    }

    /**
     * 批量向文本流末尾追加多个节点
     *
     * @param children 节点可变参数数组
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow addAll(Node... children) {
        if (children != null) {
            getChildren().addAll(children);
        }
        return this;
    }

    /**
     * 批量向文本流末尾追加节点集合
     *
     * @param children 节点集合
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow addAll(Collection<? extends Node> children) {
        if (children != null) {
            getChildren().addAll(children);
        }
        return this;
    }

    /**
     * 快捷追加一段基础文本到流中
     *
     * @param content 字符串内容
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow addText(String content) {
        if (content != null) {
            getChildren().add(FXText.create(content));
        }
        return this;
    }

    /**
     * 快捷追加一段指定样式的文本（如特定的 CSS 样式串）
     *
     * @param content   字符串内容
     * @param cssStyles 内联 CSS 样式（例如 "-fx-fill: -color-accent-emphasis;"）
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow addText(String content, String cssStyles) {
        if (content != null) {
            Text text = new Text(content);
            if (cssStyles != null && !cssStyles.isBlank()) {
                text.setStyle(cssStyles);
            }
            getChildren().add(text);
        }
        return this;
    }

    /**
     * 清空当前文本流中的所有文本与节点
     *
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow clear() {
        getChildren().clear();
        return this;
    }

    // ==================== 原生排版属性流式扩展 ====================

    /**
     * 设置文本流内部所有内容的水平对齐方式
     *
     * @param alignment TextAlignment 枚举值（LEFT, RIGHT, CENTER, JUSTIFY）
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow textAlignment(TextAlignment alignment) {
        if (alignment != null) {
            setTextAlignment(alignment);
        }
        return this;
    }

    /**
     * 快捷设置对齐方式：居中对齐
     *
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow alignCenter() {
        return textAlignment(TextAlignment.CENTER);
    }

    /**
     * 快捷设置对齐方式：两端对齐（常用于大段国际化技术文档排版）
     *
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow alignJustify() {
        return textAlignment(TextAlignment.JUSTIFY);
    }

    /**
     * 设置多行文本之间的行间距（Line Spacing）
     *
     * @param spacing 间距像素值
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow lineSpacing(double spacing) {
        setLineSpacing(spacing);
        return this;
    }

    // ==================== 基础布局属性流式扩展 ====================

    /**
     * 设置文本流容器的统一内边距 (Padding)
     *
     * @param padding Insets 内边距对象
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow padding(Insets padding) {
        setPadding(padding);
        return this;
    }

    /**
     * 快捷设置文本流四周统一的内边距值
     *
     * @param value 边距像素值
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow padding(double value) {
        setPadding(new Insets(value));
        return this;
    }

    /**
     * 局部精细控制文本流四个方向的内边距值
     */
    public FXTextFlow padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置容器固定宽度（同步刷新最小与首选宽度，常用于防止大段富文本超出父容器边界）
     *
     * @param w 宽度值（像素）
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器固定高度
     *
     * @param h 高度值（像素）
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 快捷锁定首选宽高尺度
     *
     * @param w 宽度（像素）
     * @param h 高度（像素）
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 当置于 HBox 父容器中时，声明横向最高延伸优先级
     *
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 当置于 VBox 父容器中时，声明纵向最高延伸优先级
     *
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== AtlantaFX 核心语义化排版样式（Typography） ====================

    /**
     * 激活语义化字体：大标题样式级（Title Large）
     */
    public FXTextFlow titleLarge() {
        return stylesClass(Styles.TITLE_1);
    }

    /**
     * 激活语义化字体：中标题样式级（Title Medium）
     */
    public FXTextFlow titleMedium() {
        return stylesClass(Styles.TITLE_3);
    }

    /**
     * 激活语义化字体：小标题样式级（Title Small）
     */
    public FXTextFlow titleSmall() {
        return stylesClass(Styles.TITLE_4);
    }

    /**
     * 激活语义化字体：副标题或说明文案（Caption / Muted / Small Style）
     */
    public FXTextFlow caption() {
        return stylesClass(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
    }
    // ==================== AtlantaFX 现代语义状态与背景色深度封装 ====================

    /**
     * 快捷将文本流转换为“精美交互卡片化底面板”
     * 融合 AtlantaFX 的配色边界，自带 1 像素的微弱优雅边框线与圆角
     *
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow bgCardStyle() {
        return styleCss(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-border-muted;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-radius: 6px;"
        );
    }

    /**
     * 一键注入现代化拟物态立体阴影
     *
     * @param level 阴影等级（1-4）
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow shadow(int level) {
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3, Styles.ELEVATED_4);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
            case 4 -> stylesClass(Styles.ELEVATED_4);
        }
        return this;
    }

    /**
     * 快捷设置自定义背景色（支持应用 AtlantaFX 主题变量如 "-color-bg-subtle"）
     *
     * @param color 颜色字符串或主题变量名
     * @return FXTextFlow 实例（链式调用）
     */
    public FXTextFlow background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    // ==================== 组件通用状态控制 ====================

    /**
     * 控制可见性
     */
    public FXTextFlow visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 控制是否纳入布局边界计算
     */
    public FXTextFlow managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 改变组件整体透明度
     */
    public FXTextFlow opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 控制全局禁用状态
     */
    public FXTextFlow disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }
}