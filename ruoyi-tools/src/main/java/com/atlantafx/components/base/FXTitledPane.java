package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * FXTitledPane - 可折叠标题面板组件
 * 继承自 JavaFX TitledPane，实现 IFXNode 接口支持链式调用
 * 提供便捷的折叠/展开、样式设置和事件监听方法
 */
public class FXTitledPane extends TitledPane implements IFXNode<FXTitledPane> {

    /**
     * 默认构造函数
     */
    public FXTitledPane() {
        super();
    }

    /**
     * 创建带标题和内容的面板
     *
     * @param title   标题文本
     * @param content 内容节点
     */
    public FXTitledPane(String title, Node content) {
        super(title, content);
    }

    /**
     * 创建空白标题面板实例
     *
     * @return FXTitledPane 实例
     */
    public static FXTitledPane create() {
        return new FXTitledPane();
    }

    /**
     * 创建带标题和内容的面板实例
     *
     * @param title   标题文本
     * @param content 内容节点
     * @return FXTitledPane 实例
     */
    public static FXTitledPane create(String title, Node content) {
        return new FXTitledPane(title, content);
    }

    /**
     * 设置标题文本
     *
     * @param text 标题文本
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane title(String text) {
        setText(text);
        return this;
    }

    /**
     * 获取当前标题文本
     *
     * @return 标题文本
     */
    public String getTitle() {
        return super.getText();
    }

    /**
     * 设置内容节点
     *
     * @param node 内容节点
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane content(Node node) {
        setContent(node);
        return this;
    }

    /**
     * 设置面板展开状态
     *
     * @param expanded true-展开，false-折叠
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane expanded(boolean expanded) {
        setExpanded(expanded);
        return this;
    }

    /**
     * 设置面板是否可折叠
     *
     * @param collapsible true-可折叠，false-不可折叠
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane collapsible(boolean collapsible) {
        setCollapsible(collapsible);
        return this;
    }

    /**
     * 设置标题面板动画效果
     * 展开/折叠时是否显示动画
     *
     * @param animated true-显示动画，false-不显示
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane animated(boolean animated) {
        setAnimated(animated);
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色标题栏
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色标题栏
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色标题栏
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 应用信息样式（Info）
     * 蓝色标题栏
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane info() {
        return stylesClass(Styles.ACCENT);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置标题面板内边距
     *
     * @param value
     * @return
     */
    public FXTitledPane padding(double value) {
        setPadding(new Insets(value));
        return this;
    }

    /**
     * 设置标题面板内边距
     *
     * @param top
     * @param right
     * @param bottom
     * @param left
     * @return
     */
    public FXTitledPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置标题面板尺寸
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置标题面板宽度
     *
     * @param w 宽度值（像素）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置标题面板高度
     *
     * @param h 高度值（像素）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置内容区域内边距
     *
     * @param v 内边距值（像素）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane contentPadding(double v) {
        if (getContent() instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) getContent())
                    .setPadding(new Insets(v));
        }
        return this;
    }

    /**
     * 设置内容区域各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane contentPadding(double top, double right, double bottom, double left) {
        if (getContent() instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) getContent())
                    .setPadding(new Insets(top, right, bottom, left));
        }
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置标题面板可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置标题面板是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置标题面板透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置标题面板是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    public FXTitledPane id(String id) {
        setId(id);
        return this;
    }

    // ==================== 事件监听 ====================

    /**
     * 设置展开状态变更监听器
     *
     * @param listener 状态变更时的回调函数，接收新的展开状态
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane onExpandedChange(Consumer<Boolean> listener) {
        expandedProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        return this;
    }

    /**
     * 设置工具提示
     *
     * @param text 提示文本
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为设置项面板
     * 适合用于设置页面的分组
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane asSettings() {
        return collapsible(true)
                .expanded(false)
                .animated(true)
                .contentPadding(15);
    }

    /**
     * 设置为 FAQ 项面板
     * 适合用于常见问题解答
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane asFAQ() {
        return collapsible(true)
                .expanded(false)
                .animated(true)
                .accent();
    }

    /**
     * 设置为始终展开的面板
     * 不可折叠，始终显示内容
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane alwaysExpanded() {
        return collapsible(false)
                .expanded(true);
    }

    /**
     * 设置为默认展开的面板
     * 初始状态为展开
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane defaultExpanded() {
        return expanded(true);
    }

    /**
     * 设置为默认折叠的面板
     * 初始状态为折叠
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane defaultCollapsed() {
        return expanded(false);
    }

    /**
     * 切换展开/折叠状态
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane toggle() {
        setExpanded(!isExpanded());
        return this;
    }

    /**
     * 展开面板
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane expand() {
        return expanded(true);
    }

    /**
     * 折叠面板
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane collapse() {
        return expanded(false);
    }

    /**
     * 利用 CSS 延迟检索机制精准定义标题栏文本及箭头的物理样式
     *
     * @param style 纯行内 CSS 样式字符串
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane headerStyle(String style) {
        if (style != null && !style.isBlank()) {
            Node titleRegion = lookup(".title");
            if (titleRegion != null) {
                titleRegion.setStyle(style);
            } else {
                // 若初始化时还未挂载渲染树，采用行内补丁追加
                styleCss(style);
            }
        }
        return this;
    }

    /**
     * 为当前面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATION_SMALL），2级适中（Styles.ELEVATION_MEDIUM），3级最深（Styles.ELEVATION_LARGE）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane shadow(int level) {
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

    /**
     * 设置内容区域的背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane contentBackground(String color) {
        if (getContent() instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) getContent())
                    .setStyle("-fx-background-color: " + color + ";");
        }
        return this;
    }

    /**
     * 安全设置可折叠面板头部/整体的背景色
     * 强化了安全阻断过滤机制，严禁引发 -color-bg-default 的 CSS 解析自指死循环警告
     *
     * @param color CSS 颜色字符串（如 "#FFFFFF" 或主题变量 "-color-bg-default"）
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 设置为卡片样式
     * 带边框和圆角
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane asCard() {
        return stylesClass("card")
                .contentPadding(16)
                .animated(true);
    }

    /**
     * 设置为紧凑样式
     * 更小的间距和字体
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane compact() {
        return stylesClass(Styles.DENSE)
                .contentPadding(8);
    }

    /**
     * 快捷转化为极简纯文本边框折叠块（通常用于侧边控制栏属性面板，剥离大块背景噪音）
     *
     * @return FXTitledPane 实例（链式调用）
     */
    public FXTitledPane asMinimalSection() {
        setAnimated(true);
        setCollapsible(true);
        stylesClass(Styles.DENSE); // 紧凑型排版类
        return contentBackground("transparent");
    }
}
