package com.atlantafx.components.layout;

import atlantafx.base.theme.Styles;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;

import java.util.List;

/**
 * 自定义的标题栏，包含顶部导航条和进度条
 */
public class HeaderBar extends FXStackPane { // 改为继承 StackPane，方便进度条置顶/置底

    private final FXLabel titleLabel = FXLabel.create("首页");
    private final FXProgressBar progressBar = FXProgressBar.create();
    private final FXHBox customToolBar = FXHBox.create(10);
    private final FXVBox mainContainer = FXVBox.create(); // 存放原来的 navBar 内容
    private final FXScrollPane scrollPane = FXScrollPane.create(); // 新增滚动面板
    private final FXButton backButton = FXButton.create("")
            .icon(MaterialDesignA.ARROW_LEFT)
            .outline()
            .sm()
            .tooltip("返回上一页");

    public HeaderBar() {
        // 默认隐藏返回按钮（不占空间）
        backButton.setVisible(false);
        backButton.setManaged(false); // 关键：不占用布局空间

        // 绑定返回按钮的点击事件
        backButton.onAction(e -> handleBack());

        // 1. 顶部导航条 HBox
        FXHBox navBar = FXHBox.create(20).align(Pos.CENTER_LEFT).padding(0, 20, 0, 20).height(45).mxHeight(45);

        // 防止标题被挤压
        titleLabel.stylesClass(Styles.TITLE_4).setMinWidth(Region.USE_PREF_SIZE);

        Region spacer = FXRegion.create();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        customToolBar.align(Pos.CENTER_LEFT);
        // 关键：给 customToolBar 设限制，防止它撑开 Header

        // 配置 ScrollPane
        scrollPane.content(customToolBar).noScrollBars().fitToHeight().pannable(true).stylesClass("no-scrollbar-scroll-pane");

        navBar.add(backButton, titleLabel, spacer, scrollPane);

        // 2. 进度条配置
        progressBar.height(2).visible(false).progress(ProgressBar.INDETERMINATE_PROGRESS).stylesClass("header-progress-bar").setMaxWidth(Double.MAX_VALUE);

        // 滚轮转横向滚动 (体验优化)
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() != 0) {
                scrollPane.setHvalue(scrollPane.getHvalue() - e.getDeltaY() / customToolBar.getWidth());
                e.consume();
            }
        });

        // 3. 布局组装
        mainContainer.add(navBar);

        // 将主容器和进度条放入 StackPane
        this.add(mainContainer, progressBar);
        // 核心修正：使用 StackPane 的对齐方式，将进度条强行固定在底部边缘
        progressBar.stackPane(Pos.BOTTOM_CENTER);

        this.stylesClass("header-bar").styleCss("-fx-background-color: -color-bg-default; -fx-border-color: -color-border-muted; -fx-border-width: 0 0 1 0;");
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * 处理返回操作
     */
    private void handleBack() {
        AppContext.navigateBackPage();
    }

    /**
     * 更新返回按钮的可见性
     */
    public void updateBackButtonVisibility() {
        boolean shouldShow = AppContext.shouldShowBackButton();
        backButton.setVisible(shouldShow);
        backButton.setManaged(shouldShow); // 关键：控制是否占用空间
    }

    public void setCustomTools(List<Node> tools) {
        customToolBar.clear();
        if (tools != null) {
            // 修正：限制注入按钮的高度，防止它们撑开 Header
            for (Node tool : tools) {
                if (tool instanceof Button btn) {
                    // 强制统一按钮样式，避免不同页面的按钮大小不一撑坏 Header
                    btn.setMaxHeight(28);
                    btn.getStyleClass().add(Styles.SMALL); // 使用 AtlantaFX 的小尺寸样式
                } else if (tool instanceof Region r) {
                    r.setMaxHeight(26);
                }

            }
            customToolBar.getChildren().addAll(tools);
        }
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}