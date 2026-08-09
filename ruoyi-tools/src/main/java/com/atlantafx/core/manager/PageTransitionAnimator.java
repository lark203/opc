package com.atlantafx.core.manager;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * 页面切换动画工具类
 * <p>
 * 提供方向感知的页面切换动画效果：
 * - 前进（Forward）：从右向左滑入，模拟进入新页面
 * - 后退（Backward）：从左向右滑入，模拟返回上一页
 * - 淡入淡出（Fade）：通用过渡效果
 */
public final class PageTransitionAnimator {

    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    private static final double SLIDE_DISTANCE = 20; // 滑动距离（像素）

    /**
     * 执行方向感知的页面切换动画
     *
     * @param container 容器节点（StackPane）
     * @param newView   新页面视图
     * @param isForward true=前进动画，false=后退动画
     */
    public static void animateSwitch(StackPane container, Node newView, boolean isForward) {
        // 设置初始状态
        newView.setOpacity(0);
        newView.setTranslateX(isForward ? SLIDE_DISTANCE : -SLIDE_DISTANCE);

        // 清空容器并添加新视图
        container.getChildren().setAll(newView);

        // 创建平移动画
        TranslateTransition translate = new TranslateTransition(ANIMATION_DURATION, newView);
        translate.setToX(0);
        translate.setInterpolator(Interpolator.EASE_OUT);

        // 创建淡入动画
        FadeTransition fade = new FadeTransition(ANIMATION_DURATION, newView);
        fade.setToValue(1.0);

        // 并行执行
        ParallelTransition parallel = new ParallelTransition(translate, fade);
        parallel.play();
    }

    /**
     * 执行简单的淡入动画（用于异步加载完成后替换骨架屏）
     *
     * @param container 容器节点
     * @param finalView 最终视图
     */
    public static void animateFadeIn(StackPane container, Node finalView) {
        finalView.setOpacity(0);
        container.getChildren().setAll(finalView);

        FadeTransition fade = new FadeTransition(Duration.millis(400), finalView);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_IN);
        fade.play();
    }

    /**
     * 执行默认的下滑淡入动画（兼容旧代码）
     *
     * @param container 容器节点
     * @param view      视图节点
     */
    public static void animateDefault(StackPane container, Node view) {
        view.setOpacity(0);
        view.setTranslateY(10);
        container.getChildren().setAll(view);

        FadeTransition fade = new FadeTransition(ANIMATION_DURATION, view);
        fade.setToValue(1.0);

        TranslateTransition translate = new TranslateTransition(ANIMATION_DURATION, view);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }
}
