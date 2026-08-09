package com.atlantafx.core.manager;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXStackPane;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * 模态管理器，用于显示和隐藏模态对话框。
 */
public class ModalManager {

    /**
     * 隐藏模态对话框。
     *
     * @param mask 模态遮罩
     */
    public static void hide(FXStackPane mask) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), mask);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> AppContext.getMainLayout().getChildren().remove(mask));
        ft.play();
    }

    /**
     * 显示模态对话框。
     *
     * @param node 模态对话框
     * @return
     */
    public static FXStackPane show(Node node) {
        // 创建模态遮罩 (ModalBox 核心逻辑)
        FXStackPane mask = FXStackPane.create(node);
        mask.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); // 半透明遮罩
        AppContext.getMainLayout().getChildren().add(mask);

        // 简单的入场动画
        FadeTransition ft = new FadeTransition(Duration.millis(200), mask);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        return mask;
    }
}