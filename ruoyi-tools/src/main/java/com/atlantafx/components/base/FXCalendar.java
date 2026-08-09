package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * FXCalendar - 日历视图组件
 * 提供月历显示、日期选择和导航功能
 */
public class FXCalendar extends GridPane implements IFXNode<FXCalendar> {

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private java.util.function.Consumer<LocalDate> onDateSelectCallback;

    /**
     * 默认构造函数
     */
    private FXCalendar() {
        super();
        this.currentYearMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();

        setHgap(2);
        setVgap(2);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        buildCalendar();
    }

    /**
     * 创建日历实例
     *
     * @return FXCalendar 实例
     */
    public static FXCalendar create() {
        return new FXCalendar();
    }

    /**
     * 设置选中的日期
     *
     * @param date 日期
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar selectedDate(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            this.currentYearMonth = YearMonth.from(date);
        }
        buildCalendar();
        return this;
    }

    /**
     * 获取选中的日期
     *
     * @return 选中的日期
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * 设置日期选择回调
     *
     * @param callback 回调函数
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar onDateSelect(java.util.function.Consumer<LocalDate> callback) {
        this.onDateSelectCallback = callback;
        return this;
    }

    /**
     * 跳转到指定年月
     *
     * @param yearMonth 年月
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar goToYearMonth(YearMonth yearMonth) {
        this.currentYearMonth = yearMonth;
        buildCalendar();
        return this;
    }

    /**
     * 跳转到上个月
     *
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        buildCalendar();
        return this;
    }

    /**
     * 跳转到下个月
     *
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        buildCalendar();
        return this;
    }

    /**
     * 跳转到今天
     *
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar today() {
        LocalDate now = LocalDate.now();
        selectedDate = now;
        currentYearMonth = YearMonth.from(now);
        buildCalendar();
        return this;
    }

    /**
     * 构建日历 UI
     */
    private void buildCalendar() {
        getChildren().clear();

        // 添加星期标题
        String[] weekDays = {"一", "二", "三", "四", "五", "六", "日"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMinSize(40, 30);
            add(dayLabel, i, 0);
        }

        // 获取当月第一天
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // 计算起始位置（调整为周一为一周的开始）
        int startColumn = dayOfWeek - 1;
        int row = 1;
        int col = startColumn;

        // 添加日期格子
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);

            FXCalendarCell cell = new FXCalendarCell(day, date);
            cell.setMinSize(40, 40);
            cell.setAlignment(Pos.CENTER);

            // 设置样式
            if (date.equals(selectedDate)) {
                cell.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            } else if (date.equals(LocalDate.now())) {
                cell.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; -fx-text-fill: #2196F3;");
            } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                cell.setStyle("-fx-text-fill: #F44336;");
            }

            add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * 设置宽度
     *
     * @param w 宽度值
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置高度
     *
     * @param h 高度值
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 添加 CSS 样式类
     *
     * @param classes CSS 样式类名称
     * @return FXCalendar 实例（链式调用）
     */
    public FXCalendar stylesClass(String... classes) {
        getStyleClass().addAll(classes);
        return this;
    }

    /**
     * 获取当前年月
     *
     * @return 年月对象
     */
    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }

    /**
     * 内部类：日历单元格
     */
    private class FXCalendarCell extends Label {
        FXCalendarCell(int day, LocalDate date) {
            super(String.valueOf(day));

            setOnMouseClicked(e -> {
                selectedDate = date;
                buildCalendar();

                if (onDateSelectCallback != null) {
                    onDateSelectCallback.accept(date);
                }
            });

            setCursor(javafx.scene.Cursor.HAND);
        }
    }
}
