package com.atlantafx.components.base;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.Ikon;

/**
 * FXTab - 基于 AtlantaFX 风格的标签页组件
 * 继承自 JavaFX Tab，实现 IFXNode 接口支持链式调用
 * 提供丰富的样式、图标、内容绑定和状态控制的快捷方法
 */
public class FXTab extends Tab implements IFXNode<FXTab> {

    /**
     * 默认构造函数
     */
    private FXTab() {
        super();
    }

    /**
     * 创建带标题的标签页
     *
     * @param text 标签页显示的标题文本
     */
    private FXTab(String text) {
        super(text);
    }

    /**
     * 创建带标题和内容节点的标签页
     *
     * @param text    标签页显示的标题文本
     * @param content 标签页的主内容节点
     */
    private FXTab(String text, Node content) {
        super(text, content);
    }

    /**
     * 创建空标签页实例
     *
     * @return FXTab 实例
     */
    public static FXTab create() {
        return new FXTab();
    }

    /**
     * 创建带标题的标签页实例
     *
     * @param text 标签页显示的标题文本
     * @return FXTab 实例
     */
    public static FXTab create(String text) {
        return new FXTab(text);
    }

    /**
     * 创建带标题和内容节点的标签页实例
     *
     * @param text    标签页显示的标题文本
     * @param content 标签页的主内容节点
     * @return FXTab 实例
     */
    public static FXTab create(String text, Node content) {
        return new FXTab(text, content);
    }

    /**
     * 设置标签页的主内容节点
     *
     * @param content 内容节点
     * @return FXTab 实例（链式调用）
     */
    public FXTab content(Node content) {
        setContent(content);
        return this;
    }

    /**
     * 设置标签页的标题文本
     *
     * @param text 标题文本
     * @return FXTab 实例（链式调用）
     */
    public FXTab text(String text) {
        setText(text);
        return this;
    }

    /**
     * 绑定标签页标题属性到指定的 StringProperty
     *
     * @param stringProperty 字符串属性
     * @return FXTab 实例（链式调用）
     */
    public FXTab bindText(StringProperty stringProperty) {
        textProperty().bind(stringProperty);
        return this;
    }

    /**
     * 设置标签页的图形节点（通常用于显示图标）
     *
     * @param graphic 图形节点
     * @return FXTab 实例（链式调用）
     */
    public FXTab graphic(Node graphic) {
        setGraphic(graphic);
        return this;
    }

    /**
     * 快捷设置 Ikonli 图标作为标签页标题旁的图形
     *
     * @param iconCode Ikonli 图标代码
     * @return FXTab 实例（链式调用）
     */
    public FXTab icon(Ikon iconCode) {
        setGraphic(FXFontIcon.create(iconCode));
        return this;
    }

    /**
     * 设置标签页是否允许被用户手动关闭
     *
     * @param closable true-允许关闭（显示关闭按钮），false-不允许关闭
     * @return FXTab 实例（链式调用）
     */
    public FXTab closable(boolean closable) {
        setClosable(closable);
        return this;
    }

    /**
     * 设置标签页的禁用状态
     *
     * @param disabled true-禁用该标签页（用户无法点击切换），false-启用
     * @return FXTab 实例（链式调用）
     */
    public FXTab disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 绑定标签页的禁用状态属性
     *
     * @param booleanProperty 布尔属性
     * @return FXTab 实例（链式调用）
     */
    public FXTab bindDisabled(BooleanProperty booleanProperty) {
        disableProperty().bind(booleanProperty);
        return this;
    }

    /**
     * 为标签页快捷绑定悬停提示工具条
     *
     * @param text 提示文本内容
     * @return FXTab 实例（链式调用）
     */
    public FXTab tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    /**
     * 为标签页绑定自定义 Tooltip 实例
     *
     * @param tooltip Tooltip 实例
     * @return FXTab 实例（链式调用）
     */
    public FXTab tooltip(Tooltip tooltip) {
        setTooltip(tooltip);
        return this;
    }

    /**
     * 当该标签页被用户选中激活时触发的事件回调
     *
     * @param action 触发后的业务逻辑处理器
     * @return FXTab 实例（链式调用）
     */
    public FXTab onSelected(Runnable action) {
        selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && action != null) {
                action.run();
            }
        });
        return this;
    }

    /**
     * 当该标签页失去焦点（由选中变为未选中）时触发的事件回调
     *
     * @param action 触发后的业务逻辑处理器
     * @return FXTab 实例（链式调用）
     */
    public FXTab onDeselected(Runnable action) {
        selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && action != null) {
                action.run();
            }
        });
        return this;
    }

    /**
     * 当该标签页被用户关闭时触发的事件回调
     *
     * @param handler 关闭事件的处理程序
     * @return FXTab 实例（链式调用）
     */
    public FXTab onCloseRequest(javafx.event.EventHandler<javafx.event.Event> handler) {
        setOnCloseRequest(handler);
        return this;
    }
}