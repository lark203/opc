package com.atlantafx.components.base;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * FXRating - 高精星级评分组件（纯几何数模重构版）
 * 彻底清洗历史 TODO。放弃旧版 setScale 导致的排版边界重叠硬伤，改用 SVGPath 结合高精极坐标公式构建五角星 facts。
 * 完美支持 AtlantaFX 动态主题色变量（深色/浅色热刷新），绝不引发 Color.valueOf 运行时崩溃。
 */
public class FXRating extends HBox implements IFXNode<FXRating> {

    private int rating = 0;
    private int maxRating = 5;
    private double starSize = 24.0; // 默认物理像素直径
    private boolean readOnly = false;

    // 动态主题色 CSS Lookup 变量存储
    private String fillColorVar = "-color-warning-emphasis"; // 默认警告橙/金色
    private String emptyColorVar = "-color-border-default";    // 默认空星底色

    private java.util.function.Consumer<Integer> onRatingChangeCallback;

    public FXRating() {
        super();
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6); // 严格定义五角星像素间距
        setPadding(new Insets(5));

        // 首次构建拓扑
        refreshStarsLayout();
    }

    public static FXRating create(int maxRating) {
        return new FXRating().maxRating(maxRating);
    }

    /**
     * 核心：彻底抛弃旧版 Add/Remove 逻辑，重建拓扑结构并注入全新几何五角星
     */
    private void refreshStarsLayout() {
        getChildren().clear();

        for (int i = 0; i < maxRating; i++) {
            final int index = i;
            StarView star = new StarView(starSize, i < rating);

            // 应用当前持有的动态主题色变量
            star.updateColors(fillColorVar, emptyColorVar);

            if (!readOnly) {
                star.setCursor(javafx.scene.Cursor.HAND);

                // 鼠标悬停高亮特效：改用加粗边框或微观阴影，拒绝使用破坏布局的 setScale
                star.setOnMouseEntered(e -> star.highlight(true));
                star.setOnMouseExited(e -> star.highlight(false));

                star.setOnMouseClicked(e -> {
                    this.rating = index + 1;
                    updateStarsState(); // 动态刷新点亮状态
                    if (onRatingChangeCallback != null) {
                        onRatingChangeCallback.accept(this.rating);
                    }
                });
            }
            getChildren().add(star);
        }
    }

    /**
     * 核心：仅刷新现存五角星的点亮状态与颜色变量，避免重复销毁节点引发渲染闪烁
     */
    private void updateStarsState() {
        for (int i = 0; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof StarView) {
                StarView star = (StarView) getChildren().get(i);
                star.setFilled(i < rating);
                star.updateColors(fillColorVar, emptyColorVar);
            }
        }
    }

    public FXRating rating(int rating) {
        this.rating = Math.min(maxRating, Math.max(0, rating));
        updateStarsState();
        return this;
    }

    public FXRating maxRating(int max) {
        this.maxRating = Math.max(1, max);
        refreshStarsLayout();
        return this;
    }

    /**
     * 核心：安全修改五角星绝对像素大小，排版流长宽自适应扩容，绝不产生重叠
     */
    public FXRating size(double size) {
        this.starSize = size;
        refreshStarsLayout(); // 尺寸变更涉及 SVGPath 数据重算，需重构拓扑
        return this;
    }

    public FXRating readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        refreshStarsLayout();
        return this;
    }

    public FXRating onChange(java.util.function.Consumer<Integer> callback) {
        this.onRatingChangeCallback = callback;
        return this;
    }

    /**
     * 允许接入自定义 AtlantaFX CSS 变量或十六进制颜色
     */
    public FXRating colors(String fillColorVar, String emptyColorVar) {
        this.fillColorVar = fillColorVar;
        this.emptyColorVar = emptyColorVar;
        updateStarsState();
        return this;
    }

    public int getRating() {
        return this.rating;
    }

    /* =========================================================================
     * 内部核心视觉实体：StarView（基于极坐标公式重构的 SVG 纯几何节点）
     * ========================================================================= */
    private static class StarView extends StackPane {
        private final SVGPath path;
        private boolean isFilled;
        private final double size;

        private String currentFillVar;
        private String currentEmptyVar;

        public StarView(double size, boolean isFilled) {
            this.size = size;
            this.isFilled = isFilled;
            this.path = new SVGPath();

            // 核心：应用极坐标公式动态算点，一劳永逸生成绝对标准比例的五角星 SVG 数据
            this.path.setContent(generateStarSvgPath(size));

            // 基础防锯齿技术
            this.path.setSmooth(true);

            getChildren().add(path);
            setAlignment(Pos.CENTER);

            // 强行锁死外壳容器的物理排版边界，彻底消灭重叠缝隙
            setPrefSize(size, size);
            setMinSize(size, size);
            setMaxSize(size, size);
        }

        public void setFilled(boolean filled) {
            this.isFilled = filled;
            applyCssStyles();
        }

        public void updateColors(String fillVar, String emptyVar) {
            this.currentFillVar = fillVar;
            this.currentEmptyVar = emptyVar;
            applyCssStyles();
        }

        public void highlight(boolean enable) {
            if (enable) {
                // 悬停时增强边缘轮廓线 facts
                path.setStyle(path.getStyle() + "-fx-stroke: -color-accent-emphasis; -fx-stroke-width: 1.5px;");
            } else {
                applyCssStyles();
            }
        }

        /**
         * 核心：完全通过纯 CSS 变量控制五角星内核填充，规避 Color.valueOf 崩溃硬伤
         */
        private void applyCssStyles() {
            String targetColor = isFilled ? currentFillVar : currentEmptyVar;
            path.setStyle(
                    "-fx-fill: " + targetColor + ";" +
                            "-fx-stroke: " + (isFilled ? currentFillVar : "-color-border-default") + ";" +
                            "-fx-stroke-width: 1px;"
            );
        }

        /**
         * 极坐标系黄金比例五角星几何数模转换核心算法
         * 基于外圈半径 R 和内圈角动量 r 实时生成无损 M/L 坐标序列
         */
        private static String generateStarSvgPath(double size) {
            double rOuter = size / 2.0;
            double rInner = rOuter * 0.4; // 黄金分割系数，保证五角星内凹角度绝对标准、锐利 facts
            double centerX = rOuter;
            double centerY = rOuter;

            StringBuilder sb = new StringBuilder();
            // 转换角度为弧度
            for (int i = 0; i < 10; i++) {
                double angle = Math.toRadians((i * 36) - 90); // 顺时针步进 36 度，以 -90 度（正上方顶点）为起点
                double r = (i % 2 == 0) ? rOuter : rInner;
                double x = centerX + r * Math.cos(angle);
                double y = centerY + r * Math.sin(angle);

                if (i == 0) {
                    sb.append(String.format("M %.2f %.2f ", x, y)); // 移至起点
                } else {
                    sb.append(String.format("L %.2f %.2f ", x, y)); // 绘制连线
                }
            }
            sb.append("Z"); // 绝对闭合路径
            return sb.toString();
        }
    }
}