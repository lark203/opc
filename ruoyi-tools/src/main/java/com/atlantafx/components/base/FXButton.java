package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.Ikon;

/**
 * FXButton - 基于 AtlantaFX 风格的按钮组件
 * 继承自 JavaFX Button，实现 IFXNode 接口支持链式调用
 */
public class FXButton extends Button implements IFXNode<FXButton> {

    /**
     * 构造函数
     *
     * @param text 按钮显示文本
     */
    private FXButton(String text) {
        super(text);
    }

    /**
     * 创建带文本的按钮
     *
     * @param text 按钮显示文本
     * @return FXButton 实例
     */
    public static FXButton create(String text) {
        return new FXButton(text);
    }

    /**
     * 设置按钮点击事件处理器
     *
     * @param value 事件处理器，当按钮被点击时执行
     * @return FXButton 实例（链式调用）
     */
    public FXButton onAction(EventHandler<ActionEvent> value) {
        setOnAction(value);
        return this;
    }

    /**
     * 设置按钮宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXButton 实例（链式调用）
     */
    public FXButton width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置按钮高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXButton 实例（链式调用）
     */
    public FXButton height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置按钮尺寸（宽度和高度）
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXButton 实例（链式调用）
     */
    public FXButton size(double w, double h) {
        setMinWidth(w);
        setPrefWidth(w);
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置按钮在 VBox 中的垂直增长优先级
     * 使按钮在垂直方向上填充可用空间
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置按钮在 HBox 中的水平增长优先级
     * 使按钮在水平方向上填充可用空间
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置按钮工具提示（Tooltip）
     * 鼠标悬停时显示的提示信息
     *
     * @param text 提示文本
     * @return FXButton 实例（链式调用）
     */
    public FXButton tooltip(String text) {
        setTooltip(FXTooltip.create(text));
        return this;
    }

    /**
     * 设置按钮是否禁用
     * 禁用状态下按钮不可点击
     *
     * @param disabled true-禁用，false-启用
     * @return FXButton 实例（链式调用）
     */
    public FXButton disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== AtlantaFX 样式快捷方法 ====================

    /**
     * 应用强调色样式（Accent）
     * 用于突出主要操作按钮
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用成功样式（Success）
     * 通常用于表示成功、确认的操作，显示为绿色
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 通常用于表示危险、删除的操作，显示为红色
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用警告样式（Warning）
     * 通常用于表示警告、注意的操作，显示为橙色/黄色
     *
     * @return FXButton 实例（链式调用）
     *
     * 按钮没有警告色
     */
    /*public FXButton warning() {
        return stylesClass(Styles.WARNING);
    }*/

    /**
     * 应用扁平样式（Flat）
     * 无边框和背景的简约风格
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton flat() {
        return stylesClass(Styles.FLAT);
    }

    /**
     * 应用轮廓样式（Outline）
     * 只有边框，透明背景
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton outline() {
        return stylesClass(Styles.BUTTON_OUTLINED);
    }

    /**
     * 应用圆角按钮样式（Pill）
     * 完全圆角的胶囊形状按钮
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton pill() {
        return stylesClass(Styles.CENTER_PILL);
    }

    /**
     * 设置按钮为圆形（仅显示图标，无文本）
     * 通常用于工具栏、社交分享按钮等场景
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton circle() {
        return stylesClass(Styles.BUTTON_CIRCLE);
    }

    /**
     * 设置按钮为大尺寸（Large）
     * 适用于重要操作按钮
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton lg() {
        return stylesClass(Styles.LARGE);
    }

    /**
     * 设置按钮为小尺寸（Small）
     * 适用于次要操作或紧凑布局
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton sm() {
        return stylesClass(Styles.SMALL);
    }

    /**
     * 设置按钮图标（使用 Ikonli 图标库）
     * 图标将显示在按钮文本左侧
     *
     * @param iconCode Ikonli 图标代码，如 FontAwesome、MaterialDesign 等
     * @return FXButton 实例（链式调用）
     */
    public FXButton icon(Ikon iconCode) {
        setGraphic(FXFontIcon.create(iconCode));
        return this;
    }

    /**
     * 设置按钮图标位置（左/右）
     *
     * @param iconCode Ikonli 图标代码
     * @param right    true-图标在右侧，false-图标在左侧
     * @return FXButton 实例（链式调用）
     */
    public FXButton iconWithText(Ikon iconCode, boolean right) {
        FXFontIcon icon = FXFontIcon.create(iconCode);
        if (right) {
            setGraphicTextGap(5);
            setContentDisplay(ContentDisplay.RIGHT);
        }
        setGraphic(icon);
        return this;
    }

    /**
     * 设置标签可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXButton 实例（链式调用）
     */
    public FXButton visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置标签是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXButton 实例（链式调用）
     */
    public FXButton managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    // ==================== 交互与事件扩展 ====================

    /**
     * 设置按钮双击事件处理器
     *
     * @param handler 双击时的事件处理程序
     * @return FXButton 实例（链式调用）
     */
    public FXButton onDoubleClick(EventHandler<MouseEvent> handler) {
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handler.handle(event);
            }
        });
        return this;
    }

    /**
     * 设置鼠标悬停与离开的监听器
     *
     * @param onHover  鼠标移入时的操作（可为 null）
     * @param onExited 鼠标移出时的操作（可为 null）
     * @return FXButton 实例（链式调用）
     */
    public FXButton onHover(EventHandler<MouseEvent> onHover,
                            EventHandler<MouseEvent> onExited) {
        if (onHover != null) setOnMouseEntered(onHover);
        if (onExited != null) setOnMouseExited(onExited);
        return this;
    }

    /**
     * 为按钮绑定全局快捷键（当所在的 Scene 激活时触发点击）
     *
     * @param combination 键盘组合键，例如 KeyCombination.valueOf("ShortCut+S")
     * @return FXButton 实例（链式调用）
     */
    public FXButton shortcut(KeyCombination combination) {
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(combination, this::fire);
            }
        });
        return this;
    }

    // ==================== 状态控制扩展 ====================

    /**
     * 设置按钮的加载状态 (Loading)
     * 激活时：显示一个旋转的加载轮廓（或进度动画），并禁用按钮防止重复点击。
     *
     * @param isLoading true-进入加载状态，false-恢复正常
     * @return FXButton 实例（链式调用）
     */
    public FXButton loading(boolean isLoading) {
        setDisable(isLoading);
        if (isLoading) {
            // 保存原有的 graphic 并在需要时恢复，这里使用 AtlantaFX 的动画样式辅助
            // 假设使用 Ikonli 的加载图标，或自带的 Spinner 节点
            // 示例：这里可以动态换成一个旋转的动画节点
            stylesClass(Styles.BUTTON_OUTLINED);
        } else {
            getStyleClass().remove(Styles.BUTTON_OUTLINED);
        }
        return this;
    }

    /**
     * 设置按钮的选中/激活状态（通常配合 Toggle 按钮样式或伪类使用）
     *
     * @param selected true-呈选中高亮状态，false-常态
     * @return FXButton 实例（链式调用）
     */
    public FXButton selected(boolean selected) {
        if (selected) {
            stylesClass("selected"); // 或配合伪类 pseudoClassStateChanged
        } else {
            getStyleClass().remove("selected");
        }
        return this;
    }

    // ==================== 图标与文本排版增强 ====================

    /**
     * 设置按钮图标并控制其完全的展示方位（上、下、左、右）
     *
     * @param iconCode Ikonli 图标代码
     * @param display  JavaFX ContentDisplay 枚举 (LEFT, RIGHT, TOP, BOTTOM)
     * @param gap      图标与文字之间的间距（像素）
     * @return FXButton 实例（链式调用）
     */
    public FXButton icon(Ikon iconCode, ContentDisplay display, double gap) {
        setGraphic(FXFontIcon.create(iconCode));
        setContentDisplay(display);
        setGraphicTextGap(gap);
        return this;
    }

    /**
     * 设置纯图标按钮（不显示文本，同时自动消除内部边距并保持居中，常用于紧凑工具栏）
     *
     * @param iconCode Ikonli 图标代码
     * @return FXButton 实例（链式调用）
     */
    public FXButton iconOnly(Ikon iconCode) {
        setText(null);
        setGraphic(FXFontIcon.create(iconCode));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        return this;
    }

    // ==================== 高级样式定制 ====================

    /**
     * 设置自定义文本及图标颜色
     * 完美破解 AtlantaFX 内部 CSS 权重覆盖问题，支持硬编码颜色与主题变量
     *
     * @param color CSS 颜色字符串（如 "white"、"#FF0000"）或 AtlantaFX 变量（如 "-color-accent-fg"）
     * @return FXButton 实例（链式调用）
     */
    public FXButton fontColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return this;
        }
        // 1. 如果传入的是标准十六进制或颜色名，为了防止 AtlantaFX 的内部 Text 节点不跟随，
        //    我们同时复写 -fx-text-fill 和 AtlantaFX 核心文本颜色变量 -color-btn-fg
        if (!color.startsWith("-")) {
            return styleCss(
                    "-fx-text-fill: " + color + ";" +
                            "-color-btn-fg: " + color + ";" +
                            "-color-btn-fg-hover: " + color + ";" +
                            "-color-btn-fg-focused: " + color + ";"
            );
        }

        // 2. 如果传入的是 AtlantaFX 变量（例如 "-color-accent-fg"），直接将核心变量重定向
        return styleCss("-color-btn-fg: " + color + ";");
    }

    /**
     * 快捷将按钮文字设置为指定颜色
     *
     * @param color Paint 对象（如 Color.WHITE）
     * @return FXButton 实例（链式调用）
     */
    public FXButton fontColor(Paint color) {
        setTextFill(color);
        return this;
    }

    /**
     * 设置自定义按钮背景色（打破主题色限制，用于突发性的个性定制）
     *
     * @param color CSS 颜色字符串
     * @return FXButton 实例（链式调用）
     * @note 无法改变背景色 和 Atlantafx样式冲突所以不生效，但是如果不用Atlantafx样式，可以调用此方法
     */
    public FXButton background(String color) {
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 快捷将按钮文字设置为粗体
     *
     * @return FXButton 实例（链式调用）
     */
    public FXButton bold() {
        return stylesClass(Styles.TEXT_BOLD);
    }
}
