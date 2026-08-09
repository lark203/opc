package com.atlantafx.components.base;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * FXTooltip - 基于 AtlantaFX 风格的提示工具条组件
 * 继承自 JavaFX Tooltip，实现 IFXNode 接口支持链式调用
 * 提供丰富的样式、排版及显示行为快捷方法
 * <p>
 */
public class FXTooltip extends Tooltip implements IFXNode<FXTooltip> {

    /**
     * 默认构造函数
     */
    private FXTooltip() {
        super();
    }

    /**
     * 创建带文本内容的提示工具条
     *
     * @param text 提示显示的文本内容
     */
    private FXTooltip(String text) {
        super(text);
    }

    /**
     * 创建空提示工具条实例
     *
     * @return FXTooltip 实例
     */
    public static FXTooltip create() {
        return new FXTooltip();
    }

    /**
     * 创建带文本的提示工具条实例
     *
     * @param text 提示显示的文本内容
     * @return FXTooltip 实例
     */
    public static FXTooltip create(String text) {
        return new FXTooltip(text);
    }

    /**
     * 将提示条安装到指定的节点上
     * 它是 Tooltip.install(node, tooltip) 的链式快捷封装
     *
     * @param node 要绑定该提示条的 UI 节点
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip install(Node node) {
        Tooltip.install(node, this);
        return this;
    }

    /**
     * 绑定提示文本属性到指定的 StringProperty
     * 当源属性变化时，提示文本自动更新
     *
     * @param stringProperty 要绑定的字符串属性
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip bindText(StringProperty stringProperty) {
        textProperty().bind(stringProperty);
        return this;
    }

    /**
     * 设置提示文本的字体大小
     *
     * @param fontSize 字体大小值（逻辑像素）
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip fontSize(double fontSize) {
        setFont(new Font(fontSize));
        return this;
    }

    /**
     * 设置提示文本颜色
     * 支持 CSS 颜色格式（如 "#FFFFFF"、"white"）
     *
     * @param color CSS 格式的颜色字符串
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip fontColor(String color) {
        return styleCss("-fx-text-fill: " + color + ";");
    }

    /**
     * 设置提示框的背景颜色
     * 支持 CSS 颜色格式（如 "#333333"）
     *
     * @param color CSS 格式的背景颜色字符串
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip background(String color) {
        return styleCss("-fx-background-color: " + color + ";");
    }

    /**
     * 设置文本是否自动换行
     *
     * @param wrapText true-启用换行，false-禁用换行
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip wrapText(boolean wrapText) {
        setWrapText(wrapText);
        return this;
    }

    /**
     * 设置图形节点（如图标），显示在提示文本旁边
     *
     * @param graphic 关联的图形节点
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip graphic(Node graphic) {
        setGraphic(graphic);
        return this;
    }

    /**
     * 设置提示窗体的最大显示物理宽度（超出则依据自动换行策略进行折行）
     *
     * @param width 最大像素宽度值
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip maxWidthValue(double width) {
        setMaxWidth(width);
        return this;
    }

    // ==================== 行为控制扩展方法 ====================

    /**
     * 设置鼠标悬停到节点上后，提示条弹出的延迟时间
     *
     * @param millis 延迟毫秒数
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip showDelay(double millis) {
        setShowDelay(Duration.millis(millis));
        return this;
    }

    /**
     * 设置提示条保持显示的最长时间
     *
     * @param millis 持续显示毫秒数
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip showDuration(double millis) {
        setShowDuration(Duration.millis(millis));
        return this;
    }

    /**
     * 设置鼠标离开节点后，提示条消失的延迟时间
     *
     * @param millis 隐藏延迟毫秒数
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip hideDelay(double millis) {
        setHideDelay(Duration.millis(millis));
        return this;
    }

    /**
     * 设置提示条的锚点位置
     *
     * @param anchorLocation 锚点位置
     * @return FXTooltip 实例（链式调用）
     */
    public FXTooltip anchorLocation(AnchorLocation anchorLocation) {
        setAnchorLocation(anchorLocation);
        return this;
    }
}