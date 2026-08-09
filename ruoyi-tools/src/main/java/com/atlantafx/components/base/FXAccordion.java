package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * FXAccordion - 手风琴折叠组件
 * 封装多个 FXTitledPane，实现互斥展开效果
 * 一次只能展开一个面板，其他面板自动折叠
 */
public class FXAccordion extends VBox implements IFXNode<FXAccordion> {

    private final List<FXTitledPane> panes = new ArrayList<>();
    private boolean allowMultipleExpand = false;

    /**
     * 默认构造函数
     */
    public FXAccordion() {
        super();
        setSpacing(0);
    }

    /**
     * 创建手风琴组件
     *
     * @param panes 标题面板数组
     */
    public FXAccordion(FXTitledPane... panes) {
        super();
        setSpacing(0);
        addPanes(panes);
    }

    /**
     * 创建空白手风琴实例
     *
     * @return FXAccordion 实例
     */
    public static FXAccordion create() {
        return new FXAccordion();
    }

    /**
     * 创建带面板的手风琴实例
     *
     * @param panes 标题面板数组
     * @return FXAccordion 实例
     */
    public static FXAccordion create(FXTitledPane... panes) {
        return new FXAccordion(panes);
    }

    /**
     * 添加单个标题面板
     *
     * @param pane 标题面板
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion addPane(FXTitledPane pane) {
        panes.add(pane);
        getChildren().add(pane);

        // 设置互斥展开逻辑
        if (!allowMultipleExpand) {
            pane.expandedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    collapseAllExcept(pane);
                }
            });
        }

        // 应用样式
        pane.styleCss("-fx-border-color: -color-border-muted; -fx-border-width: 0 0 1px 0;");

        return this;
    }

    /**
     * 批量添加标题面板
     *
     * @param panes 标题面板数组
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion addPanes(FXTitledPane... panes) {
        for (FXTitledPane pane : panes) {
            addPane(pane);
        }
        return this;
    }

    /**
     * 移除指定的标题面板
     *
     * @param pane 要移除的面板
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion removePane(FXTitledPane pane) {
        panes.remove(pane);
        getChildren().remove(pane);
        return this;
    }

    /**
     * 清空所有标题面板
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion clear() {
        panes.clear();
        getChildren().clear();
        return this;
    }

    /**
     * 设置是否允许多个面板同时处于展开状态
     *
     * @param allow true-允许多个面板共存展开（抽屉模式），false-独占互斥展开（经典手风琴）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion allowMultipleExpand(boolean allow) {
        this.allowMultipleExpand = allow;
        // 如果切回互斥模式且当前有多个展开，收拢到仅保留第一个展开的面板
        if (!allow) {
            FXTitledPane firstExpanded = getExpandedPane();
            if (firstExpanded != null) {
                collapseAllExcept(firstExpanded);
            }
        }
        return this;
    }

    /**
     * 依据索引精准展开指定的面板
     *
     * @param index 面板对应的下标索引（从 0 开始）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion expandIndex(int index) {
        if (index >= 0 && index < panes.size()) {
            expandPane(panes.get(index));
        }
        return this;
    }

    /**
     * 强行展开指定的面板实例
     *
     * @param pane 目标标题面板
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion expandPane(FXTitledPane pane) {
        if (pane != null && panes.contains(pane)) {
            pane.setExpanded(true);
            if (!allowMultipleExpand) {
                collapseAllExcept(pane);
            }
        }
        return this;
    }

    /**
     * 依据索引精准折叠指定的面板
     *
     * @param index 面板对应的下标索引（从 0 开始）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion collapseIndex(int index) {
        if (index >= 0 && index < panes.size()) {
            panes.get(index).setExpanded(false);
        }
        return this;
    }

    /**
     * 折叠指定的面板实例
     *
     * @param pane 目标标题面板
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion collapsePane(FXTitledPane pane) {
        if (pane != null) {
            pane.setExpanded(false);
        }
        return this;
    }

    /**
     * 一键折叠所有面板
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion collapseAll() {
        for (FXTitledPane pane : panes) {
            pane.setExpanded(false);
        }
        return this;
    }

    /**
     * 一键展开所有面板（仅在开启 allowMultipleExpand 时生效）
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion expandAll() {
        if (allowMultipleExpand) {
            for (FXTitledPane pane : panes) {
                pane.setExpanded(true);
            }
        }
        return this;
    }

    /**
     * 动态反转、切换指定索引面板的展开/折叠状态
     *
     * @param index 面板对应的下标索引
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion toggleIndex(int index) {
        if (index >= 0 && index < panes.size()) {
            FXTitledPane pane = panes.get(index);
            boolean state = pane.isExpanded();
            if (!state) {
                expandPane(pane);
            } else {
                collapsePane(pane);
            }
        }
        return this;
    }

    /**
     * 获取当前处于展开状态的面板首个索引值
     *
     * @return 展开的面板索引位置，若全部折叠则返回 -1
     */
    public int getExpandedIndex() {
        for (int i = 0; i < panes.size(); i++) {
            if (panes.get(i).isExpanded()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取当前处于展开状态的面板实例
     *
     * @return 展开的面板对象，若无则返回 null
     */
    public FXTitledPane getExpandedPane() {
        for (FXTitledPane pane : panes) {
            if (pane.isExpanded()) {
                return pane;
            }
        }
        return null;
    }

    /**
     * 获取不可变的托管面板清单
     *
     * @return 面板列表
     */
    public List<FXTitledPane> getPanes() {
        return panes;
    }

    /**
     * 获取当前容器内挂载的面板总数
     *
     * @return 面板总数量
     */
    public int size() {
        return panes.size();
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion warning() {
        return stylesClass(Styles.WARNING);
    }

    /**
     * 为整个手风琴容器加持立体拟物化阴影质感（基于 AtlantaFX Styles 伪类）
     *
     * @param level 阴影等级。1级最轻（Styles.ELEVATED_1）至 4级最深（Styles.ELEVATED_4）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion shadow(int level) {
        getStyleClass().removeAll(Styles.ELEVATED_1, Styles.ELEVATED_2, Styles.ELEVATED_3, Styles.ELEVATED_4);
        switch (level) {
            case 1 -> stylesClass(Styles.ELEVATED_1);
            case 2 -> stylesClass(Styles.ELEVATED_2);
            case 3 -> stylesClass(Styles.ELEVATED_3);
            case 4 -> stylesClass(Styles.ELEVATED_4);
        }
        return this;
    }

    /**
     * 快捷将整个手风琴容器升级为高级“轻量交互式卡片外壳样式”
     * 融合 AtlantaFX 的配色边界，自带 1 像素的微弱优雅边框线和圆角
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion bgCardStyle() {
        return styleCss(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-border-muted;" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 6px;" +
                        "-fx-border-radius: 6px;"
        );
    }

    // ==================== 布局增强 ====================

    /**
     * 设置手风琴容器的统一内边距 (Padding)
     *
     * @param padding Insets 内边距对象
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion padding(Insets padding) {
        setPadding(padding);
        return this;
    }

    /**
     * 快捷设置手风琴容器四周统一的像素内边距值
     *
     * @param value 边距像素值
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion padding(double value) {
        setPadding(new Insets(value));
        return this;
    }

    /**
     * 精确控制手风琴容器四个方向的内边距值
     */
    public FXAccordion padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置面板间的物理间距
     *
     * @param spacing 间距像素值
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion spacing(double spacing) {
        setSpacing(spacing);
        return this;
    }

    /**
     * 设置组件固定宽度（同步刷新最小与首选宽度）
     *
     * @param w 宽度值（像素）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置组件固定高度（同步刷新最小与首选高度）
     *
     * @param h 高度值（像素）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 快捷锁定首选宽高尺度
     *
     * @param w 宽度（像素）
     * @param h 高度（像素）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 当置于 VBox 父容器中时，声明纵向最高延伸优先级（填满剩余空间）
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 当置于 HBox 父容器中时，声明横向最高延伸优先级
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置手风琴可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置手风琴是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置手风琴透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置手风琴是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为设置页面手风琴
     * 适合用于分组设置项
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion asSettings() {
        return spacing(0);
    }

    /**
     * 设置为 FAQ 手风琴
     * 适合用于常见问题解答
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion asFAQ() {
        return spacing(5)
                .accent();
    }

    /**
     * 设置为导航菜单手风琴
     * 适合用于多级菜单
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion asNavigation() {
        return spacing(0)
                .width(250);
    }

    /**
     * 设置为卡片风格手风琴
     * 每个面板都有边框和圆角
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion asCard() {
        spacing(8);
        for (FXTitledPane pane : panes) {
            pane.asCard();
        }
        return this;
    }

    /**
     * 设置为紧凑风格手风琴
     * 更小的间距和字体
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion compact() {
        return spacing(2);
    }

    /**
     * 设置为宽松风格手风琴
     * 更大的间距
     *
     * @return FXAccordion 实例（链式调用）
     */
    public FXAccordion spacious() {
        return spacing(10);
    }

    // ==================== 内部核心辅助处理 ====================

    /**
     * 核心互斥驱动：折叠除指定面板之外的其余所有面板
     */
    private void collapseAllExcept(FXTitledPane except) {
        for (FXTitledPane pane : panes) {
            if (pane != except) {
                pane.setExpanded(false);
            }
        }
    }
}
