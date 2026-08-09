package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import com.atlantafx.AppContext;
import com.atlantafx.core.constant.NotificationLevel;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.Ikon;

/**
 * FXLabel - 基于 AtlantaFX 风格的文本标签组件
 * 继承自 JavaFX Label，实现 IFXNode 接口支持链式调用
 * 针对现代排版层级、文本溢出裁剪、穿透式颜色注入以及点击快捷复制进行了深度 Fluent 封装
 */
public class FXLabel extends Label implements IFXNode<FXLabel> {

    /**
     * 默认无参构造函数，设置为私有
     */
    private FXLabel() {
        super();
    }

    /**
     * 带文本内容的构造函数，设置为私有
     *
     * @param text 标签显示的初始文本
     */
    private FXLabel(String text) {
        super(text);
    }

    /**
     * 带文本和图形节点的构造函数，设置为私有
     *
     * @param text    标签显示的初始文本
     * @param graphic 标签关联的图形节点（如 Icon）
     */
    private FXLabel(String text, Node graphic) {
        super(text, graphic);
    }

    // ==================== 静态工厂方法 (Static Creators) ====================

    /**
     * 创建一个空文本的 FXLabel 实例
     *
     * @return FXLabel 实例（链式调用入口）
     */
    public static FXLabel create() {
        return new FXLabel();
    }

    /**
     * 创建一个指定文本内容的 FXLabel 实例
     *
     * @param text 文本内容
     * @return FXLabel 实例（链式调用入口）
     */
    public static FXLabel create(String text) {
        return new FXLabel(text);
    }

    /**
     * 创建一个包含文本内容与图形节点的 FXLabel 实例
     *
     * @param text    文本内容
     * @param graphic 图形节点
     * @return FXLabel 实例（链式调用入口）
     */
    public static FXLabel create(String text, Node graphic) {
        return new FXLabel(text, graphic);
    }

    // ==================== 核心文本与属性绑定流式扩展 ====================

    /**
     * 设置标签的显示文本
     *
     * @param text 文本内容
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel text(String text) {
        setText(text);
        return this;
    }

    /**
     * 将标签的文本属性单向绑定到外部的 StringProperty 上
     *
     * @param stringProperty 响应式字符串属性源
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel bindText(StringProperty stringProperty) {
        textProperty().bind(stringProperty);
        return this;
    }

    /**
     * 设置原生的 Font 字体实例
     *
     * @param font 字体对象
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel font(Font font) {
        setFont(font);
        return this;
    }

    /**
     * 快捷设置系统默认字体的字号大小
     *
     * @param size 字体大小（单位：像素）
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel fontSize(double size) {
        setFont(Font.font(getFont().getFamily(), size));
        return this;
    }

    // ==================== 穿透式高级颜色定制 ====================

    /**
     * 快捷将标签文本及内置图标设置为指定的 Paint 颜色对象
     *
     * @param color Paint 对象（如 Color.RED）
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel fontColor(Paint color) {
        setTextFill(color);
        return this;
    }

    /**
     * 【深度优化】设置自定义文本及图标颜色
     * 完美穿透并覆盖 AtlantaFX 内置的专属文本 CSS 权重，支持十六进制绝对色与系统预设变量
     *
     * @param color CSS 颜色字符串（如 "white"、"#ef4444"）或 AtlantaFX 变量（如 "-color-accent-fg"）
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel fontColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return this;
        }
        // 如果传入的是绝对颜色，为了防止 AtlantaFX 的皮肤样式将其覆盖，
        // 同时强制复写 -fx-text-fill 和内置的专属文本颜色变量
        if (!color.startsWith("-")) {
            return styleCss(
                    "-fx-text-fill: " + color + ";" +
                            "-color-fg-default: " + color + ";" +
                            "-color-fg-muted: " + color + ";"
            );
        }
        // 如果传入的是主题色变量，重定向核心继承属性
        return styleCss("-fx-text-fill: " + color + ";");
    }

    /**
     * 设置标签可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置标签是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    // ==================== 图标与多方位排版流式扩展 ====================

    /**
     * 快捷设置标签左侧的图形节点 (Graphic)
     *
     * @param graphic 任意 Node 节点
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel graphic(Node graphic) {
        setGraphic(graphic);
        return this;
    }

    /**
     * 快捷注入一个来自 Ikonli 库的图标，默认居左
     *
     * @param iconCode Ikonli 图标编码
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel icon(Ikon iconCode) {
        setGraphic(FXFontIcon.create(iconCode));
        return this;
    }

    /**
     * 复合定制：注入 Ikonli 图标并精细调节相对于文本的方位和间距
     *
     * @param iconCode Ikonli 图标编码
     * @param display  JavaFX ContentDisplay 枚举 (LEFT, RIGHT, TOP, BOTTOM)
     * @param gap      图标与文字之间的像素间距
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel icon(Ikon iconCode, ContentDisplay display, double gap) {
        setGraphic(FXFontIcon.create(iconCode));
        setContentDisplay(display);
        setGraphicTextGap(gap);
        return this;
    }

    // ==================== 经典对齐、剪裁与换行控制 ====================

    /**
     * 设置组件内部的整体对齐方式 (Content Alignment)
     *
     * @param pos Pos 对齐枚举
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel align(Pos pos) {
        setAlignment(pos);
        return this;
    }

    /**
     * 设置多行文本情况下的文本对齐行内策略 (Text Alignment)
     *
     * @param alignment TextAlignment 对齐枚举
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel textAlignment(TextAlignment alignment) {
        setTextAlignment(alignment);
        return this;
    }

    /**
     * 设置文本超出宽度边界时是否允许自动换行
     *
     * @param wrapText true-允许换行，false-单行并伴随裁剪
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel wrapText(boolean wrapText) {
        setWrapText(wrapText);
        return this;
    }

    /**
     * 设置单行超长文本的省略号及截断样式规则
     *
     * @param style OverrunStyle 裁剪枚举（例如 OverrunStyle.ELLIPSIS 尾部加省略号）
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel textOverrun(OverrunStyle style) {
        setTextOverrun(style);
        return this;
    }

    // ==================== 基础容器布局属性流式扩展 ====================

    /**
     * 设置标签的四个方向内边距 (Padding)
     */
    public FXLabel padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 快捷设置标签四周统一的内边距值
     */
    public FXLabel padding(double value) {
        setPadding(new Insets(value));
        return this;
    }

    /**
     * 设置当置于 HBox 布局中时的横向自适应填充优先级
     */
    public FXLabel hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置当置于 VBox 布局中时的纵向自适应填充优先级
     */
    public FXLabel vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== AtlantaFX 官方字形层级定义 (Typography) ====================

    /**
     * 激活一阶大标题字形样式 (H1)
     */
    public FXLabel h1() {
        return stylesClass(Styles.TITLE_1);
    }

    /**
     * 激活二阶中标题字形样式 (H2)
     */
    public FXLabel h2() {
        return stylesClass(Styles.TITLE_2);
    }

    /**
     * 激活三阶次级标题字形样式 (H3)
     */
    public FXLabel h3() {
        return stylesClass(Styles.TITLE_3);
    }

    /**
     * 激活四阶小标题字形样式 (H4)
     */
    public FXLabel h4() {
        return stylesClass(Styles.TITLE_4);
    }

    /**
     * 激活补充说明/副标题细字形样式 (Caption)
     */
    public FXLabel caption() {
        return stylesClass(Styles.TEXT_CAPTION);
    }

    /**
     * 快捷加粗字体
     */
    public FXLabel bold() {
        return stylesClass(Styles.TEXT_BOLD);
    }

    /**
     * 添加副标题样式
     * 适用于副标题、小标题
     *
     * @return FXFontIcon 实例（链式调用）
     */
    public FXLabel subTitle() {
        return stylesClass(Styles.TEXT_SUBTLE);
    }

    // ==================== AtlantaFX 官方特色状态色与文本变体 ====================

    /**
     * 将文本状态置为系统“强调色”
     */
    public FXLabel accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 将文本状态置为系统“成功色”（通常为绿色，表示审核通过或正常激活）
     */
    public FXLabel success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 将文本状态置为系统“危险色”（通常为红色，表示警告、错误或强力警示）
     */
    public FXLabel danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 将文本状态置为系统“警告色”（通常为橙/黄色，表示中等风险提示）
     */
    public FXLabel warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 将文本渲染为“柔和哑色模式”（通过变淡降低信息权重，常用于副标题、描述文本或失效提示）
     */
    public FXLabel muted() {
        return stylesClass(Styles.TEXT_MUTED);
    }

    /**
     * 【特色业务定制】一键重置标签状态样式
     */
    public FXLabel resetState() {
        getStyleClass().removeAll(Styles.DANGER, Styles.WARNING, Styles.SUCCESS, Styles.ACCENT);
        return this;
    }

    // ==================== 特色高级业务定制扩展 ====================

    /**
     * 【特色业务定制】一键开启文本“点击快捷复制”高级交互能力
     * 启用后鼠标悬停会展现手型光标，点击会自动将当前标签内的文本内容抽取并塞入操作系统的剪贴板，同时弹出右下角系统通知。
     *
     * @return FXLabel 实例（链式调用）
     */
    public FXLabel enableClickToCopy() {
        setTooltip(FXTooltip.create("点击复制"));
        setStyle("-fx-cursor: hand;");
        setOnMouseClicked(e -> {
            String txt = getText();
            if (txt != null && !txt.isEmpty()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(txt);
                clipboard.setContent(content);
                AppContext.showNotification("内容已成功复制到剪贴板！", NotificationLevel.INFO);
            }
        });
        return this;
    }
}