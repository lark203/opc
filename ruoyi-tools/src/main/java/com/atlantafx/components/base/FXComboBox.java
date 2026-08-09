package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * FXComboBox - 下拉选择框组件
 * 继承自 JavaFX ComboBox，实现 IFXNode 接口支持链式调用
 * 提供便捷的数据绑定、显示转换和样式设置方法
 */
public class FXComboBox<T> extends ComboBox<T> implements IFXNode<FXComboBox<T>> {

    /**
     * 默认构造函数
     */
    private FXComboBox() {
        super();
    }

    /**
     * 创建下拉选择框实例
     *
     * @param <T> 数据类型
     * @return FXComboBox 实例
     */
    public static <T> FXComboBox<T> create() {
        return new FXComboBox<>();
    }

    /**
     * 设置下拉框的数据项列表
     *
     * @param items 数据项ObservableList
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> add(ObservableList<T> items) {
        setItems(items);
        return this;
    }

    /**
     * 快速设置下拉框的数据项列表
     *
     * @param items 数据项 List
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> add(List<T> items) {
        setItems(FXCollections.observableArrayList(items));
        return this;
    }

    /**
     * 以可变参数（Varargs）形式快捷注入数据集
     *
     * @param items 数据模型数组
     * @return FXComboBox 实例（链式调用）
     */
    @SuppressWarnings("unchecked")
    public FXComboBox<T> add(T... items) {
        if (items != null) {
            setItems(FXCollections.observableArrayList(items));
        }
        return this;
    }

    /**
     * 设置双向转换器
     * 支持从字符串还原为数据对象，适用于可编辑的下拉框
     *
     * @param toString   对象转字符串函数
     * @param fromString 字符串转对象函数
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> converter(Function<T, String> toString, Function<String, T> fromString) {
        setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : toString.apply(object);
            }

            @Override
            public T fromString(String string) {
                return fromString.apply(string);
            }
        });
        return this;
    }

    /**
     * 设置默认选中项
     *
     * @param item 要选中的数据项
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> select(T item) {
        setValue(item);
        return this;
    }

    /**
     * 设置默认选中项（通过索引）
     *
     * @param index 选中项的索引位置（从 0 开始）
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> selectIndex(int index) {
        if (index >= 0 && index < getItems().size()) {
            getSelectionModel().select(index);
        }
        return this;
    }

    /**
     * 强行选中指定的数据模型实例对象
     *
     * @param item 目标数据对象
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> selectItem(T item) {
        if (item != null) {
            getSelectionModel().select(item);
        }
        return this;
    }

    /**
     * 强行驱动下拉框选中集合中的第一个元素
     *
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> selectFirst() {
        getSelectionModel().selectFirst();
        return this;
    }

    /**
     * 强行驱动下拉框选中集合中的最后一个元素
     *
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> selectLast() {
        getSelectionModel().selectLast();
        return this;
    }

    /**
     * 建立与外部值模型属性对象的双向响应式数据流绑定
     *
     * @param property 外部源模型属性槽（泛型 T 保持对齐）
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> bindBidirectional(Property<T> property) {
        if (property != null) {
            valueProperty().bindBidirectional(property);
        }
        return this;
    }

    /**
     * 清空选择
     *
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> clearSelection() {
        getSelectionModel().clearSelection();
        return this;
    }

    /**
     * 设置选择变更监听器
     *
     * @param listener 选择变更时的回调函数，接收新的选中值
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> onSelect(Consumer<T> listener) {
        valueProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        return this;
    }

    // ==================== 高阶事件监听与数据渲染器 ====================

    /**
     * 快捷注册选中项变更监听器，专注于捕获用户每次切换下拉项后的新模型数据
     *
     * @param listener 消费事件的回调函数（接收最新的选中项泛型实例）
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> onSelectChanged(Consumer<T> listener) {
        if (listener != null) {
            getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        }
        return this;
    }

    /**
     * 快捷注册高度简化的字符串文本转换映射器（Converter）
     * 常用于非 String 复杂 POJO 模型在下拉框按钮和基础行中的纯文本呈现
     *
     * @param toStringMapper 泛型转换为单行文本的函数式映射规则
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> displayMapper(Function<T, String> toStringMapper) {
        if (toStringMapper != null) {
            setConverter(new StringConverter<T>() {
                @Override
                public String toString(T object) {
                    return object == null ? "" : toStringMapper.apply(object);
                }

                @Override
                public T fromString(String string) {
                    return null; // 表单下拉通常只读，不提供逆向文本解析
                }
            });
        }
        return this;
    }

    /**
     * 设置底层的原生基础单元格工厂（Cell Factory）
     * 用于深度重写、全量定制下拉弹窗中每一行的 Node 骨架呈现
     */
    public FXComboBox<T> cellFactory(Callback<ListView<T>, ListCell<T>> cellFactory) {
        setCellFactory(cellFactory);
        return this;
    }

    /**
     * 设置下拉框激活状态按钮区（Button Cell）的显示渲染器
     */
    public FXComboBox<T> buttonCell(ListCell<T> buttonCell) {
        setButtonCell(buttonCell);
        return this;
    }

    // ==================== 物理排版与视口约束 ====================

    /**
     * 设定下拉框弹窗视口的最大可见物理行数
     * 超过此阈值时系统内部会自动唤醒纵向轻量滚动条（Scrollbar），避免破坏整体表单布局
     *
     * @param rowCount 最大可见数据行数
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> maxVisibleRows(int rowCount) {
        setVisibleRowCount(rowCount);
        return this;
    }

    /**
     * 控制下拉框输入轨道的全局可编辑性
     *
     * @param editable true-支持键入过滤，false-标准只读下拉
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    /**
     * 设置未选中任何资产时的占位提示灰色文案（Prompt Text）
     */
    public FXComboBox<T> prompt(String text) {
        setPromptText(text);
        return this;
    }

    /**
     * 锁定下拉组件的固定物理首选宽度
     */
    public FXComboBox<T> width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置下拉框高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXComboBox 实例（链式调用）
     */
    public FXComboBox<T> height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 当置于 VBox 容器骨架内时，声明纵向拉伸延伸优先级
     */
    public FXComboBox<T> vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 当置于 HBox 容器骨架内时，声明横向拉伸延伸优先级
     */
    public FXComboBox<T> hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== AtlantaFX 核心语义化皮肤与样式定制 ====================

    /**
     * 批量安全追加底层原始的 CSS 样式类
     */
    public FXComboBox<T> stylesClass(String... classes) {
        if (classes != null) {
            getStyleClass().addAll(classes);
        }
        return this;
    }

    /**
     * 一键剥离下拉框的立体边框边框骨架，转换为极简的无框底纹皮肤风格
     */
    public FXComboBox<T> flat() {
        return stylesClass(Styles.FLAT);
    }

    /**
     * 转换当前下拉框为大号尺寸规格（Large Size）
     */
    public FXComboBox<T> large() {
        getStyleClass().removeAll(Styles.SMALL, Styles.LARGE);
        return stylesClass(Styles.LARGE);
    }

    /**
     * 转换当前下拉框为小号紧凑型尺寸规格（Small Size）
     */
    public FXComboBox<T> small() {
        getStyleClass().removeAll(Styles.SMALL, Styles.LARGE);
        return stylesClass(Styles.SMALL);
    }

    /**
     * 注入语义色：信息提示与标准高亮强调状态 (Accent)
     */
    public FXComboBox<T> accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 注入语义色：数据校验成功绿高亮状态 (Success)
     */
    public FXComboBox<T> success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 注入语义色：警告或可疑数据段高亮提示状态 (Warning)
     */
    public FXComboBox<T> warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 注入语义色：非法拦截、数据异常校验未通过红高亮状态 (Danger)
     */
    public FXComboBox<T> danger() {
        return stylesClass(Styles.DANGER);
    }

    // ==================== 组件通用状态物理控制 ====================

    /**
     * 快捷控制组件的运行时全局禁用状态
     */
    public FXComboBox<T> disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 控制可见性
     */
    public FXComboBox<T> visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 控制是否纳入布局边界计算
     */
    public FXComboBox<T> managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 调整组件全局不透明度
     */
    public FXComboBox<T> opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }
}
