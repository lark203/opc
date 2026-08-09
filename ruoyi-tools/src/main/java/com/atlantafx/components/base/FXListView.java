package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * FXListView - 基于 AtlantaFX 风格的列表视图组件
 * 继承自 JavaFX ListView，实现 IFXNode 接口支持链式调用
 *
 * @param <T> 列表条目的数据类型
 */
public class FXListView<T> extends ListView<T> implements IFXNode<FXListView<T>> {

    /**
     * 构造函数（私有化，通过静态工厂方法创建）
     */
    private FXListView() {
        super();
    }

    /**
     * 构造函数（私有化，带初始数据集）
     *
     * @param items 初始绑定的数据列表
     */
    private FXListView(ObservableList<T> items) {
        super(items);
    }

    /**
     * 创建空列表视图
     *
     * @param <T> 数据类型
     * @return FXListView 实例
     */
    public static <T> FXListView<T> create() {
        return new FXListView<>();
    }

    /**
     * 创建带初始数据集的列表视图
     *
     * @param items 初始绑定的数据列表
     * @param <T>   数据类型
     * @return FXListView 实例
     */
    public static <T> FXListView<T> create(ObservableList<T> items) {
        return new FXListView<>(items);
    }

    // ==================== 核心属性控制 ====================

    /**
     * 设置列表数据源
     *
     * @param items 数据列表
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> items(ObservableList<T> items) {
        setItems(items);
        return this;
    }

    /**
     * 双向绑定数据源属性
     *
     * @param property 可观察的数据列表属性
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> itemsProperty(Property<ObservableList<T>> property) {
        itemsProperty().bindBidirectional(property);
        return this;
    }

    /**
     * 设置列表排列方向（横向或纵向）
     *
     * @param orientation 布局方向 (Orientation.HORIZONTAL 或 Orientation.VERTICAL)
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> orientation(Orientation orientation) {
        setOrientation(orientation);
        return this;
    }

    /**
     * 一键开启横向滚动列表模式
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> horizontal() {
        return orientation(Orientation.HORIZONTAL);
    }

    /**
     * 设置列表是否可编辑
     *
     * @param editable true-允许编辑，false-禁止编辑
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    // ==================== 尺寸与布局扩展 ====================

    /**
     * 设置组件固定宽度（同时设置最小、首选、最大宽度，常用于固定侧边栏）
     *
     * @param w 宽度（像素）
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置组件固定高度
     *
     * @param h 高度（像素）
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置组件尺寸
     *
     * @param w 宽度（像素）
     * @param h 高度（像素）
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置列表项的固定高度（设置此值可极大优化海量数据滚动时的性能）
     *
     * @param height 行高（像素）
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> fixedCellSize(double height) {
        setFixedCellSize(height);
        return this;
    }

    /**
     * 设置组件在 VBox 中的垂直增长优先级
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置组件在 HBox 中的水平增长优先级
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置是否受布局管理器控制
     *
     * @param managed true-受管理，false-脱离布局占用
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    // ==================== AtlantaFX 样式扩展 ====================

    /**
     * 移除组件的外边框与阴影，使其融于背景（常用于卡片式布局内部）
     * 映射 AtlantaFX Styles.BORDERLESS
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> borderless() {
        return stylesClass(Styles.BORDERED);
    }

    /**
     * 启用密集的紧凑排列模式，压缩 Cell 的 Padding 间距
     * 映射 AtlantaFX Styles.DENSE
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> dense() {
        return stylesClass(Styles.DENSE);
    }

    /**
     * 移除常规的行分割线（Grid Lines）
     * 映射 AtlantaFX Styles.STRIPED (配合自定义或默认样式去除线)
     * 在 AtlantaFX 中，部分组件通过特定 class 或直接在 borderless 中弱化分割线
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> striped() {
        return stylesClass(Styles.STRIPED);
    }

    // ==================== 状态控制与占位 ====================

    /**
     * 设置数据为空时的纯文本占位提示
     *
     * @param text 提示文本
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> placeholder(String text) {
        setPlaceholder(new Label(text));
        return this;
    }

    /**
     * 设置数据为空时的自定义节点占位（支持复杂的 ProgressIndicator 或图文组合）
     *
     * @param node 自定义 JavaFX 节点
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> placeholder(Node node) {
        setPlaceholder(node);
        return this;
    }

    /**
     * 设置是否禁用整个列表组件
     *
     * @param disabled true-禁用交互，false-激活
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 选择模式与交互 ====================

    /**
     * 设置选择模式（单选 / 多选）
     *
     * @param mode SelectionMode.SINGLE 或 SelectionMode.MULTIPLE
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> selectionMode(SelectionMode mode) {
        getSelectionModel().setSelectionMode(mode);
        return this;
    }

    /**
     * 快捷开启多选模式
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> multipleSelection() {
        return selectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * 监听选择变更事件（规避了复杂的 Listener 编写）
     *
     * @param consumer 回调函数，传入 (oldValue, newValue)
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> onSelectionChanged(BiConsumer<T, T> consumer) {
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (consumer != null) {
                consumer.accept(oldVal, newVal);
            }
        });
        return this;
    }

    /**
     * 快速绑定点击事件（包含单击与双击识别）
     *
     * @param clickCount 触发事件所需的点击次数（1-单击，2-双击）
     * @param consumer   回调函数，传入当前被点击的条目数据
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> onItemClick(int clickCount, java.util.function.Consumer<T> consumer) {
        setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == clickCount) {
                T selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem != null && consumer != null) {
                    consumer.accept(selectedItem);
                }
            }
        });
        return this;
    }

    /**
     * 快捷绑定行双击事件
     *
     * @param consumer 回调函数，传入当前被双击的条目数据
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> onItemDoubleClick(java.util.function.Consumer<T> consumer) {
        return onItemClick(2, consumer);
    }

    // ==================== Cell 工厂增强 (渲染器定制) ====================

    /**
     * 快捷设置基于字符串映射的文本渲染器（适用于简单 POJO 对象的特定字段展示）
     *
     * @param mapper 将抽象数据对象转换为 String 的映射函数
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> cellTextFactory(Function<T, String> mapper) {
        setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(mapper.apply(item));
                }
            }
        });
        return this;
    }

    /**
     * 开放完整的高级 Cell 工厂定制，支持函数式快捷接入
     *
     * @param cellBuilder 传入自定义的行渲染逻辑，包含 (item, listCell) 的双向注入
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> cellFactory(BiConsumer<T, ListCell<T>> cellBuilder) {
        setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    // 清除可能残留的样式，防止虚拟化容器复用污染
                    getStyleClass().remove("cell-present");
                } else {
                    if (!getStyleClass().contains("cell-present")) {
                        getStyleClass().add("cell-present");
                    }
                    cellBuilder.accept(item, this);
                }
            }
        });
        return this;
    }

    /**
     * 原生 JavaFX Cell 工厂接入（保留完整底层的 Callback 协议）
     *
     * @param factory 完整的通用 Cell 工厂回调
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> rawCellFactory(Callback<ListView<T>, ListCell<T>> factory) {
        setCellFactory(factory);
        return this;
    }

    // ==================== 工具方法与滚动控制 ====================

    /**
     * 滚动到指定索引行
     *
     * @param index 目标索引
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> scrollToRow(int index) {
        scrollTo(index);
        return this;
    }

    /**
     * 滚动到指定的对象条目所在行
     *
     * @param item 目标数据对象
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> scrollToItem(T item) {
        scrollTo(item);
        return this;
    }

    /**
     * 快捷选中特定索引对应的行
     *
     * @param index 索引值
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> select(int index) {
        getSelectionModel().select(index);
        return this;
    }

    /**
     * 清除当前列表中所有的选中状态
     *
     * @return FXListView 实例（链式调用）
     */
    public FXListView<T> clearSelection() {
        getSelectionModel().clearSelection();
        return this;
    }
}