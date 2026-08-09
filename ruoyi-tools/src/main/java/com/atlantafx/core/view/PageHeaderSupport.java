package com.atlantafx.core.view;

import javafx.scene.Node;

import java.util.Collections;
import java.util.List;

/**
 * 页面 Header 支持接口
 */
public interface PageHeaderSupport {
    /**
     * 默认返回空列表，子类可覆盖以提供按钮
     */
    default List<Node> getHeaderTools() {
        return Collections.emptyList();
    }
}