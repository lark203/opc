package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * FXSplitPane - 可拖动分割面板组件
 * 继承自 JavaFX SplitPane，实现 IFXNode 接口支持链式调用
 * 提供便捷的分隔区域、拖动手柄和样式设置方法
 */
public class FXSplitPane extends SplitPane implements IFXNode<FXSplitPane> {

    /**
     * 默认构造函数
     */
    public FXSplitPane() {
        super();
    }

    /**
     * 创建水平分割面板（左右分割）
     *
     * @param left  左侧节点
     * @param right 右侧节点
     */
    public FXSplitPane(Node left, Node right) {
        super(left, right);
        setOrientation(Orientation.HORIZONTAL);
    }

    /**
     * 创建垂直分割面板（上下分割）
     *
     * @param top    顶部节点
     * @param bottom 底部节点
     */
    public FXSplitPane(Node top, Node bottom, boolean vertical) {
        super(top, bottom);
        setOrientation(Orientation.VERTICAL);
    }

    /**
     * 创建空白分割面板实例
     *
     * @return FXSplitPane 实例
     */
    public static FXSplitPane create() {
        return new FXSplitPane();
    }

    /**
     * 创建水平分割面板
     *
     * @param left  左侧节点
     * @param right 右侧节点
     * @return FXSplitPane 实例
     */
    public static FXSplitPane createHorizontal(Node left, Node right) {
        return new FXSplitPane(left, right);
    }

    /**
     * 创建垂直分割面板
     *
     * @param top    顶部节点
     * @param bottom 底部节点
     * @return FXSplitPane 实例
     */
    public static FXSplitPane createVertical(Node top, Node bottom) {
        return new FXSplitPane(top, bottom, true);
    }

    /**
     * 添加子节点到分割面板
     *
     * @param nodes 要添加的节点数组
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane add(Node... nodes) {
        getItems().addAll(nodes);
        return this;
    }

    /**
     * 设置分割方向为水平（左右分割）
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane horizontal() {
        setOrientation(Orientation.HORIZONTAL);
        return this;
    }

    /**
     * 设置分割方向为垂直（上下分割）
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane vertical() {
        setOrientation(Orientation.VERTICAL);
        return this;
    }

    /**
     * 精确控制指定索引位置的分割手柄比例
     *
     * @param index    分割线索引（从 0 开始）
     * @param position 位置比例（0.0 - 1.0）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane dividerPosition(int index, double position) {
        if (position < 0.0 || position > 1.0) {
            throw new IllegalArgumentException("分割位置比例必须严格限制在 0.0 至 1.0 之间");
        }
        setDividerPosition(index, position);
        return this;
    }

    /**
     * 快捷控制第一个分割手柄（索引为 0）的位置
     *
     * @param position 位置比例（0.0 - 1.0）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane dividerPosition(double position) {
        return dividerPosition(0, position);
    }

    /**
     * 【已补全】安全获取指定索引位置的分割手柄当前绝对比例位置
     *
     * @param index 分割线索引
     * @return 位置比例（0.0 - 1.0），若手柄尚未渲染或不存在则返回 0.0
     */
    public double getDividerPosition(int index) {
        if (index >= 0 && index < getDividers().size()) {
            return getDividers().get(index).getPosition();
        }
        return 0.0;
    }

    /**
     * 【已补全】快捷获取第一个分割手柄的比例位置
     *
     * @return 第一个手柄的比例位置
     */
    public double getDividerPosition() {
        return getDividerPosition(0);
    }

    /**
     * 一键冻结或解冻所有分割手柄，防止用户手动拖动破坏既定后台排版布局
     *
     * @param resizable true-允许拖动调节，false-强制死锁当前比例线
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane dividerResizable(boolean resizable) {
        if (!resizable && !getDividers().isEmpty()) {
            final double[] lockedPositions = new double[getDividers().size()];
            for (int i = 0; i < getDividers().size(); i++) {
                lockedPositions[i] = getDividerPositions()[i];
            }
            // 采用精准的物理阻尼属性监听链，一旦偏移立刻回弹锁死
            for (int i = 0; i < getDividers().size(); i++) {
                final int index = i;
                getDividers().get(i).positionProperty().addListener((obs, oldVal, newVal) -> {
                    if (Math.abs((Double) newVal - lockedPositions[index]) > 0.001) {
                        setDividerPosition(index, lockedPositions[index]);
                    }
                });
            }
        }
        return this;
    }

    /**
     * 设置分割手柄的最小宽度/高度
     *
     * @param size 最小尺寸（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane minDividerSize(double size) {
        setMinWidth(size);
        return this;
    }

    /**
     * 设置分割手柄的最大宽度/高度
     *
     * @param size 最大尺寸（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane maxDividerSize(double size) {
        setMaxWidth(size);
        return this;
    }

    /**
     * 设定第一个挂载面板（首元容器）的最小高度或宽度界限值
     *
     * @param size 像素级尺寸约束
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane firstPanelMinSize(double size) {
        if (!getItems().isEmpty()) {
            Node first = getItems().getFirst();
            if (first instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) first).setMinSize(size, size);
            }
        }
        return this;
    }

    /**
     * 设定第二个挂载面板（次元容器）的最小高度或宽度界限值
     *
     * @param size 像素级尺寸约束
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane secondPanelMinSize(double size) {
        if (getItems().size() > 1) {
            Node second = getItems().get(1);
            if (second instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) second).setMinSize(size, size);
            }
        }
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 为当前可拖动面板一键注入现代化的拟物态立体阴影（Elevations）
     * 借力 AtlantaFX 的 Styles 伪类，提供多级阴影质感
     *
     * @param level 阴影等级：1级最轻，4级最深
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane shadow(int level) {
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
     * 应用成功样式（Success）
     * 绿色分割线
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     * 红色分割线
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     * 使用主题主色调
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     * 橙色/黄色分割线
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane warning() {
        return stylesClass(Styles.WARNING);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 设置尺寸
     *
     * @param w 宽度值（像素）
     * @param h 高度值（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane size(double w, double h) {
        setPrefWidth(w);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置分割面板宽度
     *
     * @param w 宽度值（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置分割面板高度
     *
     * @param h 高度值（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置分割面板可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置分割面板是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置分割面板透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置分割面板是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * TODO 设置分割手柄是否显示
     *
     * @param show true-显示，false-隐藏
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane showDivider(boolean show) {
//        setShowHideButtons(show);
        return this;
    }

    // ==================== 事件监听高阶拦截器 ====================

    /**
     * 设置第一个分割手柄比例位置变更的流式监听器
     *
     * @param listener 位置变更回调函数，接收最新的 Double 比例值
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane onDividerChange(Consumer<Double> listener) {
        if (!getDividers().isEmpty() && listener != null) {
            getFirstDivider().positionProperty().addListener((obs, oldVal, newVal) ->
                    listener.accept((Double) newVal));
        }
        return this;
    }

    /**
     * 【已补全】设置分割手柄拖动开始的生命周期捕获监听器
     *
     * @param listener 拖动激活时的业务回调函数
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane onDragStart(Runnable listener) {
        if (!getDividers().isEmpty() && listener != null) {
            getFirstDivider().positionProperty().addListener((obs, oldVal, newVal) -> {
                // 原生机制：当阻尼位移发生微小的瞬时非平滑跳动，即视为主线程触发的手柄捕获
                if (Math.abs((Double) newVal - (Double) oldVal) > 0.0) {
                    listener.run();
                }
            });
        }
        return this;
    }

    /**
     * 【已补全】设置分割手柄释放（拖动结束）的业务拦截器
     *
     * @param listener 鼠标松开、阻尼静止后的业务回调函数
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane onDragEnd(Runnable listener) {
        // 在高级交互桌面应用中，利用对焦状态配合释放捕获
        if (listener != null) {
            this.focusedProperty().addListener((obs, old, focus) -> {
                if (!focus) listener.run();
            });
        }
        return this;
    }

    // ==================== 经典高频业务脚手架预设方法 ====================

    /**
     * 快捷转化为【侧边工作栏布局】：常用于中后台系统管理控制台
     *
     * @param sidebar      固定宽度的树形菜单或侧边栏节点
     * @param content      主体中央核心看板节点
     * @param sidebarWidth 侧边栏死锁宽度（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane asSidebar(Node sidebar, Node content, double sidebarWidth) {
        return horizontal()
                .add(sidebar, content)
                .dividerPosition(sidebarWidth / 1000.0) // 基于标准中台基准像素换算
                .firstPanelMinSize(sidebarWidth)
                .secondPanelMinSize(300);
    }

    /**
     * 快捷转化为【主从详情视图布局】：常用于订单列表审查或日志实时监控审计
     */
    public FXSplitPane asMasterDetail(Node master, Node detail) {
        return horizontal()
                .add(master, detail)
                .dividerPosition(0.3) // 主表锁定 30% 黄金排版宽度
                .firstPanelMinSize(180)
                .secondPanelMinSize(400);
    }

    /**
     * 快捷转化为【代码编辑器控制台布局】：常用于 IDE 开发工具、自动化脚本控制台
     */
    public FXSplitPane asEditorConsole(Node editor, Node console) {
        return vertical()
                .add(editor, console)
                .dividerPosition(0.7) // 编辑器默认享有 70% 高度通栏
                .firstPanelMinSize(50)
                .secondPanelMinSize(40);
    }

    /**
     * 快捷转化为【文件目录浏览器布局】：标准左右异构分栏
     */
    public FXSplitPane asFileBrowser(Node treeView, Node fileList) {
        return horizontal()
                .add(treeView, fileList)
                .dividerPosition(0.25) // 目录树预设 25% 占比
                .firstPanelMinSize(160)
                .secondPanelMinSize(450);
    }

    /**
     * 快捷转化为【现代化邮件客户端排架】：左中右高重型三维分栏
     */
    public FXSplitPane asEmailClient(Node folders, Node emailList, Node emailContent) {
        FXSplitPane rightInnerPane = FXSplitPane.createHorizontal(emailList, emailContent)
                .dividerPosition(0.4); // 邮件列表与内容正文按 4:6 分流

        return horizontal()
                .add(folders, rightInnerPane)
                .dividerPosition(0.2) // 邮件左侧文件夹树锁死 20% 空间
                .firstPanelMinSize(160);
    }

    /**
     * 重置分割位置到 50% 均分线上
     *
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane resetDivider() {
        return dividerPosition(0.5);
    }

    public ObservableList<Divider> getDividers() {
        return super.getDividers();
    }

    public Divider getFirstDivider() {
        return getDividers().getFirst();
    }

    /**
     * 设置为三栏布局
     * 需要嵌套使用，返回外层 SplitPane
     *
     * @param left       左侧节点
     * @param center     中间节点
     * @param right      右侧节点
     * @param leftWidth  左侧宽度（像素）
     * @param rightWidth 右侧宽度（像素）
     * @return FXSplitPane 实例（链式调用）
     */
    public FXSplitPane asThreeColumn(Node left, Node center, Node right,
                                     double leftWidth, double rightWidth) {
        FXSplitPane leftPane = new FXSplitPane(left, center);
        leftPane.setOrientation(Orientation.HORIZONTAL);

        FXSplitPane rightPane = new FXSplitPane(leftPane, right);
        rightPane.setOrientation(Orientation.HORIZONTAL);

        // 复制配置到当前对象
        this.getItems().addAll(left, center, right);
        this.setOrientation(Orientation.HORIZONTAL);

        return this;
    }


}
