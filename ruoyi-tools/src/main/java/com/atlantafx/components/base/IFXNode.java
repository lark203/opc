package com.atlantafx.components.base;

import javafx.scene.Node;

public interface IFXNode<T> {
    /**
     * 添加样式类 - 类选择器
     *
     * @param classes 样式类
     * @return this
     */
    default T stylesClass(String... classes) {
        ((Node) this).getStyleClass().addAll(classes);
        return (T) this;
    }

    /**
     * 添加样式 - 样式属性
     *
     * @param style 样式属性
     * @return this
     */
    default T styleCss(String style) {
        ((Node) this).setStyle(style);
        return (T) this;
    }
}