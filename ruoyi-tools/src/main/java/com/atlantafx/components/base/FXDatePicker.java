package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * FXDatePicker - 日期选择器组件
 * 继承自 JavaFX DatePicker，实现 IFXNode 接口支持链式调用
 * 提供便捷的日期选择、格式化和样式设置方法
 */
public class FXDatePicker extends DatePicker implements IFXNode<FXDatePicker> {

    /**
     * 默认构造函数
     */
    private FXDatePicker() {
        super();
    }

    /**
     * 创建带初始日期的日期选择器
     *
     * @param date 初始日期
     */
    private FXDatePicker(LocalDate date) {
        super(date);
    }

    /**
     * 创建空白日期选择器实例
     *
     * @return FXDatePicker 实例
     */
    public static FXDatePicker create() {
        return new FXDatePicker();
    }

    /**
     * 创建带初始日期的日期选择器实例
     *
     * @param date 初始日期
     * @return FXDatePicker 实例
     */
    public static FXDatePicker create(LocalDate date) {
        return new FXDatePicker(date);
    }

    /**
     * 设置选中的日期
     *
     * @param date 日期
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker value(LocalDate date) {
        setValue(date);
        return this;
    }

    /**
     * 设置提示文本
     *
     * @param text 提示文本
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker prompt(String text) {
        setPromptText(text);
        return this;
    }

    /**
     * 添加 CSS 样式类到日期选择器
     *
     * @param classes CSS 样式类名称（可变参数）
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker stylesClass(String... classes) {
        getStyleClass().addAll(classes);
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     * 绿色边框
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色边框，表示错误或过期
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色边框
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 应用信息样式（Info）
     * 蓝色边框
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker info() {
        return stylesClass(Styles.ACCENT);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置日期选择器宽度
     *
     * @param w 宽度值（像素）
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置日期选择器高度
     *
     * @param h 高度值（像素）
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    // ==================== 日期范围限制 ====================

    /**
     * 设置最早可选日期
     *
     * @param date 最早日期
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker minDate(LocalDate date) {
        setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(date));
            }
        });
        return this;
    }

    /**
     * 设置最晚可选日期
     *
     * @param date 最晚日期
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker maxDate(LocalDate date) {
        setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(date));
            }
        });
        return this;
    }

    /**
     * 设置日期范围
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker dateRange(LocalDate startDate, LocalDate endDate) {
        setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(startDate) || item.isAfter(endDate));
            }
        });
        return this;
    }

    /**
     * 禁用周末（周六和周日）
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker disableWeekends() {
        setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    java.time.DayOfWeek day = item.getDayOfWeek();
                    setDisable(day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY);
                }
            }
        });
        return this;
    }

    /**
     * 禁用指定日期列表
     *
     * @param dates 要禁用的日期数组
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker disableDates(LocalDate... dates) {
        setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    for (LocalDate date : dates) {
                        if (item.equals(date)) {
                            setDisable(true);
                            break;
                        }
                    }
                }
            }
        });
        return this;
    }

    /**
     * 高亮显示指定日期
     *
     * @param dates      要高亮的日期数组
     * @param styleClass CSS 样式类名
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker highlightDates(java.util.List<LocalDate> dates, String styleClass) {
        setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && dates.contains(item)) {
                    getStyleClass().add(styleClass);
                }
            }
        });
        return this;
    }

    // ==================== 格式化 ====================

    /**
     * 设置日期显示格式
     *
     * @param pattern 日期格式模式（如 "yyyy-MM-dd"）
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker format(String pattern) {
        setConverter(new javafx.util.StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty()
                        ? LocalDate.parse(string, formatter)
                        : null;
            }
        });
        return this;
    }

    /**
     * 设置为中文格式
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker chineseFormat() {
        return format("yyyy 年 MM 月 dd 日");
    }

    /**
     * 设置为短格式（yyyy-MM-dd）
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker shortFormat() {
        return format("yyyy-MM-dd");
    }

    /**
     * 设置为长格式（包含星期）
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker longFormat() {
        return format("yyyy-MM-dd EEEE");
    }

    // ==================== 状态控制 ====================

    /**
     * 设置日期选择器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置日期选择器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置日期选择器透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置日期选择器是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置日期选择器是否可编辑
     * 可编辑状态下可以直接输入日期
     *
     * @param editable true-可编辑，false-只能选择
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    // ==================== 事件监听 ====================

    /**
     * 设置日期变更监听器
     *
     * @param listener 日期变更时的回调函数，接收新的日期
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker onValueChange(Consumer<LocalDate> listener) {
        valueProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        return this;
    }

    /**
     * 设置工具提示
     *
     * @param text 提示文本
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker tooltip(String text) {
        setTooltip(new javafx.scene.control.Tooltip(text));
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为生日选择器
     * 限制合理的年龄范围（0-120 岁）
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker asBirthday() {
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(120);
        return dateRange(minDate, today)
                .prompt("请选择出生日期")
                .shortFormat();
    }

    /**
     * 设置为预约日期选择器
     * 只能选择今天及以后的日期
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker asAppointment() {
        return minDate(LocalDate.now())
                .prompt("请选择预约日期")
                .disableWeekends()
                .shortFormat();
    }

    /**
     * 设置为截止日期选择器
     * 只能选择今天及以前的日期
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker asDeadline() {
        return maxDate(LocalDate.now())
                .prompt("请选择截止日期")
                .shortFormat();
    }

    /**
     * 设置为工作日选择器
     * 只能选择周一到周五
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker asWorkday() {
        return disableWeekends()
                .prompt("请选择工作日")
                .shortFormat();
    }

    /**
     * 设置为本月日期范围
     * 只能选择当前月份的日期
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker currentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
        return dateRange(firstDay, lastDay)
                .prompt("选择本月日期");
    }

    /**
     * 清除选择的日期
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker clear() {
        setValue(null);
        return this;
    }

    /**
     * 设置为今天
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker today() {
        setValue(LocalDate.now());
        return this;
    }

    /**
     * 设置为明天
     *
     * @return FXDatePicker 实例（链式调用）
     */
    public FXDatePicker tomorrow() {
        setValue(LocalDate.now().plusDays(1));
        return this;
    }
}
