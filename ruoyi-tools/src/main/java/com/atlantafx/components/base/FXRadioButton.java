package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

/**
 * FXRadioButton - 单选按钮组件
 * 继承自 JavaFX RadioButton，实现 IFXNode 接口支持链式调用
 * 通常与 ToggleGroup 配合使用，实现互斥选择
 */
public class FXRadioButton extends RadioButton implements IFXNode<FXRadioButton> {

    /**
     * 默认构造函数
     */
    private FXRadioButton() {
        super();
    }

    /**
     * 创建带文本的单选按钮
     *
     * @param text 单选按钮显示文本
     */
    private FXRadioButton(String text) {
        super(text);
    }

    /**
     * 创建空白单选按钮实例
     *
     * @return FXRadioButton 实例
     */
    public static FXRadioButton create() {
        return new FXRadioButton();
    }

    /**
     * 创建带文本的单选按钮实例
     *
     * @param text 单选按钮显示文本
     * @return FXRadioButton 实例
     */
    public static FXRadioButton create(String text) {
        return new FXRadioButton(text);
    }

    /**
     * 设置单选按钮选中状态
     *
     * @param selected true-选中，false-未选中
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton selected(boolean selected) {
        setSelected(selected);
        return this;
    }

    /**
     * 双向绑定选中属性，常用于与表现层 ViewModel 状态联动
     *
     * @param property 可观察的布尔属性
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton selectedProperty(Property<Boolean> property) {
        selectedProperty().bindBidirectional(property);
        return this;
    }

    /**
     * 设置单选按钮文本
     *
     * @param text 显示文本
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton text(String text) {
        setText(text);
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色单选按钮
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色单选按钮
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色单选按钮
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 快捷将单选框右侧的标签文本设置为粗体
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton bold() {
        return stylesClass(Styles.TEXT_BOLD);
    }

    /**
     * 设置自定义文本及内部单选原点圆框的配色方案。
     * 完美破解 AtlantaFX 内部针对 RadioButton 骨骼节点权重极高的内置覆盖问题。
     *
     * @param colorString CSS 标准颜色串或主题色（如 "#1E88E5"、"pink"、"-color-accent-fg"）
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton fontColor(String colorString) {
        if (colorString == null || colorString.trim().isEmpty()) {
            return this;
        }
        // 深度重置文本填充色及单选组件各伪类下的核心背景与边框变量，防止出现颜色剥离断层
        return styleCss(
                "-fx-text-fill: " + colorString + ";" +
                        "-color-radio-bg-selected: " + colorString + ";" +
                        "-color-radio-border-selected: " + colorString + ";" +
                        "-color-radio-bg-selected-focused: " + colorString + ";"
        );
    }

    /**
     * 通过 Paint 载体快捷配置标签文本颜色（仅作用于文本本身）
     *
     * @param color Paint 颜色对象（如 Color.RED 等）
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton fontColor(Paint color) {
        setTextFill(color);
        return this;
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置组件在水平箱子布局 (HBox) 中的水平生长优先级为 ALWAYS
     *
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置单选按钮宽度
     *
     * @param w 宽度值（像素）
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置单选按钮高度
     *
     * @param h 高度值（像素）
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 一键配置组件固定几何尺寸
     *
     * @param w 宽度（像素值）
     * @param h 高度（像素值）
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置单选按钮可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置单选按钮是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置单选按钮透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置单选按钮是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 事件监听 ====================

    /**
     * 设置选中状态变更监听器
     *
     * @param listener 状态变更时的回调函数，接收新的选中状态
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton onAction(java.util.function.Consumer<Boolean> listener) {
        selectedProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        return this;
    }

    /**
     * 设置点击事件处理器
     *
     * @param handler 点击事件处理器
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton onClick(javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, handler);
        return this;
    }

    /**
     * 设置工具提示
     *
     * @param text 提示文本
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    /**
     * 绑定到切换组
     *
     * @param group ToggleGroup 实例
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton toggleGroup(ToggleGroup group) {
        setToggleGroup(group);
        return this;
    }

    /**
     * 响应式监听选中状态变更（函数式快捷包裹类）。
     * 当该组件在互斥切换组中由于其他同伴被选中而被动变更为 false 时，此监听器同样会准确捕获。
     *
     * @param listener 函数式状态回调闭包，传入最新更替后的布尔状态值 (isSelected)
     * @return FXRadioButton 实例（链式调用）
     */
    public FXRadioButton onSelectedChanged(java.util.function.Consumer<Boolean> listener) {
        selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (listener != null) {
                listener.accept(newVal);
            }
        });
        return this;
    }
}
