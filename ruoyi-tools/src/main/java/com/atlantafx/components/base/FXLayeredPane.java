package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * FXLayeredPane - 多层叠放容器组件（流式重构与防塌陷版）
 * 核心：完全继承并扩展 StackPane 的拓扑层级，提供运行时动态升降层、防溢出裁剪等生产级。
 */
public class FXLayeredPane extends StackPane implements IFXNode<FXLayeredPane> {

    /**
     * 默认构造函数
     * 核心修正：默认启用子节点边界物理裁剪，防止底层图层溢出震荡破坏全局排版。
     */
    public FXLayeredPane() {
        super();
        clipToBounds(true);
    }

    /**
     * 带初始节点集的快速工厂
     */
    public static FXLayeredPane create(Node... nodes) {
        return new FXLayeredPane().add(nodes);
    }

    public static FXLayeredPane create() {
        return new FXLayeredPane();
    }

    /**
     * 链式流 API：批量追加节点至最顶层
     */
    public FXLayeredPane add(Node... nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                if (node != null) {
                    getChildren().add(node);
                }
            }
        }
        return this;
    }

    /**
     * 链式流 API：将指定节点精准植入特定层级索引
     */
    public FXLayeredPane addAt(int index, Node node) {
        if (node == null) return this;
        int size = getChildren().size();
        // 防越界安全截断
        int safeIndex = Math.max(0, Math.min(index, size));
        getChildren().add(safeIndex, node);
        return this;
    }

    /**
     * 动态控制流：强行将某一节点提升至绝对最顶层展示
     */
    public FXLayeredPane bringToFront(Node node) {
        if (node != null && getChildren().contains(node)) {
            getChildren().remove(node);
            getChildren().add(node); // StackPane 尾部节点最后绘制，即最顶层
        }
        return this;
    }

    /**
     * 动态控制流：强行将某一节点压入绝对最底层垫底
     */
    public FXLayeredPane sendToBack(Node node) {
        if (node != null && getChildren().contains(node)) {
            getChildren().remove(node);
            getChildren().add(0, node); // 索引 0 先绘制，即最底层
        }
        return this;
    }

    /**
     * 链式流 API：一键开启或关闭物理边界裁剪
     */
    public FXLayeredPane clipToBounds(boolean clip) {
        if (clip) {
            Rectangle clipRect = new Rectangle();
            // 物理绑定自身尺寸，规避静态死锁
            clipRect.widthProperty().bind(widthProperty());
            clipRect.heightProperty().bind(heightProperty());
            setClip(clipRect);
        } else {
            setClip(null);
        }
        return this;
    }

    /**
     * 快捷预设风格：自适应半透明 HUD 蒙版层
     */
    public FXLayeredPane asHudOverlay() {
        return clipToBounds(true)
                .background("transparent")
                .padding(0);
    }

    /**
     * 快捷预设风格：现代化轻盈画布底板
     */
    public FXLayeredPane asCanvasContainer() {
        return clipToBounds(true)
                .styleCss("-fx-background-color: -color-bg-default; " +
                        "-fx-border-color: -color-border-default; " +
                        "-fx-border-width: 1px; ");
    }

    /* =========================================================================
     * 基础属性管线微调支持
     * ========================================================================= */
    public FXLayeredPane padding(int value) {
        setPadding(new Insets(value));
        return this;
    }

    public FXLayeredPane background(String color) {
        setStyle("-fx-background-color: " + color + ";");
        return this;
    }

    public int getLayerCount() {
        return getChildren().size();
    }

    public boolean isEmpty() {
        return getChildren().isEmpty();
    }

    public boolean contains(Node node) {
        return getChildren().contains(node);
    }

    public int indexOf(Node node) {
        return getChildren().indexOf(node);
    }

    public FXLayeredPane clearAll() {
        getChildren().clear();
        return this;
    }
}