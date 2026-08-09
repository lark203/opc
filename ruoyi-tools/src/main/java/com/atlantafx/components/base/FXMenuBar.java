package com.atlantafx.components.base;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * FXMenuBar - 菜单栏组件
 * 继承自 JavaFX MenuBar，实现 IFXNode 接口支持链式调用
 * 提供便捷的菜单项管理、快捷键和样式设置方法
 */
public class FXMenuBar extends MenuBar implements IFXNode<FXMenuBar> {

    /**
     * 默认构造函数
     */
    private FXMenuBar() {
        super();
    }

    /**
     * 创建带菜单的菜单栏
     *
     * @param menus 菜单数组
     */
    private FXMenuBar(Menu... menus) {
        super(menus);
    }

    /**
     * 创建空白菜单栏实例
     *
     * @return FXMenuBar 实例
     */
    public static FXMenuBar create() {
        return new FXMenuBar();
    }

    /**
     * 创建带菜单的菜单栏实例
     *
     * @param menus 菜单数组
     * @return FXMenuBar 实例
     */
    public static FXMenuBar create(Menu... menus) {
        return new FXMenuBar(menus);
    }

    /**
     * 添加菜单到菜单栏
     *
     * @param menus 菜单数组
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar add(Menu... menus) {
        getMenus().addAll(menus);
        return this;
    }

    /**
     * 核心：通过 Consumer 直接暴露 FXMenu 本身，取消 Builder 中转
     *
     * @param title      顶部一级菜单名称
     * @param menuConfig 菜单内部项流式配置闭包
     */
    public FXMenuBar addMenu(String title, Consumer<FXMenu> menuConfig) {
        FXMenu menu = FXMenu.create(title);
        if (menuConfig != null) {
            menuConfig.accept(menu);
        }
        getMenus().add(menu);
        return this;
    }

    /**
     * 核心：直接平铺喂入已经装配就绪的 FXMenu 实例
     */
    public FXMenuBar addMenus(Menu... menus) {
        if (menus != null) {
            getMenus().addAll(menus);
        }
        return this;
    }

    /**
     * 移除指定的菜单
     *
     * @param menus 菜单数组
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar remove(Menu... menus) {
        getMenus().removeAll(menus);
        return this;
    }

    /**
     * 清空所有菜单
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar clear() {
        getMenus().clear();
        return this;
    }

    // ==================== AtlantaFX 状态样式 ====================

    /**
     * 应用成功样式（Success）
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar success() {
        return stylesClass(Styles.SUCCESS);
    }

    /**
     * 应用危险样式（Danger）
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar danger() {
        return stylesClass(Styles.DANGER);
    }

    /**
     * 应用强调色样式（Accent）
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar accent() {
        return stylesClass(Styles.ACCENT);
    }

    /**
     * 应用警告样式（Warning）
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar warning() {
        return stylesClass(Styles.WARNING);
    }

    // ==================== 布局增强 ====================

    /**
     * 设置菜单栏宽度
     *
     * @param w 宽度值（像素）
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar width(double w) {
        setMinWidth(w);
        setPrefWidth(w);
        return this;
    }

    /**
     * 设置菜单栏高度
     *
     * @param h 高度值（像素）
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar height(double h) {
        setMinHeight(h);
        setPrefHeight(h);
        return this;
    }

    /**
     * 设置菜单栏背景颜色
     *
     * @param color CSS 格式的颜色字符串
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar background(String color) {
        setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                        javafx.scene.paint.Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        javafx.scene.layout.CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));
        return this;
    }

    /**
     * 设置菜单栏边框
     *
     * @param width 边框宽度（像素）
     * @param color CSS 格式的颜色字符串
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar border(double width, String color) {
        setBorder(new javafx.scene.layout.Border(
                new javafx.scene.layout.BorderStroke(
                        javafx.scene.paint.Color.valueOf(color.startsWith("#") ?
                                (color.length() == 7 ? color + "FF" : color) : color),
                        javafx.scene.layout.BorderStrokeStyle.SOLID,
                        javafx.scene.layout.CornerRadii.EMPTY,
                        new javafx.scene.layout.BorderWidths(width)
                )
        ));
        return this;
    }

    // ==================== 状态控制 ====================

    /**
     * 设置菜单栏可见性
     *
     * @param visible true-可见，false-隐藏
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar visible(boolean visible) {
        setVisible(visible);
        return this;
    }

    /**
     * 设置菜单栏是否受布局管理
     *
     * @param managed true-受管理，false-不受管理
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar managed(boolean managed) {
        setManaged(managed);
        return this;
    }

    /**
     * 设置菜单栏透明度
     *
     * @param opacity 透明度值（0.0-1.0）
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar opacity(double opacity) {
        setOpacity(opacity);
        return this;
    }

    /**
     * 设置菜单栏是否禁用
     *
     * @param disabled true-禁用，false-启用
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar disabled(boolean disabled) {
        setDisable(disabled);
        return this;
    }

    /**
     * 设置菜单栏是否使用系统原生样式
     *
     * @param useNative true-使用原生样式，false-自定义样式
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar useNative(boolean useNative) {
        setUseSystemMenuBar(useNative);
        return this;
    }

    // ==================== 预设用途快捷方法 ====================

    /**
     * 设置为主窗口菜单栏
     * 标准样式，白色背景
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public double asMainMenuBar() {
        return background("#ffffff")
                .border(0, "#e0e0e0")
                .prefHeight(30);
    }

    /**
     * 设置为深色主题菜单栏
     * 深色背景，浅色文字
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar darkTheme() {
        return background("#2d2d2d")
                .border(0, "#404040")
                .stylesClass("dark-menu");
    }

    /**
     * 设置为紧凑菜单栏
     * 更小的高度
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public double compact() {
        return prefHeight(25);
    }

    /**
     * 获取所有菜单
     *
     * @return Menu 数组
     */
    public Menu[] getMenusArray() {
        return getMenus().toArray(new Menu[0]);
    }

    /**
     * 获取菜单数量
     *
     * @return 菜单数量
     */
    public int size() {
        return getMenus().size();
    }

    /**
     * 检查菜单栏是否为空
     *
     * @return true-为空，false-不为空
     */
    public boolean isEmpty() {
        return getMenus().isEmpty();
    }

    /**
     * 快速构建常用菜单（文件、编辑、视图、帮助）
     *
     * @return FXMenuBar 实例（链式调用）
     */
    public FXMenuBar withStandardMenus(Consumer<String> onFileNew,
                                       Consumer<String> onFileOpen,
                                       Consumer<String> onFileSave,
                                       EventHandler<ActionEvent> onExit) {
        // 文件菜单
        addMenu("文件 (_F)", builder -> builder
                .add("新建 (_N)", "Ctrl+N", onFileNew != null ? e -> onFileNew.accept("new") : null)
                .add("打开 (_O)", "Ctrl+O", onFileOpen != null ? e -> onFileOpen.accept("open") : null)
                .add("保存 (_S)", "Ctrl+S", onFileSave != null ? e -> onFileSave.accept("save") : null)
                .separator()
                .add("退出 (_X)", onExit)
        );

        // 编辑菜单
        addMenu("编辑 (_E)", builder -> builder
                .add("撤销 (_U)", "Ctrl+Z", null)
                .add("重做 (_R)", "Ctrl+Y", null)
                .separator()
                .add("剪切 (_T)", "Ctrl+X", null)
                .add("复制 (_C)", "Ctrl+C", null)
                .add("粘贴 (_P)", "Ctrl+V", null)
        );

        // 视图菜单
        addMenu("视图 (_V)", builder -> builder
                .add("放大", "Ctrl++", null)
                .add("缩小", "Ctrl+-", null)
                .add("实际大小", "Ctrl+0", null)
                .separator()
                .checkItem("全屏模式", null)
        );

        // 帮助菜单
        addMenu("帮助 (_H)", builder -> builder
                .add("查看帮助", "F1", null)
                .add("关于", e -> System.out.println("关于对话框"))
        );

        return this;
    }

    /**
     * 核心：内置开箱即用的标准桌面应用操作系统级基础菜单骨架
     *
     * @param onFileNew  文件新建事件回调
     * @param onFileOpen 文件打开事件回调
     * @param onFileSave 文件保存事件回调
     * @param onExit     应用安全退出事件处理器
     * @return FXMenuBar 实例（链式调用契约）
     */
    public FXMenuBar useSystemDefaultShortcuts(Consumer<String> onFileNew,
                                               Consumer<String> onFileOpen,
                                               Consumer<String> onFileSave,
                                               EventHandler<ActionEvent> onExit) {
        // 1. 注入标准 [文件] 菜单
        addMenu("文件 (_F)", menu -> menu
                .add("新建 (_N)", "Ctrl+N", onFileNew != null ? e -> onFileNew.accept("new") : null)
                .add("打开 (_O)", "Ctrl+O", onFileOpen != null ? e -> onFileOpen.accept("open") : null)
                .add("保存 (_S)", "Ctrl+S", onFileSave != null ? e -> onFileSave.accept("save") : null)
                .separator()
                .add("退出 (_X)", onExit)
        );

        // 2. 注入标准 [编辑] 菜单
        addMenu("编辑 (_E)", menu -> menu
                .add("撤销 (_U)", "Ctrl+Z", null)
                .add("重做 (_R)", "Ctrl+Y", null)
                .separator()
                .add("剪切 (_T)", "Ctrl+X", null)
                .add("复制 (_C)", "Ctrl+C", null)
                .add("粘贴 (_P)", "Ctrl+V", null)
        );

        return this;
    }

    /**
     * 激活 AtlantaFX 扁平化嵌入式专有样式类
     *
     * @return FXMenuBar 实例（链式调用契约）
     */
    public FXMenuBar useFlatEmbeddedStyle() {
        getStyleClass().add(Styles.FLAT);
        return this;
    }

    /**
     * 水平方向百分之百撑满父容器
     *
     * @return FXMenuBar 实例（链式调用契约）
     */
    public FXMenuBar useFillWidthLayout() {
        VBox.setVgrow(this, Priority.NEVER);
        setMaxWidth(Double.MAX_VALUE);
        return this;
    }
}
