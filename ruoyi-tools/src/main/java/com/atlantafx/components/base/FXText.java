package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import com.atlantafx.AppContext;
import com.atlantafx.core.constant.NotificationLevel;
import javafx.beans.property.StringProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * FXText - 基于 AtlantaFX 风格的文本组件
 * 继承自 JavaFX Text，实现 IFXNode 接口支持链式调用
 * 提供丰富的样式和文本排版快捷方法
 */
public class FXText extends Text implements IFXNode<FXText> {

    /**
     * 默认构造函数
     */
    private FXText() {
        super();
    }

    /**
     * 创建带文本内容的文本节点
     *
     * @param text 显示的文本内容
     */
    private FXText(String text) {
        super(text);
    }

    /**
     * 创建带坐标和文本内容的文本节点
     *
     * @param x    文本起点的 X 坐标
     * @param y    文本起点的 Y 坐标
     * @param text 显示的文本内容
     */
    private FXText(double x, double y, String text) {
        super(x, y, text);
    }

    /**
     * 创建空文本实例
     *
     * @return FXText 实例
     */
    public static FXText create() {
        return new FXText();
    }

    /**
     * 创建带文本的文本实例
     *
     * @param text 显示的文本内容
     * @return FXText 实例
     */
    public static FXText create(String text) {
        return new FXText(text);
    }

    /**
     * 创建带坐标和文本的文本实例
     *
     * @param x    文本起点的 X 坐标
     * @param y    文本起点的 Y 坐标
     * @param text 显示的文本内容
     * @return FXText 实例
     */
    public static FXText create(double x, double y, String text) {
        return new FXText(x, y, text);
    }

    /**
     * 绑定文本属性到指定的 StringProperty
     * 当源属性变化时，文本内容自动更新
     *
     * @param stringProperty 要绑定的字符串属性
     * @return FXText 实例（链式调用）
     */
    public FXText bind(StringProperty stringProperty) {
        textProperty().bind(stringProperty);
        return this;
    }

    /**
     * 设置字体大小
     *
     * @param fontSize 字体大小值（逻辑像素）
     * @return FXText 实例（链式调用）
     */
    public FXText fontSize(double fontSize) {
        setFont(new Font(fontSize));
        return this;
    }

    /**
     * 绑定字体属性到指定的 Font 对象
     * 当源属性变化时，字体自动更新
     *
     * @param font Font 对象，用于指定字体
     * @return FXText 实例（链式调用）
     */
    public FXText fontSize(Font font) {
        setFont(font);
        return this;
    }

    /**
     * 设置文本颜色
     * 支持 CSS 颜色格式（如 "#FF0000"、"red"）
     *
     * @param color CSS 格式的颜色字符串
     * @return FXText 实例（链式调用）
     */
    public FXText fontColor(String color) {
        return styleCss("-fx-fill: " + color + ";");
    }

    /**
     * 绑定文本颜色属性到指定的 Paint 对象
     * 当源属性变化时，文本颜色自动更新
     *
     * @param color Paint 对象，用于指定颜色
     * @return FXText 实例（链式调用）
     */
    public FXText fontColor(Paint color) {
        setFill(color);
        return this;
    }

    /**
     * 设置文本可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXText 实例（链式调用）
     */
    public FXText visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置文本是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXText 实例（链式调用）
     */
    public FXText managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置多行文本的自动包裹宽度。
     * 当文本超出此宽度时会自动换行。
     *
     * @param width 自动换行的包裹宽度
     * @return FXText 实例（链式调用）
     */
    public FXText wrappingWidth(double width) {
        setWrappingWidth(width);
        return this;
    }

    // ==================== AtlantaFX 标题样式快捷方法 ====================

    /**
     * 应用一级标题样式（H1）
     * 最大号标题，用于页面主标题
     *
     * @return FXText 实例（链式调用）
     */
    public FXText h1() {
        return stylesClass(Styles.TITLE_1);
    }

    /**
     * 应用二级标题样式（H2）
     * 用于章节标题
     *
     * @return FXText 实例（链式调用）
     */
    public FXText h2() {
        return stylesClass(Styles.TITLE_2);
    }

    /**
     * 应用三级标题样式（H3）
     * 用于子章节标题
     *
     * @return FXText 实例（链式调用）
     */
    public FXText h3() {
        return stylesClass(Styles.TITLE_3);
    }

    /**
     * 应用四级标题样式（H4）
     * 最小号标题，用于小组件标题
     *
     * @return FXText 实例（链式调用）
     */
    public FXText h4() {
        return stylesClass(Styles.TITLE_4);
    }

    /**
     * 应用粗体文本样式
     * 加粗显示文本，强调重要内容
     *
     * @return FXText 实例（链式调用）
     */
    public FXText bold() {
        return stylesClass(Styles.TEXT, Styles.TEXT_BOLD);
    }

    /**
     * 应用柔和文本样式
     * 降低文本对比度，用于次要信息或提示文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText muted() {
        return stylesClass(Styles.TEXT, Styles.TEXT_MUTED);
    }

    /**
     * 添加斜体文本样式
     * 斜体显示文本，用于强调文本
     *
     * @return FXText 实例（链式调用）
     * @note 如果系统中对应的字体没有配套的斜体文件，则不会添加斜体样式
     */
    public FXText italic() {
        return stylesClass(Styles.TEXT, Styles.TEXT_ITALIC);
    }

    /**
     * 添加更粗的文本样式
     * 创建更粗的文本，用于强调文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText bolder() {
        return stylesClass(Styles.TEXT, Styles.TEXT_BOLDER);
    }

    /**
     * 添加较小的文本样式
     * 创建较小的文本，用于辅助文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText small() {
        return stylesClass(Styles.TEXT, Styles.TEXT_SMALL);
    }

    /**
     * 添加标题小号文本样式
     * 创建更小的标题文本，用于辅助标题
     *
     * @return FXText 实例（链式调用）
     */
    public FXText caption() {
        return stylesClass(Styles.TEXT, Styles.TEXT_CAPTION);
    }

    /**
     * 添加更细的文本样式
     * 创建更细的文本，用于辅助文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText lighter() {
        return stylesClass(Styles.TEXT, Styles.TEXT_LIGHTER);
    }

    /**
     * 添加正常文本样式
     * 创建正常文本，用于正文文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText normal() {
        return stylesClass(Styles.TEXT, Styles.TEXT_NORMAL);
    }

    /**
     * 添加倾斜文本样式
     * 创建倾斜的文本，用于辅助文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText oblique() {
        return stylesClass(Styles.TEXT, Styles.TEXT_OBLIQUE);
    }

    /**
     * 添加强调文本样式
     * 创建强调的文本，用于强调文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText onEmphasis() {
        return stylesClass(Styles.TEXT, Styles.TEXT_ON_EMPHASIS);
    }

    /**
     * 添加下划线文本样式
     * 创建下划线的文本，用于辅助文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText underlined() {
        return stylesClass(Styles.TEXT, Styles.TEXT_UNDERLINED);
    }

    /**
     * 添加删除线文本样式
     * 创建删除线的文本，用于辅助文本
     *
     * @return FXText 实例（链式调用）
     */
    public FXText strikethrough() {
        return stylesClass(Styles.TEXT, Styles.TEXT_STRIKETHROUGH);
    }

    /**
     * 设置文本为等宽字体
     * 适用于显示代码、数字等需要对齐的场景
     *
     * @return FXText 实例（链式调用）
     */
    public FXText monospace() {
        return styleCss("-fx-font-family: monospace;");
    }

    // ==================== 扩展常用方法 ====================

    /**
     * 设置文本对齐方式（多行文本时生效）
     *
     * @param alignment 文本对齐枚举值（如 LEFT, RIGHT, CENTER, JUSTIFY）
     * @return FXText 实例（链式调用）
     */
    public FXText align(TextAlignment alignment) {
        setTextAlignment(alignment);
        return this;
    }

    /**
     * 设置文本省略号截断（文本过长时显示...）
     * 注意：JavaFX Text 节点原生不支持对单个节点的 ellipsis 渲染，
     * 此处通过 CSS 模拟溢出截断行为，主要配合特定父容器或需要固定边界的场景。
     *
     * @return FXText 实例（链式调用）
     */
    public FXText ellipsis() {
        return styleCss("-fx-text-overrun: ELLIPSIS;");
    }

    /**
     * 设置文本为可复制（点击时直接复制文本内容到系统剪贴板）
     *
     * @return FXText 实例（链式调用）
     */
    public FXText copyable() {
        setOnMouseClicked(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(getText());
            clipboard.setContent(content);
            AppContext.showNotification("已复制到剪贴板:" + getText(), NotificationLevel.INFO);
        });
        return this;
    }

    /**
     * 设置文本透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXText 实例（链式调用）
     */
    public FXText opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置文本旋转角度
     *
     * @param angle 旋转角度（度数）
     * @return FXText 实例（链式调用）
     */
    public FXText rotate(double angle) {
        setRotate(angle);
        return this;
    }

    // ==================== 预设颜色快捷方法 ====================

    /**
     * 设置文本为强调色
     * 使用 AtlantaFX 主题的主色调
     *
     * @return FXText 实例（链式调用）
     */
    public FXText accent() {
        return stylesClass(Styles.TEXT, Styles.ACCENT);
    }

    /**
     * 设置文本为成功色（绿色）
     * 适用于成功、确认状态
     *
     * @return FXText 实例（链式调用）
     */
    public FXText success() {
        return stylesClass(Styles.TEXT, Styles.SUCCESS);
    }

    /**
     * 设置文本为危险色（红色）
     * 适用于错误提示、危险状态
     *
     * @return FXText 实例（链式调用）
     */
    public FXText danger() {
        return stylesClass(Styles.TEXT, Styles.DANGER);
    }

    /**
     * 设置文本为警告色（橙色/黄色）
     * 适用于警告、注意提示
     *
     * @return FXText 实例（链式调用）
     */
    public FXText warning() {
        return stylesClass(Styles.TEXT, Styles.WARNING);
    }

    /**
     * 添加副标题文本样式
     * 适用于副标题、微弱小提示
     *
     * @return FXText 实例（链式调用）
     */
    public FXText subTitle() {
        return stylesClass(Styles.TEXT, Styles.TEXT_SUBTLE);
    }
}