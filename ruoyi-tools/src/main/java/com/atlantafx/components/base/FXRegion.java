package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * FXRegion - 通用区域组件
 * 继承自 JavaFX Region，提供链式调用支持
 * 用作空白占位、分隔线、自定义容器等场景的基础构建块
 */
public class FXRegion extends Region implements IFXNode<FXRegion> {

    /**
     * 默认构造函数
     */
    private FXRegion() {
        super();
    }

    /**
     * 创建空白的区域实例
     *
     * @return FXRegion 实例
     */
    public static FXRegion create() {
        return new FXRegion();
    }

    /**
     * 设置区域宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置区域高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置区域最大宽度
     *
     * @param w 最大宽度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion mxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    /**
     * 设置区域最大高度
     *
     * @param h 最大高度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion mxHeight(double h) {
        setMaxHeight(h);
        return this;
    }

    /**
     * 设置区域最小宽度
     *
     * @param w 最小宽度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion mnWidth(double w) {
        setMinWidth(w);
        return this;
    }

    /**
     * 设置区域最小高度
     *
     * @param h 最小高度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion mnHeight(double h) {
        setMinHeight(h);
        return this;
    }

    /**
     * 设置区域尺寸（宽度和高度）
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置区域在 VBox 中的垂直增长优先级
     * 使区域在垂直方向填充所有可用空间
     *
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置区域在 HBox 中的水平增长优先级
     *
     * @return
     */
    public FXRegion hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置区域的内边距
     *
     * @param v 内边距值（像素），应用于四个方向
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置区域各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置区域可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置区域是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置区域透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置区域旋转角度
     *
     * @param angle 旋转角度（度数）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion rotate(double angle) {
        setRotate(angle);
        return this;
    }

    /**
     * 设置区域的 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置区域背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion background(String color) {
        setStyle("-fx-background-color: " + color + ";");
        return this;
    }

    /**
     * 设置区域边框
     *
     * @param width  边框宽度（像素）
     * @param color  CSS 格式的颜色字符串
     * @param radius 圆角半径（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx;",
                width, color, radius
        );
        return styleCss(style);
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为水平弹性空间
     * 在 HBox 中使用时，会自动填充水平方向的可用空间
     *
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion hSpacer() {
        setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置为垂直弹性空间
     * 在 VBox 中使用时，会自动填充垂直方向的可用空间
     *
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion vSpacer() {
        setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置为水平分隔线
     * 创建一条细长的水平线，常用于内容分隔
     *
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion hDivider() {
        setMinHeight(2);
        setPrefHeight(2);
        setMaxHeight(2);
        setMinWidth(Double.MAX_VALUE);
        setPrefWidth(Double.MAX_VALUE);
        return background("#e0e0e0");
    }

    /**
     * 设置为垂直分隔线
     * 创建一条细长的垂直线，常用于列分隔
     *
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion vDivider() {
        setMinWidth(2);
        setPrefWidth(2);
        setMaxWidth(2);
        setMinHeight(50);
        setPrefHeight(50);
        return background("#e0e0e0");
    }

    /**
     * 设置为固定尺寸的垫片（Spacer）
     * 用于在布局中创建固定大小的空白区域
     *
     * @param width  宽度（像素）
     * @param height 高度（像素）
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion spacer(double width, double height) {
        setMinWidth(width);
        setPrefWidth(width);
        setMinHeight(height);
        setPrefHeight(height);
        return this;
    }

    /**
     * 设置为圆形装饰元素
     * 创建一个圆形的区域，可用作指示器、装饰点等
     *
     * @param diameter 直径（像素）
     * @param color    颜色
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion circle(double diameter, String color) {
        setMinSize(diameter, diameter);
        setPrefSize(diameter, diameter);
        setMaxSize(diameter, diameter);
        setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: %spx;",
                color, diameter / 2
        ));
        return this;
    }

    /**
     * 设置为条纹背景
     * 创建斜纹背景效果，常用于标记特殊区域
     *
     * @param color1 第一种颜色
     * @param color2 第二种颜色
     * @return FXRegion 实例（链式调用）
     */
    public FXRegion stripedBackground(String color1, String color2) {
        setStyle(String.format(
                "-fx-background-color: repeating-linear-gradient(45deg, %s, %s 10px, %s 10px, %s 20px);",
                color1, color2, color1, color2
        ));
        return this;
    }
}
