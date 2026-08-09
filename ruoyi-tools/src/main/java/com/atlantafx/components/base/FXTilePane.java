package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

/**
 * FXTilePane - 平铺布局容器
 * 继承自 JavaFX TilePane，实现 IFXNode 接口支持链式调用
 * 所有子节点大小一致，整齐排列
 */
public class FXTilePane extends TilePane implements IFXNode<FXTilePane> {

    /**
     * 默认构造函数
     */
    public FXTilePane() {
        super();
    }

    /**
     * 创建平铺布局（指定间距）
     *
     * @param hgap 水平间距（像素）
     * @param vgap 垂直间距（像素）
     */
    public FXTilePane(double hgap, double vgap) {
        super(hgap, vgap);
    }

    /**
     * 创建平铺布局（指定方向和间距）
     *
     * @param orientation 方向（水平/垂直）
     * @param hgap        水平间距（像素）
     * @param vgap        垂直间距（像素）
     */
    public FXTilePane(Orientation orientation, double hgap, double vgap) {
        super(orientation, hgap, vgap);
    }

    /**
     * 创建空白平铺布局实例
     *
     * @return FXTilePane 实例
     */
    public static FXTilePane create() {
        return new FXTilePane();
    }

    /**
     * 创建平铺布局实例
     *
     * @param hgap 水平间距（像素）
     * @param vgap 垂直间距（像素）
     * @return FXTilePane 实例
     */
    public static FXTilePane create(double hgap, double vgap) {
        return new FXTilePane(hgap, vgap);
    }

    /**
     * 创建平铺布局实例
     *
     * @param orientation 方向（水平/垂直）
     * @param hgap        水平间距（像素）
     * @param vgap        垂直间距（像素）
     * @return FXTilePane 实例
     */
    public static FXTilePane create(Orientation orientation, double hgap, double vgap) {
        return new FXTilePane(orientation, hgap, vgap);
    }

    /**
     * 添加一个或多个子节点到容器中
     *
     * @param nodes 要添加的节点数组
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane add(Node... nodes) {
        getChildren().addAll(nodes);
        return this;
    }

    /**
     * 移除指定的子节点
     *
     * @param nodes 要移除的节点数组
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane remove(Node... nodes) {
        getChildren().removeAll(nodes);
        return this;
    }

    /**
     * 清空所有子节点
     *
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane clear() {
        getChildren().clear();
        return this;
    }

    /**
     * 设置流的方向为水平
     *
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane horizontal() {
        setOrientation(Orientation.HORIZONTAL);
        return this;
    }

    /**
     * 设置流的方向为垂直
     *
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane vertical() {
        setOrientation(Orientation.VERTICAL);
        return this;
    }

    /**
     * 设置子节点的对齐方式
     *
     * @param pos 对齐位置枚举值
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane align(Pos pos) {
        setAlignment(pos);
        return this;
    }

    /**
     * 设置水平间距
     *
     * @param gap 水平间距（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane hgap(double gap) {
        setHgap(gap);
        return this;
    }

    /**
     * 设置垂直间距
     *
     * @param gap 垂直间距（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane vgap(double gap) {
        setVgap(gap);
        return this;
    }

    /**
     * 设置间距
     *
     * @param gap 间距值（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane gap(double gap) {
        setHgap(gap);
        setVgap(gap);
        return this;
    }

    /**
     * 设置瓦片尺寸
     *
     * @param width  宽度（像素）
     * @param height 高度（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane tileSize(double width, double height) {
        setPrefTileWidth(width);
        setPrefTileHeight(height);
        return this;
    }

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane padding(double v) {
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
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置首选列数
     * 水平布局时控制列数，垂直布局时控制行数
     *
     * @param columns 列数
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane prefColumns(int columns) {
        setPrefColumns(columns);
        return this;
    }

    /**
     * 设置首选行数
     * 水平布局时控制行数，垂直布局时控制列数
     *
     * @param rows 行数
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane prefRows(int rows) {
        setPrefRows(rows);
        return this;
    }

    /**
     * 设置瓦片宽度
     * 所有子节点会被调整为这个宽度
     *
     * @param width 宽度（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane tileWidth(double width) {
        setPrefTileWidth(width);
        return this;
    }

    /**
     * 设置瓦片高度
     * 所有子节点会被调整为这个高度
     *
     * @param height 高度（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane tileHeight(double height) {
        setPrefTileHeight(height);
        return this;
    }

    /**
     * 设置容器尺寸
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置容器宽度
     *
     * @param w 宽度值（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度
     *
     * @param h 高度值（像素）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane id(String id) {
        setId(id);
        return this;
    }

    // ==================== 父级容器增长优先级分配 ====================

    public FXTilePane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    public FXTilePane hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    public FXTilePane border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx; -fx-background-radius: %spx;",
                width, color, radius, radius
        );
        return styleCss(style);
    }

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置透明度
     *
     * @param opacity 透明度值
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 为当前面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATION_SMALL），2级适中（Styles.ELEVATION_MEDIUM），3级最深（Styles.ELEVATION_LARGE）
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane shadow(int level) {
        // 先行清理可能存在的旧阴影伪类
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3, Styles.ELEVATED_4);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
            case 4 -> stylesClass(Styles.ELEVATED_4);
        }
        return this;
    }

    // ==================== 快捷高级结构网格排版模板 ====================

    /**
     * 一键转化为多媒体/图片墙自适应排版架构
     * Fixed Square Tile Grid Flow
     *
     * @param tileSize 正方形单元格瓦片的单边绝对尺寸（像素值）
     * @param columns  首选期望限制列数
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane asImageGrid(double tileSize, int columns) {
        return gap(8)
                .padding(10)
                .tileSize(tileSize, tileSize)
                .prefColumns(columns)
                .align(Pos.TOP_LEFT);
    }

    /**
     * 一键转化为均等矩阵微型指标看板流布局
     * Dashboard Grid Flow
     *
     * @param width  微指标卡片固定宽度
     * @param height 微指标卡片固定高度
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane asDashboard(double width, double height) {
        return gap(15)
                .padding(12)
                .tileSize(width, height)
                .align(Pos.CENTER_LEFT);
    }

    /**
     * 设置为联系人列表布局
     * 适合显示头像网格
     *
     * @return FXTilePane 实例（链式调用）
     */
    public FXTilePane asContactList(double width, double height) {
        return gap(12).padding(12)
                .tileSize(width, height)
                .prefColumns(5)
                .align(Pos.CENTER);
    }
}
