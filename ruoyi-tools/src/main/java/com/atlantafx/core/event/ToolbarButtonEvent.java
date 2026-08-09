package com.atlantafx.core.event;

import javafx.scene.Node;

/**
 * 工具栏按钮事件：用于动态添加/移除标题栏工具栏按钮
 * <p>
 * 使用方式：
 * <pre>{@code
 * // 添加按钮
 * EventBus.publish(ToolbarButtonEvent.add(myButton));
 *
 * // 移除按钮
 * EventBus.publish(ToolbarButtonEvent.remove(myButton));
 *
 * // 清空所有自定义按钮
 * EventBus.publish(ToolbarButtonEvent.clear());
 * }</pre>
 */
public record ToolbarButtonEvent(
        ActionType actionType,
        Node button,
        int position
) implements AppEvent {

    /**
     * 操作类型
     */
    public enum ActionType {
        /**
         * 添加按钮
         */
        ADD,
        /**
         * 移除按钮
         */
        REMOVE,
        /**
         * 清空所有自定义按钮
         */
        CLEAR,
        /**
         * 设置所有自定义按钮（替换现有）
         */
        SET_ALL
    }

    /**
     * 创建添加按钮事件（默认添加到末尾）
     *
     * @param button 要添加的按钮节点
     * @return ToolbarButtonEvent 事件对象
     */
    public static ToolbarButtonEvent add(Node button) {
        return new ToolbarButtonEvent(ActionType.ADD, button, -1);
    }

    /**
     * 创建添加按钮事件（指定位置）
     *
     * @param button   要添加的按钮节点
     * @param position 添加位置（-1 表示末尾）
     * @return ToolbarButtonEvent 事件对象
     */
    public static ToolbarButtonEvent add(Node button, int position) {
        return new ToolbarButtonEvent(ActionType.ADD, button, position);
    }

    /**
     * 创建移除按钮事件
     *
     * @param button 要移除的按钮节点
     * @return ToolbarButtonEvent 事件对象
     */
    public static ToolbarButtonEvent remove(Node button) {
        return new ToolbarButtonEvent(ActionType.REMOVE, button, -1);
    }

    /**
     * 创建清空所有自定义按钮事件
     *
     * @return ToolbarButtonEvent 事件对象
     */
    public static ToolbarButtonEvent clear() {
        return new ToolbarButtonEvent(ActionType.CLEAR, null, -1);
    }

    /**
     * 创建设置所有按钮事件（替换现有按钮）
     *
     * @param buttons 新的按钮列表
     * @return ToolbarButtonEvent 事件对象
     */
    public static ToolbarButtonEvent setAll(java.util.List<Node> buttons) {
        return new ToolbarButtonEvent(ActionType.SET_ALL, null, -1);
    }
}