package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * FXTextArea - 基于 AtlantaFX 风格的多行文本输入框组件
 * 继承自 JavaFX TextArea本地组件，实现 IFXNode 接口支持全链式流式编程
 * 深度整合了 AtlantaFX 大文本框的各种状态变体、网格对齐、自动换行、等宽字体及表单动态校验机制。
 */
public class FXTextArea extends TextArea implements IFXNode<FXTextArea> {

    /**
     * 构造函数私有化，强制通过静态工厂方法进行实例化
     */
    private FXTextArea() {
        super();
    }

    /**
     * 构造函数私有化（带初始文本内容）
     *
     * @param text 初始的文本内容
     */
    private FXTextArea(String text) {
        super(text);
    }

    /**
     * 创建一个空白的多行文本输入框实例
     *
     * @return FXTextArea 实例（链式调用入口）
     */
    public static FXTextArea create() {
        return new FXTextArea();
    }

    /**
     * 创建带提示文本的多行文本输入框实例
     *
     * @param prompt 灰色底纹提示文本
     * @return FXTextArea 实例（链式调用入口）
     */
    public static FXTextArea create(String prompt) {
        return new FXTextArea().prompt(prompt);
    }

    /**
     * 创建带初始内容和提示文本的多行文本输入框实例
     *
     * @param text   初始内容文本
     * @param prompt 提示文本
     * @return FXTextArea 实例（链式调用入口）
     */
    public static FXTextArea create(String text, String prompt) {
        return new FXTextArea(text).prompt(prompt);
    }

    // ==================== 基础常规属性与网格尺寸封装 ====================

    /**
     * 设置多行文本框的当前内容
     *
     * @param text 文本内容
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea text(String text) {
        setText(text);
        return this;
    }

    /**
     * 双向绑定多行文本框的文本属性到指定的 StringProperty
     *
     * @param stringProperty 响应式字符串属性对象
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea bindText(StringProperty stringProperty) {
        textProperty().bindBidirectional(stringProperty);
        return this;
    }

    /**
     * 设置占位提示文本（Prompt Text）
     *
     * @param text 提示文本内容
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea prompt(String text) {
        setPromptText(text);
        return this;
    }

    /**
     * 保留源方法：设置文本区域的首选显式行数（控制大文本框的初始物理高度）
     *
     * @param rows 行数
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea rowCount(int rows) {
        setPrefRowCount(rows);
        return this;
    }

    /**
     * 保留源方法：设置文本区域的首选显式列数（控制大文本框的初始物理宽度）
     *
     * @param cols 列数
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea colCount(int cols) {
        setPrefColumnCount(cols);
        return this;
    }

    /**
     * 设置输入框是否为只读状态
     *
     * @param editable true-可编辑，false-只读（不可修改但文本仍可选中复制）
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    /**
     * 设置输入框的完全禁用状态
     *
     * @param disabled true-禁用，false-启用
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置文本自动换行策略（Wrap Text）
     *
     * @param wrap true-文本超出右侧边界自动折行，false-产生横向滚动条
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea wrapText(boolean wrap) {
        setWrapText(wrap);
        return this;
    }

    /**
     * 保留源方法：一键控制并重定位输入光标到指定的文本字符索引位置
     *
     * @param pos 光标位置索引
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea positionCaretValue(int pos) {
        positionCaret(pos);
        return this;
    }

    /**
     * 设置当置于 VBox 容器中时的纵向延伸优先级（常用于大文本域通栏撑开高度）
     *
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置当置于 HBox 容器中时的横向延伸优先级
     *
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置多行文本框的偏好固定宽度
     *
     * @param width 像素宽度
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea prefWidthValue(double width) {
        setPrefWidth(width);
        return this;
    }

    /**
     * 设置多行文本框的偏好固定高度
     *
     * @param height 像素高度
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea prefHeightValue(double height) {
        setPrefHeight(height);
        return this;
    }

    // ==================== 业务安全控制：字数截断与响应式监听 ====================

    /**
     * 保留并优化源方法：强力限制最大允许输入的字符长度（超出部分精准动态截断）
     *
     * @param maxLength 最大允许的文本长度
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea maxLength(int maxLength) {
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                setText(newVal.substring(0, maxLength));
            }
        });
        return this;
    }

    /**
     * 保留并重构源方法：监听多行文本区域内文本内容的实时变动
     *
     * @param listener 接收新文本字符串的 Consumer 回调
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea onTextChange(Consumer<String> listener) {
        if (listener != null) {
            textProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        }
        return this;
    }

    /**
     * 当在多行文本框内按下特定组合键或回车时的键盘按键快捷监听处理器
     *
     * @param handler 键盘事件处理器
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea onKeyPressed(EventHandler<KeyEvent> handler) {
        if (handler != null) {
            addEventHandler(KeyEvent.KEY_PRESSED, handler);
        }
        return this;
    }

    /**
     * 为多行文本框绑定快捷悬停浮窗提示信息
     *
     * @param text 提示内容文本
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    // ==================== AtlantaFX 专属形态与样式变体 ====================

    /**
     * 扁平化无边框风格 (Styles.FLAT)
     * 移除实体四周坚硬的边框，常用于仪表盘卡片面板或无感嵌入式编写区域
     *
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea flat() {
        return stylesClass(Styles.FLAT);
    }

    // ==================== 表单校验状态交互变体 ====================

    /**
     * 激活成功态高亮变体 (Styles.SUCCESS) - 绿色边框及阴影聚焦发光
     * 常用于配置项（如 JSON 格式、公钥注入）通过异步后台校验正确时的绿色视觉渲染
     *
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea success() {
        pseudoClassStateChanged(Styles.STATE_SUCCESS, true);
        return this;
    }

    /**
     * 激活危险/错误态高亮变体 (Styles.DANGER) - 红色警示边框
     * 常用于内容空拦截、非法语法格式（如 XML 报错、SQL 格式不正确）的危险红框预警
     *
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea danger() {
        pseudoClassStateChanged(Styles.STATE_DANGER, true);
        return this;
    }


    /**
     * 恢复/重置多行文本框的所有形态状态（清除所有成功、危险、警告类的色彩修饰）
     *
     * @return FXTextArea 实例（链式调用）
     */
    public FXTextArea resetState() {
        pseudoClassStateChanged(Styles.STATE_SUCCESS, false);
        pseudoClassStateChanged(Styles.STATE_DANGER, false);
        return this;
    }
}