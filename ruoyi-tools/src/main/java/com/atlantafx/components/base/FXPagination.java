package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

/**
 * FXPagination - 分页控件组件
 * 提供页码导航、上一页/下一页功能
 *
 */
public class FXPagination extends HBox implements IFXNode<FXPagination> {

    private int currentPage = 1;
    private int totalPages = 10;
    private Consumer<Integer> onPageChangeCallback;

    /**
     * 默认构造函数
     */
    public FXPagination() {
        super();
        setAlignment(Pos.CENTER);
        setSpacing(5);
        setPadding(new Insets(5));
        buildUI();
    }

    /**
     * 创建分页控件实例
     *
     * @param totalPages 总页数
     * @return FXPagination 实例
     */
    public static FXPagination create(int totalPages) {
        return new FXPagination().totalPages(totalPages);
    }

    /**
     * 设置当前页码
     *
     * @param page 页码（从 1 开始）
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination currentPage(int page) {
        this.currentPage = Math.max(1, Math.min(page, totalPages));
        updateButtons();
        return this;
    }

    /**
     * 设置总页数
     *
     * @param total 总页数
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination totalPages(int total) {
        this.totalPages = Math.max(1, total);
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        buildUI();
        return this;
    }

    /**
     * 设置页码变更回调
     *
     * @param callback 回调函数
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination onPageChange(Consumer<Integer> callback) {
        this.onPageChangeCallback = callback;
        return this;
    }

    /**
     * 跳转到首页
     *
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination first() {
        return currentPage(1);
    }

    /**
     * 跳转到末页
     *
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination last() {
        return currentPage(totalPages);
    }

    /**
     * 跳转到上一页
     *
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination previous() {
        return currentPage(currentPage - 1);
    }

    /**
     * 跳转到下一页
     *
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination next() {
        return currentPage(currentPage + 1);
    }

    /**
     * 构建 UI
     */
    private void buildUI() {
        getChildren().clear();

        // 首页按钮
        Button btnFirst = new Button("«");
        btnFirst.setDisable(currentPage <= 1);
        btnFirst.setOnAction(e -> first());
        getChildren().add(btnFirst);

        // 上一页按钮
        Button btnPrev = new Button("‹");
        btnPrev.setDisable(currentPage <= 1);
        btnPrev.setOnAction(e -> previous());
        getChildren().add(btnPrev);

        // 页码按钮
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, currentPage + 2);

        for (int i = startPage; i <= endPage; i++) {
            Button btnPage = new Button(String.valueOf(i));
            btnPage.setPrefSize(32, 32);

            if (i == currentPage) {
                btnPage.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                int finalI = i;
                btnPage.setOnAction(e -> currentPage(finalI));
            }

            getChildren().add(btnPage);
        }

        // 下一页按钮
        Button btnNext = new Button("›");
        btnNext.setDisable(currentPage >= totalPages);
        btnNext.setOnAction(e -> next());
        getChildren().add(btnNext);

        // 末页按钮
        Button btnLast = new Button("»");
        btnLast.setDisable(currentPage >= totalPages);
        btnLast.setOnAction(e -> last());
        getChildren().add(btnLast);
    }

    /**
     * 更新按钮状态
     */
    private void updateButtons() {
        buildUI();

        // 触发回调
        if (onPageChangeCallback != null) {
            onPageChangeCallback.accept(currentPage);
        }
    }

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * 检查是否有上一页
     *
     * @return true-有上一页，false-无
     */
    public boolean hasPrevious() {
        return currentPage > 1;
    }

    /**
     * 检查是否有下一页
     *
     * @return true-有下一页，false-无
     */
    public boolean hasNext() {
        return currentPage < totalPages;
    }

    /**
     * 设置宽度
     *
     * @param w 宽度值
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置高度
     *
     * @param h 高度值
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 添加 CSS 样式类
     *
     * @param classes CSS 样式类名称
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination stylesClass(String... classes) {
        getStyleClass().addAll(classes);
        return this;
    }

    /**
     * 设置为紧凑样式
     *
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination compact() {
        setSpacing(3);
        setPadding(new Insets(2));
        return this;
    }

    /**
     * 设置为宽松样式
     *
     * @return FXPagination 实例（链式调用）
     */
    public FXPagination spacious() {
        setSpacing(8);
        setPadding(new Insets(8));
        return this;
    }
}
