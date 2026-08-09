package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * FXToolBar - 工具栏容器组件
 * 继承自 JavaFX ToolBar，实现 IFXNode 接口支持链式调用
 * 提供便捷的工具按钮管理、分隔线和样式设置方法
 * <p>
 */
public class FXToolBar extends ToolBar implements IFXNode<FXToolBar> {

    /**
     * 默认构造函数
     */
    private FXToolBar() {
        super();
        initializeDefaultStyle();
    }

    /**
     * 创建带子节点的工具栏
     *
     * @param children 子节点数组
     */
    private FXToolBar(Node... children) {
        super(children);
        initializeDefaultStyle();
    }

    /**
     * 创建空白工具栏实例
     *
     * @return FXToolBar 实例
     */
    public static FXToolBar create() {
        return new FXToolBar();
    }

    /**
     * 创建带子节点的工具栏实例
     *
     * @param children 子节点数组
     * @return FXToolBar 实例
     */
    public static FXToolBar create(Node... children) {
        return new FXToolBar(children);
    }

    /**
     * 初始化工具栏默认的轻量样式底衬
     */
    private void initializeDefaultStyle() {
        // 赋予标准内边距，防范内部子控件直接物理碰撞工具栏边缘
        setPadding(new Insets(6, 10, 6, 10));
    }

    /**
     * 添加一个或多个子节点到工具栏
     *
     * @param nodes 要添加的节点数组
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar add(Node... nodes) {
        getItems().addAll(nodes);
        return this;
    }

    /**
     * 在指定索引位置添加子节点
     *
     * @param index 插入位置的索引（从 0 开始）
     * @param node  要添加的节点数组
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar addAt(int index, Node node) {
        getItems().add(index, node);
        return this;
    }

    /**
     * 移除指定的子节点
     *
     * @param nodes 要移除的节点数组
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar remove(Node... nodes) {
        getItems().removeAll(nodes);
        return this;
    }

    /**
     * 清空所有子节点
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar clear() {
        getItems().clear();
        return this;
    }

    /**
     * 向工具栏中注入局域物理分隔线（自动识别当前工具栏的横纵朝向）
     *
     * @return FXToolBar 实例（链式调用契约）
     */
    public FXToolBar separator() {
        Separator separator = new Separator();
        // 核心：如果当前是横向工具栏，则分隔线垂直立起；反之水平横卧
        separator.setOrientation(getOrientation() == Orientation.HORIZONTAL ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        getItems().add(separator);
        return this;
    }

    /**
     * 添加弹性空间（占位符）
     * 用于推开其他工具按钮
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar addSpacer() {
        FXRegion spacer = FXRegion.create();
        spacer.setMaxWidth(Double.MAX_VALUE);
        spacer.setMaxHeight(Double.MAX_VALUE);
        if (getOrientation() == Orientation.HORIZONTAL) {
            HBox.setHgrow(spacer, Priority.ALWAYS);
        } else {
            VBox.setVgrow(spacer, Priority.ALWAYS);
        }
        getItems().add(spacer);
        return this;
    }

    /**
     * 设置工具栏方向为水平
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar horizontal() {
        setOrientation(Orientation.HORIZONTAL);
        return this;
    }

    /**
     * 设置工具栏方向为垂直
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar vertical() {
        setOrientation(Orientation.VERTICAL);
        return this;
    }

    /**
     * 设置工具栏四边的内边距
     *
     * @param v 内边距值（像素）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置工具栏各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 统一设置工具栏尺寸
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置工具栏宽度
     *
     * @param w 宽度值（像素）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置工具栏高度
     *
     * @param h 高度值（像素）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色工具栏
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色工具栏
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色工具栏
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 应用 Flat 样式（Flat）
     * 灰色工具栏
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar flat() {
        return stylesClass(Styles.FLAT);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置在 HBox 中的水平增长优先级
     *
     * @return
     */
    public FXToolBar hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置工具栏可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置工具栏是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置工具栏透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置工具栏是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置工具栏背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar background(String color) {
        setBackground(new Background(
                new BackgroundFill(
                        Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));
        return this;
    }

    /**
     * 设置工具栏边框
     *
     * @param width  边框宽度（像素）
     * @param color  CSS 格式的颜色字符串
     * @param radius 圆角半径（像素）
     * @return FXToolBar 实例（链式调用）
     */
    public FXToolBar border(double width, String color, double radius) {
        setBorder(new Border(
                new BorderStroke(
                        Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(radius),
                        new BorderWidths(width)
                )
        ));
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为主窗口顶部工具栏
     * 水平布局，紧凑间距
     *
     * @return FXToolBar 实例（链式调用）
     */
    public double asMainToolbar() {
        return horizontal()
                .align(Pos.CENTER_LEFT)
                .padding(8)
                .background("#f5f5f5")
                .prefHeight(50);
    }

    /**
     * 设置为侧边工具栏
     * 垂直布局，适合放置工具按钮
     *
     * @return FXToolBar 实例（链式调用）
     */
    public double asSidebar() {
        return vertical()
                .align(Pos.TOP_CENTER)
                .padding(8)
                .background("#fafafa")
                .prefWidth(60)
                ;
    }

    private FXToolBar align(Pos pos) {
        return this;
    }

    /**
     * 设置为底部状态栏工具栏
     * 水平布局，显示状态信息
     *
     * @return FXToolBar 实例（链式调用）
     */
    public double asStatusbar() {
        return horizontal()
                .align(Pos.CENTER_LEFT)
                .padding(5, 10, 5, 10)
                .background("#e8e8e8")
                .prefHeight(30)
                ;
    }

    /**
     * 设置为编辑器工具栏
     * 水平布局，包含多个按钮组
     *
     * @return FXToolBar 实例（链式调用）
     */
    public double asEditor() {
        return horizontal()
                .align(Pos.CENTER_LEFT)
                .padding(6)
                .background("#ffffff")
                .border(1, "#dddddd", 0)
                .prefHeight(45)
                ;
    }

    /**
     * 设置为导航工具栏
     * 水平布局，居中显示
     *
     * @return FXToolBar 实例（链式调用）
     */
    public double asNavigation() {
        return horizontal()
                .align(Pos.CENTER)
                .padding(10)
                .background("#ffffff")
                .border(0, "#e0e0e0", 4)
                .prefHeight(60)
                ;
    }

    /**
     * 优雅地为工具栏头部追加带视觉隔离线的核心标题
     *
     * @param text 标题文本描述
     * @return FXToolBar 实例（链式调用契约）
     */
    public FXToolBar withTitle(String text) {
        if (text == null || text.isEmpty()) return this;

        FXLabel label = FXLabel.create(text).fontSize(13.0).bold();
        getItems().addFirst(label);

        Separator headSeparator = new Separator();
        headSeparator.setOrientation(getOrientation() == Orientation.HORIZONTAL ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        getItems().add(1, headSeparator);

        return this;
    }

    /**
     * 获取工具栏中的所有按钮
     *
     * @return Node 数组
     */
    public Node[] getItemsArray() {
        return getItems().toArray(new Node[0]);
    }

    /**
     * 检查工具栏是否为空
     *
     * @return true-为空，false-不为空
     */
    public boolean isEmpty() {
        return getItems().isEmpty();
    }
}
