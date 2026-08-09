package com.atlantafx.features.virtuallist;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 从零实现的「虚拟列表」组件（窗口化 / Windowing 技术）。
 * <p>
 * 原理：内容层 {@code canvas} 的高度被设置为 {@code 总行数 × 行高}，从而撑出正确的滚动条；
 * 但单元格节点并非为每一行创建，而是只维护一个大小为「可见行数 + 缓冲」的对象池，
 * 滚动时仅更新池中节点展示的数据并将其重新定位到对应 Y 坐标。
 * 因此无论数据量是 1 万还是 100 万，常驻节点数都约等于屏幕可见行数（约 20~40 个），
 * 滚动时每帧只需要更新这些节点的文本，从而保持极低的内存占用与极高的流畅度。
 *
 * @param <T> 行数据类型
 */
public class VirtualListPane<T> extends VBox {

    /** 列定义 */
    public static class ColumnDef<T> {
        final String title;
        final double width;                 // 固定列宽；传 -1 表示弹性拉伸（占据剩余宽度）
        final Function<T, String> text;     // 取该列展示文本
        final Function<T, String> color;    // 可选：根据行数据返回文本颜色（CSS 颜色字符串）
        final Pos align;                    // 单元格内对齐方式

        public ColumnDef(String title, double width, Function<T, String> text,
                         Function<T, String> color, Pos align) {
            this.title = title;
            this.width = width;
            this.text = text;
            this.color = color;
            this.align = align;
        }

        public ColumnDef(String title, double width, Function<T, String> text, Pos align) {
            this(title, width, text, null, align);
        }
    }

    private final ScrollPane scroll = new ScrollPane();
    private final Pane canvas = new Pane();          // 内容层：高度 = 总行数 × 行高
    private final HBox header = new HBox();          // 表头（固定，不随滚动移动）
    private final List<Label> headerCells = new ArrayList<>();
    private final List<ColumnDef<T>> columns = new ArrayList<>();
    private List<T> items = new ArrayList<>();

    private double rowHeight = 32;
    private int bufferRows = 8;                       // 上下额外缓冲行数，避免快速滚动露白
    private boolean showHeader = true;

    private final List<HBox> pool = new ArrayList<>(); // 单元格对象池

    // 对外暴露的实时指标，供演示面板读取
    private final IntegerProperty renderedCount = new SimpleIntegerProperty(0);
    private final StringProperty rangeText = new SimpleStringProperty("0 - 0 / 0");

    public VirtualListPane() {
        super(4);
        setFillWidth(true);

        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        canvas.setMinHeight(0);
        scroll.setContent(canvas);

        if (showHeader) {
            header.setFillHeight(true);
            header.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(header);
        }
        getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // 任何可能影响窗口的几何变化都触发重算
        scroll.vvalueProperty().addListener((o, ov, nv) -> updateWindow());
        scroll.widthProperty().addListener((o, ov, nv) -> {
            applyColumnWidths();
            updateWindow();
        });
        scroll.heightProperty().addListener((o, ov, nv) -> updateWindow());
        canvas.widthProperty().addListener((o, ov, nv) -> {
            applyColumnWidths();
            updateWindow();
        });
    }

    // ============================ 对外配置 API ============================

    public VirtualListPane<T> setColumns(List<ColumnDef<T>> cols) {
        this.columns.clear();
        this.columns.addAll(cols);
        buildHeader();
        applyColumnWidths();
        return this;
    }

    public VirtualListPane<T> setRowHeight(double rh) {
        this.rowHeight = rh;
        return this;
    }

    public VirtualListPane<T> setBufferRows(int buffer) {
        this.bufferRows = Math.max(0, buffer);
        return this;
    }

    public VirtualListPane<T> setHeaderVisible(boolean visible) {
        this.showHeader = visible;
        header.setManaged(visible);
        header.setVisible(visible);
        return this;
    }

    /** 设置数据源（全量列表）。内部只引用引用，不会复制。 */
    public void setItems(List<T> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
        scroll.setVvalue(0);
        // 首帧布局可能尚未完成，延后到下一帧确保视口尺寸已知
        updateWindow();
        javafx.application.Platform.runLater(this::updateWindow);
    }

    public IntegerProperty renderedCountProperty() {
        return renderedCount;
    }

    public StringProperty rangeTextProperty() {
        return rangeText;
    }

    /** 当前可见区间文本，形如 "101 - 140 / 100000" */
    public String getRangeText() {
        return rangeText.get();
    }

    /** 当前常驻（已实例化）单元格数量 */
    public int getRenderedCount() {
        return renderedCount.get();
    }

    /** 跳转到列表底部（用于测试大跨度滚动流畅度） */
    public void scrollToBottom() {
        scroll.setVvalue(1.0);
    }

    // ============================ 内部实现 ============================

    private void buildHeader() {
        header.getChildren().clear();
        headerCells.clear();
        for (ColumnDef<T> col : columns) {
            Label title = new Label(col.title);
            title.setStyle("-fx-font-weight:bold; -fx-text-fill:-color-fg-default;");
            title.setPadding(new Insets(0, 8, 0, 8));
            title.setAlignment(col.align);
            title.setPrefHeight(rowHeight);
            title.setMinHeight(rowHeight);
            header.getChildren().add(title);
            headerCells.add(title);
        }
        header.setStyle("-fx-background-color:-color-bg-inset; -fx-border-color:-color-border-default; " +
                "-fx-border-width:0 0 1 0;");
    }

    /** 依据当前可用宽度计算每一列的实际像素宽度（含弹性列） */
    private double[] computeWidths(double totalWidth) {
        double fixed = 0;
        int flexCount = 0;
        for (ColumnDef<T> c : columns) {
            if (c.width > 0) fixed += c.width;
            else flexCount++;
        }
        double flex = (flexCount > 0 && totalWidth > fixed)
                ? (totalWidth - fixed) / flexCount
                : 0;
        double[] w = new double[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            w[i] = columns.get(i).width > 0 ? columns.get(i).width : Math.max(60, flex);
        }
        return w;
    }

    private void applyColumnWidths() {
        double w = canvas.getWidth() > 0 ? canvas.getWidth() : scroll.getWidth();
        if (w <= 0) return;
        double[] widths = computeWidths(w);
        for (int i = 0; i < headerCells.size(); i++) {
            Label hc = headerCells.get(i);
            hc.setPrefWidth(widths[i]);
            hc.setMinWidth(widths[i]);
            hc.setMaxWidth(widths[i]);
        }
    }

    /** 创建（或复用）一个单元格行节点 */
    private HBox createCell() {
        HBox row = new HBox();
        row.setFillHeight(true);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPrefHeight(rowHeight);
        row.setMinHeight(rowHeight);
        row.setMaxHeight(rowHeight);
        for (int c = 0; c < columns.size(); c++) {
            Label lbl = new Label();
            lbl.setPadding(new Insets(0, 8, 0, 8));
            lbl.setPrefHeight(rowHeight);
            lbl.setMinHeight(rowHeight);
            HBox.setHgrow(lbl, Priority.NEVER);
            row.getChildren().add(lbl);
        }
        canvas.getChildren().add(row);
        return row;
    }

    private void fillCell(HBox row, T item, int index, double[] widths) {
        for (int c = 0; c < columns.size(); c++) {
            Label lbl = (Label) row.getChildren().get(c);
            ColumnDef<T> col = columns.get(c);
            lbl.setText(col.text.apply(item));
            lbl.setPrefWidth(widths[c]);
            lbl.setMinWidth(widths[c]);
            lbl.setMaxWidth(widths[c]);
            lbl.setAlignment(col.align);
            if (col.color != null) {
                String color = col.color.apply(item);
                lbl.setStyle(color != null
                        ? "-fx-text-fill:" + color + ";"
                        : "-fx-text-fill:-color-fg-default;");
            }
        }
        // 隔行底色
        String bg = (index % 2 == 0) ? "transparent" : "rgba(125,125,125,0.07)";
        row.setStyle("-fx-background-color:" + bg + ";");
    }

    /** 核心：根据当前滚动位置，只渲染可见窗口内的单元格 */
    private void updateWindow() {
        int total = items.size();
        double totalH = total * rowHeight;
        canvas.setMinHeight(totalH);
        canvas.setPrefHeight(totalH);

        double vh = scroll.getViewportBounds().getHeight();
        if (vh <= 0) {
            vh = scroll.getHeight() - (showHeader ? header.getHeight() : 0);
        }
        if (vh <= 0) vh = 400; // 兜底，等待布局完成

        double maxOffset = Math.max(0, totalH - vh);
        double offset = scroll.getVvalue() * maxOffset;

        int first = (int) Math.floor(offset / rowHeight) - bufferRows;
        if (first < 0) first = 0;
        int visible = (int) Math.ceil(vh / rowHeight) + 1;
        int last = first + visible + bufferRows * 2;
        if (last > total) last = total;

        double[] widths = computeWidths(canvas.getWidth() > 0 ? canvas.getWidth() : scroll.getWidth());

        int needed = Math.max(0, last - first);
        while (pool.size() < needed) {
            pool.add(createCell());
        }

        int idx = 0;
        for (int i = first; i < last; i++, idx++) {
            HBox cell = pool.get(idx);
            fillCell(cell, items.get(i), i, widths);
            cell.setLayoutY(i * rowHeight);
            cell.setVisible(true);
        }
        for (int k = idx; k < pool.size(); k++) {
            pool.get(k).setVisible(false);
        }

        renderedCount.set(needed);
        rangeText.set(total == 0
                ? "0 - 0 / 0"
                : (first + 1) + " - " + last + " / " + total);
    }
}
