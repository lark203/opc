package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXBorderPane - 边界布局容器
 * 继承自 JavaFX BorderPane，实现 IFXNode 接口支持链式调用
 * 将区域分为上、下、左、右、中五个位置
 */
public class FXBorderPane extends BorderPane implements IFXNode<FXBorderPane> {

    /**
     * 默认构造函数
     */
    public FXBorderPane() {
        super();
    }

    /**
     * 创建带中心节点的边界布局
     *
     * @param center 中心节点
     */
    public FXBorderPane(Node center) {
        super(center);
    }

    /**
     * 创建空白边界布局实例
     *
     * @return FXBorderPane 实例
     */
    public static FXBorderPane create() {
        return new FXBorderPane();
    }

    /**
     * 创建带中心节点的边界布局实例
     *
     * @param center 中心节点
     * @return FXBorderPane 实例
     */
    public static FXBorderPane create(Node center) {
        return new FXBorderPane(center);
    }

    // ==================== 五大物理核心区域流式控制 ====================

    /**
     * 设置顶部节点
     *
     * @param node 顶部节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane top(Node node) {
        setTop(node);
        return this;
    }

    /**
     * 设置顶部区域节点，并对其进行高度限制约束
     *
     * @param node   目标顶部节点
     * @param height 锁定的固定高度（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane top(Node node, double height) {
        setTop(node);
        if (node instanceof javafx.scene.layout.Region region) {
            region.setMinHeight(height);
            region.setPrefHeight(height);
            region.setMaxHeight(height);
        }
        return this;
    }

    /**
     * 设置底部节点
     *
     * @param node 底部节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane bottom(Node node) {
        setBottom(node);
        return this;
    }

    /**
     * 设置底部区域节点，并对其进行高度限制约束
     *
     * @param node   目标底部节点
     * @param height 锁定的固定高度（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane bottom(Node node, double height) {
        setBottom(node);
        if (node instanceof javafx.scene.layout.Region region) {
            region.setMinHeight(height);
            region.setPrefHeight(height);
            region.setMaxHeight(height);
        }
        return this;
    }

    /**
     * 设置左侧节点
     *
     * @param node 左侧节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane left(Node node) {
        setLeft(node);
        return this;
    }

    /**
     * 设置左侧导航区域节点，并对其进行宽度限制约束
     *
     * @param node  目标左侧节点
     * @param width 锁定的固定宽度（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane left(Node node, double width) {
        setLeft(node);
        if (node instanceof javafx.scene.layout.Region region) {
            region.setMinWidth(width);
            region.setPrefWidth(width);
            region.setMaxWidth(width);
        }
        return this;
    }

    /**
     * 设置右侧节点
     *
     * @param node 右侧节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane right(Node node) {
        setRight(node);
        return this;
    }

    /**
     * 设置右侧辅助面板区域节点，并对其进行宽度限制约束
     *
     * @param node  目标右侧节点
     * @param width 锁定的固定宽度（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane right(Node node, double width) {
        setRight(node);
        if (node instanceof javafx.scene.layout.Region region) {
            region.setMinWidth(width);
            region.setPrefWidth(width);
            region.setMaxWidth(width);
        }
        return this;
    }

    /**
     * 设置中心节点
     *
     * @param node 中心节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane center(Node node) {
        setCenter(node);
        return this;
    }

    /**
     * 设置所有区域的节点
     *
     * @param top    顶部节点
     * @param bottom 底部节点
     * @param left   左侧节点
     * @param right  右侧节点
     * @param center 中心节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane all(Node top, Node bottom, Node left, Node right, Node center) {
        setTop(top);
        setBottom(bottom);
        setLeft(left);
        setRight(right);
        setCenter(center);
        return this;
    }

    // ==================== 几何间距与限界控制 ====================

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane padding(double v) {
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
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 同步锁定面板的首选宽度与高度
     *
     * @param width  宽度像素值
     * @param height 高度像素值
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane size(double width, double height) {
        setPrefWidth(width);
        setPrefHeight(height);
        return this;
    }

    /**
     * 设置容器宽度
     *
     * @param w 宽度值（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度
     *
     * @param h 高度值（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    public FXBorderPane mxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    public FXBorderPane mxHeight(double h) {
        setMaxHeight(h);
        return this;
    }

    public FXBorderPane mnWidth(double w) {
        setMinWidth(w);
        return this;
    }

    public FXBorderPane mnHeight(double h) {
        setMinHeight(h);
        return this;
    }

    // ==================== 父级容器增长优先级分配 ====================

    /**
     * 强制将当前面板在 VBox 父布局中设置为垂直撑满
     *
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 强制将当前面板在 HBox 父布局中设置为水平撑满
     *
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== Node 通用行为增强 ====================

    /**
     * 设置 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    public FXBorderPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    // ==================== AtlantaFX 高级样式定制 ====================

    /**
     * 安全设置面板背景色
     * 同时复写 JavaFX 的背景色及 AtlantaFX 的全局核心变量，以保证在明暗主题切换时样式权重正常
     *
     * @param color CSS 颜色字符串（如 "#FFFFFF" 或主题变量 "-color-bg-default"）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 快捷设置自定义边框颜色和粗细与圆角
     *
     * @param width  边框线粗细（像素）
     * @param color  CSS 格式边框颜色
     * @param radius 圆角半径（像素）
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane border(double width, String color, double radius) {
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
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane shadow(int level) {
        // 先清理可能存在的旧阴影伪类
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
        }
        return this;
    }

    // ==================== 快捷高级架构视图布局（Scaffolds） ====================

    /**
     * 设置为经典桌面应用主脚手架布局
     * 包含：顶部标题状态栏 + 左侧主要导航 + 中间核心工作区 + 底部系统状态栏
     *
     * @param header  顶部标题栏区域节点
     * @param sidebar 左侧系统菜单导航栏节点
     * @param content 中心核心工作空间节点
     * @param footer  底部状态指示栏区域节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane asAppLayout(Node header, Node sidebar, Node content, Node footer) {
        return top(header, 60.0)
                .left(sidebar, 240.0)
                .center(content)
                .bottom(footer, 32.0);
    }

    /**
     * 设置为简单页面布局
     * 只有顶部和中心区域
     *
     * @param header  顶部节点
     * @param content 中心节点
     * @return FXBorderPane 实例（链式调用）
     */
    public FXBorderPane asSimplePage(Node header, Node content) {
        return top(header, 60).center(content);
    }
}
