package com.atlantafx.components.base;

import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * FXImage - 基于 JavaFX 26 的流式图像数据加载源
 * 封装原生 Image，提供链式初始化、资源路径和输入流的快捷构建
 */
public class FXImage extends Image implements IFXNode<FXImage> {

    /**
     * 私有构造函数：通过 URL 字符串加载
     */
    private FXImage(String url) {
        super(url);
    }

    /**
     * 私有构造函数：指定是否后台加载
     */
    private FXImage(String url, boolean backgroundLoading) {
        super(url, backgroundLoading);
    }

    /**
     * 私有构造函数：指定是否保持宽高比及平滑过滤
     */
    private FXImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        super(url, requestedWidth, requestedHeight, preserveRatio, smooth);
    }

    /**
     * 私有构造函数：指定是否后台加载、是否保持宽高比及平滑过滤
     */
    private FXImage(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
        super(url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading);
    }

    /**
     * 私有构造函数：从输入流中加载
     */
    private FXImage(InputStream is) {
        super(is);
    }

    /**
     * 私有构造函数：从输入流中加载、指定是否后台加载
     */
    private FXImage(InputStream is, boolean backgroundLoading) {
        super(is, backgroundLoading);
    }

    /**
     * 创建图像实例（通过系统路径或网络 URL）
     *
     * @param url 图像源路径 (如 "file:icon.png" 或 "https://example.com/logo.png")
     * @return FXImage 实例
     */
    public static FXImage create(String url) {
        return new FXImage(url);
    }

    /**
     * 创建图像实例（通过系统路径或网络 URL）
     *
     * @param url               图像源路径
     * @param backgroundLoading 是否后台加载
     * @return FXImage 实例
     */
    public static FXImage create(String url, boolean backgroundLoading) {
        return new FXImage(url, backgroundLoading);
    }

    /**
     * 创建高度可定制的图像实例
     *
     * @param url             图像源路径
     * @param requestedWidth  请求加载的裁剪/缩放宽度
     * @param requestedHeight 请求加载的裁剪/缩放高度
     * @param preserveRatio   是否保持长宽比
     * @param smooth          是否启用平滑双线性过滤
     * @return FXImage 实例
     */
    public static FXImage create(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth) {
        return new FXImage(url, requestedWidth, requestedHeight, preserveRatio, smooth);
    }

    /**
     * 创建高度可定 bicubic 过滤的图像实例
     *
     * @param url               图像源路径
     * @param requestedWidth    请求加载的裁剪/缩放宽度
     * @param requestedHeight   请求加载的裁剪/缩放高度
     * @param preserveRatio     是否保持长宽比
     * @param smooth            是否启用平滑双线性过滤
     * @param backgroundLoading 是否后台加载
     * @return FXImage 实例
     */
    public static FXImage create(String url, double requestedWidth, double requestedHeight, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
        return new FXImage(url, requestedWidth, requestedHeight, preserveRatio, smooth, backgroundLoading);
    }

    /**
     * 从输入流创建图像实例（常用于二进制数据或 Jar 包内资源读取）
     *
     * @param is 输入流
     * @return FXImage 实例
     */
    public static FXImage create(InputStream is) {
        return new FXImage(is);
    }

    /**
     * 从输入流创建图像实例（常用于二进制数据或 Jar 包内资源读取）
     *
     * @param is                输入流
     * @param backgroundLoading 是否后台加载
     * @return FXImage 实例
     */
    public static FXImage create(InputStream is, boolean backgroundLoading) {
        return new FXImage(is, backgroundLoading);
    }
}