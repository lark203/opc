package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXScrollPane - 可滚动容器组件
 * 继承自 JavaFX ScrollPane，实现 IFXNode 接口支持链式调用
 * 提供便捷的滚动控制和样式设置方法
 */
public class FXScrollPane extends ScrollPane implements IFXNode<FXScrollPane> {

    /**
     * 默认构造函数
     */
    public FXScrollPane() {
        super();
    }

    /**
     * 创建带内容节点的滚动面板
     *
     * @param node 要包装在滚动面板中的内容节点
     */
    public FXScrollPane(Node node) {
        super(node);
    }

    /**
     * 创建空白的滚动面板实例
     *
     * @return FXScrollPane 实例
     */
    public static FXScrollPane create() {
        return new FXScrollPane();
    }

    /**
     * 创建带内容节点的滚动面板实例
     *
     * @param node 要包装在滚动面板中的内容节点
     * @return FXScrollPane 实例
     */
    public static FXScrollPane create(Node node) {
        return new FXScrollPane(node);
    }

    // ==================== 视窗内容控制 API ====================

    /**
     * 设置滚动面板的内容节点
     *
     * @param node 内容节点
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane content(Node node) {
        setContent(node);
        return this;
    }

    /**
     * 设置内容宽度适配滚动面板宽度
     * 启用后，内容会自动调整宽度以匹配视口宽度
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane fitToWidth() {
        setFitToWidth(true);
        return this;
    }

    /**
     * 设置内容高度适配滚动面板高度
     * 启用后，内容会自动调整高度以匹配视口高度
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane fitToHeight() {
        setFitToHeight(true);
        return this;
    }

    // ==================== 滚动条物理策略控制 ====================

    /**
     * 设置无滚动条样式
     * 隐藏水平和垂直滚动条，但保留滚动功能
     * 适用于自定义滚动条 UI 的场景
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane noScrollBars() {
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        return this;
    }

    /**
     * 设置始终显示滚动条
     * 即使内容未超出视口也显示滚动条
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane alwaysScrollBars() {
        setHbarPolicy(ScrollBarPolicy.ALWAYS);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        return this;
    }

    /**
     * 设置按需动态显示纵横滚动条（默认原生策略）
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane asNeededScrollBars() {
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        return this;
    }

    // ==================== 几何间距与限界控制 ====================

    public FXScrollPane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置滚动面板宽度
     *
     * @param w 宽度值（像素）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置滚动面板高度
     *
     * @param h 高度值（像素）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置滚动面板最大宽度
     *
     * @param w 最大宽度值（像素）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane mxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    /**
     * 设置滚动面板最大高度
     *
     * @param h 最大高度值（像素）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane mxHeight(double h) {
        setMaxHeight(h);
        return this;
    }

    /**
     * 设置滚动面板内边距
     *
     * @param v 内边距值（像素），应用于四个方向
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置滚动面板各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    // ==================== 父级容器增长优先级分配 ====================

    /**
     * 强制将当前可滚动视窗在 VBox 父布局中垂直拉伸撑满
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 强制将当前可滚动视窗在 HBox 父布局中水平拉伸撑满
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== 可见性和状态 ====================

    /**
     * 设置滚动面板可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置滚动面板是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置滚动面板透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置滚动面板是否禁用
     * 禁用状态下无法进行滚动操作
     *
     * @param disabled true-禁用，false-启用
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置滚动面板的 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置水平滚动条策略
     * 控制水平滚动条的显示行为
     *
     * @param policy 滚动条策略：ALWAYS（总是显示）、NEVER（从不显示）、AS_NEEDED（按需显示）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane hbar(ScrollBarPolicy policy) {
        setHbarPolicy(policy);
        return this;
    }

    /**
     * 设置垂直滚动条策略
     * 控制垂直滚动条的显示行为
     *
     * @param policy 滚动条策略：ALWAYS（总是显示）、NEVER（从不显示）、AS_NEEDED（按需显示）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane vbar(ScrollBarPolicy policy) {
        setVbarPolicy(policy);
        return this;
    }

    // ==================== 滚动行为增强 ====================

    /**
     * 设置滚动面板的 PANNING 模式
     * 启用后可以通过鼠标拖拽来平移内容视图
     *
     * @param enabled true-启用拖拽平移，false-禁用
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane pannable(boolean enabled) {
        setPannable(enabled);
        return this;
    }

    /**
     * 设置是否启用鼠标滚轮滚动
     * 禁用后，鼠标滚轮不会触发滚动
     *
     * @param enabled true-启用滚轮滚动，false-禁用
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane wheelScrolling(boolean enabled) {
        if (!enabled) {
            addEventFilter(ScrollEvent.SCROLL, Event::consume);
        }
        return this;
    }

    /**
     * 设置水平滚动位置
     *
     * @param value 滚动位置值（0.0-1.0），0.0 表示最左侧，1.0 表示最右侧
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane hvalue(double value) {
        setHvalue(value);
        return this;
    }

    /**
     * 设置垂直滚动位置
     *
     * @param value 滚动位置值（0.0-1.0），0.0 表示最顶部，1.0 表示最底部
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane vvalue(double value) {
        setVvalue(value);
        return this;
    }

    /**
     * 滚动到顶部
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane scrollToTop() {
        setVvalue(0.0);
        return this;
    }

    /**
     * 滚动到底部
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane scrollToBottom() {
        setVvalue(1.0);
        return this;
    }

    /**
     * 滚动到最左侧
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane scrollToLeft() {
        setHvalue(0.0);
        return this;
    }

    /**
     * 滚动到最右侧
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane scrollToRight() {
        setHvalue(1.0);
        return this;
    }

    // ==================== 样式增强 ====================

    /**
     * 设置滚动面板背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane background(String color) {
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 设置滚动面板边框
     *
     * @param width  边框宽度（像素）
     * @param color  CSS 格式的颜色字符串
     * @param radius 圆角半径（像素）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx;",
                width, color, radius
        );
        return styleCss(style);
    }

    /**
     * 快捷转化为带有 AtlantaFX Edge-to-Edge 风格的无损滚动视窗
     * 去除外围原生厚边框，使其能无缝融入诸如左侧侧边栏、右侧抽屉等高阶容器内部
     *
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane bgEdgeToEdgeStyle() {
        return stylesClass(Tweaks.EDGE_TO_EDGE);
    }

    /**
     * 为当前面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATION_SMALL），2级适中（Styles.ELEVATION_MEDIUM），3级最深（Styles.ELEVATION_LARGE）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane shadow(int level) {
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
     * 基于三次立方缓动算法（Ease-Out Cubic）平滑垂直滚动到指定位置率值
     * 优雅处理点击侧边菜单快捷锚定或回到顶部按钮的物理回弹动效
     *
     * @param targetVValue 目标垂直滚动系数值（取值范围 0.0 ~ 1.0）
     * @return FXScrollPane 实例（链式调用）
     */
    public FXScrollPane smoothScrollTo(double targetVValue) {
        // 范围限界修正
        final double finalTarget = Math.clamp(targetVValue, 0.0, 1.0);
        final double startValue = getVvalue();
        final double delta = finalTarget - startValue;

        if (Math.abs(delta) < 0.01) {
            setVvalue(finalTarget);
            return this;
        }

        final long startTime = System.nanoTime();
        final long durationNanos = 300_000_000L; // 锁死 300ms 动效周期

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedNanos = now - startTime;
                if (elapsedNanos >= durationNanos) {
                    setVvalue(finalTarget);
                    stop(); // 终止计时器
                } else {
                    double progress = (double) elapsedNanos / durationNanos;
                    // 三次立方阶梯缓动公式：f(t) = 1 - (1 - t)^3
                    double easedProgress = 1.0 - Math.pow(1.0 - progress, 3);
                    setVvalue(startValue + (delta * easedProgress));
                }
            }
        };
        timer.start();
        return this;
    }
}
