package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
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
 * FXTreeTable - 基于 AtlantaFX 风格的增强型树形表格视图组件
 * 封装 JavaFX TreeTableView，提供完整的链式调用与高阶响应式驱动支持
 * 构造方法完全私有化，依靠静态工厂控制组件实例化形态
 *
 * @param <T> 树形表格行模型的数据实体泛型类型
 */
public class FXTreeTable<T> extends TreeTableView<T> implements IFXNode<FXTreeTable<T>> {

    /**
     * 默认构造函数（私有化，禁止外部直接通过 new 实例化）
     */
    private FXTreeTable() {
        super();
        // 默认遵循后台管理系统规范，不显示虚设的根节点本身，直接呈现子层级
        setShowRoot(false);
    }

    /**
     * 带初始根节点的构造函数（私有化）
     *
     * @param root 虚拟根节点
     */
    private FXTreeTable(TreeItem<T> root) {
        super(root);
        setShowRoot(false);
    }

    /**
     * 创建空树形表格视图实例的静态工厂方法
     *
     * @param <T> 数据实体泛型类型
     * @return FXTreeTable 实例
     */
    public static <T> FXTreeTable<T> create() {
        return new FXTreeTable<>();
    }

    /**
     * 创建带初始根节点树形表格视图实例的静态工厂方法
     *
     * @param root 虚拟根节点
     * @param <T>  数据实体泛型类型
     * @return FXTreeTable 实例
     */
    public static <T> FXTreeTable<T> create(TreeItem<T> root) {
        return new FXTreeTable<>(root);
    }

    // ==================== 核心数据与列注册控制 ====================

    /**
     * 设置树形表格的根节点
     *
     * @param root 虚拟根节点
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> root(TreeItem<T> root) {
        setRoot(root);
        return this;
    }

    /**
     * 双向绑定根节点属性
     *
     * @param property 可观察的树节点属性
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> rootProperty(Property<TreeItem<T>> property) {
        rootProperty().bindBidirectional(property);
        return this;
    }

    /**
     * 是否显示根节点
     *
     * @param showRoot true-显示根节点，false-隐藏根节点
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> showRoot(boolean showRoot) {
        setShowRoot(showRoot);
        return this;
    }

    /**
     * 批量追加注册树形表格列组件
     *
     * @param columns 封装的扩展列组件数组
     * @return FXTreeTable 实例（链式调用）
     */
    @SuppressWarnings("unchecked")
    public FXTreeTable<T> columns(FXTreeTableColumn<T, ?>... columns) {
        if (columns != null) {
            getColumns().addAll(columns);
        }
        return this;
    }

    /**
     * 以集合形式批量追加注册树形表格列组件
     *
     * @param columns 列组件集合
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> columns(List<FXTreeTableColumn<T, ?>> columns) {
        if (columns != null) {
            getColumns().addAll(columns);
        }
        return this;
    }

    /**
     * 添加树形表格列
     *
     * @param name         列名
     * @param propertyFunc 属性映射函数，接收 TreeItem<T>，返回 ObservableValue<V>
     * @param colSpec      对列的进一步配置回调
     * @param <V>          列数据类型
     * @return FXTreeTable 实例（链式调用）
     */
    public <V> FXTreeTable<T> addColumn(String name,
                                        Function<TreeItem<T>, ObservableValue<V>> propertyFunc,
                                        Consumer<FXTreeTableColumn<T, V>> colSpec) {
        FXTreeTableColumn<T, V> col = FXTreeTableColumn.create(name);
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
     * @return FXTreeTable 实例（链式调用）
     */
    public <V> FXTreeTable<T> addColumn(String name, Function<TreeItem<T>, ObservableValue<V>> propertyFunc) {
        return addColumn(name, propertyFunc, null);
    }

    /**
     * 设置树形表格整体是否允许编辑
     *
     * @param editable true-允许编辑，false-禁止编辑
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    // ==================== 尺寸与容器布局增强 ====================

    /**
     * 设置组件固定宽度（同步锚定最小、最大和首选宽度）
     *
     * @param w 宽度（像素值）
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置组件固定高度（同步锚定最小、最大和首选高度）
     *
     * @param h 高度（像素值）
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 一键配置组件固定几何尺寸
     *
     * @param w 宽度（像素值）
     * @param h 高度（像素值）
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置固定行高（Cell Height）
     *
     * @param height 行高像素值
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> fixedCellSize(double height) {
        setFixedCellSize(height);
        return this;
    }

    // ==================== 列宽控制 ====================

    /**
     * 设置列宽自适应策略
     *
     * @param callback 调整策略回调函数
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> columnResizePolicy(Callback<ResizeFeatures, Boolean> callback) {
        setColumnResizePolicy(callback);
        return this;
    }

    /**
     * 设置最后一列弹性拉伸模式
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> flexLastColumn() {
        return columnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /**
     * 设置所有列等比例拉伸模式
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> constrainedResize() {
        return columnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * 设置组件在垂直箱子布局 (VBox) 中的垂直生长优先级为 ALWAYS
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置组件在水平箱子布局 (HBox) 中的水平生长优先级为 ALWAYS
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== AtlantaFX 核心样式骨架定制 ====================

    /**
     * 应用条纹斑马线交替明暗样式（Striped）
     * 映射 AtlantaFX Styles.STRIPED
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> striped() {
        return stylesClass(Styles.STRIPED);
    }

    /**
     * 应用无边框融合样式（Borderless）
     * 映射 AtlantaFX Styles.BORDERLESS
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> border() {
        return stylesClass(Styles.BORDERED);
    }

    /**
     * 应用高密度压缩排列样式（Dense）
     * 映射 AtlantaFX Styles.DENSE
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> dense() {
        return stylesClass(Styles.DENSE);
    }

    /**
     * 深度覆盖树形表格及其视窗内核的背景颜色
     *
     * @param colorString CSS 标准颜色或 AtlantaFX 主题变量
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> backgroundColor(String colorString) {
        if (colorString != null && !colorString.trim().isEmpty()) {
            return styleCss("-fx-background-color: " + colorString + ";" +
                    "-fx-control-inner-background: " + colorString + ";");
        }
        return this;
    }

    // ==================== 层级动态展现控制控制流 ====================

    /**
     * 一键递归级联展开整棵树所有的分支节点
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> expandAll() {
        TreeItem<T> rootNode = getRoot();
        if (rootNode != null) {
            expandNodeRecursively(rootNode, true);
        }
        return this;
    }

    /**
     * 一键递归级联收拢折叠整棵树所有的分支节点
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> collapseAll() {
        TreeItem<T> rootNode = getRoot();
        if (rootNode != null) {
            expandNodeRecursively(rootNode, false);
        }
        return this;
    }

    /**
     * 递归控制节点展开/折叠内部辅助工具流
     */
    private void expandNodeRecursively(TreeItem<T> item, boolean isExpand) {
        if (item != null) {
            item.setExpanded(isExpand);
            for (TreeItem<T> child : item.getChildren()) {
                expandNodeRecursively(child, isExpand);
            }
        }
    }

    // ==================== 选择模式与事件响应消费 ====================

    /**
     * 设置行选择模式
     *
     * @param mode SelectionMode.SINGLE(单选) 或 SelectionMode.MULTIPLE(多选)
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> selectionMode(SelectionMode mode) {
        getSelectionModel().setSelectionMode(mode);
        return this;
    }

    /**
     * 设置选择模式为单选
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> singleSelection() {
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        return this;
    }

    /**
     * 设置选择模式为多选
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> multiSelection() {
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return this;
    }

    /**
     * 清除所有选中项
     *
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> clearSelection() {
        getSelectionModel().clearSelection();
        return this;
    }

    /**
     * 选中指定索引的行
     *
     * @param index 行索引
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> selectIndex(int index) {
        getSelectionModel().select(index);
        return this;
    }

    /**
     * 获取当前选中的树节点
     *
     * @return 选中的树节点，未选中则返回 null
     */
    public TreeItem<T> getSelectedItem() {
        return getSelectionModel().getSelectedItem();
    }

    /**
     * 设置选择变更监听器
     *
     * @param listener 选择变更时的回调函数
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> onSelect(Consumer<TreeItem<T>> listener) {
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) listener.accept(newVal);
        });
        return this;
    }

    /**
     * 响应式监听树形表格选中行更替变更
     *
     * @param consumer 变更回调闭包，传出 (旧选中行数据对象, 新选中行数据对象)
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> onSelectionChanged(BiConsumer<T, T> consumer) {
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (consumer != null) {
                T oldData = oldVal != null ? oldVal.getValue() : null;
                T newData = newVal != null ? newVal.getValue() : null;
                consumer.accept(oldData, newData);
            }
        });
        return this;
    }

    /**
     * 快捷绑定物理精确点击监听器
     *
     * @param clickCount 目标点击计数（1 代表单击，2 代表双击）
     * @param consumer   回调闭包，接收当前被选中的树节点包含的实体数据
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> onRowClick(int clickCount, Consumer<T> consumer) {
        setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == clickCount) {
                TreeItem<T> selectedItem = getSelectionModel().getSelectedItem();
                if (selectedItem != null && consumer != null) {
                    consumer.accept(selectedItem.getValue());
                }
            }
        });
        return this;
    }

    /**
     * 快捷绑定行双击事件
     *
     * @param consumer 回调闭包，接收当前双击树节点的行实体数据对象
     * @return FXTreeTable 实例（链式调用）
     */
    public FXTreeTable<T> onRowDoubleClick(Consumer<T> consumer) {
        return onRowClick(2, consumer);
    }

    /**
     * 递归展开所有子节点
     */
    private void expandAll(TreeItem<T> item) {
        item.setExpanded(true);
        for (TreeItem<T> child : item.getChildren()) {
            expandAll(child);
        }
    }

    /**
     * 递归折叠所有子节点
     */
    private void collapseAll(TreeItem<T> item) {
        item.setExpanded(false);
        for (TreeItem<T> child : item.getChildren()) {
            collapseAll(child);
        }
    }

    /**
     * 递归展开到指定深度
     */
    private void expandToDepth(TreeItem<T> item, int maxDepth, int currentDepth) {
        if (currentDepth <= maxDepth) {
            item.setExpanded(true);
            for (TreeItem<T> child : item.getChildren()) {
                expandToDepth(child, maxDepth, currentDepth + 1);
            }
        }
    }
}