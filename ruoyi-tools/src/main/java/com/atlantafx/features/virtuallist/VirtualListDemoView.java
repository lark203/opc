package com.atlantafx.features.virtuallist;

import com.atlantafx.AppContext;
import com.atlantafx.components.base.*;
import com.atlantafx.core.annotation.Page;
import com.atlantafx.core.view.BaseView;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 虚拟列表演示页面。
 * 通过三种渲染方式对比，直观体验「窗口化虚拟化」在十万级（乃至百万级）数据下的流畅度：
 * <ul>
 *   <li>{@code 自研虚拟列表}：从零实现的窗口化（Windowing）虚拟列表</li>
 *   <li>{@code JavaFX TableView}：框架内置、基于 VirtualFlow 的原生虚拟化表格</li>
 *   <li>{@code 全量渲染}：非虚拟化对照（限流 1 万行，避免直接卡死），用于对比节点爆炸的代价</li>
 * </ul>
 */
@Page(id = "virtual-list-demo", name = "虚拟列表演示", icon = "mdi2t-table",
        order = 6, level = 1, lazyLoad = true)
public class VirtualListDemoView extends BaseView {

    /** 渲染方式 */
    private enum Mode {
        CUSTOM("自研虚拟列表(窗口化)"),
        TABLE("JavaFX TableView(原生)"),
        NAIVE("全量渲染(对比·限1万)");
        final String label;
        Mode(String label) { this.label = label; }
        @Override
        public String toString() { return label; }
    }

    /** 列配置（两种引擎共用） */
    private record Col(String title, double width, Function<DemoRecord, String> value,
                       Function<DemoRecord, String> color, Pos align) {}

    private static final double ROW = 34;        // 行高
    private static final int NAIVE_CAP = 10000;  // 全量渲染对照上限

    private List<DemoRecord> allData = new ArrayList<>();
    private List<DemoRecord> filtered = new ArrayList<>();
    private Mode currentMode = Mode.CUSTOM;

    // 控件
    private FXComboBox<Integer> dataSizeCombo;
    private FXComboBox<Mode> modeCombo;
    private FXCustomTextField searchField;
    private FXVBox centerCard;
    private VirtualListPane<DemoRecord> vlist;

    // 指标
    private final FXLabel fpsLabel = FXLabel.create("FPS --").bold();
    private final FXLabel maxFrameLabel = FXLabel.create("最大帧耗时 --");
    private final FXLabel nodeLabel = FXLabel.create("常驻节点 --");
    private final FXLabel rangeLabel = FXLabel.create("可见区间 --");
    private final FXLabel verdictLabel = FXLabel.create("流畅度 --").bold();

    private IntSupplier nodeCountSupplier = () -> -1;
    private Supplier<String> rangeSupplier = () -> "--";
    private Runnable scrollBottomAction = () -> {};
    private int naiveNodeCount = 0;

    private final AnimationTimer fpsTimer = new AnimationTimer() {
        private long last = 0;
        private long windowStart = 0;
        private long frames = 0;
        private long maxDelta = 0;

        @Override
        public void handle(long now) {
            if (last == 0) { last = now; windowStart = now; return; }
            long delta = now - last;
            last = now;
            if (delta > maxDelta) maxDelta = delta;
            frames++;
            if (now - windowStart >= 500_000_000) { // 每 500ms 统计一次
                double fps = frames * 1_000_000_000.0 / (now - windowStart);
                fpsLabel.setText(String.format("FPS %.0f", fps));
                maxFrameLabel.setText(String.format("最大帧耗时 %.1f ms", maxDelta / 1_000_000.0));
                if (maxDelta <= 18_000_000) verdictLabel.text("流畅 ✅").fontColor("#3fb950");
                else if (maxDelta <= 34_000_000) verdictLabel.text("良好 ⚠").fontColor("#d29922");
                else verdictLabel.text("卡顿 ⛔").fontColor("#f85149");
                nodeLabel.setText(nodeCountSupplier.getAsInt() < 0
                        ? "JavaFX 内部虚拟化" : "常驻节点 " + nodeCountSupplier.getAsInt());
                rangeLabel.setText("可见区间 " + rangeSupplier.get());
                frames = 0;
                windowStart = now;
                maxDelta = 0;
            }
        }
    };

    @Override
    protected void onPageCreated() {
        fpsTimer.start();
    }

    @Override
    protected Node onPageInit() {
        FXVBox root = FXVBox.create(14);

        root.add(FXLabel.create("虚拟列表演示 · Virtual List").h3());
        root.add(FXLabel.create(
                "窗口化虚拟化：无论数据量多大，常驻节点仅约等于屏幕上可见的行数，滚动时每帧只更新少量节点。" +
                        "可切换渲染方式、调整数据量、拉满滚动条，结合右侧实时 FPS 与帧耗时直观感受流畅度。")
                .subTitle().wrapText(true));

        // ===== 控制栏 =====
        FXHBox controls = FXHBox.create(12).align(Pos.CENTER_LEFT);
        dataSizeCombo = FXComboBox.<Integer>create()
                .add(10_000, 100_000, 1_000_000).select(100_000).width(150)
                .onSelect(cnt -> regenerate(cnt));
        modeCombo = FXComboBox.<Mode>create()
                .add(Mode.values()).select(Mode.CUSTOM).width(240)
                .displayMapper(m -> m.label)
                .onSelect(this::showEngine);
        searchField = FXCustomTextField.create()
                .prompt("搜索 姓名 / 工号 / 部门 / 状态").width(240)
                .onEnter(e -> applyFilter());
        FXButton searchBtn = FXButton.create("搜索").accent().icon(MaterialDesignM.MAGNIFY)
                .onAction(e -> applyFilter());
        FXButton regenBtn = FXButton.create("重新生成").icon(MaterialDesignR.RESTART)
                .onAction(e -> regenerate(dataSizeCombo.getValue()));
        FXButton bottomBtn = FXButton.create("滚到底部").onAction(e -> scrollBottomAction.run());

        controls.add(
                FXLabel.create("数据量").bold(), dataSizeCombo,
                FXLabel.create("渲染方式").bold(), modeCombo,
                searchField, searchBtn, regenBtn, bottomBtn);

        // ===== 指标栏 =====
        FXHBox metrics = FXHBox.create(18).align(Pos.CENTER_LEFT);
        metrics.add(fpsLabel, maxFrameLabel, nodeLabel, rangeLabel, verdictLabel);

        // ===== 列表容器 =====
        centerCard = FXVBox.create(0)
                .border(1, "-color-border-default", 10)
                .background("-color-bg-default")
                .fillWidth(true);
        VBox.setVgrow(centerCard, Priority.ALWAYS);

        root.add(controls, metrics, centerCard);

        // 首次进入默认生成 10 万条（后台线程，避免阻塞 UI）
        regenerate(100_000);

        return root;
    }

    @Override
    protected void onPageDispose() {
        fpsTimer.stop();
        centerCard.getChildren().clear();
        allData = null;
        filtered = null;
    }

    // ============================ 数据与过滤 ============================

    private void regenerate(int count) {
        AppContext.startLoading("正在生成 " + count + " 条数据…");
        int finalCount = count;
        new Thread(() -> {
            List<DemoRecord> data = DemoRecord.generate(finalCount);
            Platform.runLater(() -> {
                allData = data;
                applyFilter();
                AppContext.stopLoading("已生成 " + finalCount + " 条");
                AppContext.showNotification("已生成 " + finalCount + " 条演示数据",
                        com.atlantafx.core.constant.NotificationLevel.SUCCESS);
            });
        }).start();
    }

    private void applyFilter() {
        String kw = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            filtered = new ArrayList<>(allData);
        } else {
            filtered = new ArrayList<>();
            for (DemoRecord r : allData) {
                if (r.name.toLowerCase().contains(kw)
                        || r.id.toLowerCase().contains(kw)
                        || r.department.toLowerCase().contains(kw)
                        || r.status.toLowerCase().contains(kw)) {
                    filtered.add(r);
                }
            }
        }
        showEngine(currentMode);
    }

    // ============================ 渲染引擎 ============================

    private List<Col> columns() {
        return List.of(
                new Col("序号", 90, r -> String.valueOf(r.seq), null, Pos.CENTER),
                new Col("工号", 120, r -> r.id, null, Pos.CENTER),
                new Col("姓名", 150, r -> r.name, null, Pos.CENTER_LEFT),
                new Col("部门", 150, r -> r.department, null, Pos.CENTER_LEFT),
                new Col("状态", 100, r -> r.status, r -> DemoRecord.statusColor(r.status), Pos.CENTER),
                new Col("绩效评分", 110, r -> String.valueOf(r.score), null, Pos.CENTER_RIGHT),
                new Col("月度额度", 160, r -> "¥" + String.format("%,.2f", r.amount), null, Pos.CENTER_RIGHT),
                new Col("入职日期", 160, r -> r.createdAt, null, Pos.CENTER)
        );
    }

    private void showEngine(Mode mode) {
        currentMode = mode;
        centerCard.getChildren().clear();
        Node engine;
        switch (mode) {
            case CUSTOM -> {
                vlist = new VirtualListPane<>();
                List<VirtualListPane.ColumnDef<DemoRecord>> defs = new ArrayList<>();
                for (Col c : columns()) {
                    defs.add(new VirtualListPane.ColumnDef<>(
                            c.title(), c.width(), c.value(), c.color(), c.align()));
                }
                vlist.setRowHeight(ROW).setBufferRows(10).setColumns(defs);
                vlist.setItems(filtered);
                engine = vlist;
                nodeCountSupplier = () -> vlist.renderedCountProperty().get();
                rangeSupplier = vlist::getRangeText;
                scrollBottomAction = vlist::scrollToBottom;
            }
            case TABLE -> {
                FXTableView<DemoRecord> table = FXTableView.create();
                table.fixedCellSize(ROW).striped().dense().flexLastColumn().vgrow();
                for (Col c : columns()) {
                    table.addColumn(c.title(),
                            (DemoRecord r) -> new SimpleStringProperty(c.value().apply(r)),
                            col -> col.width(c.width()).align(c.align()));
                }
                table.items(filtered);
                engine = table;
                nodeCountSupplier = () -> -1;
                rangeSupplier = () -> "JavaFX 原生 VirtualFlow 虚拟化";
                int lastIdx = Math.max(0, filtered.size() - 1);
                scrollBottomAction = () -> table.scrollTo(lastIdx);
            }
            case NAIVE -> {
                if (filtered.size() > NAIVE_CAP) {
                    AppContext.showNotification(
                            "全量渲染已限流至 " + NAIVE_CAP + " 行（10 万+ 全量节点会直接卡死，此处仅为对比）",
                            com.atlantafx.core.constant.NotificationLevel.WARNING);
                }
                engine = buildNaive(filtered);
                nodeCountSupplier = () -> naiveNodeCount;
                rangeSupplier = () -> "1 - " + naiveNodeCount + " / " + filtered.size()
                        + (filtered.size() > NAIVE_CAP ? "（已限流）" : "");
                scrollBottomAction = () -> {
                    if (engine instanceof ScrollPane sp) sp.setVvalue(1.0);
                };
            }
            default -> engine = new VBox();
        }
        centerCard.add(engine);
        VBox.setVgrow(engine, Priority.ALWAYS);
    }

    /** 非虚拟化对照：为前 NAIVE_CAP 行逐行创建真实节点（用于对比节点爆炸的代价） */
    private Node buildNaive(List<DemoRecord> data) {
        ScrollPane sp = new ScrollPane();
        VBox box = new VBox(0);
        sp.setFitToWidth(true);
        sp.setContent(box);
        int cap = Math.min(data.size(), NAIVE_CAP);
        naiveNodeCount = cap;
        List<Col> cols = columns();
        for (int i = 0; i < cap; i++) {
            DemoRecord r = data.get(i);
            HBox row = new HBox();
            row.setPrefHeight(ROW);
            row.setMinHeight(ROW);
            row.setMaxHeight(ROW);
            for (Col c : cols) {
                Label lbl = new Label(c.value().apply(r));
                lbl.setPrefWidth(c.width());
                lbl.setMinWidth(c.width());
                lbl.setMaxWidth(c.width());
                lbl.setAlignment(c.align());
                lbl.setPadding(new Insets(0, 8, 0, 8));
                if (c.color() != null) {
                    String col = c.color().apply(r);
                    if (col != null) lbl.setStyle("-fx-text-fill:" + col + ";");
                }
                row.getChildren().add(lbl);
            }
            row.setStyle((i % 2 == 0)
                    ? "-fx-background-color:transparent;"
                    : "-fx-background-color:rgba(125,125,125,0.07);");
            box.getChildren().add(row);
        }
        return sp;
    }
}
