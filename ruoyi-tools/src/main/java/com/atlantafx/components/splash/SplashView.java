package com.atlantafx.components.splash;

import com.atlantafx.util.TaskRunner;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class SplashView {

    private final Stage stage;
    private final Label statusLabel = new Label(String.format("%s (%.0f%%)", "正在启动系统...", 0.0d));

    public SplashView() {
        // 1. 设置舞台为完全透明
        this.stage = new Stage(StageStyle.TRANSPARENT);

        // 2. 加载 Logo 图片
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/splash-logo.png")));
        ImageView logoView = new ImageView(image);
        logoView.setFitWidth(400); // 根据图片实际尺寸调整
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        // 3. 状态文字样式：参考示例使用粉紫色或白色
        statusLabel.setTextFill(Color.web("#FFB6C1")); // 浅粉色，匹配示例风格
        statusLabel.setStyle("-fx-font-family: 'Microsoft YaHei', 'System'; -fx-font-size: 20px;");

        // 4. 布局：垂直居中，去掉背景色
        VBox root = new VBox(20, logoView, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: transparent;"); // 容器完全透明

        // 5. 场景设置
        Scene scene = new Scene(root); // 稍微给大一点空间防止裁剪
        scene.setFill(Color.TRANSPARENT); // 场景底色透明

        stage.setScene(scene);
    }

    /**
     * 更新加载进度和状态文本
     *
     * @param progress 0.0 到 1.0 (虽然示例中是百分比文字，此处可保留参数)
     * @param status   状态文本，例如 "正在加载插件管理器... (10%)"
     */
    public void updateProgress(double progress, String status) {
        // 使用 TaskRunner 确保在 UI 线程更新
        TaskRunner.runInFx(() -> statusLabel.setText(String.format("%s (%.0f%%)", status, progress * 100)));
    }

    public void show() {
        stage.show();
        stage.centerOnScreen();
    }

    public void hide(Runnable onFinished) {
        // 简单关闭，不带动画效果[cite: 5]
        stage.close();
        if (onFinished != null) onFinished.run();
    }
}