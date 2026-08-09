package com.atlantafx.components.base;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * 骨架屏组件：使用动画模拟“脉冲”闪烁效果
 */
public class SkeletonPlaceholder extends VBox {

    public SkeletonPlaceholder() {
        setSpacing(15);
        setPadding(new Insets(20));

        // 模拟一个标题块
        Rectangle titleBase = createBar(200, 25);
        // 模拟几行正文内容
        Rectangle line1 = createBar(Double.MAX_VALUE, 15);
        Rectangle line2 = createBar(Double.MAX_VALUE, 15);
        Rectangle line3 = createBar(300, 15);

        getChildren().addAll(titleBase, line1, line2, line3);

        // 启动脉冲动画
        startPulseAnimation();
    }

    private Rectangle createBar(double width, double height) {
        Rectangle rect = new Rectangle(width, height);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        // 使用 AtlantaFX 的变量色或灰色
        rect.setFill(Color.web("#e0e0e0", 0.6));
        if (width == Double.MAX_VALUE) {
            rect.widthProperty().bind(this.widthProperty().subtract(40));
        }
        return rect;
    }

    private void startPulseAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(this.opacityProperty(), 0.4)),
                new KeyFrame(Duration.millis(800), new KeyValue(this.opacityProperty(), 0.8)),
                new KeyFrame(Duration.millis(1600), new KeyValue(this.opacityProperty(), 0.4))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}