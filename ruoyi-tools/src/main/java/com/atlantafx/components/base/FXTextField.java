package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * FXTextField - 基于 AtlantaFX 风格的文本输入框组件
 * 继承自 JavaFX TextField，实现 IFXNode 接口支持链式调用
 * 深度整合了 AtlantaFX 样式变体（密文、状态边框、圆角、清空按钮及内外嵌图标排版）
 */
public class FXTextField extends TextField implements IFXNode<FXTextField> {

    /**
     * 构造函数私有化，强制通过静态工厂方法 create() 实例化
     */
    private FXTextField() {
        super();
    }

    /**
     * 创建一个空白的文本输入框实例
     *
     * @return FXTextField 实例（链式调用入口）
     */
    public static FXTextField create() {
        return new FXTextField();
    }

    /**
     * 创建带提示文本的文本输入框实例
     *
     * @param prompt 提示文本（当输入框为空时灰色显示）
     * @return FXTextField 实例（链式调用入口）
     */
    public static FXTextField create(String prompt) {
        return new FXTextField().prompt(prompt);
    }

    // ==================== 基础核心属性流式封装 ====================

    /**
     * 设置输入框的当前文本内容
     *
     * @param text 文本内容
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField text(String text) {
        setText(text);
        return this;
    }

    /**
     * 绑定输入框的文本属性到指定的 StringProperty
     *
     * @param stringProperty 字符串属性对象
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField bindText(StringProperty stringProperty) {
        textProperty().bindBidirectional(stringProperty);
        return this;
    }

    /**
     * 设置输入框的提示文本（Prompt Text）
     *
     * @param text 提示文本内容
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField prompt(String text) {
        setPromptText(text);
        return this;
    }

    /**
     * 设置输入框是否为只读状态
     *
     * @param editable true-可编辑，false-只读不可点
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    /**
     * 设置输入框的禁用状态
     *
     * @param disabled true-完全禁用，false-正常交互
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置文本的对齐方式（靠左、居中、靠右）
     *
     * @param alignment Pos 枚举对象
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField align(Pos alignment) {
        setAlignment(alignment);
        return this;
    }

    /**
     * 设置当置于 VBox 容器中时的纵向延伸优先级
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置当置于 HBox 容器中时的横向延伸优先级（常用于表单通栏拉伸）
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置输入框的偏好宽度
     *
     * @param width 像素宽度
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField prefWidthValue(double width) {
        setPrefWidth(width);
        return this;
    }

    /**
     * 设置输入框宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置输入框高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置输入框可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置输入框是否受布局管理
     * managed=false 时，父容器会忽略此节点的存在
     *
     * @param managed true-受管理，false-不受管理
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    // ==================== 核心增强：文本验证与长度限制 ====================

    /**
     * 保留源方法：限制输入框的最大允许输入字符长度
     *
     * @param maxLength 最大长度限制数
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField maxLength(int maxLength) {
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                setText(newVal.substring(0, maxLength));
            }
        });
        return this;
    }

    /**
     * 快捷控制：开启纯数字输入限制模式（自动拦截所有非数字按键）
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField numericOnly() {
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        return this;
    }

    // ==================== 事件监听机制流式封装 ====================

    /**
     * 保留源方法：监听文本内容的实时变更
     *
     * @param listener 接收新文本字符串的 Consumer 回调
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField onTextChange(Consumer<String> listener) {
        if (listener != null) {
            textProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        }
        return this;
    }

    /**
     * 保留源方法并标准化：当在输入框内按下回车键（Enter）时的快捷监听触发器
     *
     * @param listener 回车键按下时的事件处理器
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField onEnter(EventHandler<KeyEvent> listener) {
        if (listener != null) {
            addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    listener.handle(event);
                }
            });
        }
        return this;
    }

    /**
     * 为输入框绑定快捷悬停提示信息
     *
     * @param text 提示文本
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    // ==================== AtlantaFX 专属形态与样式变体 ====================

    /**
     * 胶囊圆角外形风格 (Styles.PILL)
     * 将左右两侧物理裁剪为完全圆角，打造极简前卫的搜索条质感
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField pill() {
        return stylesClass(Styles.CENTER_PILL);
    }

    /**
     * 圆角形外形风格 (Styles.ROUNDED)
     * 借助 AtlantaFX 机制将输入框的左右两侧物理裁剪为圆角，创建更圆润的输入框样式
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField rounded() {
        return stylesClass(Styles.ROUNDED);
    }

    // ==================== 尺寸变体控制（对齐 FXButton 规范） ====================

    /**
     * 大号尺寸 (Styles.LARGE)
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField lg() {
        return stylesClass(Styles.LARGE);
    }

    /**
     * 小号紧凑尺寸 (Styles.SMALL)
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField sm() {
        return stylesClass(Styles.SMALL);
    }

    // ==================== 表单校验状态交互定制 ====================

    /**
     * 激活成功态高亮变体 (Styles.SUCCESS)
     * 输入框边框及阴影聚焦时会自然折射出优雅的“生态绿”，常用于表单校验正确时
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField success() {
        pseudoClassStateChanged(Styles.STATE_SUCCESS, true);
        return this;
    }

    /**
     * 激活危险/错误态高亮变体 (Styles.DANGER)
     * 输入框边框直接转为“警示红”，聚焦时呈现危险发光，常用于空拦截、格式错误、密码校验失败
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField danger() {
        pseudoClassStateChanged(Styles.STATE_DANGER, true);
        return this;
    }

    /**
     * 恢复/重置输入框的所有状态状态（清除 success 和 danger 变体类）
     *
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField resetState() {
        pseudoClassStateChanged(Styles.STATE_SUCCESS, false);
        pseudoClassStateChanged(Styles.STATE_DANGER, false);
        return this;
    }

    // ==================== 高级自定义颜色定制（解决 Looked-up 权重覆盖） ====================

    /**
     * 突破主题限制，局部精细化复写输入框的文本及光标基础色值
     *
     * @param colorHex CSS 颜色字符串（如 "#3b82f6"）或系统变量
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField fontColor(String colorHex) {
        if (colorHex == null || colorHex.trim().isEmpty()) return this;
        return styleCss(
                "-fx-text-fill: " + colorHex + ";" +
                        "-fx-prompt-text-fill: " + colorHex + "a0;" // 自动为提示文字附加一定的半透明度
        );
    }

    /**
     * 突破主题限制，局部复写输入框的底层背景色
     *
     * @param colorHex CSS 颜色字符串或 Looked-up 变量名
     * @return FXTextField 实例（链式调用）
     */
    public FXTextField backgroundColor(String colorHex) {
        if (colorHex == null || colorHex.trim().isEmpty()) return this;
        return styleCss("-fx-background-color: " + colorHex + ";");
    }
}