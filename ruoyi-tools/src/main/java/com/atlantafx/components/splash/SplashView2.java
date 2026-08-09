package com.atlantafx.components.splash;

import com.atlantafx.util.TaskRunner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class SplashView2 {

    private final Stage stage;
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label statusLabel = new Label("准备启动...");

    public SplashView2() {
        this.stage = new Stage(StageStyle.TRANSPARENT);

        // 1. 图片部分
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/assets/images/splash-logo.png")));
        ImageView logoView = new ImageView(image);
        logoView.setFitWidth(350); // 根据你的图片比例调整
        logoView.setPreserveRatio(true);

        // 2. 进度条样式 (AtlantaFX 默认样式或自定义)
        progressBar.setPrefWidth(350);
        progressBar.setPrefHeight(4);
        progressBar.setStyle("-fx-accent: #E67E22;"); // 使用图片中的橙色作为进度条颜色

        // 3. 状态文字
        statusLabel.setTextFill(Color.web("#888888"));
        statusLabel.setStyle("-fx-font-size: 12px;");

        // 4. 垂直布局
        VBox root = new VBox(20, logoView, progressBar, statusLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        // 白色或极简灰背景，带细微边框
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Scene scene = new Scene(root, Color.TRANSPARENT);
        stage.setScene(scene);
    }

    /**
     * 更新进度和文字
     *
     * @param progress 0.0 到 1.0
     * @param status   状态描述
     */
    public void updateProgress(double progress, String status) {
        TaskRunner.runInFx(() -> {
            progressBar.setProgress(progress);
            statusLabel.setText(status);
        });
    }

    public void show() {
        stage.show();
        stage.centerOnScreen();
    }

    public void hide(Runnable onFinished) {
        stage.close();
        if (onFinished != null) onFinished.run();
    }
}