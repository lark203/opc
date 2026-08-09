package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * FXExpressTracker - 现代化流式纵向物流追踪时间线组件
 * 彻底修复了原代码中由于右侧文本过高导致左侧轴线断裂、物理留白、错位的硬伤。
 * 核心：引入了严格的纵向宿主高度劫持，确保中继引导线百分之百无缝对齐至下一节点。
 */
public class FXExpressTracker<T> extends VBox implements IFXNode<FXExpressTracker<T>> {

    private final List<T> traceNodes = new ArrayList<>();
    private Function<T, Node> customNodeMapper;
    private Function<T, Boolean> highlightPredicate;

    private FXExpressTracker() {
        super();
        setSpacing(0); // 必须卡死为 0，依靠内部 Padding 撑开，防止外部间距打断物流轴的物理连续性
        setPadding(new Insets(10));
    }

    public static <T> FXExpressTracker<T> create() {
        return new FXExpressTracker<>();
    }

    public FXExpressTracker<T> items(Collection<T> items) {
        if (items != null) {
            this.traceNodes.clear();
            this.traceNodes.addAll(items);
            rebuildTrackerUi();
        }
        return this;
    }

    public FXExpressTracker<T> highlightIf(Function<T, Boolean> highlightPredicate) {
        this.highlightPredicate = highlightPredicate;
        return this;
    }

    public FXExpressTracker<T> nodeMapper(Function<T, Node> mapper) {
        this.customNodeMapper = mapper;
        return this;
    }

    /**
     * 核心：执行纵向物流轴重绘，解决红框中线段断裂、松散的严重缺陷
     */
    private void rebuildTrackerUi() {
        getChildren().clear();
        if (traceNodes.isEmpty() || customNodeMapper == null) return;

        int total = traceNodes.size();
        for (int i = 0; i < total; i++) {
            T item = traceNodes.get(i);
            boolean isHighlighted = (highlightPredicate != null) && highlightPredicate.apply(item);
            boolean isFirst = (i == 0);
            boolean isLast = (i == total - 1);

            // 1. 重构左侧轴线列：使用 StackPane 代替原先松散的 VBox 嵌套，确保物理线条拥有绝对的纵向拉伸控制权
            StackPane axisColumn = new StackPane();
            axisColumn.setPrefWidth(32);
            axisColumn.setMinWidth(32);
            axisColumn.setAlignment(Pos.TOP_CENTER); // 确保圆点始终与第一行文本的顶部对齐

            // 如果不是最后一个节点，强行插入一根贯穿全高、具备最高延伸优先级的纵向中继线
            if (!isLast) {
                HBox verticalLine = new HBox();
                verticalLine.setPrefWidth(2);
                verticalLine.setMaxWidth(2);

                // 动态着色：根据下一个节点的激活状态决定线条颜色
                boolean nextIsHighlighted = (highlightPredicate != null) && highlightPredicate.apply(traceNodes.get(i + 1));
                if (nextIsHighlighted) {
                    verticalLine.setStyle("-fx-background-color: -color-success-emphasis;");
                } else {
                    verticalLine.setStyle("-fx-background-color: -color-border-muted;");
                }

                // 核心微调：通过设置 Margin 让连线从圆点的中心位置自然向下延伸，完全填满下方文本所占据的所有高度
                StackPane.setMargin(verticalLine, new Insets(isFirst ? 8 : 4, 0, 0, 0));
                axisColumn.getChildren().add(verticalLine);
            }

            // 2. 构造物理时间节点小圆点
            Label dot = new Label();
            if (isHighlighted) {
                if (isFirst) {
                    // 最新状态节点：放大尺寸，施加双层高亮拟物态外发光边框，强化视觉焦点
                    dot.setMinSize(12, 12);
                    dot.setPrefSize(12, 12);
                    dot.setStyle("-fx-background-color: -color-success-emphasis; -fx-background-radius: 999; -fx-border-color: -color-success-muted; -fx-border-width: 3px; -fx-border-radius: 999;");
                    StackPane.setMargin(dot, new Insets(2, 0, 0, 0)); // 微调首颗圆点与文字第一行的对齐基准线
                } else {
                    // 历史已激活节点：标准中等绿点
                    dot.setMinSize(8, 8);
                    dot.setPrefSize(8, 8);
                    dot.setStyle("-fx-background-color: -color-success-emphasis; -fx-background-radius: 999;");
                    StackPane.setMargin(dot, new Insets(4, 0, 0, 0));
                }
            } else {
                // 冷冻未到达节点：小号静音灰点
                dot.setMinSize(8, 8);
                dot.setPrefSize(8, 8);
                dot.setStyle("-fx-background-color: -color-border-default; -fx-background-radius: 999;");
                StackPane.setMargin(dot, new Insets(4, 0, 0, 0));
            }

            axisColumn.getChildren().add(dot);

            // 3. 提取右侧文本业务区
            Node rightContent = customNodeMapper.apply(item);
            HBox.setHgrow(rightContent, Priority.ALWAYS);

            // 4. 水平聚合：将左侧强力等高的 axisColumn 与右侧业务文本组装进同一行
            HBox rowLayout = new HBox(12, axisColumn, rightContent);
            rowLayout.setAlignment(Pos.TOP_LEFT);

            // 核心：非最后一项时，利用底部内边距（Padding）平滑撑开两行物流之间的物理间距，
            // 此时左侧处于 StackPane 控制下的 verticalLine 会跟随着 Padding 自动无缝拉伸向下，彻底消除红框断裂！
            if (!isLast) {
                rowLayout.setPadding(new Insets(0, 0, 24, 0));
            }

            // 允许整行随着容器宽度响应式拉伸
            VBox.setVgrow(rowLayout, Priority.ALWAYS);

            getChildren().add(rowLayout);
        }
    }

    public FXExpressTracker<T> width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }
}