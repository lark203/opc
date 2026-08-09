package com.atlantafx.core.theme;

import com.atlantafx.AppContext;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.util.Duration;

/**
 * 界面主题切换动画
 */
public class ThemeTransitioner {

    // 定义更灵动的插值曲线：类似于流畅的减速效果
    private static final Interpolator ELASTIC_CURVE = Interpolator.ofSpline(0.25, 0.1, 0.25, 1.0);
    private static final Duration ANIM_DURATION = Duration.millis(600); // 缩短时间，显得更轻快

    /**
     * 优化：使用 ImageView 缓存截图，并使用圆遮罩进行动画
     */
    public static void transitionCircle(Scene scene, double centerX, double centerY, Runnable themeSwitcher) {
        StackPane root = (StackPane) scene.getRoot();

        // 立即锁定交互，防止二次点击
        root.setDisable(true);

        // 优化 1: 显式设置 Snapshot 参数以提升性能
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT); // 避免填充默认白色背景

        // 优化 2: 某些高分屏 (HiDPI) 下，需要处理缩放比，否则截图会模糊
        double scale = Screen.getPrimary().getOutputScaleX();
        params.setTransform(Transform.scale(scale, scale));

        // 获取当前 UI 的状态快照
        WritableImage snapshot = root.snapshot(params, null);
        ImageView imageView = new ImageView(snapshot);

        // 优化 3: 硬件加速位图缓存
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);

        // 执行主题切换（这会改变 CSS，但因为旧图盖在上面，用户看不到瞬间的闪烁）
        themeSwitcher.run();

        // 将旧图覆盖
        root.getChildren().add(imageView);

        // 计算半径
        double width = root.getWidth();
        double height = root.getHeight();
        double maxW = Math.max(centerX, width - centerX);
        double maxH = Math.max(centerY, height - centerY);
        double finalRadius = Math.sqrt(maxW * maxW + maxH * maxH);

        Circle mask = new Circle(centerX, centerY, finalRadius);
        imageView.setClip(mask);

        // 动画执行
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(mask.radiusProperty(), finalRadius)),
                new KeyFrame(Duration.millis(800), new KeyValue(mask.radiusProperty(), 0, Interpolator.EASE_OUT))
        );

        timeline.setOnFinished(e -> {
            root.getChildren().remove(imageView);
            // 显式清理资源，防止内存泄漏
            imageView.setImage(null);
            root.setDisable(false);
        });

        timeline.play();
    }

    /**
     * 优化：使用 ImageView 缓存截图，并使用圆遮罩进行动画 -- 备用方法
     */
    public static void transitionCircle(StackPane root, double centerX, double centerY, Runnable themeSwitcher) {

        // 如果已经在切换中（判断是否存在旧的截图层），则跳过
        if (root.getChildren().stream().anyMatch(n -> n instanceof ImageView && n.getStyleClass().contains("temp-snapshot"))) {
            return;
        }

        root.setDisable(true);

        WritableImage snapshot = root.snapshot(new SnapshotParameters(), null);
        ImageView imageView = new ImageView(snapshot);
        imageView.getStyleClass().add("temp-snapshot"); // 标记
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);

        themeSwitcher.run();
        root.getChildren().add(imageView);

        double finalRadius = Math.sqrt(Math.pow(root.getWidth(), 2) + Math.pow(root.getHeight(), 2));
        Circle mask = new Circle(centerX, centerY, finalRadius);
        imageView.setClip(mask);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(800), new KeyValue(mask.radiusProperty(), 0, Interpolator.EASE_OUT))
        );

        timeline.setOnFinished(e -> {
            root.getChildren().remove(imageView);
            imageView.setImage(null);
            root.setDisable(false);
        });

        timeline.play();
    }

    public static void transitionCircle(double centerX, double centerY, boolean isDark, Runnable themeSwitcher) {
        StackPane root = AppContext.getMainLayout();
        if (root.getChildren().stream().anyMatch(n -> n.getStyleClass().contains("temp-snapshot"))) {
            return;
        }

        root.setDisable(true);

        // 1. 截取旧界面快照
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = root.snapshot(params, null);
        ImageView imageView = new ImageView(snapshot);
        imageView.getStyleClass().add("temp-snapshot");
        imageView.setCache(true);
        imageView.setCacheHint(CacheHint.SPEED);

        // 2. 切换主题（此时 root 内部的布局已变为新主题样式）
        themeSwitcher.run();

        // 获取主布局容器 (innerLayout)
        // 根据你的 MainLayout.java，innerLayout 是第一个子节点
        Node mainLayout = root.getChildren().getFirst();

        double finalRadius = Math.sqrt(Math.pow(root.getWidth(), 2) + Math.pow(root.getHeight(), 2));
        Circle mask = new Circle(centerX, centerY, 0);
        Timeline timeline = new Timeline();

        if (isDark) {
            // --- 变黑：向内收缩 (逻辑不变) ---
            root.getChildren().add(imageView); // 快照(白)在上
            mask.setRadius(finalRadius);
            imageView.setClip(mask);
            timeline.getKeyFrames().add(
                    new KeyFrame(ANIM_DURATION, new KeyValue(mask.radiusProperty(), 0, ELASTIC_CURVE))
            );
        } else {
            // --- 变白：向外扩散 (关键修正) ---
            root.getChildren().addFirst(imageView); // 1. 将旧快照(黑)插到底层
            mainLayout.setClip(mask);             // 2. 对新布局执行剪裁
            mask.setRadius(0);                    // 3. 半径从 0 开始

            timeline.getKeyFrames().add(
                    new KeyFrame(ANIM_DURATION, new KeyValue(mask.radiusProperty(), finalRadius, ELASTIC_CURVE))
            );
        }

        timeline.setOnFinished(e -> {
            mainLayout.setClip(null); // 动画结束必须清除剪裁，否则影响布局响应
            root.getChildren().remove(imageView);
            imageView.setImage(null);
            root.setDisable(false);
        });

        timeline.play();
    }
}