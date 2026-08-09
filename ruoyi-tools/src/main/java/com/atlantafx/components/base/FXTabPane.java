package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * FXTabPane - 标签页面板组件
 * 继承自 JavaFX TabPane，实现 IFXNode 接口支持链式调用
 * 提供便捷的标签页管理、样式控制、排版方位和布局优化方法
 */
public class FXTabPane extends TabPane implements IFXNode<FXTabPane> {

    /**
     * 默认构造函数
     */
    private FXTabPane() {
        super();
    }

    /**
     * 创建标签页面板实例
     *
     * @return FXTabPane 实例
     */
    public static FXTabPane create() {
        return new FXTabPane();
    }

    /**
     * 向面板中快捷追加一个现成的 Tab 选项卡
     *
     * @param tab 实现了或继承自 JavaFX Tab 的对象
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane add(Tab tab) {
        getTabs().add(tab);
        return this;
    }

    /**
     * 向面板中快捷追加多个现成的 Tab 选项卡
     *
     * @param tabs Tab 数组
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane addAll(Tab... tabs) {
        getTabs().addAll(tabs);
        return this;
    }

    /**
     * 创建并直接追加一个带文本和内容的全新标签页
     *
     * @param title   标签页显示的标题
     * @param content 标签页的主内容区域节点
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane add(String title, Node content) {
        getTabs().add(FXTab.create(title, content));
        return this;
    }

    /**
     * 快捷定位并选中指定的标签页索引
     *
     * @param index 选项卡下标，从 0 开始
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane select(int index) {
        if (index >= 0 && index < getTabs().size()) {
            getSelectionModel().select(index);
        }
        return this;
    }

    /**
     * 快捷选中指定的 Tab 实例
     *
     * @param tab 要激活选中的 Tab 对象
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane select(Tab tab) {
        getSelectionModel().select(tab);
        return this;
    }

    /**
     * 设置全局的标签页关闭策略
     *
     * @param policy TabClosingPolicy 枚举 (ALL_TABS, SELECTED_TAB, UNAVAILABLE)
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane closingPolicy(TabPane.TabClosingPolicy policy) {
        setTabClosingPolicy(policy);
        return this;
    }

    /**
     * 快捷将标签页的关闭按钮完全禁用（用户无法关闭任何 Tab）
     *
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane disableClosing() {
        setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return this;
    }

    /**
     * 设置标签页 Header 的展现方向（上、下、左、右）
     *
     * @param side Side 枚举 (TOP, BOTTOM, LEFT, RIGHT)
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane side(Side side) {
        setSide(side);
        return this;
    }

    /**
     * 设置标签页面板的整体禁用状态
     *
     * @param disabled true-完全禁用，false-正常交互
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置标签页 Header 的拖拽重排行为策略
     *
     * @param reorderable true-允许拖拽调换标签页顺序，false-固定不可动
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane reorderable(boolean reorderable) {
        setTabDragPolicy(reorderable ? TabDragPolicy.REORDER : TabDragPolicy.FIXED);
        return this;
    }

    /**
     * 设置每个标签页的最小宽度
     *
     * @param width 像素宽度
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane tabMinWidth(double width) {
        setTabMinWidth(width);
        return this;
    }

    /**
     * 设置每个标签页的最大宽度
     *
     * @param width 像素宽度
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane tabMaxWidth(double width) {
        setTabMaxWidth(width);
        return this;
    }

    /**
     * 设置每个标签页的固定精确高度
     *
     * @param height 像素高度
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane tabHeight(double height) {
        setTabMinHeight(height);
        setTabMaxHeight(height);
        return this;
    }

    // ==================== AtlantaFX 特色样式快捷封装 ====================

    /**
     * 扁平化风格样式 (Styles.FLAT)
     * 针对轻量切换卡（如 Preview / Code、数据卡片切换）进行了骨架优化，隐藏沉重的包裹背景底色
     *
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane flat() {
        return stylesClass(Styles.FLAT);
    }

    /**
     * 经典边框卡片夹风格 (Styles.TABS_CLASSIC)
     *
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane classic() {
        return stylesClass(Styles.TABS_CLASSIC);
    }

    /**
     * 无边框卡片风格 (Styles.TABS_BORDER_TOP)
     *
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane borderless() {
        return stylesClass(Styles.TABS_BORDER_TOP);
    }

    /**
     * 浮动图标卡片变体风格 (Styles.TABS_FLOATING)
     *
     * @return FXTabPane 实例（链式调用）
     */
    public FXTabPane floating() {
        return stylesClass(Styles.TABS_FLOATING);
    }
}