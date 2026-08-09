package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * FXSpinner - 数字微调器组件
 * 继承自 JavaFX Spinner，实现 IFXNode 接口支持链式调用
 * 提供便捷的数值输入、步进控制和样式设置方法
 */
public class FXSpinner extends Spinner<Integer> implements IFXNode<FXSpinner> {

    /**
     * 默认构造函数
     */
    private FXSpinner() {
        super(0, 100, 0, 1);
        initDefaultConfiguration();
    }

    /**
     * 创建数字微调器（指定范围和初始值）
     *
     * @param min          最小值
     * @param max          最大值
     * @param initialValue 初始值
     */
    private FXSpinner(int min, int max, int initialValue) {
        super(min, max, initialValue, 1);
        initDefaultConfiguration();
    }

    /**
     * 创建数字微调器（指定范围、初始值和步长）
     *
     * @param min          最小值
     * @param max          最大值
     * @param initialValue 初始值
     * @param stepAmount   步长
     */
    private FXSpinner(int min, int max, int initialValue, int stepAmount) {
        super(min, max, initialValue, stepAmount);
        initDefaultConfiguration();
    }

    /**
     * 初始化默认配置，确保值工厂加载成功
     */
    private void initDefaultConfiguration() {
        // 兜底保护：确保 ValueFactory 永远不为 null 且为 Integer 类型
        if (getValueFactory() == null) {
            setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0, 1));
        }
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建数字微调器实例（默认范围 0-100，初始值 0）
     *
     * @return FXSpinner 实例
     */
    public static FXSpinner create() {
        return new FXSpinner();
    }

    /**
     * 创建数字微调器实例
     *
     * @param min          最小值
     * @param max          最大值
     * @param initialValue 初始值
     * @return FXSpinner 实例
     */
    public static FXSpinner create(int min, int max, int initialValue) {
        return new FXSpinner(min, max, initialValue);
    }

    /**
     * 创建数字微调器实例
     *
     * @param min          最小值
     * @param max          最大值
     * @param initialValue 初始值
     * @param stepAmount   步长
     * @return FXSpinner 实例
     */
    public static FXSpinner create(int min, int max, int initialValue, int stepAmount) {
        return new FXSpinner(min, max, initialValue, stepAmount);
    }

    // ==================== 核心数值与值工厂扩展 (支持链式) ====================

    /**
     * 设置当前值
     *
     * @param value 当前值
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner value(int value) {
        getValueFactory().setValue(value);
        return this;
    }

    /**
     * 安全设置最小值（保持当前最大值与步长，自动修正当前越界值）
     *
     * @param min 最小值
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner min(int min) {
        int currentMax = getMax();
        int currentStep = getStepAmount();
        int currentVal = getValue() != null ? getValue() : min;
        setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                min, Math.max(min, currentMax), Math.max(min, currentVal), currentStep));
        return this;
    }

    /**
     * 安全设置最大值（保持当前最小值与步长，自动修正当前越界值）
     *
     * @param max 最大值
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner max(int max) {
        int currentMin = getMin();
        int currentStep = getStepAmount();
        int currentVal = getValue() != null ? getValue() : currentMin;
        setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                currentMin, max, Math.min(max, currentVal), currentStep));
        return this;
    }

    /**
     * 设置步长
     *
     * @param step 步长
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner step(int step) {
        setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                getMin(), getMax(), getValue() != null ? getValue() : getMin(), step));
        return this;
    }

    /**
     * 获取当前绑定的 Integer 值属性，便于外部进行双向绑定
     *
     * @return 值属性
     */
    public Property<Integer> valuePropertyInstance() {
        return getValueFactory().valueProperty();
    }

    // ==================== 属性安全获取器 (Getter) ====================

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public int getMin() {
        if (getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory factory) {
            return factory.getMin();
        }
        return 0;
    }

    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public int getMax() {
        if (getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory factory) {
            return factory.getMax();
        }
        return 100;
    }

    /**
     * 获取步长
     *
     * @return 步长
     */
    public int getStepAmount() {
        if (getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory factory) {
            return factory.getAmountToStepBy();
        }
        return 1;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 将步进按钮放置在组件两侧（左减右加），极度适合触控或移动端大面积操作布局
     */
    public FXSpinner split() {
        return stylesClass(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
    }

    public FXSpinner splitLeft() {
        return stylesClass(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL);
    }

    public FXSpinner splitRight() {
        return stylesClass(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
    }

    /**
     * 将步进按钮垂直分列在组件两侧（下减上加）
     */
    public FXSpinner splitVertical() {
        return stylesClass(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
    }

    public FXSpinner splitVerticalLeft() {
        return stylesClass(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL);
    }
    // ==================== 布局增强 ====================

    public FXSpinner size(double w, double h) {
        setMinWidth(w);
        setPrefWidth(w);
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    public FXSpinner vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    public FXSpinner hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置微调器宽度
     *
     * @param w 宽度值（像素）
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置微调器高度
     *
     * @param h 高度值（像素）
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置微调器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置微调器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置微调器透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置微调器是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置微调器是否可编辑
     * 可编辑状态下可以直接输入数字
     *
     * @param editable true-可编辑，false-只能点击按钮
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    /**
     * 设置是否允许数值循环（越界包裹循环）
     * 激活时，当数值超过最大值后会回归至最小值，反之亦然
     *
     * @param cyclic true-允许循环，false-边界死锁
     */
    public FXSpinner cyclic(boolean cyclic) {
        if (getValueFactory() instanceof SpinnerValueFactory.IntegerSpinnerValueFactory factory) {
            factory.setWrapAround(cyclic);
        }
        return this;
    }

    // ==================== 事件监听 ====================

    /**
     * 注册值变更监听器（Consumer 函数式回调）
     *
     * @param listener 接收新数值的回调闭包
     */
    public FXSpinner onValueChange(Consumer<Integer> listener) {
        valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                listener.accept(newVal);
            }
        });
        return this;
    }

    /**
     * 设置工具提示
     *
     * @param text 提示文本
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner tooltip(String text) {
        setTooltip(FXTooltip.create(text));
        return this;
    }

    /**
     * 为内部输入框设置提示占位文本 (Prompt Text)
     * 解决原生 JavaFX 中 Spinner 无法直接设置 Prompt Text 的设计痛点
     */
    public FXSpinner prompt(String text) {
        getEditor().setPromptText(text);
        return this;
    }

    // ==================== 高级样式定制 (破解主题权重) ====================

    /**
     * 定制微调器内部输入文字及图标变量颜色
     *
     * @param color CSS 颜色值或变量
     */
    public FXSpinner fontColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return this;
        }
        return styleCss("-fx-text-inner-color: " + color + ";");
    }

    // ==================== 语义化预设快捷方法 ====================

    /**
     * 设置为数量选择器
     * 范围 0-999，步长 1
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asQuantity() {
        return min(0).max(999).value(1).step(1)
                .prompt("数量")
                .width(80);
    }

    /**
     * 设置为年龄选择器
     * 范围 0-120，步长 1
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asAge() {
        return min(0).max(120).value(18).step(1)
                .prompt("年龄")
                .width(60);
    }

    /**
     * 设置为评分选择器
     * 范围 1-5，步长 1
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asRating() {
        return min(1).max(5).value(3).step(1)
                .width(60);
    }

    /**
     * 设置为小时选择器
     * 范围 0-23，步长 1，循环模式
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asHour() {
        return min(0).max(23).value(0).step(1)
                .cyclic(true)
                .width(100);
    }

    /**
     * 设置为分钟选择器
     * 范围 0-59，步长 1，循环模式
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asMinute() {
        return min(0).max(59).value(0).step(1)
                .cyclic(true)
                .width(60);
    }

    /**
     * 设置为年份选择器
     * 范围 1900-2100，步长 1
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asYear() {
        int currentYear = java.time.Year.now().getValue();
        return min(1900).max(2100).value(currentYear).step(1)
                .width(100);
    }

    /**
     * 设置为月份选择器
     * 范围 1-12，步长 1，循环模式
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asMonth() {
        return min(1).max(12).value(1).step(1)
                .cyclic(true)
                .width(60);
    }

    /**
     * 设置为日期选择器（日）
     * 范围 1-31，步长 1，循环模式
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asDay() {
        return min(1).max(31).value(1).step(1)
                .cyclic(true)
                .width(60);
    }

    /**
     * 设置为百分比选择器
     * 范围 0-100，步长 5
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asPercent() {
        return min(0).max(100).value(50).step(5)
                .width(80);
    }

    /**
     * 设置为优先级选择器
     * 范围 1-10，步长 1
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner asPriority() {
        return min(1).max(10).value(5).step(1)
                .width(60);
    }

    /**
     * 重置为最小值
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner reset() {
        value(getMin());
        return this;
    }

    /**
     * 设置为中间值
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner toMiddle() {
        value((getMin() + getMax()) / 2);
        return this;
    }

    /**
     * 设置为最大值
     *
     * @return FXSpinner 实例（链式调用）
     */
    public FXSpinner toMax() {
        value(getMax());
        return this;
    }
}
