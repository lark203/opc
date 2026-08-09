package com.atlantafx.components.functional;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXCustomTextField;
import com.atlantafx.components.base.FXListView;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.core.view.ViewFactory;
import com.atlantafx.core.view.ViewFactory.PageMeta;
import com.atlantafx.util.TaskRunner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * FXMenuSearchDialog - 全局菜单智能检索中心
 * 核心：快捷键触发，秒级检索，全键盘无缝上下选中并回车跳转
 */
public class FXMenuSearchDialog extends Stage {

    private final FXCustomTextField searchField;
    private final FXListView<PageMeta> resultListView;
    private final FilteredList<PageMeta> filteredItems;

    public FXMenuSearchDialog(Stage owner) {
        super();
        initOwner(owner);
        initStyle(StageStyle.TRANSPARENT);
        initModality(Modality.APPLICATION_MODAL); // 锁定模态防御

        // 1. 从 ViewFactory 抓取全量注册菜单，并剥离隐藏与目录节点
        ObservableList<PageMeta> allMenus = FXCollections.observableArrayList(ViewFactory.getMenuItems());
        this.filteredItems = new FilteredList<>(allMenus, page -> !page.isHidden() && !ViewFactory.isFolder(page.name()));

        // 2. 输入检索框装配
        this.searchField = FXCustomTextField.create("输入菜单名称进行检索... (↑↓切换, Enter跳转, Esc退出)").searchStyle();

        // 3. 结果列表装配与高保真渲染
        this.resultListView = FXListView.<PageMeta>create(this.filteredItems).striped();
        this.resultListView.setPrefHeight(280);
        this.resultListView.cellTextFactory(pageMeta -> (pageMeta.parentName() != null && !pageMeta.parentName().isBlank())
                ? pageMeta.parentName() + " > " + pageMeta.name()
                : pageMeta.name());

        // 4. 面板纵向拓扑布局
        VBox rootLayout = FXVBox.create(4).add(searchField, resultListView).padding(12).background("transparent").shadow(3);

        Scene scene = new Scene(rootLayout, 520, 380);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // =========================================================================
        // 核心修正 facts：利用 setOnShown 攻克初次弹出不居中的硬伤
        // =========================================================================
        this.setOnShown(event -> {
            if (getOwner() instanceof Stage primaryStage) {
                // 此时 getWidth() 与 getHeight() 已经获得了操作系统的真实排版物理，不再为 0
                double centerX = primaryStage.getX() + (primaryStage.getWidth() - this.getWidth()) / 2;
                double centerY = primaryStage.getY() + (primaryStage.getHeight() - this.getHeight()) / 4;

                this.setX(centerX);
                this.setY(centerY);
            }
        });

        // 5. 绑定核心交互管线
        setupInteractionPipeline();
    }

    /**
     * 核心：智能动态合并键入、焦点流转与跳转清算
     */
    private void setupInteractionPipeline() {
        // 监听文本框变化驱动内存快速重构过滤断言
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filteredItems.setPredicate(page -> {
                if (page.isHidden() || ViewFactory.isFolder(page.name())) {
                    return false;
                }
                if (newText == null || newText.isBlank()) {
                    return true;
                }
                String lowerFilter = newText.trim().toLowerCase();
                return page.name().toLowerCase().contains(lowerFilter) ||
                        page.title().toLowerCase().contains(lowerFilter) ||
                        (page.id() != null && page.id().toLowerCase().contains(lowerFilter));
            });
            // 自动高亮第一项
            if (!filteredItems.isEmpty()) {
                resultListView.getSelectionModel().selectFirst();
            }
        });

        // 接管输入框键盘事件 facts（防止焦点高频切换断层）
        searchField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.DOWN) {
                int currentIndex = resultListView.getSelectionModel().getSelectedIndex();
                if (currentIndex < filteredItems.size() - 1) {
                    resultListView.getSelectionModel().select(currentIndex + 1);
                    resultListView.scrollTo(currentIndex + 1);
                }
                event.consume(); // 拦截
            } else if (code == KeyCode.UP) {
                int currentIndex = resultListView.getSelectionModel().getSelectedIndex();
                if (currentIndex > 0) {
                    resultListView.getSelectionModel().select(currentIndex - 1);
                    resultListView.scrollTo(currentIndex - 1);
                }
                event.consume();
            } else if (code == KeyCode.ENTER) {
                executeNavigation();
                event.consume();
            } else if (code == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });

        // 支持列表直接双击跳转
        resultListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                executeNavigation();
            }
        });
    }

    /**
     * 核心：清算当前选中项并调取路由执行跨页跳转
     */
    private void executeNavigation() {
        PageMeta selectedPage = resultListView.getSelectionModel().getSelectedItem();
        if (selectedPage != null) {
            close(); // 优先隐退搜索框，防止视窗焦点死锁

            // 核心安全调用：由于 AppContext 的 NavigationService 是静态委托代理，直接下发路由指令
            TaskRunner.runInFx(() -> {
                // 通过您的项目框架总线进行无损跳转
                AppContext.navigateTo(selectedPage.id(), false);
            });
        }
    }

    /**
     * 唤醒重洗展现
     */
    public void showAndReset() {
        searchField.clear();
        if (!filteredItems.isEmpty()) {
            resultListView.getSelectionModel().selectFirst();
        }
        show();
        searchField.requestFocus(); // 强制锚定输入焦点
    }
}