package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * FXStatusBar - 状态栏组件
 * 继承自 JavaFX HBox，实现 IFXNode 接口支持链式调用
 * 提供便捷的状态信息显示、进度条和消息提示方法
 */
public class FXStatusBar extends HBox implements IFXNode<FXStatusBar> {

    private final FXLabel statusLabel;
    private final FXLabel rightLabel;
    private ProgressBar progressBar;

    /**
     * 默认构造函数
     */
    private FXStatusBar() {
        super();
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(5, 10, 5, 10));

        // 创建左侧状态标签
        statusLabel = FXLabel.create("就绪").fontColor("#666666");

        // 创建右侧标签
        rightLabel = FXLabel.create().align(Pos.CENTER_RIGHT);

        // 添加弹性空间
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(statusLabel, spacer, rightLabel);
    }

    /**
     * 创建空白状态栏实例
     *
     * @return FXStatusBar 实例
     */
    public static FXStatusBar create() {
        return new FXStatusBar();
    }

    /**
     * 设置左侧状态文本
     *
     * @param text 状态文本
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar text(String text) {
        statusLabel.setText(text);
        return this;
    }

    /**
     * 获取当前状态文本
     *
     * @return 状态文本
     */
    public String getText() {
        return statusLabel.getText();
    }

    /**
     * 设置右侧文本
     *
     * @param text 右侧文本
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar rightText(String text) {
        rightLabel.setText(text);
        return this;
    }

    /**
     * 添加进度条到状态栏
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar withProgress() {
        if (progressBar == null) {
            progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(200);

            // 找到 spacer 的位置并插入进度条
            for (int i = 0; i < getChildren().size(); i++) {
                Node node = getChildren().get(i);
                if (node instanceof HBox && ((HBox) node).getMaxWidth() == Double.MAX_VALUE) {
                    getChildren().add(i, progressBar);
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 设置进度值
     *
     * @param progress 进度值（0.0-1.0）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar progress(double progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
        return this;
    }

    /**
     * 移除进度条
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar removeProgress() {
        if (progressBar != null) {
            getChildren().remove(progressBar);
            progressBar = null;
        }
        return this;
    }

    /**
     * 添加自定义节点到状态栏
     *
     * @param node 自定义节点
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar add(Node node) {
        getChildren().add(node);
        return this;
    }

    /**
     * 添加分隔线
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar separator() {
        Separator sep = new Separator();
        sep.setMaxHeight(15);
        getChildren().add(sep);
        return this;
    }

    /**
     * 设置状态栏高度
     *
     * @param h 高度值（像素）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置状态栏宽度
     *
     * @param w 宽度值（像素）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 添加 CSS 样式类到状态栏
     *
     * @param classes CSS 样式类名称（可变参数）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar stylesClass(String... classes) {
        getStyleClass().addAll(classes);
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色状态栏
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色状态栏，表示错误
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色状态栏
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 应用信息样式（Info）
     * 蓝色状态栏
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar info() {
        return stylesClass(Styles.ACCENT);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置状态栏背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar background(String color) {
        setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                        javafx.scene.paint.Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        javafx.scene.layout.CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));
        return this;
    }

    /**
     * 设置状态栏边框
     *
     * @param width 边框宽度（像素）
     * @param color CSS 格式的颜色字符串
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar border(double width, String color) {
        setBorder(new javafx.scene.layout.Border(
                new javafx.scene.layout.BorderStroke(
                        javafx.scene.paint.Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        javafx.scene.layout.BorderStrokeStyle.SOLID,
                        javafx.scene.layout.CornerRadii.EMPTY,
                        new javafx.scene.layout.BorderWidths(width)
                )
        ));
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置状态栏可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置状态栏是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置状态栏透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置状态栏是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为主窗口底部状态栏
     * 标准样式，灰色背景
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar asMainStatusbar() {
        return background("#f5f5f5")
                .border(1, "#e0e0e0")
                .height(30)
                .text("就绪");
    }

    /**
     * 设置为深色主题状态栏
     * 深色背景，浅色文字
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar darkTheme() {
        return background("#2d2d2d")
                .border(1, "#404040")
                .stylesClass("dark-status");
    }

    /**
     * 设置为紧凑状态栏
     * 更小的高度
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar compact() {
        return height(25).padding(3);
    }

    /**
     * 设置为宽松状态栏
     * 更大的高度和内边距
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar spacious() {
        return height(40).padding(8);
    }

    /**
     * 设置内边距
     *
     * @param v 内边距值（像素）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    // ==================== 状态消息快捷方法 ====================

    /**
     * 显示就绪状态
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar ready() {
        return text("就绪");
    }

    /**
     * 显示加载中状态
     *
     * @param message 加载消息
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar loading(String message) {
        return text(message).withProgress().progress(-1); // 不确定进度
    }

    /**
     * 显示成功消息
     *
     * @param message 成功消息
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar success(String message) {
        return text(message).success();
    }

    /**
     * 显示错误消息
     *
     * @param message 错误消息
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar error(String message) {
        return text("错误：" + message).danger();
    }

    /**
     * 显示警告消息
     *
     * @param message 警告消息
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar warn(String message) {
        return text("警告：" + message).warning();
    }

    /**
     * 显示信息消息
     *
     * @param message 信息消息
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar info(String message) {
        return text(message).info();
    }

    /**
     * 显示进度消息
     *
     * @param message  进度消息
     * @param progress 进度值（0.0-1.0）
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar progress(String message, double progress) {
        return text(message).withProgress().progress(progress);
    }

    /**
     * 清空状态栏
     * 移除所有自定义节点，保留基本结构
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar clear() {
        statusLabel.setText("");
        rightLabel.setText("");
        removeProgress();
        return this;
    }

    /**
     * 重置状态栏
     * 恢复到初始状态
     *
     * @return FXStatusBar 实例（链式调用）
     */
    public FXStatusBar reset() {
        return clear()
                .text("就绪")
                .background("#f5f5f5")
                .border(1, "#e0e0e0");
    }

    /**
     * 获取左侧状态标签
     *
     * @return FXLabel 实例
     */
    public FXLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * 获取右侧标签
     *
     * @return FXLabel 实例
     */
    public FXLabel getRightLabel() {
        return rightLabel;
    }

    /**
     * 获取进度条（如果存在）
     *
     * @return ProgressBar 实例或 null
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
