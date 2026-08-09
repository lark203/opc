package com.atlantafx.components.base;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * FXImageView - 基于 AtlantaFX 风格的图像渲染视窗组件
 * 继承自 JavaFX ImageView，实现 IFXNode 接口支持链式调用
 * 包含丰富的长宽缩放、比例锁定、视窗裁剪以及针对 AtlantaFX 的样式扩展
 */
public class FXImageView extends ImageView implements IFXNode<FXImageView> {

    /**
     * 默认私有构造函数
     */
    private FXImageView() {
        super();
    }

    /**
     * 带图像源的私有构造函数
     */
    private FXImageView(Image image) {
        super(image);
    }

    /**
     * 带 URL 路径的私有构造函数
     */
    private FXImageView(String url) {
        super(url);
    }

    /**
     * 创建空图像渲染视窗实例
     *
     * @return FXImageView 实例
     */
    public static FXImageView create() {
        return new FXImageView();
    }

    /**
     * 创建指定图像源的渲染视窗实例
     *
     * @param image 实现了或继承自 JavaFX Image 的对象
     * @return FXImageView 实例
     */
    public static FXImageView create(Image image) {
        return new FXImageView(image);
    }

    /**
     * 根据 URL 快捷创建渲染视窗实例
     *
     * @param url 图像路径
     * @return FXImageView 实例
     */
    public static FXImageView create(String url) {
        return new FXImageView(url);
    }

    public static FXImageView create(String url, boolean backgroundLoading) {
        return new FXImageView(FXImage.create(url, backgroundLoading));
    }

    /**
     * 链式绑定图像源
     *
     * @param image 图像源
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView image(Image image) {
        setImage(image);
        return this;
    }

    /**
     * 设置视窗渲染的最佳像素宽度
     *
     * @param width 像素宽度
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView width(double width) {
        setFitWidth(width);
        return this;
    }

    /**
     * 设置视窗渲染的最佳像素高度
     *
     * @param height 像素高度
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView height(double height) {
        setFitHeight(height);
        return this;
    }

    /**
     * 一键快捷设置渲染视窗的尺寸
     *
     * @param width  像素宽度
     * @param height 像素高度
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView size(double width, double height) {
        setFitWidth(width);
        setFitHeight(height);
        return this;
    }

    /**
     * 设置在缩放时是否无条件保持原图的宽高比
     *
     * @param preserveRatio true-保持比例不拉伸，false-拉伸铺满视窗
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView preserveRatio(boolean preserveRatio) {
        setPreserveRatio(preserveRatio);
        return this;
    }

    /**
     * 设置缩放算法平滑度
     *
     * @param smooth true-采用高质量过滤（可能会消耗些许性能），false-快速渲染
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView smooth(boolean smooth) {
        setSmooth(smooth);
        return this;
    }

    /**
     * 设置图像对齐视窗的几何裁剪区 (Viewport)
     * 用于局部图片放大、游戏精灵表（Sprite Sheet）切割或地图瓦片渲染
     *
     * @param value 矩形裁剪区域
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView viewport(Rectangle2D value) {
        setViewport(value);
        return this;
    }

    /**
     * 链式设置组件的布局位置
     *
     * @param value 布局位置
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView layoutX(double value) {
        setLayoutX(value);
        return this;
    }

    /**
     * 链式设置组件的布局位置
     *
     * @param value 布局位置
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView layoutY(double value) {
        setLayoutY(value);
        return this;
    }

    /**
     * 设置组件的可见性
     *
     * @param visible true-可见，false-隐藏占位
     * @return FXImageView 实例（链式调用）
     */
    public FXImageView visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    // ==================== AtlantaFX 样式与效果增强 ====================

    /**
     * 设置组件的不透明度
     *
     * @param value
     * @return
     */
    public FXImageView opacity(double value) {
        setOpacity(value);
        return this;
    }
}