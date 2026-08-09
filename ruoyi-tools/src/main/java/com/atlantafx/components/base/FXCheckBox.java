package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * FXCheckBox - 复选框组件
 * 继承自 JavaFX CheckBox，实现 IFXNode 接口支持链式调用
 * 提供便捷的选中状态控制、样式设置和事件监听方法
 */
public class FXCheckBox extends CheckBox implements IFXNode<FXCheckBox> {

    /**
     * 默认构造函数
     */
    private FXCheckBox() {
        super();
    }

    /**
     * 创建带文本的复选框
     *
     * @param text 复选框显示文本
     */
    private FXCheckBox(String text) {
        super(text);
    }

    /**
     * 创建空白复选框实例
     *
     * @return FXCheckBox 实例
     */
    public static FXCheckBox create() {
        return new FXCheckBox();
    }

    /**
     * 创建带文本的复选框实例
     *
     * @param text 复选框显示文本
     * @return FXCheckBox 实例
     */
    public static FXCheckBox create(String text) {
        return new FXCheckBox(text);
    }

    /**
     * 设置复选框选中状态
     *
     * @param selected true-选中，false-未选中
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox selected(boolean selected) {
        setSelected(selected);
        return this;
    }

    /**
     * 设置复选框文本
     *
     * @param text 显示文本
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox text(String text) {
        setText(text);
        return this;
    }

    /**
     * 设置复选框的图形标记（Icon/Node），支持富文本或图片组合
     *
     * @param graphic 节点对象
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox graphic(Node graphic) {
        setGraphic(graphic);
        return this;
    }

    /**
     * 启用或关闭不确定状态（三态复选框模式：Checked, Unchecked, Indeterminate）
     * 允许复选框开启 allowsIndeterminate 属性并使其进入灰色横线混态，常用于多级树形菜单全选/半选联动
     *
     * @param indeterminate true-激活半选混态，false-恢复两态切换
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox indeterminate(boolean indeterminate) {
        setAllowIndeterminate(true);
        setIndeterminate(indeterminate);
        return this;
    }

    /**
     * 建立与外部布尔属性对象的双向响应式数据流绑定
     *
     * @param property 外部源模型布尔属性槽
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox bindBidirectional(Property<Boolean> property) {
        if (property != null) {
            selectedProperty().bindBidirectional(property);
        }
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 一键剥离复选框的默认背景骨架，转换为更纯粹简约的无框内嵌皮肤风格
     */
    public FXCheckBox flat() {
        return stylesClass(Styles.FLAT);
    }

    /**
     * 转换当前复选框为大号尺寸规格（Large Size）
     */
    public FXCheckBox large() {
        return stylesClass(Styles.LARGE);
    }

    /**
     * 转换当前复选框为小号紧凑型尺寸规格（Small Size）
     */
    public FXCheckBox small() {
        return stylesClass(Styles.SMALL);
    }

    /**
     * 应用成功样式（Success）
     * 绿色复选框，表示确认、同意
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色复选框，表示警告、重要选项
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色复选框
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox warning() {
        return stylesClass(Styles.WARNING);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 当置于 HBox 容器骨架内时，声明横向拉伸延伸优先级
     */
    public FXCheckBox hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置复选框宽度
     *
     * @param w 宽度值（像素）
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置复选框高度
     *
     * @param h 高度值（像素）
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置组件内部各个构成元素之间的填充内边距
     */
    public FXCheckBox padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 快捷设置统一数值的四周内边距
     */
    public FXCheckBox padding(double value) {
        setPadding(new Insets(value));
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置复选框可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置复选框是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置复选框透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置复选框是否禁用
     * 禁用状态下无法交互
     *
     * @param disabled true-禁用，false-启用
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 事件监听 ====================

    /**
     * 快捷注册状态变更动作监听器，专注于捕获用户每次勾选/反选后的新布尔数值
     *
     * @param listener 消费事件的回调函数（接收最新的选中状态）
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox onAction(Consumer<Boolean> listener) {
        if (listener != null) {
            selectedProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        }
        return this;
    }

    /**
     * 注册底层的 ActionEvent 处理器
     *
     * @param handler JavaFX 标准事件处理器
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox onActionHandler(EventHandler<ActionEvent> handler) {
        setOnAction(handler);
        return this;
    }

    /**
     * 设置点击事件处理器
     *
     * @param handler 点击事件处理器
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox onClick(EventHandler<MouseEvent> handler) {
        addEventHandler(MouseEvent.MOUSE_CLICKED, handler);
        return this;
    }

    /**
     * 设置工具提示
     *
     * @param text 提示文本
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 场景预设：必选项声明
     * 深度优化原代码：不再简单采用暴力字符串破坏性追加，而是通过内部创建红色的富文本指示星号标记注入到 Graphic 管道中，
     * 确保后续动态执行 getText() 的数据结构绝对纯净安全，完美契合企业重型表单校验。
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox required() {
        Text star = FXText.create(" *").styleCss("-fx-fill: -color-danger-emphasis; -fx-font-weight: bold;");

        Label labelNode = FXLabel.create(getText());
        HBox box = FXHBox.create(0).add(labelNode, star).align(Pos.CENTER_LEFT);

        setText(""); // 清空硬编码基础文本轨道
        return graphic(box); // 将带有语义红星号的 HBox 面板整体接管为 graphic 宿主
    }

    /**
     * 快捷将当前复选框降级为静音不打扰灰色说明状态，常用于已被弃用的表单子项展示
     */
    public FXCheckBox muted() {
        return stylesClass(Styles.TEXT_MUTED);
    }

    /**
     * 设置为全选复选框样式
     * 通常用于列表头部的全选控制
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox selectAll() {
        setText("全选");
        return stylesClass(Styles.TEXT_BOLD);
    }

    /**
     * 设置为同意协议复选框
     * 显示"我已阅读并同意"文本
     *
     * @return FXCheckBox 实例（链式调用）
     */
    public FXCheckBox agreeTerms() {
        setText("我已阅读并同意相关条款和协议");
        return this;
    }
}
