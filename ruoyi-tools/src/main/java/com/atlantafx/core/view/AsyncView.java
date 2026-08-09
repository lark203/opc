package com.atlantafx.core.view;

import javafx.scene.Node;

/**
 * 异步加载的 View
 * 
 * <p>数据加载策略：
 * - 第一次进入页面时显示骨架屏并加载数据
 * - 后续重复进入页面时直接显示已缓存的数据，不再显示骨架屏
 */
public interface AsyncView {
    /**
     * 后台加载数据
     */
    void loadData();

    /**
     * 加载完成后渲染 UI
     */
    void setupUI();

    /**
     * 提供一个骨架屏占位图，如果返回 null 则使用默认占位
     */
    default Node getSkeleton() {
        return null;
    }
}
