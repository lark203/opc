package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import com.atlantafx.util.TaskRunner;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * FXOnboarding - 高精无损操作指引组件
 * 修正：彻底修复了因组件未完成 Layout 测绘导致的“零值塌陷”与坐标二次漂移硬伤。
 */
public class FXOnboarding extends StackPane implements IFXNode<FXOnboarding> {

    private final List<OnboardingStep> steps = new ArrayList<>();
    private int currentStepIndex = 0;
    private boolean showing = false;
    private Consumer<Integer> onStepChange;
    private Runnable onComplete;
    private Runnable onSkip;

    private final Region maskOverlay;
    private final VBox tooltipCard;
    private final Label titleLabel;
    private final Label contentLabel;
    private final Button prevButton;
    private final Button nextButton;
    private final Button skipButton;

    public FXOnboarding() {
        super();

        // 1. 全屏半透明阻断暗色遮罩
        this.maskOverlay = new Region();
        this.maskOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");

        // 2. 现代化自适应发光气泡卡片
        this.tooltipCard = new VBox(12);
        this.tooltipCard.setPickOnBounds(true);
        // 核心修正：气泡必须脱离流式排版控制
        this.tooltipCard.setManaged(false);
        this.tooltipCard.setStyle(
                "-fx-background-color: -color-bg-overlay;" +
                        "-fx-border-color: -color-accent-emphasis;" +
                        "-fx-border-width: 1.5px;"
        );
        this.tooltipCard.setPadding(new Insets(16));
        this.tooltipCard.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.25), 20, 0.3, 0, 8));

        this.titleLabel = new Label();
        this.titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        this.contentLabel = new Label();
        this.contentLabel.setWrapText(true);
        this.contentLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");

        // 3. 动作控制纽
        this.prevButton = new Button("上一步");
        this.prevButton.getStyleClass().add(Styles.BUTTON_OUTLINED);

        this.nextButton = new Button("下一步");
        this.nextButton.getStyleClass().addAll(Styles.ACCENT);

        this.skipButton = new Button("跳过");
        this.skipButton.getStyleClass().addAll(Styles.DANGER);

        HBox actionBar = new HBox(10, skipButton, new Region(), prevButton, nextButton);
        HBox.setHgrow(actionBar.getChildren().get(1), Priority.ALWAYS);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        this.tooltipCard.getChildren().addAll(titleLabel, contentLabel, actionBar);

        this.prevButton.setOnAction(e -> previousStep());
        this.nextButton.setOnAction(e -> nextStep());
        this.skipButton.setOnAction(e -> handleSkip());

        // 强制设为绝对独立自由悬浮图层，防止干扰普通业务排版
        setManaged(false);
        setVisible(false);
    }

    public static FXOnboarding create() {
        return new FXOnboarding();
    }

    public FXOnboarding addStep(Node target, String title, String content, Pos position) {
        this.steps.add(new OnboardingStep(target, title, content, position));
        return this;
    }

    public void start() {
        if (steps.isEmpty()) return;
        this.showing = true;
        this.currentStepIndex = 0;

        if (getParent() instanceof Pane) {
            Pane parentPane = (Pane) getParent();
            // 全屏物理绑定 facts
            prefWidthProperty().bind(parentPane.widthProperty());
            prefHeightProperty().bind(parentPane.heightProperty());
        }

        getChildren().clear();
        getChildren().addAll(maskOverlay, tooltipCard);
        setVisible(true);
        updateStepView();
    }

    public void nextStep() {
        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            updateStepView();
            if (onStepChange != null) onStepChange.accept(currentStepIndex);
        } else {
            handleComplete();
        }
    }

    public void previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            updateStepView();
            if (onStepChange != null) onStepChange.accept(currentStepIndex);
        }
    }

    /**
     * 核心修正：强制数据刷写与高精物理坐标逆向投影映射
     */
    private void updateStepView() {
        if (currentStepIndex < 0 || currentStepIndex >= steps.size()) return;

        OnboardingStep step = steps.get(currentStepIndex);

        // 强解文本死锁：清除旧缓存，直接推入全新数据 facts
        titleLabel.setText(step.title);
        contentLabel.setText(step.content);

        prevButton.setDisable(currentStepIndex == 0);
        if (currentStepIndex == steps.size() - 1) {
            nextButton.setText("完成引导");
            nextButton.getStyleClass().removeAll(Styles.ACCENT);
            nextButton.getStyleClass().add(Styles.SUCCESS);
        } else {
            nextButton.setText("下一步");
            nextButton.getStyleClass().removeAll(Styles.SUCCESS);
            nextButton.getStyleClass().add(Styles.ACCENT);
        }

        // 核心：必须等待 JavaFX UI 线程将上一步的排版清算完毕，再行提取真实物理微调坐标
        TaskRunner.runInFx(() -> {
            locateTooltipHighPrecision(step.target, tooltipCard, step.position);
            playSmoothTransition();
        });
    }

    /**
     * 生产级高精物理坐标重算管线
     */
    private void locateTooltipHighPrecision(Node target, VBox tooltip, Pos position) {
        // 固定气泡的基准测试边界尺寸
        double tw = 340;
        double th = 150;
        tooltip.setPrefWidth(tw);
        tooltip.setPrefHeight(th);

        if (target == null || target.getScene() == null) {
            // 兜底：如果节点彻底真空，将其抛至画布正中央
            tooltip.setLayoutX((getWidth() - tw) / 2);
            tooltip.setLayoutY((getHeight() - th) / 2);
            return;
        }

        // 1. 获取目标节点在 Scene 绝对主窗口下的物理坐标 facts
        Bounds sceneBounds = target.localToScene(target.getBoundsInLocal());

        // 2. 核心纠伤：将 Scene 绝对坐标，逆向投影转换到当前 FXOnboarding 容器的局部空间中（抹平二次偏移）
        Bounds localBounds = this.sceneToLocal(sceneBounds);

        double tx = localBounds.getMinX();
        double ty = localBounds.getMinY();
        double targetW = localBounds.getWidth();
        double targetH = localBounds.getHeight();

        double finalX = tx;
        double finalY = ty;

        // 3. 根据方位语义计算边缘间隙（附加 14px 物理缓冲防重叠隔离带）
        switch (position) {
            case TOP_LEFT:
                finalX = tx;
                finalY = ty - th - 14;
                break;
            case TOP_RIGHT:
                finalX = tx + targetW - tw;
                finalY = ty - th - 14;
                break;
            case BOTTOM_LEFT:
                finalX = tx;
                finalY = ty + targetH + 14;
                break;
            case BOTTOM_RIGHT:
                finalX = tx + targetW - tw;
                finalY = ty + targetH + 14;
                break;
            case CENTER:
            default:
                finalX = tx + (targetW - tw) / 2;
                finalY = ty + (targetH - th) / 2;
                break;
        }

        // 4. 边界溢出防御：防止气泡框飘出屏幕左侧或顶侧边界
        if (finalX < 10) finalX = 10;
        if (finalY < 10) finalY = 10;
        if (finalX + tw > getWidth()) finalX = getWidth() - tw - 10;
        if (finalY + th > getHeight()) finalY = getHeight() - th - 10;

        // 执行绝对物理摆放
        tooltip.setLayoutX(finalX);
        tooltip.setLayoutY(finalY);
    }

    private void playSmoothTransition() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), tooltipCard);
        fadeIn.setFromValue(0.2);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(180), tooltipCard);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition anim = new ParallelTransition(fadeIn, scaleIn);
        anim.play();
    }

    private void handleSkip() {
        close();
        if (onSkip != null) onSkip.run();
    }

    private void handleComplete() {
        close();
        if (onComplete != null) onComplete.run();
    }

    private void close() {
        this.showing = false;
        setVisible(false);
        getChildren().clear();
    }

    public FXOnboarding onStepChange(Consumer<Integer> callback) {
        this.onStepChange = callback;
        return this;
    }

    public FXOnboarding onComplete(Runnable callback) {
        this.onComplete = callback;
        return this;
    }

    public FXOnboarding onSkip(Runnable callback) {
        this.onSkip = callback;
        return this;
    }

    public static class OnboardingStep {
        final Node target;
        final String title;
        final String content;
        final Pos position;

        public OnboardingStep(Node target, String title, String content, Pos position) {
            this.target = target;
            this.title = title;
            this.content = content;
            this.position = position != null ? position : Pos.CENTER;
        }
    }
}