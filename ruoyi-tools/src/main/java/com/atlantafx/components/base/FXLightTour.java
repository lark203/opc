package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import com.atlantafx.util.TaskRunner;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * FXLightTour - 轻量级绝对定位框选引导组件（CSS 颜色修正版）
 * 彻底解决了 Color.web() 无法解析 CSS 变量的崩溃硬伤。
 * 核心：全面切换为基于样式表的原生变量绑定，配合全覆盖 AnchorPane 实施高亮红框投影。
 */
public class FXLightTour extends AnchorPane implements IFXNode<FXLightTour> {

    private static class TourStep {
        final Node targetNode;
        final String title;
        final String description;

        TourStep(Node targetNode, String title, String description) {
            this.targetNode = targetNode;
            this.title = title;
            this.description = description;
        }
    }

    private final List<TourStep> tourSteps = new ArrayList<>();
    private int currentStepIndex = 0;

    private final Pane highlightFocusBorder;
    private final VBox infoBubble;
    private final Label titleLabel;
    private final Label descLabel;
    private final Button actionBtn;
    private Pane targetRoot;

    private FXLightTour() {
        super();

        // 1. 设置半透明黑色遮罩滤镜（45% 密度），提供柔和的游戏暗场仪式感
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");

        // 2. 初始化霓虹高亮框：直接将 -color-accent-emphasis 写入 setStyle，规避 Color.web() 的崩溃陷阱
        this.highlightFocusBorder = new Pane();
        this.highlightFocusBorder.setMouseTransparent(true); // 物理穿透：高亮框本体不拦截任何鼠标事件
        this.highlightFocusBorder.setStyle(
                "-fx-border-color: -color-accent-emphasis;" + // 动态解析 AtlantaFX 的高亮主色调
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 6px;" +
                        "-fx-background-color: transparent;" // 中央纯透明，完美露出底层复杂的文本和高交互组件
        );

        // 使用标准的 JavaFX Color 常量为 DropShadow 提供光圈特效
        this.highlightFocusBorder.setEffect(new DropShadow(10, Color.rgb(33, 150, 243, 0.6)));

        // 3. 初始化顶层独立引导气泡面板
        this.titleLabel = new Label();
        this.titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: -color-fg-default;");

        this.descLabel = new Label();
        this.descLabel.setWrapText(true);
        this.descLabel.setMaxWidth(260);
        this.descLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px; -fx-line-spacing: 3px;");

        this.actionBtn = new Button("我知道了");
        this.actionBtn.getStyleClass().addAll(Styles.SMALL, Styles.ACCENT);
        this.actionBtn.setOnAction(e -> nextStep());

        this.infoBubble = new VBox(12, titleLabel, descLabel, actionBtn);
        this.infoBubble.setPadding(new Insets(16, 20, 16, 20));
        this.infoBubble.setStyle(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: -color-border-default;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;"
        );
        this.infoBubble.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.25)));

        // 4. 将霓虹框与操作气泡装入本 AnchorPane 绝对定位舞台中
        getChildren().addAll(highlightFocusBorder, infoBubble);

        // 全量拦截事件，确保新手引导独占性
        this.setOnMouseClicked(e -> e.consume());
        this.setOnMousePressed(e -> e.consume());
    }

    public static FXLightTour create() {
        return new FXLightTour();
    }

    public FXLightTour addStep(Node targetNode, String title, String description) {
        if (targetNode != null) {
            this.tourSteps.add(new TourStep(targetNode, title, description));
        }
        return this;
    }

    /**
     * 自动上浮劫持：逆向追溯获取当前主窗口的最顶层全局壳
     */
    public void start() {
        if (tourSteps.isEmpty()) return;
        this.currentStepIndex = 0;

        Node firstTarget = tourSteps.get(0).targetNode;
        Scene scene = firstTarget.getScene();
        if (scene == null) {
            TaskRunner.runInFx(this::start);
            return;
        }

        Parent sceneRoot = scene.getRoot();
        if (sceneRoot instanceof Pane) {
            this.targetRoot = (Pane) sceneRoot;
        } else {
            if (scene.getWindow().getScene().getRoot() instanceof Pane) {
                this.targetRoot = (Pane) scene.getWindow().getScene().getRoot();
            } else {
                return;
            }
        }

        // 双向属性绑定，确保大窗口拉伸时黑幕面积像素级实时跟进
        this.prefWidthProperty().bind(targetRoot.widthProperty());
        this.prefHeightProperty().bind(targetRoot.heightProperty());

        if (!targetRoot.getChildren().contains(this)) {
            targetRoot.getChildren().add(this);
        }

        renderCurrentStep();
    }

    private void nextStep() {
        if (currentStepIndex < tourSteps.size() - 1) {
            currentStepIndex++;
            renderCurrentStep();
        } else {
            this.prefWidthProperty().unbind();
            this.prefHeightProperty().unbind();
            if (targetRoot != null) {
                targetRoot.getChildren().remove(this);
            }
        }
    }

    /**
     * 核心：执行轻量框选坐标无缝投影
     */
    private void renderCurrentStep() {
        TourStep step = tourSteps.get(currentStepIndex);

        if (currentStepIndex == tourSteps.size() - 1) {
            actionBtn.setText("完成引导");
            actionBtn.getStyleClass().remove(Styles.ACCENT);
            if (!actionBtn.getStyleClass().contains(Styles.SUCCESS)) {
                actionBtn.getStyleClass().add(Styles.SUCCESS);
            }
        } else {
            actionBtn.setText("下一步 (" + (currentStepIndex + 1) + "/" + tourSteps.size() + ")");
            actionBtn.getStyleClass().remove(Styles.SUCCESS);
            if (!actionBtn.getStyleClass().contains(Styles.ACCENT)) {
                actionBtn.getStyleClass().add(Styles.ACCENT);
            }
        }

        Scene scene = step.targetNode.getScene();
        if (scene == null || targetRoot == null) return;

        // 1. 获取目标节点在窗口中的绝对物理边界盒，并自动纠正父级 ScrollPane 产生的局部隐藏切片
        Bounds targetBoundsInScene = step.targetNode.localToScene(step.targetNode.getBoundsInLocal());
        Parent parent = step.targetNode.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) parent;
                Bounds scrollBoundsInScene = scrollPane.localToScene(scrollPane.getBoundsInLocal());
                targetBoundsInScene = intersectBounds(targetBoundsInScene, scrollBoundsInScene);
            }
            parent = parent.getParent();
        }

        // 2. 将全局边界转译为当前 AnchorPane 的一比一局域相对坐标
        Bounds boundsInTour = this.sceneToLocal(targetBoundsInScene);
        double targetX = boundsInTour.getMinX();
        double targetY = boundsInTour.getMinY();
        double targetW = boundsInTour.getWidth();
        double targetH = boundsInTour.getHeight();

        if (targetW <= 2) targetW = step.targetNode.getBoundsInLocal().getWidth();
        if (targetH <= 2) targetH = step.targetNode.getBoundsInLocal().getHeight();

        // 3. 核心：运用纯净的 AnchorPane 绝对定位更新霓虹高亮框
        // 微微向外延展 4 像素以形成精緻的间隙呼吸感
        AnchorPane.setLeftAnchor(highlightFocusBorder, targetX - 4);
        AnchorPane.setTopAnchor(highlightFocusBorder, targetY - 4);
        highlightFocusBorder.setPrefSize(targetW + 8, targetH + 8);

        // 4. 定位提示气泡面板
        titleLabel.setText(step.title);
        descLabel.setText(step.description);

        double bubbleX = targetX + (targetW / 2) - 150;
        double bubbleY = targetY + targetH + 16;

        // 防视口越界逆向反转安全机制
        if (bubbleY + 180 > targetRoot.getHeight()) {
            bubbleY = targetY - 185;
        }
        if (bubbleX < 15) bubbleX = 15;
        if (bubbleX + 300 > targetRoot.getWidth()) bubbleX = targetRoot.getWidth() - 315;

        AnchorPane.setLeftAnchor(infoBubble, bubbleX);
        AnchorPane.setTopAnchor(infoBubble, bubbleY);

        // 5. 强行拉回图形管线的最顶层，激活操作焦点
        TaskRunner.runInFx(() -> {
            this.toFront();
            infoBubble.toFront();
            actionBtn.requestFocus();
        });
    }

    private Bounds intersectBounds(Bounds b1, Bounds b2) {
        double minX = Math.max(b1.getMinX(), b2.getMinX());
        double minY = Math.max(b1.getMinY(), b2.getMinY());
        double maxX = Math.min(b1.getMaxX(), b2.getMaxX());
        double maxY = Math.min(b1.getMaxY(), b2.getMaxY());

        if (minX < maxX && minY < maxY) {
            return new javafx.geometry.BoundingBox(minX, minY, maxX - minX, maxY - minY);
        }
        return b1;
    }
}