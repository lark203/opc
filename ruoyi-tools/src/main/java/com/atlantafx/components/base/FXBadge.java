package com.atlantafx.components.base;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * FXBadge - 高精物理对齐徽章角标组件
 * 核心：完全修复了 Color.valueOf() 无法解析 CSS 变量的崩溃硬伤。
 * 采用纯 CSS 样式桥接机制，完美跟随 AtlantaFX 动态主题（深色/浅色）实时刷新色值。
 */
public class FXBadge extends StackPane implements IFXNode<FXBadge> {

    private final Group badgeContainer;    // 角标物理外壳（Group 隔绝父级排版挤压）
    private final StackPane badgeView;     // 角标视觉实体（取代原来的 Circle）
    private final Label badgeLabel;        // 角标文本标签

    private int count = 0;
    private boolean isDot = false;
    private Pos currentPos = Pos.TOP_RIGHT;

    // 运行期微调偏置量
    private double offsetX = 0;
    private double offsetY = 0;

    private FXBadge(Node content) {
        super();

        // 1. 初始化角标内部骨架（使用 StackPane 作为视觉底衬，以便利用 CSS 变量设置背景色）
        badgeView = new StackPane();
        badgeLabel = new Label("0");

        // 赋予角标文字基础样式，文字颜色使用主题反色变量
        badgeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: -color-fg-emphasis; -fx-font-weight: bold;");
        badgeView.getChildren().add(badgeLabel);
        badgeView.setAlignment(Pos.CENTER);

        // 2. 使用 Group 包裹，彻底使其脱离父级布局流清算，防止干扰宿主大小测量
        badgeContainer = new Group(badgeView);
        badgeContainer.setManaged(false);

        // 3. 装配整体物理拓扑
        if (content != null) {
            getChildren().add(content);
        }
        getChildren().add(badgeContainer);

        // 4. 监听尺寸震荡，实施高精几何顶点交点重算
        registerGeometryBindings();

        // 默认初始化为危险红状态
        danger();
    }

    public static FXBadge create(Node content) {
        return new FXBadge(content);
    }

    /**
     * 核心：注册高精几何对齐绑定算法，保证角标物理圆ion始终锁死在宿主顶点
     */
    private void registerGeometryBindings() {
        layoutBoundsProperty().addListener((obs, old, newBounds) -> updateBadgePosition());
        badgeContainer.layoutBoundsProperty().addListener((obs, old, newBounds) -> updateBadgePosition());
    }

    /**
     * 核心：执行绝对像素平移换算，抹平靠右上角不够偏的硬伤
     */
    private void updateBadgePosition() {
        double width = getWidth();
        double height = getHeight();
        double badgeW = badgeContainer.getBoundsInLocal().getWidth();
        double badgeH = badgeContainer.getBoundsInLocal().getHeight();

        double targetX = 0;
        double targetY = 0;

        switch (currentPos) {
            case TOP_LEFT:
                targetX = 0 - (badgeW / 2.0);
                targetY = 0 - (badgeH / 2.0);
                break;
            case TOP_RIGHT:
                // 核心：宿主总宽减去角标半宽，使角标物理中心点精准对齐宿主右上角尖端
                targetX = width - (badgeW / 2.0);
                targetY = 0 - (badgeH / 2.0);
                break;
            case BOTTOM_LEFT:
                targetX = 0 - (badgeW / 2.0);
                targetY = height - (badgeH / 2.0);
                break;
            case BOTTOM_RIGHT:
                targetX = width - (badgeW / 2.0);
                targetY = height - (badgeH / 2.0);
                break;
            case CENTER:
                targetX = (width - badgeW) / 2.0;
                targetY = (height - badgeH) / 2.0;
                break;
            default:
                targetX = width - (badgeW / 2.0);
                targetY = 0 - (badgeH / 2.0);
                break;
        }

        badgeContainer.setLayoutX(targetX + offsetX);
        badgeContainer.setLayoutY(targetY + offsetY);
    }

    /**
     * 设置角标物理挂载方位
     */
    public FXBadge position(Pos pos) {
        if (pos != null) {
            this.currentPos = pos;
            updateBadgePosition();
        }
        return this;
    }

    /**
     * 像素级偏置量精细调校
     */
    public FXBadge offset(double x, double y) {
        this.offsetX = x;
        this.offsetY = y;
        updateBadgePosition();
        return this;
    }

    /**
     * 下发未读计数值，根据字符密度动态自适应缩放外壳大小
     */
    public FXBadge count(int count) {
        this.count = count;
        this.isDot = false;
        badgeLabel.setVisible(true);

        if (count <= 0) {
            badgeContainer.setVisible(false);
        } else {
            badgeContainer.setVisible(true);
            if (count > 99) {
                badgeLabel.setText("99+");
                applyViewStyle(22, 22, 11); // 扩容为椭圆大胶囊
            } else if (count >= 10) {
                badgeLabel.setText(String.valueOf(count));
                applyViewStyle(20, 20, 10);
            } else {
                badgeLabel.setText(String.valueOf(count));
                applyViewStyle(16, 16, 8);  // 标准紧凑圆形
            }
        }
        updateBadgePosition();
        return this;
    }

    /**
     * 切换至免数字打扰的纯指示红点状态（Dot Mode）
     */
    public FXBadge asDot() {
        this.isDot = true;
        badgeLabel.setVisible(false);
        badgeContainer.setVisible(true);
        applyViewStyle(8, 8, 4); // 大厂经典的 8px 直径微型指示点
        updateBadgePosition();
        return this;
    }

    /**
     * 自定义角标绝对直径大小
     */
    public FXBadge size(double diameter) {
        applyViewStyle(diameter, diameter, diameter / 2.0);
        updateBadgePosition();
        return this;
    }

    /* =========================================================================
     * 核心：通过动态拼接 Style 字符串，完美注入 CSS Lookups 变量，避开 Color 解析硬伤
     * ========================================================================= */

    private void applyViewStyle(double width, double height, double radius) {
        badgeView.setPrefSize(width, height);
        badgeView.setMinSize(width, height);
        badgeView.setMaxSize(width, height);

        // 动态继承当前的主题色变量
        String currentBgColor = (String) badgeView.getProperties().getOrDefault("badge-bg-var", "-color-danger-emphasis");

        badgeView.setStyle(
                "-fx-background-color: " + currentBgColor + ";" +
                        "-fx-background-radius: " + radius + "px;"
        );
    }

    private void changeThemeColorVariable(String cssVariable) {
        badgeView.getProperties().put("badge-bg-var", cssVariable);
        // 重新触发样式重绘渲染
        if (isDot) {
            asDot();
        } else {
            count(this.count);
        }
    }

    public FXBadge danger() {
        changeThemeColorVariable("-color-danger-emphasis");
        return this;
    }

    public FXBadge success() {
        changeThemeColorVariable("-color-success-emphasis");
        return this;
    }

    public FXBadge warning() {
        changeThemeColorVariable("-color-warning-emphasis");
        return this;
    }

    public FXBadge accent() {
        changeThemeColorVariable("-color-accent-emphasis");
        return this;
    }

    /* =========================================================================
     * 开箱即用高频业务快捷快捷路径补全
     * ========================================================================= */

    public FXBadge asUnreadMark() {
        return position(Pos.TOP_RIGHT)
                .danger()
                .asDot()
                .offset(3, -3); // 配合物理对齐公式，向右上各外扩 3 像素，达成完美的右上角边缘凌空跃出感
    }

    public FXBadge asSaleTag() {
        return position(Pos.BOTTOM_LEFT)
                .accent()
                .offset(-3, 3);
    }

    public FXBadge asNewTag() {
        return position(Pos.TOP_RIGHT)
                .success()
                .asDot()
                .offset(3, -3);
    }

    /**
     * 为角标追加物理隔离外边框（通常用于防止角标色与宿主底色融合）
     */
    public FXBadge border(double width, String colorVariable, double radius) {
        String existingStyle = badgeView.getStyle();
        badgeView.setStyle(existingStyle +
                "-fx-border-color: " + colorVariable + ";" +
                "-fx-border-width: " + width + "px;" +
                "-fx-border-radius: " + radius + "px;"
        );
        return this;
    }
}