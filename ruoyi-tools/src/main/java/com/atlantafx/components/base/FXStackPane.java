package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * FXStackPane - 堆叠布局容器
 * 继承自 JavaFX StackPane，实现 IFXNode 接口支持链式调用
 * 子节点会层叠显示，后添加的节点在上层
 */
public class FXStackPane extends StackPane implements IFXNode<FXStackPane> {

    /**
     * 默认构造函数
     */
    public FXStackPane() {
        super();
    }

    /**
     * 创建带子节点的堆叠布局
     *
     * @param children 子节点数组
     */
    public FXStackPane(Node... children) {
        super(children);
    }

    /**
     * 创建空白堆叠布局实例
     *
     * @return FXStackPane 实例
     */
    public static FXStackPane create() {
        return new FXStackPane();
    }

    /**
     * 创建带子节点的堆叠布局实例
     *
     * @param children 子节点数组
     * @return FXStackPane 实例
     */
    public static FXStackPane create(Node... children) {
        return new FXStackPane(children);
    }

    /**
     * 添加一个或多个子节点到容器中
     *
     * @param nodes 要添加的节点数组
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane add(Node... nodes) {
        getChildren().addAll(nodes);
        return this;
    }

    /**
     * 在指定索引位置添加子节点
     *
     * @param index 插入位置的索引（从 0 开始）
     * @param node  要添加的节点数组
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane addAt(int index, Node node) {
        getChildren().add(index, node);
        return this;
    }

    /**
     * 将指定节点插入到层叠面板的最底层（渲染在所有现有子节点之下）
     *
     * @param node 目标底部节点
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane addAsBottom(Node node) {
        if (node != null) {
            getChildren().addFirst(node);
        }
        return this;
    }

    /**
     * 将指定节点插入到层叠面板的最顶层（渲染在所有现有子节点之上）
     *
     * @param node 目标顶部节点
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane addAsTop(Node node) {
        if (node != null) {
            getChildren().addLast(node);
        }
        return this;
    }

    /**
     * 移除指定的子节点
     *
     * @param nodes 要移除的节点数组
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane remove(Node... nodes) {
        getChildren().removeAll(nodes);
        return this;
    }

    /**
     * 清空所有子节点
     *
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane clear() {
        getChildren().clear();
        return this;
    }

    // ==================== 布局对齐与盒模型属性 ====================

    /**
     * 设置容器的对齐方式
     * 决定子节点在容器中的整体对齐位置
     *
     * @param pos 对齐位置枚举值
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane align(Pos pos) {
        setAlignment(pos);
        return this;
    }

    /**
     * 单独控制某一个子节点在层叠面板中的局部对齐方式
     *
     * @param node     目标子节点
     * @param position 该节点的独立对齐枚举
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane alignNode(Node node, Pos position) {
        if (node != null) {
            StackPane.setAlignment(node, position);
        }
        return this;
    }

    /**
     * 为指定的子节点设置外边距（Margin）约束
     *
     * @param node   目标子节点
     * @param insets 外边距空间
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane marginNode(Node node, Insets insets) {
        if (node != null) {
            StackPane.setMargin(node, insets);
        }
        return this;
    }

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素），应用于上下左右四个方向
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置容器各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    // ==================== 尺寸限界控制 ====================

    /**
     * 同步锁定面板的首选宽度与高度
     *
     * @param width  宽度像素值
     * @param height 高度像素值
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane size(double width, double height) {
        setPrefWidth(width);
        setPrefHeight(height);
        return this;
    }

    /**
     * 设置容器宽度
     *
     * @param w 宽度值（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度
     *
     * @param h 高度值（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置容器最大宽度
     *
     * @param w 最大宽度值（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane mxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    /**
     * 设置容器最大高度
     *
     * @param h 最大高度值（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane mxHeight(double h) {
        setMaxHeight(h);
        return this;
    }

    /**
     * 设置容器最小宽度
     *
     * @param w 最小宽度值（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane mnWidth(double w) {
        setMinWidth(w);
        return this;
    }

    /**
     * 设置容器最小高度
     *
     * @param h 最小高度值（像素）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane mnHeight(double h) {
        setMinHeight(h);
        return this;
    }

    // ==================== 父级容器增长优先级分配 ====================

    /**
     * 强制将当前面板在 VBox 父布局中设置为垂直撑满
     *
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane vgrow() {
        javafx.scene.layout.VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 强制将当前面板在 HBox 父布局中设置为水平撑满
     *
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane hgrow() {
        javafx.scene.layout.HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== Node 通用行为与扩展 ====================

    /**
     * 设置 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置容器透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    // ==================== AtlantaFX 特色定制变体 ====================

    /**
     * 安全设置面板背景色
     * 同时复写 JavaFX 的背景色及 AtlantaFX 的全局卡片/面板控制变量，以保证在明暗主题切换时样式权重正常
     *
     * @param color CSS 颜色字符串（如 "#FFFFFF" 或主题变量 "-color-bg-default"）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 快捷设置自定义边框
     *
     * @param width  边框线粗细（像素）
     * @param color  CSS 格式边框颜色
     * @param radius 圆角弧度像素值
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx; -fx-background-radius: %spx;",
                width, color, radius, radius
        );
        return styleCss(style);
    }

    /**
     * 为当前面板注入现代化拟物态多级立体阴影（Elevations）
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATION_SMALL），2级适中（Styles.ELEVATION_MEDIUM），3级最深（Styles.ELEVATION_LARGE）
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane shadow(int level) {
        // 先清理可能存在的旧阴影伪类
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
        }
        return this;
    }

    /**
     * 设置弹性空间占位符
     * 创建一个填充背景的弹性空间
     *
     * @param backgroundColor 背景颜色
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane asSpacer(String backgroundColor) {
        setMinSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return background(backgroundColor);
    }

    /**
     * 设置为卡片容器
     * 白色背景、圆角、阴影效果
     *
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane asCard() {
        background("-color-bg-default");
        border(1, "#e0e0e0", 8);
        setEffect(new DropShadow(
                10, 0, 2, Color.rgb(0, 0, 0, 0.15)
        ));
        padding(16);
        return this;
    }

    /**
     * 设置为覆盖层
     * 半透明黑色背景，用于模态遮罩
     *
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane asOverlay() {
        background("rgba(0,0,0,0.5)");
        setMinSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return this;
    }

    // ==================== 多层交互扩展 API (Layer & Modal Controls) ====================

    /**
     * 独占显示指定索引层：将目标层的子节点设置为可见，并将其他所有子节点层批量隐藏
     * 适用于不依赖多窗口，直接在单一面板内切换“加载中层”、“空白无数据层”或“主业务层”的单页排版场景
     *
     * @param visibleLayerIndex 期望独占显示的子节点索引
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane showLayerExclusive(int visibleLayerIndex) {
        int size = getChildren().size();
        for (int i = 0; i < size; i++) {
            Node node = getChildren().get(i);
            boolean target = (i == visibleLayerIndex);
            node.setVisible(target);
            node.setManaged(target);
        }
        return this;
    }

    /**
     * 快捷顶层遮罩控制：若需要弹窗、加载动画等独占全屏事件，控制最顶层子节点的可见性
     *
     * @param show true-显示顶层遮罩并使能，false-隐藏顶层遮罩并使其不参与布局
     * @return FXStackPane 实例（链式调用）
     */
    public FXStackPane toggleTopMask(boolean show) {
        if (!getChildren().isEmpty()) {
            Node topNode = getChildren().getLast();
            topNode.setVisible(show);
            topNode.setManaged(show);
        }
        return this;
    }
}