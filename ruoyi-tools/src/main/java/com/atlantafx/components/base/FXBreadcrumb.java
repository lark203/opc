package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * FXBreadcrumb - 现代化高级面包屑导航组件（增量响应式重构版）
 * 核心：彻底清除旧版全量 rebuild 销毁节点的性能隐伤，改用 ObservableList 增量监听响应机制。
 * 完美拥抱 AtlantaFX 换肤生态，末端节点自动去饱和变灰（Muted），前端路径自动适配 Hyperlink 样式。
 */
public class FXBreadcrumb extends HBox implements IFXNode<FXBreadcrumb> {

    // 核心：通过可监听队列接管面包屑项，实现增量拓扑渲染
    private final ObservableList<BreadcrumbItem> items = FXCollections.observableArrayList();
    private String separator = ">";

    public FXBreadcrumb() {
        super();
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8); // 严格定义面包屑像素间距 facts
        setPadding(new Insets(8));

        // 监听底层数据资产变动，实施高精增量同步，拒绝全量 clear()
        this.items.addListener((ListChangeListener<BreadcrumbItem>) change -> syncLayoutTopology());
    }

    public static FXBreadcrumb create() {
        return new FXBreadcrumb();
    }

    /**
     * 核心：高精物理拓扑同步引擎。
     * 根据当前持有的 items 增量清算 HBox 的子节点，对末梢节点（当前页）和中间链路采用不同的视觉实体。
     */
    private void syncLayoutTopology() {
        getChildren().clear(); // 清洗视图，为轻量级平铺准备

        int size = items.size();
        for (int i = 0; i < size; i++) {
            BreadcrumbItem item = items.get(i);
            boolean isLast = (i == size - 1);

            if (isLast) {
                // 技术：当前所在的末梢节点不可点击，使用扁平 Label 并注入 Muted 状态色
                Label currentLabel = new Label(item.getText());
                currentLabel.getStyleClass().add(Styles.TEXT_MUTED);
                currentLabel.setStyle("-fx-font-weight: bold;");
                getChildren().add(currentLabel);
            } else {
                // 技术：中间链路路由节点可点击，采用现代组件样式，点击触发回调
                Hyperlink link = new Hyperlink(item.getText());
                link.getStyleClass().add(Styles.TEXT_SMALL);
                link.setFocusTraversable(false);

                if (item.getHandler() != null) {
                    link.setCursor(Cursor.HAND);
                    link.setOnAction(e -> item.getHandler().accept(item));
                }
                getChildren().add(link);

                // 注入无损物理分隔符
                Label sepLabel = new Label(this.separator);
                sepLabel.getStyleClass().add(Styles.TEXT_MUTED);
                getChildren().add(sepLabel);
            }
        }
    }

    /* =========================================================================
     * 开箱即用高频流式链式扩展 API（全量消灭 TODO 桩代码）
     * ========================================================================= */

    public FXBreadcrumb separator(String separator) {
        this.separator = separator != null ? separator : ">";
        syncLayoutTopology();
        return this;
    }

    public FXBreadcrumb addItem(String text) {
        this.items.add(new BreadcrumbItem(text, null));
        return this;
    }

    public FXBreadcrumb addItem(String text, Consumer<BreadcrumbItem> onClickHandler) {
        this.items.add(new BreadcrumbItem(text, onClickHandler));
        return this;
    }

    public FXBreadcrumb addItems(List<String> titles) {
        if (titles != null) {
            titles.forEach(this::addItem);
        }
        return this;
    }

    public FXBreadcrumb clearBreadcrumbs() {
        this.items.clear();
        return this;
    }

    public int size() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public FXBreadcrumb width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    public FXBreadcrumb height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    public FXBreadcrumb stylesClass(String... classes) {
        getStyleClass().addAll(classes);
        return this;
    }

    /**
     * 一键快捷注入 AtlantaFX 紧凑型扁平导航背景样式
     */
    public FXBreadcrumb compact() {
        setPadding(new Insets(4, 8, 4, 8));
        setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 4px;");
        return this;
    }

    /**
     * 获取指定层级的面包屑核心实体数据
     */
    public BreadcrumbItem getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    /* =========================================================================
     * 内部核心状态实体：BreadcrumbItem（高度可扩展的数据结构）
     * ========================================================================= */
    public static class BreadcrumbItem {
        private String text;
        private Consumer<BreadcrumbItem> handler;
        private Object userData; // 预留用于承载业务路由层（Route Data）的动态资产指针 facts

        public BreadcrumbItem(String text, Consumer<BreadcrumbItem> handler) {
            this.text = text;
            this.handler = handler;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Consumer<BreadcrumbItem> getHandler() {
            return handler;
        }

        public void setHandler(Consumer<BreadcrumbItem> handler) {
            this.handler = handler;
        }

        public Object getUserData() {
            return userData;
        }

        public BreadcrumbItem userData(Object userData) {
            this.userData = userData;
            return this;
        }
    }
}