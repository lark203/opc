package com.atlantafx.components.base;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.function.Consumer;

/**
 * FXMenu - 菜单组件
 * 封装 JavaFX Menu，提供链式调用支持
 * 用于构建下拉菜单、子菜单等
 */
public class FXMenu extends Menu implements IFXNode<FXMenu> {


    /**
     * 默认构造函数
     */
    private FXMenu() {
        super();
    }

    /**
     * 创建带标题的菜单
     *
     * @param title 菜单标题
     */
    private FXMenu(String title) {
        super(title);
    }

    /**
     * 创建带标题和图标的菜单
     *
     * @param title   菜单标题
     * @param graphic 图标节点
     */
    private FXMenu(String title, javafx.scene.Node graphic) {
        super(title, graphic);
    }

    /**
     * 创建空白菜单实例
     *
     * @return FXMenu 实例
     */
    public static FXMenu create() {
        return new FXMenu();
    }

    /**
     * 创建空白菜单实例
     *
     * @param title 菜单标题
     * @return FXMenu 实例
     */
    public static FXMenu create(String title) {
        return new FXMenu(title);
    }

    /**
     * 创建带标题和图标的菜单实例
     *
     * @param title   菜单标题
     * @param graphic 图标节点
     * @return FXMenu 实例
     */
    public static FXMenu create(String title, Node graphic) {
        return new FXMenu(title, graphic);
    }

    /**
     * 核心：向菜单中追加普通菜单项
     */
    public FXMenu add(String text) {
        return add(text, null, null);
    }

    /**
     * 添加菜单项
     *
     * @param text    菜单项文本
     * @param handler 点击事件处理器
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu add(String text, EventHandler<ActionEvent> handler) {
        add(text, null, handler);
        return this;
    }

    /**
     * 添加带快捷键的菜单项
     *
     * @param text     菜单项文本
     * @param keyCombo 快捷键组合（如 "Ctrl+S"）
     * @param handler  点击事件处理器
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu add(String text, String keyCombo, EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem(text);
        if (keyCombo != null) {
            item.setAccelerator(parseAccelerator(keyCombo));
        }
        if (handler != null) {
            item.setOnAction(handler);
        }
        getItems().add(item);
        return this;
    }

    /**
     * 添加复选菜单项
     *
     * @param text    菜单项文本
     * @param handler 状态变更处理器
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu checkItem(String text, Consumer<Boolean> handler) {
        return checkItem(text, false, handler);
    }

    public FXMenu checkItem(String text, boolean selected, Consumer<Boolean> handler) {
        CheckMenuItem item = new CheckMenuItem(text);
        item.setSelected(selected);
        if (handler != null) {
            item.selectedProperty().addListener((obs, oldVal, newVal) -> handler.accept(newVal));
        }
        getItems().add(item);
        return this;
    }

    /**
     * 添加单选菜单项
     *
     * @param text        菜单项文本
     * @param toggleGroup 切换组
     * @param handler     选择处理器
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu radioItem(String text, ToggleGroup toggleGroup, EventHandler<ActionEvent> handler) {
        return radioItem(text, false, toggleGroup, handler);
    }

    public FXMenu radioItem(String text, boolean selected, ToggleGroup toggleGroup, EventHandler<ActionEvent> handler) {
        RadioMenuItem item = new RadioMenuItem(text);
        item.setSelected(selected);
        if (toggleGroup != null) {
            item.setToggleGroup(toggleGroup);
        }
        if (handler != null) {
            item.setOnAction(handler);
        }
        getItems().add(item);
        return this;
    }

    /**
     * 添加分隔线
     *
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu separator() {
        getItems().add(new SeparatorMenuItem());
        return this;
    }

    /**
     * 添加子菜单
     *
     * @param title         子菜单名称
     * @param submenuConfig 子菜单流式配置闭包
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu submenu(String title, Consumer<FXMenu> submenuConfig) {
        FXMenu subMenu = new FXMenu(title);
        if (submenuConfig != null) {
            submenuConfig.accept(subMenu);
        }
        getItems().add(subMenu);
        return this;
    }

    /**
     * 添加自定义菜单项
     *
     * @param node 自定义节点
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu custom(Node node) {
        getItems().add(new CustomMenuItem(node));
        return this;
    }

    /**
     * 设置菜单是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXMenu 实例（链式调用）
     */
    public FXMenu disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 解析快捷键组合字符串
     */
    private KeyCodeCombination parseAccelerator(String accelerator) {
        if (accelerator == null || accelerator.isEmpty()) return null;
        String upper = accelerator.toUpperCase();
        boolean control = upper.contains("CTRL") || upper.contains("CONTROL");
        boolean shift = upper.contains("SHIFT");
        boolean alt = upper.contains("ALT");

        String[] parts = upper.split("\\+");
        String keyPart = parts[parts.length - 1].trim();
        javafx.scene.input.KeyCode keyCode;

        switch (keyPart) {
            case "PLUS":
                keyCode = javafx.scene.input.KeyCode.PLUS;
                break;
            case "MINUS":
                keyCode = javafx.scene.input.KeyCode.MINUS;
                break;
            case "EQUALS":
                keyCode = javafx.scene.input.KeyCode.EQUALS;
                break;
            case "SPACE":
                keyCode = javafx.scene.input.KeyCode.SPACE;
                break;
            default:
                try {
                    keyCode = javafx.scene.input.KeyCode.valueOf(keyPart);
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
        return new KeyCodeCombination(keyCode,
                control ? KeyCombination.CONTROL_DOWN : KeyCombination.CONTROL_ANY,
                shift ? KeyCombination.SHIFT_DOWN : KeyCombination.SHIFT_ANY,
                alt ? KeyCombination.ALT_DOWN : KeyCombination.ALT_ANY
        );
    }
}