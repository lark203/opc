package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXAnchorPane - 基于 AtlantaFX 风格的锚定布局面板组件
 * 继承自 JavaFX AnchorPane，实现 IFXNode 接口支持链式调用
 * 针对高频的相对/绝对定位、多节点批量锚定以及现代卡片样式进行了深度流式（Fluent）封装
 */
public class FXAnchorPane extends AnchorPane implements IFXNode<FXAnchorPane> {

    /**
     * 构造函数私有化，强制通过静态工厂方法 create() 进行实例化
     */
    private FXAnchorPane() {
        super();
    }

    /**
     * 创建一个全新的 FXAnchorPane 实例
     *
     * @return FXAnchorPane 实例（链式调用入口）
     */
    public static FXAnchorPane create() {
        return new FXAnchorPane();
    }

    // ==================== 子节点添加与流式锚定扩展 ====================

    /**
     * 向面板中追加一个普通子节点（不设置初始锚点）
     *
     * @param child 子节点
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane add(Node child) {
        if (child != null) {
            getChildren().add(child);
        }
        return this;
    }

    /**
     * 批量向面板中追加多个普通子节点
     *
     * @param children 子节点可变参数数组
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane addAll(Node... children) {
        if (children != null) {
            getChildren().addAll(children);
        }
        return this;
    }

    /**
     * 【流式高频】向面板添加子节点，并为其精确指定四个方向的锚点边界值
     * 如果某个方向不需要锚定，传入 null 即可
     *
     * @param child  子节点
     * @param top    上方锚点距离（像素），可为 null
     * @param right  右侧锚点距离（像素），可为 null
     * @param bottom 下方锚点距离（像素），可为 null
     * @param left   左侧锚点距离（像素），可为 null
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane add(Node child, Double top, Double right, Double bottom, Double left) {
        anchor(child, top, right, bottom, left);
        return this;
    }

    /**
     * 为已经存在于面板中的指定子节点设置或更新锚点
     *
     * @param child  子节点
     * @param top    上方锚点距离（像素），可为 null
     * @param right  右侧锚点距离（像素），可为 null
     * @param bottom 下方锚点距离（像素），可为 null
     * @param left   左侧锚点距离（像素），可为 null
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane anchor(Node child, Double top, Double right, Double bottom, Double left) {
        if (child != null) {
            if (!getChildren().contains(child)) {
                getChildren().add(child);
            }
            if (top != null) AnchorPane.setTopAnchor(child, top);
            if (right != null) AnchorPane.setRightAnchor(child, right);
            if (bottom != null) AnchorPane.setBottomAnchor(child, bottom);
            if (left != null) AnchorPane.setLeftAnchor(child, left);
        }
        return this;
    }

    /**
     * 快捷方法：将指定的多个子节点，全部以 0.0 像素的间距铺满整个 AnchorPane 容器
     * 常用于背景层叠、通栏遮罩层或全屏滑动面板
     *
     * @param children 需要填满父容器的子节点
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane anchorFull(Node... children) {
        if (children != null) {
            for (Node child : children) {
                if (!getChildren().contains(child)) {
                    getChildren().add(child);
                }
                AnchorPane.setTopAnchor(child, 0.0);
                AnchorPane.setRightAnchor(child, 0.0);
                AnchorPane.setBottomAnchor(child, 0.0);
                AnchorPane.setLeftAnchor(child, 0.0);
            }
        }
        return this;
    }

    /**
     * 快捷方法：将子节点钉在左上角
     */
    public FXAnchorPane anchorTopLeft(Node child, double top, double left) {
        return anchor(child, top, null, null, left);
    }

    /**
     * 快捷方法：将子节点钉在右上角（常用于卡片右上角的关闭或复制按钮）
     */
    public FXAnchorPane anchorTopRight(Node child, double top, double right) {
        return anchor(child, top, right, null, null);
    }

    /**
     * 快捷方法：将子节点钉在左下角
     */
    public FXAnchorPane anchorBottomLeft(Node child, double bottom, double left) {
        return anchor(child, null, null, bottom, left);
    }

    /**
     * 快捷方法：将子节点钉在右下角
     */
    public FXAnchorPane anchorBottomRight(Node child, double bottom, double right) {
        return anchor(child, null, right, bottom, null);
    }

    /**
     * 清除指定子节点身上的所有锚点约束，使其回归原生的流式排版状态
     *
     * @param child 子节点
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane clearAnchors(Node child) {
        if (child != null) {
            AnchorPane.clearConstraints(child);
        }
        return this;
    }

    // ==================== 基础布局属性流式扩展 ====================

    /**
     * 设置容器宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 快捷锁定首选宽高
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }


    /**
     * 设置面板的内边距 (Padding)
     *
     * @param padding Insets 内边距对象
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane padding(Insets padding) {
        setPadding(padding);
        return this;
    }

    /**
     * 快捷设置面板四周统一的内边距值
     *
     * @param value 边距像素值
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane padding(double value) {
        setPadding(new Insets(value));
        return this;
    }

    /**
     * 局部精细设置面板四个方向的内边距值
     */
    public FXAnchorPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置当置于 HBox 容器中时的横向延伸优先级
     */
    public FXAnchorPane hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置当置于 VBox 容器中时的纵向延伸优先级
     */
    public FXAnchorPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== AtlantaFX & 现代卡片样式定制扩展 ====================

    /**
     * 快捷将面板升级为“轻量交互式卡片外壳样式”
     * 融合 AtlantaFX 的 Subtle 配色边界，自带 1 像素的微弱优雅边框线
     *
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane bgCardStyle() {
        return styleCss(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-border-muted;" +
                        "-fx-border-width: 1;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;"
        );
    }

    /**
     * 快捷设置自定义背景色（支持应用 AtlantaFX 主题变量如 "-color-bg-subtle"）
     *
     * @param color 颜色字符串或主题变量名
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane background(String color) {
        if (color == null || color.isBlank()) return this;

        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 快捷设置自定义边框颜色和粗细
     *
     * @param color 颜色字符串
     * @param width 边框线粗细（像素）
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx;",
                width, color, radius
        );
        return styleCss(style);
    }

    /**
     * 为当前面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATION_SMALL），2级适中（Styles.ELEVATION_MEDIUM），3级最深（Styles.ELEVATION_LARGE）
     * @return FXAnchorPane 实例（链式调用）
     */
    public FXAnchorPane shadow(int level) {
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
}