package com.atlantafx.util;

import atlantafx.base.controls.RingProgressIndicator;
import atlantafx.base.util.Animations;
import com.atlantafx.AppContext;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UiHelper {

    private static final Random RANDOM = new Random();
    private static final Color[] CONFETTI_COLORS = {
            Color.web("#26A69A"), Color.web("#EC407A"), Color.web("#AB47BC"),
            Color.web("#42A5F5"), Color.web("#7E57C2"), Color.web("#FFCA28")
    };

    /**
     * 简单的节点闪烁提醒
     */
    public static void flash(Node node) {
        Animations.flash(node).play();
    }

    /**
     * 抖动提醒（如输入错误）
     */
    public static void shake(Node node) {
        Animations.shakeX(node).play();
    }

    /**
     * 统一的全屏/大块组件入场动画
     * 结合了淡入、微量上位移和极其轻微的缩放
     */
    public static void applySwiftEntry(Node node) {
        node.setOpacity(0);
        node.setTranslateY(10); // 微位移

        var fadeIn = Animations.fadeIn(node, Duration.millis(300));

        node.setScaleX(1.01); // 极其轻微的缩放感
        node.setScaleY(1.01);
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), node);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.SPLINE(0.1, 0.9, 0.2, 1.0));

        TranslateTransition move = new TranslateTransition(Duration.millis(300), node);
        move.setToY(0);

        new ParallelTransition(node, fadeIn, scale, move).play();
    }

    /**
     * 卡片类组件的悬停提亮效果
     * 模拟现代 Web 的微交互
     */
    public static void applyHoverElevate(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
            // 可以在此处动态添加一个 drop-shadow 的 CSS class
            node.getStyleClass().add("elevated-card");
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            node.getStyleClass().remove("elevated-card");
        });
    }

    /**
     * 统一的渐隐隐藏
     */
    public static void fadeOutAndHide(Node node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            node.setVisible(false);
            if (onFinished != null) onFinished.run();
        });
        ft.play();
    }

    /**
     * 带有防抖功能的任务执行
     *
     * @param btn    被点击的按钮
     * @param runner 已经构建好的 TaskRunner
     */
    public static <T> void runWithDebounce(Button btn, TaskRunner<T> runner) {
        if (btn == null || runner == null) return;

        // 1. 禁用按钮
        btn.setDisable(true);

        // 2. 注入恢复逻辑到 onFinal（确保无论成功失败都会恢复）
        runner.onFinal(() -> btn.setDisable(false));

        // 3. 执行
        runner.run();
    }

    /**
     * 增强版防抖：自动禁用并显示加载文案
     */
    public static <T> void runWithLoading(Button btn, String loadingText, TaskRunner<T> runner) {
        String oldText = btn.getText();
        Node oldGraphic = btn.getGraphic();

        btn.setDisable(true);
        if (loadingText != null) btn.setText(loadingText);

        // 使用 AtlantaFX 的 Loading 图标（如果项目中配置了）
        // btn.setGraphic(new RingProgressIndicator());

        runner.onFinal(() -> {
            btn.setDisable(false);
            btn.setText(oldText);
            btn.setGraphic(oldGraphic);
        });

        runner.run();
    }

    /**
     * 为按钮添加防抖处理
     * <p>
     * UiHelper.setDebouncedAction(myButton, Duration.millis(500), e -> {
     * log.info("触发业务操作");
     * });
     *
     * @param btn     目标按钮
     * @param delay   防抖延迟时间
     * @param handler 真正的业务逻辑
     */
    public static void setDebouncedAction(Button btn, Duration delay, EventHandler<ActionEvent> handler) {
        btn.setOnAction(new EventHandler<>() {
            private boolean clicked = false;

            @Override
            public void handle(ActionEvent event) {
                if (clicked) return;
                clicked = true;

                // 执行业务逻辑
                handler.handle(event);

                // 延迟后重置状态
                PauseTransition pause = new PauseTransition(delay);
                pause.setOnFinished(e -> clicked = false);
                pause.play();
            }
        });
    }

    /**
     * 按钮防抖执行：自动禁用、切换文字并显示旋转进度条
     *
     * @param btn         目标按钮
     * @param loadingText 加载时的文字（传 null 则保持原样）
     * @param runner      已构建的 TaskRunner
     */
    public static <T> void runWithLoading2(Button btn, String loadingText, TaskRunner<T> runner) {
        // 1. 保存原始状态
        String oldText = btn.getText();
        Node oldGraphic = btn.getGraphic();

        // 2. 创建 AtlantaFX 的环形进度条
        var progress = new RingProgressIndicator();
        progress.setPrefSize(10, 10); // 适配按钮高度的小尺寸
        progress.setMaxSize(10, 10);
        // 注意：AtlantaFX 的 RingProgressIndicator 默认是不确定的（自旋转）

        // 3. 进入加载状态
        btn.setDisable(true);
        if (loadingText != null) btn.setText(loadingText);
        btn.setGraphic(progress);

        // 4. 绑定生命周期：无论成功失败都恢复
        runner.onFinal(() -> {
            btn.setDisable(false);
            btn.setText(oldText);
            btn.setGraphic(oldGraphic);
        });

        runner.run();
    }

    /**
     * 触发全屏撒花效果
     *
     * @param container 放置 Canvas 的容器，通常建议传入 MainLayout
     */
    public static void playSuccessConfetti() {
        Pane container = AppContext.getMainLayout();
        Canvas canvas = new Canvas(container.getWidth(), container.getHeight());
        canvas.setMouseTransparent(true); // 确保不遮挡点击
        container.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        List<Particle> particles = new ArrayList<>();

        // 初始化 100 个粒子，从屏幕上方随机位置向下落
        for (int i = 0; i < 100; i++) {
            particles.add(new Particle(canvas.getWidth(), canvas.getHeight()));
        }

        new AnimationTimer() {
            int frames = 0;

            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                boolean allFinished = true;

                for (Particle p : particles) {
                    p.update();
                    p.draw(gc);
                    if (!p.isFinished()) allFinished = false;
                }

                frames++;
                // 动画结束（所有粒子出屏）或 180 帧后移除 Canvas
                if (allFinished || frames > 180) {
                    this.stop();
                    container.getChildren().remove(canvas);
                }
            }
        }.start();
    }

    private static class Particle {
        double x, y, vx, vy, ay, angle, spin;
        Color color;
        double size;
        double maxHeight;

        Particle(double canvasW, double canvasH) {
            this.maxHeight = canvasH;
            // 初始位置：屏幕中心上方一点点，呈喷泉状散开
            this.x = canvasW / 2;
            this.y = canvasH / 2 - 50;
            // 随机速度 (向上喷射)
            double theta = Math.toRadians(RANDOM.nextDouble() * 360);
            double speed = RANDOM.nextDouble() * 10 + 5;
            this.vx = Math.cos(theta) * speed;
            this.vy = Math.sin(theta) * speed - 5;
            this.ay = 0.4; // 重力加速
            this.angle = RANDOM.nextDouble() * 360;
            this.spin = RANDOM.nextDouble() * 10 - 5;
            this.color = CONFETTI_COLORS[RANDOM.nextInt(CONFETTI_COLORS.length)];
            this.size = RANDOM.nextDouble() * 8 + 4;
        }

        void update() {
            x += vx;
            y += vy;
            vy += ay;
            angle += spin;
        }

        void draw(GraphicsContext gc) {
            gc.save();
            gc.setFill(color);
            gc.translate(x, y);
            gc.rotate(angle);
            // 绘制小方块或小圆点
            gc.fillRect(-size / 2, -size / 2, size, size / 2);
            gc.restore();
        }

        boolean isFinished() {
            return y > maxHeight + 20;
        }
    }

    /**
     * 获取节点的高清截图（适配 4K/Retina 屏幕）
     */
    public static WritableImage createHighResSnapshot(Node node) {
        // 1. 获取当前主屏幕的缩放比例（例如 1.5 或 2.0）
        double pixelScale = Screen.getPrimary().getOutputScaleX();

        // 2. 配置截图参数
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        // 3. 【关键】应用缩放变换，强制提高采样率
        params.setTransform(new Scale(pixelScale, pixelScale));

        // 4. 计算目标图片的物理尺寸
        int width = (int) Math.ceil(node.getBoundsInParent().getWidth() * pixelScale);
        int height = (int) Math.ceil(node.getBoundsInParent().getHeight() * pixelScale);

        // 5. 执行高质量快照
        WritableImage snapshot = new WritableImage(width, height);
        return node.snapshot(params, snapshot);
    }
}