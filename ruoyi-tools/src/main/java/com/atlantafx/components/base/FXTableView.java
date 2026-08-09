package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * FXTableView - 表格组件
 * 继承自 JavaFX TableView，实现 IFXNode 接口支持链式调用
 * 提供便捷的数据绑定、列配置和样式控制方法
 */
public class FXTableView<T> extends TableView<T> implements IFXNode<FXTableView<T>> {

    /**
     * 默认构造函数
     */
    private FXTableView() {
        super();
    }

    /**
     * 带初始数据集的构造函数（私有化）
     *
     * @param items 初始绑定的可观察数据列表
     */
    private FXTableView(ObservableList<T> items) {
        super(items);
    }

    /**
     * 创建表格实例
     *
     * @param <T> 数据类型
     * @return FXTableView 实例
     */
    public static <T> FXTableView<T> create() {
        return new FXTableView<>();
    }

    /**
     * 创建带初始数据集表格视图实例的静态工厂方法
     *
     * @param items 初始绑定的数据列表
     * @param <T>   数据实体泛型类型
     * @return FXTableView 实例
     */
    public static <T> FXTableView<T> create(ObservableList<T> items) {
        return new FXTableView<>(items);
    }

    /**
     * 设置表格数据源（使用 ObservableList）
     *
     * @param items 数据项 ObservableList
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> items(ObservableList<T> items) {
        setItems(items);
        return this;
    }

    /**
     * 设置表格数据源（使用 List）
     * 自动转换为 ObservableList
     *
     * @param items 数据项 List
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> items(List<T> items) {
        getItems().setAll(items);
        return this;
    }

    /**
     * 双向绑定数据源属性，便于控制层进行响应式数据流流转
     *
     * @param property 可观察的数据列表属性
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> itemsProperty(Property<ObservableList<T>> property) {
        itemsProperty().bindBidirectional(property);
        return this;
    }

    /**
     * 核心方法：链式添加列
     *
     * @param name         列名
     * @param propertyFunc 属性映射函数，例如 User::getId 或 u -> u.nameProperty()
     * @param colSpec      对该列的进一步链式配置回调
     * @param <V>          列数据类型
     * @return FXTableView 实例（链式调用）
     */
    public <V> FXTableView<T> addColumn(String name,
                                        Function<T, ObservableValue<V>> propertyFunc,
                                        Consumer<FXTableColumn<T, V>> colSpec) {
        FXTableColumn<T, V> col = FXTableColumn.create(name);
        col.setCellValueFactory(cellData -> propertyFunc.apply(cellData.getValue()));
        if (colSpec != null) colSpec.accept(col);
        getColumns().add(col);
        return this;
    }

    /**
     * 简化版：添加只读列（无需额外配置）
     *
     * @param name         列名
     * @param propertyFunc 属性映射函数
     * @param <V>          列数据类型
     * @return FXTableView 实例（链式调用）
     */
    public <V> FXTableView<T> addColumn(String name, Function<T, ObservableValue<V>> propertyFunc) {
        return addColumn(name, propertyFunc, null);
    }

    /**
     * 批量追加注册表格列组件
     *
     * @param columns 封装的扩展列组件数组
     * @return FXTableView 实例（链式调用）
     */
    @SuppressWarnings("unchecked")
    public FXTableView<T> columns(FXTableColumn<T, ?>... columns) {
        if (columns != null) {
            getColumns().addAll(columns);
        }
        return this;
    }

    /**
     * 以集合形式批量追加注册表格列组件
     *
     * @param columns 列组件集合
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> columns(List<FXTableColumn<T, ?>> columns) {
        if (columns != null) {
            getColumns().addAll(columns);
        }
        return this;
    }

    /**
     * 设置表格占位节点（空数据时显示）
     *
     * @param node 占位节点（如"暂无数据"提示标签）
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> placeholder(Node node) {
        setPlaceholder(node);
        return this;
    }

    /**
     * 设置数据为空时的普通纯文本提示
     *
     * @param text 提示文本内容
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> placeholder(String text) {
        setPlaceholder(FXLabel.create(text).muted());
        return this;
    }

    // ==================== AtlantaFX 预设样式 ====================

    /**
     * 应用条纹样式（Striped）
     * 隔行变色，提高可读性
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> striped() {
        return stylesClass(Styles.STRIPED);
    }

    /**
     * 应用紧凑样式（Dense）
     * 更小的行间距，适用于数据密集型展示
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> dense() {
        return stylesClass(Styles.DENSE);
    }

    /**
     * 应用边框样式（Border）
     * 添加表格边框，用于区分表格和内容
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> border() {
        return stylesClass(Styles.BORDERED);
    }

    /**
     * 应用大尺寸样式（Large）
     * 更大的行高和字体
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> large() {
        return stylesClass(Styles.LARGE);
    }

    /**
     * 应用小尺寸样式（Small）
     * 更紧凑的布局
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> small() {
        return stylesClass(Styles.SMALL);
    }

    /**
     * 深度覆盖表格及其视窗内核的背景颜色，防止浅色穿透
     *
     * @param colorString CSS 标准颜色或 AtlantaFX 主题变量
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> backgroundColor(String colorString) {
        if (colorString != null && !colorString.trim().isEmpty()) {
            return styleCss("-fx-background-color: " + colorString + ";" +
                    "-fx-control-inner-background: " + colorString + ";");
        }
        return this;
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     * 使表格在垂直方向填充所有可用空间
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置在 HBox 中的水平增长优先级
     * 使表格在水平方向填充所有可用空间
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 一键配置组件固定几何尺寸
     *
     * @param w 宽度（像素值）
     * @param h 高度（像素值）
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置表格宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置表格高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置固定行高（Cell Height）
     * 配置此参数后将激活 VirtualFlow 的常数高度跳跃计算，避免滚动时动态差值带来的额外算力开销
     *
     * @param height 行高像素值
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> fixedCellSize(double height) {
        setFixedCellSize(height);
        return this;
    }

    // ==================== 列宽控制 ====================

    /**
     * 设置列宽自适应策略
     *
     * @param callback 调整策略回调函数
     *                 常用：TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> columnResizePolicy(Callback<ResizeFeatures, Boolean> callback) {
        setColumnResizePolicy(callback);
        return this;
    }

    /**
     * 设置最后一列弹性拉伸模式
     * 最后一列会自动填充剩余宽度
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> flexLastColumn() {
        return columnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /**
     * 设置所有列等比例拉伸模式
     * 所有列平均分配可用宽度
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> constrainedResize() {
        return columnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    // ==================== 选择控制 ====================

    /**
     * 设置行选择模式
     *
     * @param mode SelectionMode.SINGLE(单选) 或 SelectionMode.MULTIPLE(多选)
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> selectionMode(SelectionMode mode) {
        getSelectionModel().setSelectionMode(mode);
        return this;
    }

    /**
     * 设置选择模式为单选
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> singleSelection() {
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        return this;
    }

    /**
     * 设置选择模式为多选
     * 支持 Ctrl/Shift 多选
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> multiSelection() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return this;
    }

    /**
     * 清除所有选中项
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> clearSelection() {
        getSelectionModel().clearSelection();
        return this;
    }

    /**
     * 选中指定索引的行
     *
     * @param index 行索引（从 0 开始）
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> selectIndex(int index) {
        getSelectionModel().select(index);
        return this;
    }

    /**
     * 选中指定数据项
     *
     * @param item 要选中的数据项
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> selectItem(T item) {
        getSelectionModel().select(item);
        return this;
    }

    /**
     * 获取当前选中的数据项
     *
     * @return 选中的数据项，未选中则返回 null
     */
    public T getSelectedItem() {
        return getSelectionModel().getSelectedItem();
    }

    /**
     * 设置选择变更监听器
     *
     * @param listener 选择变更时的回调函数
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> onSelect(Consumer<T> listener) {
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) listener.accept(newVal);
        });
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置表格可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置表格是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置表格透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置表格是否禁用
     * 禁用状态下无法交互
     *
     * @param disabled true-禁用，false-启用
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置表格是否可编辑
     *
     * @param editable true-可编辑，false-不可编辑
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    // ==================== 数据刷新 ====================

    /**
     * 向表格添加一行数据
     *
     * @param item 要添加的数据项
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> addRow(T item) {
        getItems().add(item);
        return this;
    }

    /**
     * 从表格移除一行数据
     *
     * @param item 要移除的数据项
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> removeRow(T item) {
        getItems().remove(item);
        return this;
    }

    /**
     * 清空表格所有数据
     *
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> clearRows() {
        getItems().clear();
        return this;
    }

    /**
     * 获取表格数据ObservableList
     * 可直接用于数据绑定
     *
     * @return ObservableList<T> 数据列表
     */
    public ObservableList<T> getItemsList() {
        return getItems();
    }

    /**
     * 响应式监听表格选中行更替变更
     *
     * @param consumer 变更回调闭包，传入 (旧选中行数据对象, 新选中行数据对象)
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> onSelectionChanged(BiConsumer<T, T> consumer) {
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (consumer != null) {
                consumer.accept(oldVal, newVal);
            }
        });
        return this;
    }

    /**
     * 快捷绑定物理精确点击监听器
     *
     * @param clickCount 目标点击计数（1 代表单击，2 代表双击）
     * @param consumer   回调闭包，接收当前被选中的行模型数据实体
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> onRowClick(int clickCount, Consumer<T> consumer) {
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
     * @param consumer 回调闭包，接收当前双击行的实体数据对象
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> onRowDoubleClick(Consumer<T> consumer) {
        return onRowClick(2, consumer);
    }

    /**
     * 强制滚动视窗对齐到目标索引行
     *
     * @param index 行索引
     * @return FXTableView 实例（链式调用）
     */
    public FXTableView<T> scrollToRow(int index) {
        scrollTo(index);
        return this;
    }
}
