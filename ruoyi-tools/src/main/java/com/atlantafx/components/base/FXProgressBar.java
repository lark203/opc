package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.DoubleConsumer;

/**
 * FXProgressBar - 进度条组件
 * 继承自 JavaFX ProgressBar，实现 IFXNode 接口支持链式调用
 * 提供便捷的进度设置、样式控制和状态指示方法
 */
public class FXProgressBar extends ProgressBar implements IFXNode<FXProgressBar> {

    /**
     * 默认构造函数
     */
    private FXProgressBar() {
        super();
    }

    /**
     * 创建进度条实例
     *
     * @return FXProgressBar 实例
     */
    public static FXProgressBar create() {
        return new FXProgressBar();
    }

    /**
     * 设置进度值
     * 内置前置安全剪裁，防止数值越界破坏 UI 渲染
     *
     * @param val 进度值（0.0 ~ 1.0），小于 0 则自动判定为不确定进度模式
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar progress(double val) {
        if (val < 0) {
            setProgress(-1.0);
        } else {
            setProgress(Math.clamp(val, 0.0, 1.0));
        }
        return this;
    }

    /**
     * 带有平滑过渡动画的进度设置方法
     *
     * @param targetVal 目标进度值（0.0 ~ 1.0）
     * @param duration  动画持续时间
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar progressAnimated(double targetVal, Duration duration) {
        double safeTarget = Math.clamp(targetVal, 0.0, 1.0);
        Timeline timeline = new Timeline(
                new KeyFrame(duration, new KeyValue(progressProperty(), safeTarget))
        );
        timeline.play();
        return this;
    }

    /**
     * 设置进度条宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置进度条高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 一键配置组件固定几何尺寸
     *
     * @param w 宽度（像素值）
     * @param h 高度（像素值）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色进度条，表示成功、完成状态
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色进度条，表示错误、失败或警告状态
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调，适用于一般进度显示
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色进度条，表示需要注意的状态
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar warning() {
        return stylesClass(Styles.WARNING);
    }

    // ==================== 高级颜色个性化定制 ====================

    /**
     * 动态配置进度条内部填充区域和背景槽的配色方案
     * 完美破解 AtlantaFX 内部因嵌套 `.track` 与 `.bar` 结构导致的外层 CSS 权重失效问题
     *
     * @param fillColor  填充条颜色（CSS 标准颜色串或主题变量，如 "#4CAF50"）
     * @param trackColor 背景滑槽颜色（如 "#E0E0E0"）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar customColor(String fillColor, String trackColor) {
        StringBuilder sb = new StringBuilder();
        if (fillColor != null && !fillColor.trim().isEmpty()) {
            sb.append("-color-progress-fill: ").append(fillColor).append(";");
        }
        if (trackColor != null && !trackColor.trim().isEmpty()) {
            sb.append("-color-progress-track: ").append(trackColor).append(";");
        }
        return styleCss(sb.toString());
    }

    // ==================== 尺寸样式 ====================

    /**
     * 应用大尺寸样式（Large）
     * 更粗更高的进度条，适用于重要进度展示
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar large() {
        return stylesClass(Styles.LARGE);
    }

    /**
     * 应用小尺寸样式（Small）
     * 更紧凑的进度条，适用于空间有限的场景
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar small() {
        return stylesClass(Styles.SMALL);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     * 使进度条在垂直方向填充可用空间
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置组件在水平箱子布局 (HBox) 中的水平生长优先级为 ALWAYS
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置进度条可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置进度条是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置进度条透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置进度条是否禁用
     * 禁用状态下进度条变灰，无法交互
     *
     * @param disabled true-禁用，false-启用
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 特殊用途 ====================

    /**
     * 设置为不确定进度模式
     * 显示无限循环动画，用于不知道具体进度的场景
     *
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar indeterminate() {
        setProgress(-1.0);
        return this;
    }

    /**
     * 设置为确定进度模式
     * 显示具体进度百分比
     *
     * @param value 进度值（0.0-1.0）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar determinate(double value) {
        return progress(value);
    }

    /**
     * 设置进度变更监听器
     *
     * @param listener 进度变更时的回调函数，接收新的进度值
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar onProgressChange(DoubleConsumer listener) {
        progressProperty().addListener((obs, oldVal, newVal) -> listener.accept((Double) newVal));
        return this;
    }

    /**
     * 设置进度条在 StackPane 中的对齐方式
     *
     * @param pos 对齐位置（Pos.CENTER、Pos.TOP_CENTER 等）
     * @return FXProgressBar 实例（链式调用）
     */
    public FXProgressBar stackPane(Pos pos) {
        StackPane.setAlignment(this, pos);
        return this;
    }
}

