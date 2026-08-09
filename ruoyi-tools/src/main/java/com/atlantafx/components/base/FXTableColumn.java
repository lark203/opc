package com.atlantafx.components.base;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * FXTableColumn - 表格列组件
 * 继承自 JavaFX TableColumn，实现 IFXNode 接口支持链式调用
 * 提供便捷的列宽控制、对齐方式和单元格渲染方法
 * <p>
 */
public class FXTableColumn<T, V> extends TableColumn<T, V> implements IFXNode<FXTableColumn<T, V>> {

    /**
     * 创建表格列
     *
     * @param title 列标题文本
     */
    private FXTableColumn(String title) {
        super(title);
    }

    /**
     * 创建标准表格列组件的静态工厂方法
     *
     * @param title 列标题文本
     * @param <T>   数据实体泛型类型
     * @param <V>   目标字段泛型类型
     * @return FXTableColumn 实例
     */
    public static <T, V> FXTableColumn<T, V> create(String title) {
        return new FXTableColumn<>(title);
    }

    /**
     * 创建带固定预设列宽的表格列组件静态工厂方法
     *
     * @param title 列标题文本
     * @param width 预设列宽度（像素值）
     * @param <T>   数据实体泛型类型
     * @param <V>   目标字段泛型类型
     * @return FXTableColumn 实例
     */
    public static <T, V> FXTableColumn<T, V> create(String title, double width) {
        FXTableColumn<T, V> col = new FXTableColumn<>(title);
        col.setPrefWidth(width);
        return col;
    }

    /**
     * 设置列的最小宽度
     * 限制列可调整的最小尺寸
     *
     * @param w 最小宽度值（像素）
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> minWidth(double w) {
        setMinWidth(w);
        return this;
    }

    /**
     * 设置列的最大宽度
     * 限制列可调整的最大尺寸
     *
     * @param w 最大宽度值（像素）
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> maxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    /**
     * 设置列为固定宽度
     * 列宽不可调整，始终保持在指定值
     *
     * @param w 固定宽度值（像素）
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> fixedWidth(double w) {
        setMinWidth(w);
        setMaxWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置列内容为居中对齐
     * 适用于数字、日期、短文本等
     *
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> center() {
        setStyle(getStyle() + "-fx-alignment: CENTER;");
        return this;
    }

    /**
     * 设置列内容为左对齐
     * 适用于大多数文本内容
     *
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> left() {
        setStyle(getStyle() + "-fx-alignment: CENTER-LEFT;");
        return this;
    }

    /**
     * 设置列内容为右对齐
     * 适用于金额、数字等
     *
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> right() {
        setStyle(getStyle() + "-fx-alignment: CENTER-RIGHT;");
        return this;
    }

    /**
     * 设置列是否可见
     * 隐藏的列仍然存在于数据模型中，只是不显示
     *
     * @param visible true-可见，false-隐藏
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置列是否可编辑
     *
     * @param editable true-可编辑，false-不可编辑
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    /**
     * 设置列的 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置单元格工厂
     * 自定义单元格的渲染方式
     *
     * @param factory 单元格工厂函数
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> cellFactory(Callback<TableColumn<T, V>, TableCell<T, V>> factory) {
        setCellFactory(factory);
        return this;
    }

    /**
     * 设置单元格值为布尔值时的渲染器
     * 自动显示为复选框
     *
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> asCheckBox() {
        setCellFactory(col -> new javafx.scene.control.cell.CheckBoxTableCell<>());
        return this;
    }

    /**
     * 设置单元格值为文本时的渲染器
     * 支持简单的文本显示
     *
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> asTextField() {
        setCellFactory(col -> new javafx.scene.control.cell.TextFieldTableCell<>());
        return this;
    }

    /**
     * 设置单元格值为选择框时的渲染器
     *
     * @param items 可选项列表
     * @return FXTableColumn 实例（链式调用）
     */
    @SuppressWarnings("unchecked")
    public FXTableColumn<T, V> asComboBox(V... items) {
        setCellFactory(col -> new javafx.scene.control.cell.ComboBoxTableCell<>(items));
        return this;
    }

    /**
     * 设置列标题的对齐方式
     *
     * @param pos 对齐位置枚举值
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> headerAlignment(Pos pos) {
        setStyle(getStyle() + "-fx-label-padding: 0 5 0 5; " +
                "-fx-alignment: " + getAlignmentCss(pos) + ";");
        return this;
    }

    /**
     * 设置列标题的 CSS 样式
     *
     * @param style CSS 样式字符串
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> headerStyle(String style) {
        setStyle(getStyle() + style);
        return this;
    }

    /**
     * 添加列变更监听器
     * 当列的属性（如宽度、可见性）变化时触发
     *
     * @param listener 变更监听回调
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> onColumnChange(Runnable listener) {
        widthProperty().addListener((obs, oldVal, newVal) -> listener.run());
        return this;
    }

    // ==================== 辅助方法 ====================

    /**
     * 将 Pos 枚举转换为 CSS 对齐字符串
     */
    private String getAlignmentCss(Pos pos) {
        if (pos == Pos.CENTER) return "CENTER";
        if (pos == Pos.CENTER_LEFT) return "CENTER-LEFT";
        if (pos == Pos.CENTER_RIGHT) return "CENTER-RIGHT";
        if (pos == Pos.TOP_LEFT || pos == Pos.BOTTOM_LEFT) return "TOP-LEFT";
        if (pos == Pos.TOP_RIGHT || pos == Pos.BOTTOM_RIGHT) return "TOP-RIGHT";
        if (pos == Pos.TOP_CENTER || pos == Pos.BOTTOM_CENTER) return "TOP-CENTER";
        return "CENTER";
    }

    // ==================== 核心取值与数据映射控制 ====================

    /**
     * 高效值提取器配置（Value Factory）。
     * 无需编写笨重的属性包装类，直接通过函数式接口映射 POJO 实体的具体字段。
     *
     * @param extractor 从行数据实体 T 中提取观察值 ObservableValue<V> 的映射函数
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> valueFactory(Function<TableColumn.CellDataFeatures<T, V>, ObservableValue<V>> extractor) {
        setCellValueFactory(extractor::apply);
        return this;
    }

    /**
     * 轻量级实体对象值直接提取映射器。
     * 当实体属性为普通标准数据类型而非 JavaFX Property 属性包装时使用，自动内嵌 SimpleObjectProperty 转化流。
     *
     * @param mapper 从行实体 T 中计算转化出裸值 V 的映射函数
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> rawValueFactory(Function<T, V> mapper) {
        setCellValueFactory(features -> {
            if (features.getValue() != null) {
                return new SimpleObjectProperty<>(mapper.apply(features.getValue()));
            }
            return null;
        });
        return this;
    }

    // ==================== 几何尺寸与排版边界配置 ====================

    /**
     * 配置当前列的固定/首选宽度
     *
     * @param width 宽度（像素）
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> width(double width) {
        setPrefWidth(width);
        return this;
    }

    /**
     * 锁定列宽边界范围
     *
     * @param min 最小宽度
     * @param max 最大宽度
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> widthBounds(double min, double max) {
        setMinWidth(min);
        setMaxWidth(max);
        return this;
    }

    /**
     * 配置表格列是否允许用户通过鼠标拖动边缘调整宽度
     *
     * @param resizable true-允许拖拽调宽，false-锁定列宽
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> resizable(boolean resizable) {
        setResizable(resizable);
        return this;
    }

    /**
     * 设置表格列是否允许点击表头参与排序规则计算
     *
     * @param sortable true-允许排序，false-禁止排序
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> sortable(boolean sortable) {
        setSortable(sortable);
        return this;
    }

    /**
     * 配置列内单元格文本或图形内容的几何对齐方位
     *
     * @param pos 对齐方位枚举
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> align(Pos pos) {
        setStyle(getStyle() + "-fx-alignment: " + getAlignmentCss(pos) + ";");
        return this;
    }

    // ==================== 高阶自定义 Cell 渲染工厂 (防止复用污染) ====================

    /**
     * 轻量化纯文本格式化映射单元格工厂。
     * 用于解决数据结构内部裸值至 UI 展示字符串之间的快速无状态转化（例如日期格式化、枚举翻译等）。
     *
     * @param textConverter 文本转化映射器，传入字段裸值 V，返回目标展示字符串 String
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> cellTextFactory(Function<V, String> textConverter) {
        setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(V item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(textConverter.apply(item));
                    setGraphic(null);
                }
            }
        });
        return this;
    }

    /**
     * 工业级函数式卡片/节点流定制单元格工厂。
     * 完美隔离并破解了 JavaFX 在大数据量滚动时，由于 TableCell 虚拟池化复用（Reuse机制）带来的「数据错位与样式残留」Bug。
     * 在 empty 分支下对文本、图形节点进行绝对清空，并剥离业务专属的激活伪类标记类（`fx-table-cell-active`）。
     *
     * @param cellBuilder 闭包业务逻辑，注入当前格字段值 V、对应的 TableCell 容器以及当前行完整的实体数据包 T
     * @return FXTableColumn 实例（链式调用）
     */
    public FXTableColumn<T, V> cellFactory(TriConsumer<V, TableCell<T, V>, T> cellBuilder) {
        setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(V item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    // 彻底清除复用带来的业务 Class 污染痕迹
                    getStyleClass().remove("fx-table-cell-active");
                } else {
                    if (!getStyleClass().contains("fx-table-cell-active")) {
                        getStyleClass().add("fx-table-cell-active");
                    }
                    // 逆向追溯获取当前这一行对应的完整 POJO 数据模型
                    T rowData = getTableView().getItems().get(getIndex());
                    cellBuilder.accept(item, this, rowData);
                }
            }
        });
        return this;
    }

    // ==================== 内部辅助工具流 ====================

    /**
     * 内部高阶三元函数式特征接口
     */
    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
