package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * FXDockPane - 停靠容器组件
 * 继承自 JavaFX BorderPane，实现 IFXNode 接口支持链式调用
 * 提供 IDE 风格的可停靠窗口功能，支持多个停靠区域
 * <p>
 */
public class FXDockPane extends BorderPane implements IFXNode<FXDockPane> {

    private final Map<String, FXTitledPane> dockPanes = new HashMap<>();
    private final FXToolBar topBar;
    private final FXToolBar bottomBar;
    private final FXToolBar leftBar;
    private final FXToolBar rightBar;

    /**
     * 默认构造函数
     */
    private FXDockPane() {
        super();

        // 创建四个方向的工具栏
        topBar = FXToolBar.create();
        topBar.setOrientation(Orientation.HORIZONTAL);

        bottomBar = FXToolBar.create();
        bottomBar.setOrientation(Orientation.HORIZONTAL);

        leftBar = FXToolBar.create();
        leftBar.setOrientation(Orientation.VERTICAL);

        rightBar = FXToolBar.create();
        rightBar.setOrientation(Orientation.VERTICAL);

        setPadding(new Insets(2));
    }

    /**
     * 创建空白停靠容器实例
     *
     * @return FXDockPane 实例
     */
    public static FXDockPane create() {
        return new FXDockPane();
    }

    /**
     * 添加停靠面板到指定位置
     *
     * @param id       面板唯一标识
     * @param title    面板标题
     * @param content  面板内容
     * @param position 停靠位置（TOP/BOTTOM/LEFT/RIGHT/CENTER）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addDock(String id, String title, Node content, Pos position) {
        FXTitledPane dockPane = new FXTitledPane(title, content);
        dockPane.setExpanded(true);
        dockPane.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");

        dockPanes.put(id, dockPane);

        switch (position) {
            case Pos.TOP_CENTER:
                topBar.add(dockPane);
                break;
            case Pos.BOTTOM_CENTER:
                bottomBar.add(dockPane);
                break;
            case Pos.CENTER_LEFT:
                leftBar.add(dockPane);
                break;
            case Pos.CENTER_RIGHT:
                rightBar.add(dockPane);
                break;
            case CENTER:
                setCenter(dockPane);
                break;
            default:
                setCenter(dockPane);
        }

        return this;
    }

    /**
     * 添加停靠面板到顶部区域
     *
     * @param id      面板唯一标识
     * @param title   面板标题
     * @param content 面板内容
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addTop(String id, String title, Node content) {
        return addDock(id, title, content, Pos.TOP_LEFT);
    }

    /**
     * 添加停靠面板到底部区域
     *
     * @param id      面板唯一标识
     * @param title   面板标题
     * @param content 面板内容
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addBottom(String id, String title, Node content) {
        return addDock(id, title, content, Pos.BOTTOM_LEFT);
    }

    /**
     * 添加停靠面板到左侧区域
     *
     * @param id      面板唯一标识
     * @param title   面板标题
     * @param content 面板内容
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addLeft(String id, String title, Node content) {
        return addDock(id, title, content, Pos.CENTER_LEFT);
    }

    /**
     * 添加停靠面板到右侧区域
     *
     * @param id      面板唯一标识
     * @param title   面板标题
     * @param content 面板内容
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addRight(String id, String title, Node content) {
        return addDock(id, title, content, Pos.CENTER_RIGHT);
    }

    /**
     * 添加停靠面板到中心区域
     *
     * @param id      面板唯一标识
     * @param title   面板标题
     * @param content 面板内容
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addCenter(String id, String title, Node content) {
        return addDock(id, title, content, Pos.CENTER);
    }

    /**
     * 移除指定的停靠面板
     *
     * @param id 面板 ID
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane removeDock(String id) {
        FXTitledPane pane = dockPanes.remove(id);
        if (pane != null) {
            topBar.getItems().remove(pane);
            bottomBar.getItems().remove(pane);
            leftBar.getItems().remove(pane);
            rightBar.getItems().remove(pane);
            if (getCenter() == pane) {
                setCenter(null);
            }
        }
        return this;
    }

    /**
     * 获取指定的停靠面板
     *
     * @param id 面板 ID
     * @return FXTitledPane 实例
     */
    public FXTitledPane getDock(String id) {
        return dockPanes.get(id);
    }

    /**
     * 展开指定的停靠面板
     *
     * @param id 面板 ID
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane expandDock(String id) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setExpanded(true);
        }
        return this;
    }

    /**
     * 折叠指定的停靠面板
     *
     * @param id 面板 ID
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane collapseDock(String id) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setExpanded(false);
        }
        return this;
    }

    /**
     * 切换指定停靠面板的展开/折叠状态
     *
     * @param id 面板 ID
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane toggleDock(String id) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setExpanded(!pane.isExpanded());
        }
        return this;
    }

    /**
     * 显示指定的停靠面板
     *
     * @param id 面板 ID
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane showDock(String id) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setVisible(true);
            pane.setManaged(true);
        }
        return this;
    }

    /**
     * 隐藏指定的停靠面板
     *
     * @param id 面板 ID
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane hideDock(String id) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setVisible(false);
            pane.setManaged(false);
        }
        return this;
    }

    /**
     * 设置停靠面板的宽度
     *
     * @param id    面板 ID
     * @param width 宽度值（像素）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane dockWidth(String id, double width) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setPrefWidth(width);
        }
        return this;
    }

    /**
     * 设置停靠面板的高度
     *
     * @param id     面板 ID
     * @param height 高度值（像素）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane dockHeight(String id, double height) {
        FXTitledPane pane = dockPanes.get(id);
        if (pane != null) {
            pane.setPrefHeight(height);
        }
        return this;
    }

    /**
     * 添加分隔线到指定区域的工具栏
     *
     * @param position 位置（TOP/BOTTOM/LEFT/RIGHT）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane addSeparator(Pos position) {
        Separator sep = new Separator();
        switch (position) {
            case Pos.TOP_CENTER:
                topBar.separator();
                break;
            case Pos.BOTTOM_CENTER:
                bottomBar.separator();
                break;
            case Pos.CENTER_LEFT:
                leftBar.separator();
                break;
            case Pos.CENTER_RIGHT:
                rightBar.separator();
                break;
        }
        return this;
    }

    /**
     * 获取顶部工具栏
     *
     * @return FXToolBar 实例
     */
    public FXToolBar getTopBar() {
        return topBar;
    }

    /**
     * 获取底部工具栏
     *
     * @return FXToolBar 实例
     */
    public FXToolBar getBottomBar() {
        return bottomBar;
    }

    /**
     * 获取左侧工具栏
     *
     * @return FXToolBar 实例
     */
    public FXToolBar getLeftBar() {
        return leftBar;
    }

    /**
     * 获取右侧工具栏
     *
     * @return FXToolBar 实例
     */
    public FXToolBar getRightBar() {
        return rightBar;
    }

    /**
     * 设置容器四边的内边距
     *
     * @param v 内边距值（像素）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane padding(double v) {
        setPadding(new Insets(v));
        return this;
    }

    /**
     * 设置容器各方向的内边距
     *
     * @param top    上边距（像素）
     * @param right  右边距（像素）
     * @param bottom 下边距（像素）
     * @param left   左边距（像素）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane padding(double top, double right, double bottom, double left) {
        setPadding(new Insets(top, right, bottom, left));
        return this;
    }

    /**
     * 设置容器宽度
     *
     * @param w 宽度值（像素）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置容器高度
     *
     * @param h 高度值（像素）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置容器背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane background(String color) {
        setBackground(new Background(
                new BackgroundFill(
                        Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane warning() {
        return stylesClass(Styles.WARNING);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置在 VBox 中的垂直增长优先级
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane vgrow() {
        VBox.setVgrow(this, Priority.ALWAYS);
        return this;
    }

    /**
     * 添加水平填充（HBox）
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane hgrow() {
        HBox.setHgrow(this, Priority.ALWAYS);
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置容器可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置容器是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置容器透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置容器是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为 IDE 主窗口布局
     * 顶部菜单 + 工具栏，左侧项目树，右侧属性，底部输出
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane asIDE(Node menuBar, Node toolBar, Node projectTree,
                            Node properties, Node output) {
        setTop(new VBox(menuBar, toolBar));
        setLeft(projectTree);
        setRight(properties);
        setBottom(output);

        // 设置默认尺寸
        if (projectTree instanceof Region) {
            ((Region) projectTree).setPrefWidth(250);
        }
        if (properties instanceof Region) {
            ((Region) properties).setPrefWidth(300);
        }
        if (output instanceof Region) {
            ((Region) output).setPrefHeight(200);
        }

        return background("#f5f5f5")
                .padding(2);
    }

    /**
     * 设置为代码编辑器布局
     * 左侧大纲，中间编辑区，右侧问题列表
     *
     * @param outline  大纲视图
     * @param editor   编辑器
     * @param problems 问题列表
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane asCodeEditor(Node outline, Node editor, Node problems) {
        setLeft(outline);
        setCenter(editor);
        setRight(problems);

        if (outline instanceof Region) {
            ((Region) outline).setPrefWidth(200);
        }
        if (problems instanceof Region) {
            ((Region) problems).setPrefWidth(250);
        }

        return background("#ffffff")
                .padding(0);
    }

    /**
     * 设置为调试器布局
     * 上方代码，下方控制台、变量、断点
     *
     * @param code        代码区域
     * @param console     控制台
     * @param variables   变量窗口
     * @param breakpoints 断点列表
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane asDebugger(Node code, Node console,
                                 Node variables, Node breakpoints) {
        setCenter(code);

        // 底部使用 TabPane 组织多个面板
        TabPane bottomTabs = new TabPane();
        bottomTabs.getTabs().addAll(
                createTab("控制台", console),
                createTab("变量", variables),
                createTab("断点", breakpoints)
        );

        setBottom(bottomTabs);

        if (bottomTabs instanceof Region) {
            ((Region) bottomTabs).setPrefHeight(250);
        }

        return background("#f0f0f0");
    }

    /**
     * 设置为数据库管理工具布局
     * 左侧连接树，中间数据网格，右侧表结构
     *
     * @param connectionTree 连接树
     * @param dataGrid       数据网格
     * @param tableStructure 表结构
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane asDatabaseTool(Node connectionTree, Node dataGrid,
                                     Node tableStructure) {
        setLeft(connectionTree);
        setCenter(dataGrid);
        setRight(tableStructure);

        if (connectionTree instanceof Region) {
            ((Region) connectionTree).setPrefWidth(250);
        }
        if (tableStructure instanceof Region) {
            ((Region) tableStructure).setPrefWidth(300);
        }

        return background("#ffffff");
    }

    /**
     * 设置为图像编辑器布局
     * 左侧工具箱，中间画布，右侧图层和属性
     *
     * @param toolbox    工具箱
     * @param canvas     画布
     * @param layers     图层面板
     * @param properties 属性面板
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane asImageEditor(Node toolbox, Node canvas,
                                    Node layers, Node properties) {
        setLeft(toolbox);
        setCenter(canvas);

        VBox rightPanel = new VBox(layers, properties);
        rightPanel.setSpacing(5);
        setRight(rightPanel);

        if (toolbox instanceof Region) {
            ((Region) toolbox).setPrefWidth(60);
        }
        if (rightPanel instanceof Region) {
            ((Region) rightPanel).setPrefWidth(280);
        }

        return background("#404040");
    }

    /**
     * 创建标签页
     */
    private Tab createTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tab.setClosable(false);
        return tab;
    }

    /**
     * 获取所有停靠面板的数量
     *
     * @return 面板数量
     */
    public int getDockCount() {
        return dockPanes.size();
    }

    /**
     * 检查是否包含指定的停靠面板
     *
     * @param id 面板 ID
     * @return true-包含，false-不包含
     */
    public boolean hasDock(String id) {
        return dockPanes.containsKey(id);
    }

    /**
     * 清空所有停靠面板
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane clearAllDocks() {
        dockPanes.clear();
        topBar.clear();
        bottomBar.clear();
        leftBar.clear();
        rightBar.clear();
        setCenter(null);
        return this;
    }

    /**
     * 打印所有停靠面板信息（调试用）
     *
     * @return FXDockPane 实例（链式调用）
     */
    public FXDockPane debugDocks() {
        System.out.println("=== FXDockPane Layout ===");
        System.out.println("Total docks: " + dockPanes.size());
        for (Map.Entry<String, FXTitledPane> entry : dockPanes.entrySet()) {
            FXTitledPane pane = entry.getValue();
            System.out.printf("[%s] %s (expanded: %b, visible: %b)%n",
                    entry.getKey(),
                    pane.getText(),
                    pane.isExpanded(),
                    pane.isVisible());
        }
        System.out.println("=========================");
        return this;
    }
}
