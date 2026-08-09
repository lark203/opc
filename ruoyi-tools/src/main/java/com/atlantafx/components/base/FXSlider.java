package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * FXSlider - 滑块组件
 * 继承自 JavaFX Slider，实现 IFXNode 接口支持链式调用
 * 提供便捷的数值调节、刻度显示和样式设置方法
 */
public class FXSlider extends Slider implements IFXNode<FXSlider> {

    /**
     * 默认构造函数
     */
    private FXSlider() {
        super();
    }

    /**
     * 创建滑块（指定范围和初始值）
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 初始值
     */
    private FXSlider(double min, double max, double value) {
        super(min, max, value);
    }

    /**
     * 创建水平滑块（指定范围和初始值）
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 初始值
     * @return FXSlider 实例
     */
    public static FXSlider create(double min, double max, double value) {
        return new FXSlider(min, max, value);
    }

    /**
     * 创建一个默认的、范围为 0-100 且初始值为 0 的水平滑块实例
     *
     * @return FXSlider 实例（链式调用入口）
     */
    public static FXSlider create() {
        return new FXSlider();
    }

    /**
     * 创建垂直滑块
     *
     * @param min   最小值
     * @param max   最大值
     * @param value 初始值
     * @return FXSlider 实例
     */
    public static FXSlider createVertical(double min, double max, double value) {
        FXSlider slider = new FXSlider(min, max, value);
        slider.setOrientation(Orientation.VERTICAL);
        return slider;
    }

    /**
     * 强行驱动微调滑块当前的绝对数值
     *
     * @param value 目标数值
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider value(double value) {
        setValue(value);
        return this;
    }

    /**
     * 动态微调滑块的可选区间最小值值域
     */
    public FXSlider min(double min) {
        setMin(min);
        return this;
    }

    /**
     * 动态微调滑块的可选区间最大值值域
     */
    public FXSlider max(double max) {
        setMax(max);
        return this;
    }

    /**
     * 建立与外部 Double 属性对象的双向响应式数据流绑定
     * 常用于与表单模型、后台遥测指标参数槽进行实时数据同步
     *
     * @param property 外部源模型属性槽
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider bindBidirectional(Property<Number> property) {
        if (property != null) {
            valueProperty().bindBidirectional(property);
        }
        return this;
    }

    /**
     * 设置滑块方向为水平
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider horizontal() {
        setOrientation(Orientation.HORIZONTAL);
        return this;
    }

    /**
     * 设置滑块方向为垂直
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider vertical() {
        setOrientation(Orientation.VERTICAL);
        return this;
    }

    // ==================== 高频交互事件流监听 ====================

    /**
     * 快捷注册数值滑移高频监听器
     * 当用户在桌面拖拽滑块（Thumb）产生物理位移时，会实时、高频地触发此回调
     *
     * @param listener 消费最新数值的回调函数
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider onValueChanged(Consumer<Double> listener) {
        if (listener != null) {
            valueProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal.doubleValue()));
        }
        return this;
    }

    /**
     * 监听拖拽状态：当用户鼠标开始点击按压滑块时触发
     */
    public FXSlider onValueChangingStart(Runnable callback) {
        if (callback != null) {
            valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (isChanging) callback.run();
            });
        }
        return this;
    }

    /**
     * 监听拖拽状态：当用户鼠标释放拖拽、数值彻底锁死落脚时触发
     * 极度适合在此处触发重型的 RPC 远程网络同步或数据库更新，避免高频拖拽引发后端雪崩
     */
    public FXSlider onValueChangingEnd(Runnable callback) {
        if (callback != null) {
            valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging) callback.run();
            });
        }
        return this;
    }

    // ==================== 物理刻度与文本格式化重写 ====================

    /**
     * 开启或关闭物理刻度尺线（Tick Marks）与数字化标签（Tick Labels）的展示
     *
     * @param showMarks  是否绘制刻度线
     * @param showLabels 是否绘制刻度值文字
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider ticks(boolean showMarks, boolean showLabels) {
        setShowTickMarks(showMarks);
        setShowTickLabels(showLabels);
        return this;
    }

    /**
     * 设置主刻度线的物理跨度单位（例如每隔 10 像素标记一个主刻度）
     */
    public FXSlider majorUnit(double unit) {
        setMajorTickUnit(unit);
        return this;
    }

    /**
     * 设置两根主刻度线之间嵌套的次要微型刻度线总数
     */
    public FXSlider minorCount(int count) {
        setMinorTickCount(count);
        return this;
    }

    /**
     * 强制开启磁吸效应（自动对齐最近的物理刻度点值）
     */
    public FXSlider snapToTicks(boolean snap) {
        setSnapToTicks(snap);
        return this;
    }

    /**
     * 快捷注入高度简化的函数式刻度标签格式化映射器（Label Formatter）
     * 消除原代码复杂的原生匿名内部类，允许使用一行 Lambda 自由追加百分比、单位或温度后缀
     *
     * @param formatter 数字化转换为展示文本的函数式规则
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider displayFormatter(Function<Double, String> formatter) {
        if (formatter != null) {
            setLabelFormatter(new StringConverter<>() {
                @Override
                public String toString(Double value) {
                    return value == null ? "" : formatter.apply(value);
                }

                @Override
                public Double fromString(String string) {
                    return 0.0; // 滑块轴文案通常只读，不提供逆向字符串反解析
                }
            });
        }
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色滑块
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色滑块
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色滑块
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider warning() {
        return stylesClass(Styles.WARNING);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 当置于 HBox 容器骨架内时，声明横向拉伸延伸优先级
     */
    public FXSlider hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置滑块宽度
     *
     * @param w 宽度值（像素）
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置滑块高度
     *
     * @param h 高度值（像素）
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    // ==================== 刻度显示 ====================

    /**
     * 显示刻度标签
     *
     * @param show true-显示，false-隐藏
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider showTickLabels(boolean show) {
        setShowTickLabels(show);
        return this;
    }

    /**
     * 显示刻度标记
     *
     * @param show true-显示，false-隐藏
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider showTickMarks(boolean show) {
        setShowTickMarks(show);
        return this;
    }

    /**
     * 设置主要刻度单位
     *
     * @param unit 主要刻度间隔
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider majorTickUnit(double unit) {
        setMajorTickUnit(unit);
        return this;
    }

    /**
     * 设置次要刻度数量
     * 每个主要刻度之间的次要刻度数
     *
     * @param count 次要刻度数量
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider minorTickCount(int count) {
        setMinorTickCount(count);
        return this;
    }

    /**
     * 设置刻度显示格式
     * 自定义刻度标签的显示文本
     *
     * @param labelFormatter 标签格式化器
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider labelFormatter(javafx.util.StringConverter<Double> labelFormatter) {
        setLabelFormatter(labelFormatter);
        return this;
    }

    /**
     * 设置刻度间隔（快速设置）
     * 同时设置主要刻度和次要刻度
     *
     * @param tickInterval 刻度间隔
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider tickInterval(double tickInterval) {
        setMajorTickUnit(tickInterval);
        setMinorTickCount(0);
        setShowTickLabels(true);
        setShowTickMarks(true);
        return this;
    }

    /**
     * 转换当前滑块为大号尺寸规格（Large Size，大幅增加轨道的厚度与滑块按钮的交互面积）
     */
    public FXSlider large() {
        getStyleClass().removeAll(Styles.SMALL, Styles.LARGE);
        return stylesClass(Styles.LARGE);
    }

    /**
     * 转换当前滑块为小号紧凑型尺寸规格（Small Size）
     */
    public FXSlider small() {
        getStyleClass().removeAll(Styles.SMALL, Styles.LARGE);
        return stylesClass(Styles.SMALL);
    }

    // ==================== 状态控制 ====================

    /**
     * 设置滑块可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置滑块是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置滑块透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置滑块是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 事件监听 ====================

    /**
     * 设置值变更监听器
     *
     * @param listener 值变更时的回调函数，接收新的值
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider onValueChange(Consumer<Double> listener) {
        valueProperty().addListener((obs, oldVal, newVal) -> listener.accept((Double) newVal));
        return this;
    }

    /**
     * 设置拖动开始事件监听器
     *
     * @param listener 拖动开始时的回调函数
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider onDragStart(Runnable listener) {
        addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> listener.run());
        return this;
    }

    /**
     * 设置拖动结束事件监听器
     *
     * @param listener 拖动结束时的回调函数
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider onDragEnd(Runnable listener) {
        addEventHandler(javafx.scene.input.MouseEvent.MOUSE_RELEASED, event -> listener.run());
        return this;
    }

    /**
     * 设置工具提示
     *
     * @param text 提示文本
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider tooltip(String text) {
        setTooltip(new javafx.scene.control.Tooltip(text));
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 场景预设：一键升格为现代化百分比监控滑块轴
     * 区间锁定为 0-100，默认附带 % 符号文本渲染
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider asPercentage() {
        return min(0).max(100).majorUnit(25).minorCount(4)
                .ticks(true, true)
                .displayFormatter(val -> String.format("%.0f%%", val));
    }

    /**
     * 设置为音量控制滑块
     * 范围 0-100，显示音量图标
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider asVolume() {
        setMin(0);
        setMax(100);
        setValue(50);
        setMajorTickUnit(25);
        setMinorTickCount(4);
        setShowTickLabels(true);
        setShowTickMarks(true);
        return this;
    }

    /**
     * 设置为亮度控制滑块
     * 范围 0-100
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider asBrightness() {
        setMin(0);
        setMax(100);
        setValue(75);
        setMajorTickUnit(25);
        return this;
    }

    /**
     * 设置为百分比滑块
     * 范围 0-100，步长 1，显示百分比符号
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider asPercent() {
        setMin(0);
        setMax(100);
        setValue(50);
        setMajorTickUnit(10);
        setMinorTickCount(1);
        setSnapToTicks(true);
        setShowTickLabels(true);
        setShowTickMarks(true);
        setLabelFormatter(new javafx.util.StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.format("%.0f%%", value);
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string.replace("%", ""));
            }
        });
        return this;
    }

    /**
     * 场景预设：一键升格为系统星级评分滑块轴（Star Rating）
     * 区间精确卡死为 0-5 整数步长，且具有强力刻度磁吸效应
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider asRating() {
        return min(0).max(5).value(3).majorUnit(1).minorCount(0)
                .snapToTicks(true)
                .ticks(true, true)
                .displayFormatter(val -> String.format("%.0f 星", val));
    }

    /**
     * 场景预设：一键升格为高精度温度调控滑块轴
     * 支持负数区间值域跨越，自动追加 ℃ 物理单位
     *
     * @return FXSlider 实例（链式调用）
     */
    public FXSlider asTemperature() {
        return min(-20).max(50).value(22).majorUnit(10).minorCount(2)
                .ticks(true, true)
                .displayFormatter(val -> String.format("%.0f℃", val));
    }
}
