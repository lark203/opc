package com.atlantafx.core.view;

/**
 * 页面生命周期接口
 *
 * <p>页面生命周期顺序：
 * <pre>
 * onCreated() → onInit() → onShow() → onHide() → onDispose()
 *                 ↑              │
 *                 └──────────────┘ (show/hide 可多次触发)
 * </pre>
 *
 * <p>生命周期各阶段说明：
 * <ul>
 *   <li>onCreated() - 页面创建时调用（构造函数后）</li>
 *   <li>onInit() - 页面初始化时调用（节点树构建完成后）</li>
 *   <li>onShow() - 页面显示时调用（每次切换到该页面都会触发）</li>
 *   <li>onHide() - 页面隐藏时调用（每次离开该页面都会触发）</li>
 *   <li>onDispose() - 页面销毁时调用（从 ViewFactory 缓存中移除时）</li>
 * </ul>
 */
public interface PageLifecycle {

    /**
     * 页面创建时调用
     * 在构造函数执行后立即调用，适合初始化成员变量
     */
    default void onCreated() {
    }

    /**
     * 页面初始化时调用
     * 在节点树构建完成后调用，适合执行需要场景或父节点的初始化操作
     */
    default void onInit() {
    }

    /**
     * 页面显示时调用
     * 每次页面被激活/显示时都会触发，适合刷新数据或重置状态
     */
    default void onShow() {
    }

    /**
     * 页面隐藏时调用
     * 每次页面被隐藏/离开时都会触发，适合保存状态或暂停操作
     */
    default void onHide() {
    }

    /**
     * 页面销毁时调用
     * 页面从缓存中移除时调用，适合释放资源、取消订阅等清理操作
     */
    default void onDispose() {
    }

    /**
     * 获取页面是否已初始化
     *
     * @return true 表示已完成 init() 调用
     */
    default boolean isInitialized() {
        return false;
    }

    /**
     * 获取页面是否已销毁
     *
     * @return true 表示已完成 onDispose() 调用
     */
    default boolean isDisposed() {
        return false;
    }
}
