package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXFlowPane - 流式布局容器
 * 继承自 JavaFX FlowPane，实现 IFXNode 接口支持链式调用
 * 子节点按顺序排列，自动换行或换列
 */
public class FXFlowPane extends FlowPane implements IFXNode<FXFlowPane> {

    /**
     * 默认构造函数
     */
    public FXFlowPane() {
        super();
    }

    /**
     * 创建流式布局（指定间距）
     *
     * @param hgap 水平间距（像素）
     * @param vgap 垂直间距（像素）
     */
    public FXFlowPane(double hgap, double vgap) {
        super(hgap, vgap);
    }

    /**
     * 创建流式布局（指定方向和间距）
     *
     * @param orientation 方向（水平/垂直）
     * @param hgap        水平间距（像素）
     * @param vgap        垂直间距（像素）
     */
    public FXFlowPane(Orientation orientation, double hgap, double vgap) {
        super(orientation, hgap, vgap);
    }

    /**
     * 创建空白流式布局实例
     *
     * @return FXFlowPane 实例
     */
    public static FXFlowPane create() {
        return new FXFlowPane();
    }

    /**
     * 创建流式布局实例
     *
     * @param hgap 水平间距（像素）
     * @param vgap 垂直间距（像素）
     * @return FXFlowPane 实例
     */
    public static FXFlowPane create(double hgap, double vgap) {
        return new FXFlowPane(hgap, vgap);
    }

    /**
     * 创建指定布局方向和间距的流式布局实例
     *
     * @param orientation 布局方向（Orientation.HORIZONTAL 或 Orientation.VERTICAL）
     * @param hgap        水平间距（像素）
     * @param vgap        垂直间距（像素）
     * @return FXFlowPane 实例（链式调用入口）
     */
    public static FXFlowPane create(Orientation orientation, double hgap, double vgap) {
        return new FXFlowPane(orientation, hgap, vgap);
    }

    /**
     * 添加一个或多个子节点到容器中
     *
     * @param nodes 要添加的节点数组
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane add(Node... nodes) {
        getChildren().addAll(nodes);
        return this;
    }

    /**
     * 移除指定的子节点
     *
     * @param nodes 要移除的节点数组
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane remove(Node... nodes) {
        getChildren().removeAll(nodes);
        return this;
    }

    /**
     * 清空所有子节点
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane clear() {
        getChildren().clear();
        return this;
    }

    // ==================== 核心布局属性流式扩展 ====================

    /**
     * 设置流的方向为水平
     * 从左到右排列，自动换行
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane horizontal() {
        setOrientation(Orientation.HORIZONTAL);
        return this;
    }

    /**
     * 设置流的方向为垂直
     * 从上到下排列，自动换列
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane vertical() {
        setOrientation(Orientation.VERTICAL);
        return this;
    }

    /**
     * 设置水平间距
     *
     * @param gap 水平间距（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane hgap(double gap) {
        setHgap(gap);
        return this;
    }

    /**
     * 设置垂直间距
     *
     * @param gap 垂直间距（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane vgap(double gap) {
        setVgap(gap);
        return this;
    }

    /**
     * 设置间距（水平和垂直）
     *
     * @param gap 间距值（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane gap(double gap) {
        setHgap(gap);
        setVgap(gap);
        return this;
    }

    /**
     * 设置子节点的对齐方式
     *
     * @param pos 对齐位置枚举值
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane align(Pos pos) {
        setAlignment(pos);
        return this;
    }

    /**
     * 设置行/列的按行对齐基准（仅在有剩余空间时对排布产生影响）
     *
     * @param align 行或列的对齐基准枚举（如 VPos.CENTER, HPos.RIGHT）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane rowValignment(VPos align) {
        setRowValignment(align);
        return this;
    }

    /**
     * 设置列按水平方向对齐的基准
     *
     * @param align 水平对齐基准
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane columnHalignment(HPos align) {
        setColumnHalignment(align);
        return this;
    }

    /**
     * 设置内部网格瓦片的统一宽度（针对列瓦片控制）
     *
     * @param width 列宽度（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane prefWrapLength(double width) {
        setPrefWrapLength(width);
        return this;
    }

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane padding(double v) {
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
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置容器宽度
     *
     * @param w 宽度值（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度
     *
     * @param h 高度值（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 快捷锁定首选宽高尺寸
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 当作为子节点置于 HBox 容器中时的横向延伸优先级
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 当作为子节点置于 VBox 容器中时的纵向延伸优先级
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    // ==================== AtlantaFX & 现代面板样式定制扩展 ====================

    /**
     * 快捷将流式面板升级为“轻量交互式卡片外壳样式”
     * 融合 AtlantaFX 的 Subtle 配色边界，自带 1 像素的微弱优雅边框线与圆角
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane bgCardStyle() {
        return styleCss(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-border-muted;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-radius: 6px;"
        );
    }

    /**
     * 快捷设置自定义背景色（支持传入十六进制色值或 AtlantaFX 主题变量如 "-color-bg-subtle"）
     *
     * @param color 颜色字符串或主题变量名
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 快捷设置自定义边框样式与圆角半径
     *
     * @param width  边框线粗细（像素）
     * @param color  边框颜色值或主题样式变量
     * @param radius 圆角半径（像素）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx; -fx-background-radius: %spx;",
                width, color, radius, radius
        );
        return styleCss(style);
    }

    /**
     * 为当前面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级。1级最轻（ELEVATED_1），最大支持4级（ELEVATED_4）
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane shadow(int level) {
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3, Styles.ELEVATED_4);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
            case 4 -> stylesClass(Styles.ELEVATED_4);
        }
        return this;
    }

    // ==================== 业务场景预设样式（高频快捷键） ====================

    /**
     * 快捷设置为“标签云 (Tag Cloud)”布局
     * 适合紧凑排列显示标签、关键词或徽章
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane asTagCloud() {
        return horizontal().gap(8).padding(12).align(Pos.CENTER_LEFT);
    }

    /**
     * 快捷设置为“图片画廊 (Image Gallery)”网格布局
     * 适合匀称地展示图片网格、缩略图列表
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane asGallery() {
        return horizontal().gap(12).padding(16).align(Pos.TOP_LEFT);
    }

    /**
     * 快捷设置为“按钮组 (Button Group)”流式排版
     * 适合底部工具栏、弹窗操作按钮等自适应排布
     *
     * @return FXFlowPane 实例（链式调用）
     */
    public FXFlowPane asButtonGroup() {
        return horizontal().gap(8).align(Pos.CENTER_LEFT);
    }
}
