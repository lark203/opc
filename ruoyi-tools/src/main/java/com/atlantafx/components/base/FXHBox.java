package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * FXHBox - 水平布局容器组件
 * 继承自 JavaFX HBox，实现 IFXNode 接口支持链式调用
 * 提供便捷的水平布局管理和样式设置方法
 */
public class FXHBox extends HBox implements IFXNode<FXHBox> {

    /**
     * 默认构造函数
     */
    public FXHBox() {
        super();
    }

    /**
     * 创建带间距的水平布局容器
     *
     * @param spacing 子节点之间的水平间距（像素）
     */
    public FXHBox(double spacing) {
        super(spacing);
    }

    /**
     * 创建空白的水平布局容器实例
     *
     * @return FXHBox 实例
     */
    public static FXHBox create() {
        return new FXHBox();
    }

    /**
     * 创建带间距的水平布局容器实例
     *
     * @param spacing 子节点之间的水平间距（像素）
     * @return FXHBox 实例
     */
    public static FXHBox create(double spacing) {
        return new FXHBox(spacing);
    }

    /**
     * 添加一个或多个子节点到容器中
     *
     * @param nodes 要添加的节点数组
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox add(Node... nodes) {
        getChildren().addAll(nodes);
        return this;
    }

    /**
     * 在指定索引位置添加子节点
     * 可以控制节点的添加顺序
     *
     * @param index 插入位置的索引（从 0 开始）
     * @param node  要添加的节点数组
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox addAt(int index, Node node) {
        getChildren().add(index, node);
        return this;
    }

    /**
     * 添加一个节点到容器的最左侧开头
     *
     * @param node 要插入顶部的节点
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox addFirst(Node node) {
        getChildren().addFirst(node);
        return this;
    }

    /**
     * 移除指定的子节点
     *
     * @param nodes 要移除的节点数组
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox remove(Node... nodes) {
        getChildren().removeAll(nodes);
        return this;
    }

    /**
     * 清空所有子节点
     *
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox clear() {
        getChildren().clear();
        return this;
    }

    // ==================== 布局与几何属性 ====================

    /**
     * 设置容器的对齐方式
     * 决定子节点在容器中的整体对齐位置
     *
     * @param pos 对齐位置枚举值（如 Pos.TOP_LEFT、Pos.CENTER 等）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox align(Pos pos) {
        setAlignment(pos);
        return this;
    }

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素），应用于上下左右四个方向
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置容器各方向的内边距
     * 可分别控制上下左右的边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置容器宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 快捷锁定首选宽高
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置容器最大宽度
     * 限制容器在水平方向的最大尺寸
     *
     * @param w 最大宽度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox mxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    /**
     * 设置容器最大高度
     * 限制容器在垂直方向的最大尺寸
     *
     * @param h 最大高度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox mxHeight(double h) {
        setMaxHeight(h);
        return this;
    }

    /**
     * 设置容器最小宽度
     * 确保容器在水平方向的最小尺寸
     *
     * @param w 最小宽度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox mnWidth(double w) {
        setMinWidth(w);
        return this;
    }

    /**
     * 设置容器最小高度
     * 确保容器在垂直方向的最小尺寸
     *
     * @param h 最小高度值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox mnHeight(double h) {
        setMinHeight(h);
        return this;
    }

    /**
     * 设置节点在水平方向的增长优先级为 ALWAYS
     * 该节点会在水平方向填充所有可用空间
     *
     * @param node 要设置的节点
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox hgrow(Node node) {
        HBox.setHgrow(node, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置节点在水平方向的增长优先级为 NEVER
     * 该节点不会在水平方向扩展，保持其首选大小
     *
     * @param node 要设置的节点
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox hgrowN(Node node) {
        HBox.setHgrow(node, Priority.NEVER);
        return this;
    }

    /**
     * 设置节点在水平方向的增长优先级为 SOMETIMES
     * 该节点会在必要时在水平方向扩展
     *
     * @param node 要设置的节点
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox hgrowS(Node node) {
        HBox.setHgrow(node, Priority.SOMETIMES);
        return this;
    }

    /**
     * 设置子节点之间的间距
     * 动态调整水平布局的间距
     *
     * @param spacing 新的间距值（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox spacing(double spacing) {
        setSpacing(spacing);
        return this;
    }

    /**
     * 设置容器是否填充整个父容器高度
     *
     * @param fillHeight true-填充高度，false-不填充
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox fillHeight(boolean fillHeight) {
        setFillHeight(fillHeight);
        return this;
    }

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置容器透明度
     *
     * @param opacity 透明度值（0.0-1.0），0.0 完全透明，1.0 完全不透明
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置容器旋转角度
     *
     * @param angle 旋转角度（度数），正值表示顺时针旋转
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox rotate(double angle) {
        setRotate(angle);
        return this;
    }

    /**
     * 设置容器的 CSS ID
     * 用于通过 CSS 选择器精确定位和样式化
     *
     * @param id CSS ID 标识符
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox id(String id) {
        setId(id);
        return this;
    }

    // ==================== AtlantaFX 高级样式定制 ====================

    /**
     * 安全地修改容器背景色
     * 覆写 AtlantaFX 内部专属变量，防止因主题权重过高导致常规 -fx-background-color 被吞
     *
     * @param color CSS颜色表达式（如 "#FFFFFF"、"-color-bg-inset"）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 设置容器边框
     *
     * @param width  边框宽度（像素）
     * @param color  CSS 格式的颜色字符串
     * @param radius 圆角半径（像素）
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx;",
                width, color, radius
        );
        return styleCss(style);
    }

    /**
     * 设置弹性空间（占位节点）
     * 创建一个不可见的弹性空间，用于推开其他节点
     *
     * @return FXHBox 实例（链式调用）
     */
    public FXHBox spacer() {
        FXRegion spacer = FXRegion.create();
        spacer.setMaxWidth(Double.MAX_VALUE);
        spacer.setMinWidth(0);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return add(spacer);
    }

    // ==================== 滚动支持 ====================

    /**
     * 将容器包装在 ScrollPane 中
     * 当内容超出容器宽度时显示水平滚动条
     *
     * @return ScrollPane 包装后的滚动面板
     */
    public ScrollPane withScroll() {
        return FXScrollPane.create(this).fitToHeight().styleCss("-fx-background: transparent; -fx-background-color: transparent;");
    }
}
