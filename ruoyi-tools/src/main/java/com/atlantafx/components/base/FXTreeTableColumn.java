package com.atlantafx.components.base;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;

import java.util.function.Function;

/**
 * FXTreeTableColumn - 基于 AtlantaFX 风格的增强型树形表格列组件
 * 封装 JavaFX TreeTableColumn，提供链式调用支持与严格的边界安全检查
 *
 * @param <T> 表格行模型的数据实体泛型类型
 * @param <V> 当前列承载并渲染的单元格目标字段泛型类型
 */
public class FXTreeTableColumn<T, V> extends TreeTableColumn<T, V> implements IFXNode<FXTreeTableColumn<T, V>> {

    private FXTreeTableColumn(String title) {
        super(title);
    }

    public static <T, V> FXTreeTableColumn<T, V> create(String title) {
        return new FXTreeTableColumn<>(title);
    }

    public static <T, V> FXTreeTableColumn<T, V> create(String title, double width) {
        FXTreeTableColumn<T, V> col = new FXTreeTableColumn<>(title);
        col.setPrefWidth(width);
        return col;
    }

    // ==================== 核心取值与数据映射控制 ====================

    public FXTreeTableColumn<T, V> valueFactory(Function<TreeTableColumn.CellDataFeatures<T, V>, ObservableValue<V>> extractor) {
        setCellValueFactory(extractor::apply);
        return this;
    }

    public FXTreeTableColumn<T, V> rawValueFactory(Function<T, V> mapper) {
        setCellValueFactory(features -> {
            if (features.getValue() != null && features.getValue().getValue() != null) {
                return new SimpleObjectProperty<>(mapper.apply(features.getValue().getValue()));
            }
            return null;
        });
        return this;
    }

    // ==================== 几何尺寸与排版边界配置 ====================

    public FXTreeTableColumn<T, V> width(double width) {
        setPrefWidth(width);
        return this;
    }

    public FXTreeTableColumn<T, V> widthBounds(double min, double max) {
        setMinWidth(min);
        setMaxWidth(max);
        return this;
    }

    public FXTreeTableColumn<T, V> align(Pos pos) {
        setStyle(getStyle() + "-fx-alignment: " + getAlignmentCss(pos) + ";");
        return this;
    }

    /**
     * 设置列内容为居中对齐
     * 适用于数字、日期、短文本等
     *
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> center() {
        setStyle(getStyle() + "-fx-alignment: CENTER;");
        return this;
    }

    /**
     * 设置列内容为左对齐
     * 适用于大多数文本内容
     *
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> left() {
        setStyle(getStyle() + "-fx-alignment: CENTER-LEFT;");
        return this;
    }

    /**
     * 设置列内容为右对齐
     * 适用于金额、数字等
     *
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> right() {
        setStyle(getStyle() + "-fx-alignment: CENTER-RIGHT;");
        return this;
    }

    // ==================== 高阶自定义 Cell 渲染工厂 (彻底阻断 NPE 崩溃) ====================

    public FXTreeTableColumn<T, V> cellTextFactory(Function<V, String> textConverter) {
        setCellFactory(column -> new TreeTableCell<>() {
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
     * 工业级树形组件定制单元格工厂（NPE 安全修复版）
     * 完美防御 JavaFX 树形表格在大数据量快速滚动、级联折叠时因虚拟行越界导致的空指针异常
     *
     * @param cellBuilder 三元闭包业务逻辑
     * @return FXTreeTableColumn 实例
     */
    public FXTreeTableColumn<T, V> cellFactory(TriConsumer<V, TreeTableCell<T, V>, T> cellBuilder) {
        setCellFactory(column -> new TreeTableCell<>() {
            @Override
            protected void updateItem(V item, boolean empty) {
                super.updateItem(item, empty);

                // 核心安全哨兵：如果当前单元格为空，或者传入数据项本身为 null
                if (empty || item == null) {
                    clearCellStatus();
                    return;
                }

                // 防御性安全边界检查：逆向追溯底层 TreeItem 骨骼容器
                if (getTreeTableView() == null) {
                    clearCellStatus();
                    return;
                }

                TreeItem<T> treeItem = getTreeTableView().getTreeItem(getIndex());
                // 拦截越界或销毁中的虚拟树节点，直接按空处理，阻断后续的 getValue() 调用
                if (treeItem == null || treeItem.getValue() == null) {
                    clearCellStatus();
                    return;
                }

                // 安全通过哨兵核验，注入激活标记
                if (!getStyleClass().contains("fx-treetable-cell-active")) {
                    getStyleClass().add("fx-treetable-cell-active");
                }

                // 执行上层业务闭包渲染
                T rowData = treeItem.getValue();
                cellBuilder.accept(item, this, rowData);
            }

            /**
             * 提取内部公用清理逻辑，防止样式与内容重影污染
             */
            private void clearCellStatus() {
                setText(null);
                setGraphic(null);
                getStyleClass().remove("fx-treetable-cell-active");
            }
        });
        return this;
    }

    // ==================== 内部辅助工具流 ====================

    private String getAlignmentCss(Pos pos) {
        return switch (pos) {
            case CENTER -> "CENTER";
            case CENTER_LEFT -> "CENTER-LEFT";
            case CENTER_RIGHT -> "CENTER-RIGHT";
            default -> "CENTER-LEFT";
        };
    }

    /**
     * 设置单元格值为布尔值时的渲染器
     * 自动显示为复选框
     *
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> asCheckBox() {
        setCellFactory(col -> new javafx.scene.control.cell.CheckBoxTreeTableCell<>());
        return this;
    }

    /**
     * 设置单元格值为文本时的渲染器
     * 支持简单的文本显示
     *
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> asTextField() {
        setCellFactory(col -> new javafx.scene.control.cell.TextFieldTreeTableCell<>());
        return this;
    }

    /**
     * 设置单元格值为选择框时的渲染器
     *
     * @param items 可选项列表
     * @return FXTreeTableColumn 实例（链式调用）
     */
    @SuppressWarnings("unchecked")
    public FXTreeTableColumn<T, V> asComboBox(V... items) {
        setCellFactory(col -> new javafx.scene.control.cell.ComboBoxTreeTableCell<>(items));
        return this;
    }

    /**
     * 设置列标题的对齐方式
     *
     * @param pos 对齐位置枚举值
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> headerAlignment(Pos pos) {
        setStyle(getStyle() + "-fx-label-padding: 0 5 0 5; " +
                "-fx-alignment: " + getAlignmentCss(pos) + ";");
        return this;
    }

    /**
     * 设置列标题的 CSS 样式
     *
     * @param style CSS 样式字符串
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> headerStyle(String style) {
        setStyle(getStyle() + style);
        return this;
    }

    /**
     * 添加列变更监听器
     * 当列的属性（如宽度、可见性）变化时触发
     *
     * @param listener 变更监听回调
     * @return FXTreeTableColumn 实例（链式调用）
     */
    public FXTreeTableColumn<T, V> onColumnChange(Runnable listener) {
        widthProperty().addListener((obs, oldVal, newVal) -> listener.run());
        return this;
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}