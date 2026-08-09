package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;

/**
 * FXFloatingToolBar - 现代化全局可拖拽悬浮工具栏组件
 * 核心：完全基于鼠标相对位移差锁死技术，支持全平滑自由拖拽、防越界碰撞拦截、以及 AtlantaFX 悬浮发光质感。
 */
public class FXFloatingToolBar extends StackPane implements IFXNode<FXFloatingToolBar> {

    private final FXToolBar innerToolBar; // 内部持有的轻量链式工具栏躯干
    private final Button dragHandle;       // 物理拖拽锚点（手柄）

    // 运行时鼠标坐标缓冲记忆体
    private double mouseAnchorX;
    private double mouseAnchorY;

    private FXFloatingToolBar() {
        super();

        // 1. 初始化内部轻量级链式工具栏
        this.innerToolBar = FXToolBar.create().flat();

        // 2. 初始化核心：物理拖拽手柄（使用经典纯文本或图标，赋予手指悬停光标）
        this.dragHandle = FXButton.create("").icon(MaterialDesignD.DRAG_VERTICAL).circle().flat();
        this.dragHandle.setCursor(Cursor.MOVE);
        this.dragHandle.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");

        // 将手柄排在工具栏的最前端作为默认拖拽触点
        this.innerToolBar.add(dragHandle).separator();

        // 3. 为整体悬浮舱体施加 AtlantaFX 顶层卡片物理特效
        this.getChildren().add(innerToolBar);
        this.setPadding(new Insets(0));
        this.setStyle(
                "-fx-background-color: -color-bg-overlay;" + // 采用层叠覆盖色底衬
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: -color-border-default;" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 8px;"
        );
        // 施加硬件深度立体阴影，营造悬浮跃出感
        this.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.22)));

        // 4. 激活物理位移差捕获监听器
        initDragAndDropEvents();
    }

    public static FXFloatingToolBar create() {
        return new FXFloatingToolBar();
    }

    /**
     * 向悬浮栏追加业务交互节点
     */
    public FXFloatingToolBar add(Node... nodes) {
        this.innerToolBar.add(nodes);
        return this;
    }

    /**
     * 插入物理隔离线
     */
    public FXFloatingToolBar separator() {
        this.innerToolBar.separator();
        return this;
    }

    /**
     * 转向为横向悬浮长条
     */
    public FXFloatingToolBar horizontal() {
        this.innerToolBar.horizontal();
        return this;
    }

    /**
     * 转向为纵向悬浮魔方
     */
    public FXFloatingToolBar vertical() {
        this.innerToolBar.vertical();
        return this;
    }

    /**
     * 核心：执行基于绝对像素差值的无缝拖拽与碰撞隔离边界安全换算
     */
    private void initDragAndDropEvents() {
        // 鼠标按下：锁死局域相对偏移量
        this.dragHandle.setOnMousePressed(e -> {
            mouseAnchorX = e.getSceneX() - this.getLayoutX();
            mouseAnchorY = e.getSceneY() - this.getLayoutY();
            this.toFront(); // 强行拉回视图层级最高清算位
            e.consume();
        });

        // 鼠标拖动：像素级跟进并执行严苛的父视口防越界越轨拦截
        this.dragHandle.setOnMouseDragged(e -> {
            Pane parentContainer = (Pane) this.getParent();
            if (parentContainer == null) return;

            // 计算出理想物理投影坐标
            double targetX = e.getSceneX() - mouseAnchorX;
            double targetY = e.getSceneY() - mouseAnchorY;

            // 核心：执行越界碰撞安全阻断，防止工具栏被拖出主窗体导致无法拉回
            double minX = 0;
            double minY = 0;
            double maxX = parentContainer.getWidth() - this.getBoundsInLocal().getWidth();
            double maxY = parentContainer.getHeight() - this.getBoundsInLocal().getHeight();

            // 极限约束夹逼换算
            if (targetX < minX) targetX = minX;
            if (targetX > maxX) targetX = maxX;
            if (targetY < minY) targetY = minY;
            if (targetY > maxY) targetY = maxY;

            // 正式驱动物理位移更改
            this.setLayoutX(targetX);
            this.setLayoutY(targetY);
            e.consume();
        });
    }
}