package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * FXGridPane - 网格布局容器
 * 继承自 JavaFX GridPane，实现 IFXNode 接口支持链式调用
 * 按行列组织子节点，适合表单、表格等场景
 */
public class FXGridPane extends GridPane implements IFXNode<FXGridPane> {

    /**
     * 默认构造函数
     */
    public FXGridPane() {
        super();
    }

    /**
     * 创建空白网格布局实例
     *
     * @return FXGridPane 实例
     */
    public static FXGridPane create() {
        return new FXGridPane();
    }

    // ==================== 子节点矩阵精确放置 API ====================

    /**
     * 向网格中追加一个子节点并精确定位其行列坐标
     *
     * @param child  目标子节点
     * @param column 列索引（从 0 开始）
     * @param row    行索引（从 0 开始）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane addNode(Node child, int column, int row) {
        if (child != null) {
            super.add(child, column, row);
        }
        return this;
    }

    /**
     * 向网格中追加一个子节点并指定其跨行与跨列单元格约束
     *
     * @param child   目标子节点
     * @param column  列索引
     * @param row     行索引
     * @param colspan 跨列数
     * @param rowspan 跨行数
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane addNode(Node child, int column, int row, int colspan, int rowspan) {
        if (child != null) {
            super.add(child, column, row, colspan, rowspan);
        }
        return this;
    }

    /**
     * 批量添加垂直节点流（每个节点独占一行，默认放置在第 0 列）
     *
     * @param nodes 节点数组（按数组顺序从第 0 行往下排）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane addRows(Node... nodes) {
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    super.add(nodes[i], 0, i);
                }
            }
        }
        return this;
    }

    /**
     * 批量添加水平节点流（每个节点独占一列，默认放置在第 0 行）
     *
     * @param nodes 节点数组（按数组顺序从第 0 列往右排）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane addColumns(Node... nodes) {
        if (nodes != null) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] != null) {
                    super.add(nodes[i], i, 0);
                }
            }
        }
        return this;
    }

    /**
     * 批量移除指定的子节点
     *
     * @param nodes 要移除的节点数组
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane remove(Node... nodes) {
        if (nodes != null) {
            getChildren().removeAll(nodes);
        }
        return this;
    }

    /**
     * 清空网格内所有的子节点和行列约束
     *
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane clear() {
        getChildren().clear();
        getColumnConstraints().clear();
        getRowConstraints().clear();
        return this;
    }

    // ==================== 几何间距、对齐与限界控制 ====================

    /**
     * 设置容器的对齐方式
     *
     * @param pos 对齐位置枚举值
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane align(Pos pos) {
        setAlignment(pos);
        return this;
    }

    /**
     * 设置水平方向的单元格间距（列间距）
     *
     * @param value 间距像素值
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane hgap(double value) {
        setHgap(value);
        return this;
    }

    /**
     * 设置垂直方向的单元格间距（行间距）
     *
     * @param value 间距像素值
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane vgap(double value) {
        setVgap(value);
        return this;
    }

    /**
     * 同时设置网格的水平和垂直间距
     *
     * @param hgap 列间距像素值
     * @param vgap 行间距像素值
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane gap(double hgap, double vgap) {
        setHgap(hgap);
        setVgap(vgap);
        return this;
    }

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置容器各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    public FXGridPane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置容器宽度
     *
     * @param w 宽度值（像素）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度
     *
     * @param h 高度值（像素）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    public FXGridPane mxWidth(double w) {
        setMaxWidth(w);
        return this;
    }

    public FXGridPane mxHeight(double h) {
        setMaxHeight(h);
        return this;
    }

    // ==================== 物理行列行为高级硬性约束 ====================

    /**
     * 快捷锁定某一特定列的绝对物理宽度
     *
     * @param columnIndex 列索引
     * @param width       锁定的固定宽度像素值
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane fixedColumnWidth(int columnIndex, double width) {
        while (getColumnConstraints().size() <= columnIndex) {
            getColumnConstraints().add(new ColumnConstraints());
        }
        ColumnConstraints cc = getColumnConstraints().get(columnIndex);
        cc.setMinWidth(width);
        cc.setPrefWidth(width);
        cc.setMaxWidth(width);
        return this;
    }

    /**
     * 快捷设置某一特定列的百分比相对宽度（常用于响应式通栏布局）
     *
     * @param columnIndex 列索引
     * @param percentage  所占容器百分比（0.0 ~ 100.0）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane percentColumnWidth(int columnIndex, double percentage) {
        while (getColumnConstraints().size() <= columnIndex) {
            getColumnConstraints().add(new ColumnConstraints());
        }
        ColumnConstraints cc = getColumnConstraints().get(columnIndex);
        cc.setPercentWidth(percentage);
        return this;
    }

    /**
     * 快捷配置某一特定列的水平伸展优先级
     *
     * @param columnIndex 列索引
     * @param priority    增长优先级枚举（如 Priority.ALWAYS）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane columnHGrow(int columnIndex, Priority priority) {
        while (getColumnConstraints().size() <= columnIndex) {
            getColumnConstraints().add(new ColumnConstraints());
        }
        getColumnConstraints().get(columnIndex).setHgrow(priority);
        return this;
    }

    /**
     * 快捷配置某一特定行的垂直伸展优先级
     *
     * @param rowIndex 行索引
     * @param priority 增长优先级枚举
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane rowVGrow(int rowIndex, Priority priority) {
        while (getRowConstraints().size() <= rowIndex) {
            getRowConstraints().add(new RowConstraints());
        }
        getRowConstraints().get(rowIndex).setVgrow(priority);
        return this;
    }

    /**
     * 设置行高约束
     *
     * @param rowIndex    行索引
     * @param constraints 行约束对象
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane rowConstraints(int rowIndex, RowConstraints constraints) {
        getRowConstraints().add(rowIndex, constraints);
        return this;
    }


    /**
     * 设置固定行高
     *
     * @param rowIndex 行索引
     * @param height   高度（像素）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane fixedRowHeight(int rowIndex, double height) {
        RowConstraints constraints = new RowConstraints(height);
        return rowConstraints(rowIndex, constraints);
    }

    /**
     * 设置百分比行高
     *
     * @param rowIndex 行索引
     * @param percent  百分比（0-100）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane percentRowHeight(int rowIndex, double percent) {
        RowConstraints constraints = new RowConstraints();
        constraints.setPercentHeight(percent);
        return rowConstraints(rowIndex, constraints);
    }


    // ==================== Node 通用行为与扩展 ====================

    /**
     * 设置 CSS ID
     *
     * @param id CSS ID 标识符
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane id(String id) {
        setId(id);
        return this;
    }

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    public FXGridPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    // ==================== AtlantaFX 高级样式定制 ====================

    /**
     * 安全设置网格面板背景色
     * 同时复写 JavaFX 的背景色及 AtlantaFX 的全局核心变量，以保证在明暗主题切换时样式权重正常
     *
     * @param color CSS 颜色字符串（如 "#FFFFFF" 或主题变量 "-color-bg-default"）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane background(String color) {
        if (color == null || color.isBlank()) return this;
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 快捷设置网格面板边框颜色、线宽与整体圆角
     *
     * @param width  边框线粗细（像素）
     * @param color  CSS 格式边框颜色
     * @param radius 圆角半径（像素）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane border(double width, String color, double radius) {
        String style = String.format(
                "-fx-border-width: %spx; -fx-border-color: %s; -fx-border-radius: %spx; -fx-background-radius: %spx;",
                width, color, radius, radius
        );
        return styleCss(style);
    }

    /**
     * 快捷转化为带有 AtlantaFX Subtle 质感边框的卡片化底面板
     *
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane bgCardStyle() {
        border(1, "-color-border-default", 8);
        return background("-color-bg-default");
    }

    /**
     * 为当前面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATION_SMALL），2级适中（Styles.ELEVATION_MEDIUM），3级最深（Styles.ELEVATION_LARGE）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane shadow(int level) {
        // 先行清理可能存在的旧阴影伪类
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3, Styles.ELEVATED_4);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
            case 4 -> stylesClass(Styles.ELEVATED_4);
        }
        return this;
    }

    // ==================== 快捷高级结构网格排版模板 ====================

    /**
     * 一键转化为经典的标准双列重型表单排版架构
     * 结构特征：第 0 列为标签引导列，宽度锁死固定；第 1 列为输入组件列，通栏百分百自适应水平撑满整个视窗
     *
     * @param labelWidth 引导标签列的绝对物理宽度（像素值，如 120.0）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane asForm(double labelWidth) {
        return gap(12, 12)
                .padding(15)
                .fixedColumnWidth(0, labelWidth)
                .percentColumnWidth(1, 100.0)
                .columnHGrow(1, Priority.ALWAYS);
    }

    /**
     * 一键转化为多媒体等分画廊或指标看板（Dashboard Matrix）排版架构
     * 通过自动注入均等百分比切分，使网格容器中包含的每一列都具备完全绝对对称的宽度比重
     *
     * @param columnsCount 期望强制划分的总列数（例如画廊需要 4 列等分，则传入 4）
     * @return FXGridPane 实例（链式调用）
     */
    public FXGridPane asGallery(int columnsCount) {
        if (columnsCount <= 0) return this;
        getColumnConstraints().clear();
        double equalPercentage = 100.0 / columnsCount;
        for (int i = 0; i < columnsCount; i++) {
            percentColumnWidth(i, equalPercentage)
                    .columnHGrow(i, Priority.ALWAYS);
        }
        return gap(15, 15).padding(10);
    }
}
