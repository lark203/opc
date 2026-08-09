package com.atlantafx.components.base;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignE;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

import java.util.function.Consumer;

/**
 * FXCustomTextField - 基于 AtlantaFX 风格的高级定制文本输入框组件
 * 继承自 AtlantaFX CustomTextField，实现 IFXNode 接口支持链式调用
 * 突破了原生输入框的局限，完美支持在输入框内嵌左/右侧图标、行动按钮、状态校验及长度拦截。
 */
public class FXCustomTextField extends CustomTextField implements IFXNode<FXCustomTextField> {

    /**
     * 构造函数私有化，强制通过静态工厂方法 create() 实例化
     */
    private FXCustomTextField() {
        super();
    }

    /**
     * 创建一个空白的高级定制输入框实例
     *
     * @return FXCustomTextField 实例（链式调用入口）
     */
    public static FXCustomTextField create() {
        return new FXCustomTextField();
    }

    /**
     * 创建带提示文本的高级定制输入框实例
     *
     * @param prompt 提示文本（当输入框为空时灰色显示）
     * @return FXCustomTextField 实例（链式调用入口）
     */
    public static FXCustomTextField create(String prompt) {
        return new FXCustomTextField().prompt(prompt);
    }

    // ==================== 核心突破：内嵌左/右侧图形与图标支持 ====================

    /**
     * 在输入框内部的左侧嵌入一个任意的 JavaFX Node 节点
     *
     * @param node 左侧内嵌节点（如自定义按钮、复杂的标签等）
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField leftGraphic(Node node) {
        setLeft(node);
        return this;
    }

    /**
     * 【高频】在输入框内部的左侧快捷嵌入一个 Ikonli 图标
     *
     * @param iconCode Ikonli 图标代码
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField leftIcon(Ikon iconCode) {
        return leftGraphic(FXFontIcon.create(iconCode));
    }

    /**
     * 在输入框内部的右侧嵌入一个任意的 JavaFX Node 节点（常用于嵌入清除按钮、发送验证码按钮）
     *
     * @param node 右侧内嵌节点
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField rightGraphic(Node node) {
        setRight(node);
        return this;
    }

    /**
     * 【高频】在输入框内部的右侧快捷嵌入一个 Ikonli 图标
     *
     * @param iconCode Ikonli 图标代码
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField rightIcon(Ikon iconCode) {
        return rightGraphic(FXFontIcon.create(iconCode));
    }

    // ==================== 基础常规属性流式封装 ====================

    /**
     * 设置输入框的当前文本内容
     *
     * @param text 文本内容
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField text(String text) {
        setText(text);
        return this;
    }

    /**
     * 双向绑定输入框的文本属性到指定的 StringProperty
     *
     * @param stringProperty 字符串属性对象
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField bindText(StringProperty stringProperty) {
        textProperty().bindBidirectional(stringProperty);
        return this;
    }

    /**
     * 设置输入框的提示文本（Prompt Text）
     *
     * @param text 提示文本内容
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField prompt(String text) {
        setPromptText(text);
        return this;
    }

    /**
     * 设置输入框是否为只读状态
     *
     * @param editable true-可编辑，false-只读
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField editable(boolean editable) {
        setEditable(editable);
        return this;
    }

    /**
     * 设置输入框的禁用状态
     *
     * @param disabled true-完全禁用，false-正常交互
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置文本在输入框内部的对齐方式
     *
     * @param alignment Pos 枚举对象（如 Pos.CENTER_RIGHT 可用于数字货币输入）
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField alignment(Pos alignment) {
        setAlignment(alignment);
        return this;
    }

    /**
     * 设置当置于 VBox 容器中时的纵向延伸优先级
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置当置于 HBox 容器中时的横向延伸优先级（常用于多组件通栏拉伸）
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置输入框的偏好固定宽度
     *
     * @param width 像素宽度
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField prefWidthValue(double width) {
        setPrefWidth(width);
        return this;
    }

    /**
     * 设置输入框宽度（同时设置最小宽度和首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置输入框高度（同时设置最小高度和首选高度）
     *
     * @param h 高度值（像素）
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    // ==================== 业务安全控制：文本拦截与验证 ====================

    /**
     * 限制输入框的最大允许输入字符长度（超出部分自动截断）
     *
     * @param maxLength 最大长度限制数
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField maxLength(int maxLength) {
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                setText(newVal.substring(0, maxLength));
            }
        });
        return this;
    }

    /**
     * 开启纯数字输入限制模式（底层自动过滤所有非数字按键）
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField numericOnly() {
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        return this;
    }

    /**
     * 开启邮箱输入限制模式（底层自动过滤所有非邮箱按键）
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField emailOnly() {
        this.rightIcon(MaterialDesignE.EMAIL);
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*")) {
                setText(newVal.replaceAll("[^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*]", ""));
            }
        });
        return this;
    }

    /**
     * 开启手机号码输入限制模式（底层自动过滤所有非手机按键）
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField phoneOnly() {
        return rightIcon(MaterialDesignP.PHONE).maxLength(11).numericOnly();
    }

    /**
     * 监听文本框的键盘按键按下事件
     *
     * @param listener 键盘按键按下事件处理器
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField onKeyPressed(EventHandler<KeyEvent> listener) {
        addEventHandler(KeyEvent.KEY_PRESSED, listener);
        return this;
    }

    /**
     * 监听文本框的键盘按键抬起事件
     *
     * @param listener 键盘按键抬起事件处理器
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField onKeyReleased(EventHandler<KeyEvent> listener) {
        addEventHandler(KeyEvent.KEY_RELEASED, listener);
        return this;
    }

    // ==================== 事件监听机制流式封装 ====================

    /**
     * 监听文本内容的实时变更回调
     *
     * @param listener 接收新文本字符串的 Consumer 回调
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField onTextChange(Consumer<String> listener) {
        if (listener != null) {
            textProperty().addListener((obs, oldVal, newVal) -> listener.accept(newVal));
        }
        return this;
    }

    /**
     * 当在输入框内按下回车键（Enter）时的快捷监听触发器
     *
     * @param listener 回车键按下时的事件处理器
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField onEnter(EventHandler<KeyEvent> listener) {
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
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField tooltip(String text) {
        setTooltip(new Tooltip(text));
        return this;
    }

    // ==================== AtlantaFX 专属形态与尺寸变体 ====================

    /**
     * 胶囊圆角外形风格 (Styles.PILL)
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField pill() {
        return stylesClass(Styles.CENTER_PILL);
    }

    /**
     * 大号尺寸变体 (Styles.LARGE)
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField lg() {
        return stylesClass(Styles.LARGE);
    }

    /**
     * 小号紧凑尺寸变体 (Styles.SMALL)
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField sm() {
        return stylesClass(Styles.SMALL);
    }

    // ==================== 表单校验状态交互变体 ====================

    /**
     * 激活成功态高亮变体 (Styles.SUCCESS) - 绿色发光边框
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField success() {
        pseudoClassStateChanged(Styles.STATE_SUCCESS, true);
        return this;
    }

    /**
     * 激活危险/错误态高亮变体 (Styles.DANGER) - 红色警示边框
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField danger() {
        pseudoClassStateChanged(Styles.STATE_DANGER, true);
        return this;
    }

    /**
     * 恢复/重置输入框的所有状态状态（清除所有高亮色类）
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField resetState() {
        pseudoClassStateChanged(Styles.STATE_SUCCESS, false);
        pseudoClassStateChanged(Styles.STATE_DANGER, false);
        return this;
    }

    // ==================== 特色高频业务场景一键定制 ====================

    /**
     * 【定制特化】一键构建高颜值现代化“全局搜索框”形态
     * 自带胶囊外观、大号尺寸、左侧放大镜图标、右侧可清空小叉号，常用于系统的顶部导航或数据表格上游。
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField searchStyle() {
        return this.lg()
                .pill()
                .leftIcon(MaterialDesignM.MAGNIFY);
    }

    /**
     * 【定制特化】一键构建高颜值modern “可清空”形态
     * 自带右侧可清空小叉号图标，常用于表单输入框或数据表格列。
     *
     * @return FXCustomTextField 实例（链式调用）
     */
    public FXCustomTextField clearStyle() {
        FXFontIcon fxFontIcon = FXFontIcon.create(MaterialDesignC.CLOSE);
        fxFontIcon.setCursor(Cursor.HAND);
        fxFontIcon.setOnMouseClicked(event -> setText(""));
        return this.rightGraphic(fxFontIcon);
    }
}