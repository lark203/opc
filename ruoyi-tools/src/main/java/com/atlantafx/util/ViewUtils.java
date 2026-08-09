package com.atlantafx.util;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import com.atlantafx.core.constant.NotificationLevel;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;

import java.util.Objects;

public class ViewUtils {

    /**
     * 同时显示的最大通知数量，超出时自动移除最早的
     */
    private static final int MAX_NOTIFICATIONS = 6;

    /**
     * 增强版通知方法：支持级别切换及错误手动关闭
     */
    public static void showNotification(VBox notificationHolder, String message, NotificationLevel level) {
        // 1. 根据级别选择图标和样式类
        FontIcon icon;
        String styleClass;
        boolean autoHide = true;

        switch (level) {
            case WARNING -> {
                icon = new FontIcon(MaterialDesignA.ALERT_CIRCLE_OUTLINE);
                styleClass = Styles.WARNING;
            }
            case ERROR -> {
                icon = new FontIcon(MaterialDesignC.CLOSE_CIRCLE_OUTLINE);
                styleClass = Styles.DANGER;
                autoHide = false; // 错误信息不自动消失
            }
            case INFO -> {
                icon = new FontIcon(MaterialDesignI.INFORMATION_OUTLINE);
                styleClass = Styles.ACCENT;
            }
            default -> { // SUCCESS
                icon = new FontIcon(MaterialDesignC.CHECK_CIRCLE_OUTLINE);
                styleClass = Styles.SUCCESS;
            }
        }

        var notification = new Notification(message, icon);
        notification.getStyleClass().addAll(styleClass, Styles.ELEVATED_1);
        notification.setPrefWidth(350); // 略微加宽

        // 2. 关键：如果是错误或需要手动关闭，设置 onClose 回调以显示右上角关闭按钮
        if (!autoHide) {
            notification.setOnClose(e -> {
                var exitAnim = fadeOutAndRemove(notificationHolder, notification);
                exitAnim.play();
            });
        }

        // 3. 限制通知数量，超出时淡出移除最早的
        while (notificationHolder.getChildren().size() >= MAX_NOTIFICATIONS) {
            notificationHolder.getChildren().removeLast();
        }

        // 4. 执行入场动画 (沿用您之前的位移+透明度动画)
        notification.setTranslateX(20);
        notification.setOpacity(0);
        notificationHolder.getChildren().addFirst(notification);

        ParallelTransition entryAnim = createEntryAnimation(notification);
        entryAnim.play();

        // 4. 自动消失逻辑（仅限非错误消息）
        if (autoHide) {
            PauseTransition delay = new PauseTransition(Duration.seconds(4));
            delay.setOnFinished(e -> fadeOutAndRemove(notificationHolder, notification).play());
            delay.play();
        }
    }

    /**
     * 创建一个淡入并显示的动画 - 添加消息通知
     *
     * @param node
     * @return
     */
    private static ParallelTransition createEntryAnimation(Notification node) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), node);
        slideIn.setToX(0);

        return new ParallelTransition(fadeIn, slideIn);
    }

    /**
     * 创建一个淡出并删除的动画 - 移除消息通知
     *
     * @param holder
     * @param node
     * @return
     */
    private static FadeTransition fadeOutAndRemove(VBox holder, Notification node) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), node);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(finish -> holder.getChildren().remove(node));
        return fadeOut;
    }

    /**
     * 设置 Stage 的图标
     *
     * @param stage
     */
    public static void setStageIcon(Stage stage) {
        // 建议同时提供 16x16, 32x32, 64x64 等尺寸，系统会自动选择最合适的
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(ViewUtils.class.getResourceAsStream("/assets/icons/icon-16.png"))),
                new Image(Objects.requireNonNull(ViewUtils.class.getResourceAsStream("/assets/icons/icon-32.png"))),
                new Image(Objects.requireNonNull(ViewUtils.class.getResourceAsStream("/assets/icons/icon-64.png")))
        );
    }
}
