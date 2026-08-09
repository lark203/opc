package com.atlantafx.components.layout;

import atlantafx.base.theme.Styles;
import com.atlantafx.components.base.*;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.config.ConfigStore;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 1. 统筹调度大总管：【SidebarNavContainer】
 * 继承自 VBox，拥有唯一的 .sidebar 样式类，负责派发全局的单选互斥和路由回调。
 */
public class SidebarNavContainer extends FXVBox {

    private static final Logger log = LoggerFactory.getLogger(SidebarNavContainer.class);

    private static final double MAX_WIDTH = 220;
    private static final double MIN_WIDTH = 52;

    private NavMenuItem currentActiveItem = null;
    private Consumer<String> routeHandler; // 路由跳转事件回调

    // 真正负责承载中间动态添加、可滚动的菜单项容器
    private final FXVBox navBox;

    // 控制侧边栏是否处于折叠状态的属性
    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

    public SidebarNavContainer() {
        // ==========================================================================
        // 区域 1：顶部固定区 (不随着滚动) -> 放置伸缩控制按钮
        // ==========================================================================
        FXHBox topBar = FXHBox.create().align(Pos.CENTER_LEFT).padding(6, 0, 6, 3);
        Button toggleMenuBtn = FXButton.create("").icon(MaterialDesignM.MENU).stylesClass(Styles.BUTTON_CIRCLE, Styles.FLAT).onAction(e -> collapsed.set(!collapsed.get()));
        toggleMenuBtn.setCursor(javafx.scene.Cursor.HAND);
        // 绑定点击切换折叠
        topBar.add(toggleMenuBtn);

        // ==========================================================================
        // 区域 2：中间滚动内容区 -> 用 ScrollPane 包裹真正的菜单盒
        // ==========================================================================
        navBox = FXVBox.create().align(Pos.CENTER_LEFT).spacing(8);
        FXScrollPane fxScrollPane = FXScrollPane.create(navBox).noScrollBars().vgrow().fitToWidth();

        // ==========================================================================
        // 区域 3：底部的强制占位符与固定设置菜单 (不随着滚动)
        // ==========================================================================
        // 利用一个动态延伸的 Region，把底部的设置菜单顶到 VBox 的最底端
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        FXVBox bottomBar = FXVBox.create(6);

        // 创建一个固定在底部的“设置”菜单项
        NavMenuItem settingsItem = new NavMenuItem("设置", MaterialDesignC.COG_OUTLINE.getDescription(), "settings");
        settingsItem.bindToContainer(this);
        bottomBar.add(settingsItem);

        this.spacing(4).padding(4).stylesClass("sidebar").add(topBar, fxScrollPane, bottomSpacer, bottomBar);
        this.setPrefWidth(MAX_WIDTH);

        // 监听折叠状态变化
        collapsed.addListener((obs, old, isCollapsed) -> {
//            AppState.getInstance().setCollapsed(isCollapsed);
            ConfigStore.save("collapsed", String.valueOf(isCollapsed));
            if (isCollapsed) {
                this.setPrefWidth(MIN_WIDTH);
                this.setMaxWidth(MIN_WIDTH);
                this.setMinWidth(MIN_WIDTH);
            } else {
                this.setPrefWidth(MAX_WIDTH);
                this.setMaxWidth(MAX_WIDTH);
            }
        });

        collapsed.set(AppState.getInstance().isCollapsed());
    }

    /**
     * 绑定全局路由跳转事件回调
     */
    public void setOnRouteChanged(Consumer<String> routeHandler) {
        this.routeHandler = routeHandler;
    }

    /**
     * 核心中枢：管理全局菜单互斥高亮，拦截盲目重复点击
     */
    public void requestActivation(NavMenuItem targetItem) {
        if (targetItem == currentActiveItem) {
            log.warn("⚠️ [拦截] 已处于当前视图，不重复更换页面: {}", targetItem.getViewId());
            return;
        }

        if (currentActiveItem != null) {
            currentActiveItem.setActive(false);
        }

        currentActiveItem = targetItem;
        currentActiveItem.setActive(true);

        if (routeHandler != null && targetItem.getViewId() != null) {
            routeHandler.accept(targetItem.getViewId());
        }
    }

    /**
     * 添加菜单项
     */
    public void addMenuItem(NavMenuItem item) {
        item.bindToContainer(this);
        navBox.add(item);
    }

    /**
     * 添加多级目录夹
     */
    public void addFolder(NavSubMenuFolder folder) {
        folder.bindToContainer(this);
        navBox.add(folder);
    }

    /**
     * 根据页面 ID 激活对应的菜单项（静默激活，不触发路由回调）
     * 用于非菜单点击方式的页面跳转（如页面内跳转、返回等）
     *
     * @param pageId 页面 ID
     * @return true 表示找到并激活了对应的菜单项，false 表示未找到
     */
    public boolean activateMenuItemByPageId(String pageId) {
        if (pageId == null) {
            return false;
        }

        NavMenuItem targetItem = findMenuItemByPageId(pageId, navBox);

        if (targetItem != null) {
            // 展开父级文件夹（如果有的话）
            expandParentFolder(targetItem);

            // 静默激活菜单项（只更新样式，不触发路由）
            activateSilently(targetItem);
            return true;
        }

        // 检查底部栏的菜单项（如设置）
        for (var node : getChildren()) {
            if (node instanceof FXVBox && node != navBox) {
                NavMenuItem item = findMenuItemByPageId(pageId, (FXVBox) node);
                if (item != null) {
                    activateSilently(item);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 静默激活菜单项（只更新样式状态，不触发路由回调）
     */
    private void activateSilently(NavMenuItem targetItem) {
        if (targetItem == currentActiveItem) {
            return; // 已经是激活状态，无需处理
        }

        if (currentActiveItem != null) {
            currentActiveItem.setActive(false);
        }

        currentActiveItem = targetItem;
        currentActiveItem.setActive(true);
        // 注意：这里不调用 routeHandler.accept()，避免触发额外的导航
    }

    /**
     * 递归查找菜单项
     */
    private NavMenuItem findMenuItemByPageId(String pageId, FXVBox container) {
        for (var node : container.getChildren()) {
            if (node instanceof NavMenuItem item) {
                if (pageId.equals(item.getViewId())) {
                    return item;
                }
            } else if (node instanceof NavSubMenuFolder folder) {
                NavMenuItem found = findMenuItemByPageId(pageId, folder.childrenBox);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * 展开父级文件夹
     */
    private void expandParentFolder(NavMenuItem item) {
        if (collapsed.get()) {
            return;
        }
        Node parent = item.getParent();
        while (parent != null) {
            if (parent instanceof NavSubMenuFolder folder) {
                folder.expanded.set(true);
            }
            parent = parent.getParent();
        }
    }

    /**
     * 2. 独立叶子节点组件：【NavMenuItem】
     * 对应样式类 .nav-row-box。自带小竖杠，支持原生的 :selected 伪类高亮变换。
     */
    public static class NavMenuItem extends FXHBox {
        // 声明 JavaFX 官方标准伪类，用于通知 CSS 刷新样式
        private static final PseudoClass SELECTED_PSEUDO = PseudoClass.getPseudoClass("selected");

        // 菜单项状态：是否激活
        private final BooleanProperty active = new SimpleBooleanProperty(false);
        // 菜单项名称
        private final String menuText;
        // 菜单项对应的视图名称
        private final String viewId;
        // 菜单项的父级容器
        private SidebarNavContainer parentContainer;
        private final Label label;
        private final Rectangle indicator;
        private final String iconCode;

        public NavMenuItem(String text, String iconCode, String viewId) {
            this.menuText = text;
            this.viewId = viewId;
            this.iconCode = iconCode;

            // 初始化精细小竖杠
            indicator = new Rectangle(3, 14);
            indicator.setArcWidth(2);
            indicator.setArcHeight(2);
            indicator.getStyleClass().add("nav-indicator");

            // 初始化矢量图标
            FontIcon icon = FXFontIcon.create(iconCode).stylesClass("nav-icon");

            // 初始化菜单名称
            label = FXLabel.create(text).stylesClass("nav-text");

            this.stylesClass("nav-row-box").add(indicator, icon, label);

            // 核心交互：状态改变时，物理激活 CSS 选择器的伪类高亮
            active.addListener((obs, old, isNowActive) -> this.pseudoClassStateChanged(SELECTED_PSEUDO, isNowActive));

            // 点击时向上层大总管报备申请激活
            this.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                if (parentContainer != null) {
                    parentContainer.requestActivation(this);
                }
            });
        }

        /**
         * 绑定菜单项到父级容器
         */
        protected void bindToContainer(SidebarNavContainer container) {
            this.parentContainer = container;
            // 核心绑定：当 container.collapsed 为 true 时，visible 和 managed 变为 false
            label.visibleProperty().bind(container.collapsed.not());
            label.managedProperty().bind(container.collapsed.not());

            // 🌟 3. 核心修改点：小竖杠的可见性与占位流绑定
            // 只有在【被选中(active) 并且 侧边栏未折叠(not collapsed)】时才显示小竖杠
            indicator.visibleProperty().bind(this.active.and(container.collapsed.not()));
            indicator.managedProperty().bind(this.active.and(container.collapsed.not()));
        }

        public void setActive(boolean activeState) {
            this.active.set(activeState);
        }

        public boolean isActive() {
            return active.get();
        }

        public String getViewId() {
            return viewId;
        }

        public String getMenuText() {
            return menuText;
        }

        public String getIconCode() {
            return iconCode;
        }
    }

    /**
     * 3. 递归多级目录夹组件：【NavSubMenuFolder】
     * 对应样式类 .nav-folder-row 与 .nav-children-box。支持抽屉缩进及多级级联无限嵌套。
     */
    public static class NavSubMenuFolder extends FXVBox {
        private static final PseudoClass SELECTED_PSEUDO = PseudoClass.getPseudoClass("selected");

        private final FXHBox folderHeader;
        private final FXVBox childrenBox;
        private final FXFontIcon arrow;
        private final BooleanProperty expanded = new SimpleBooleanProperty(false);
        private final BooleanBinding anyChildSelected;
        private SidebarNavContainer parentContainer;

        private final Label folderLabel;
        private final Region spacer;
        private final String folderName;
        private final String iconCode;

        private PopupMenuContainer popupMenu;

        public NavSubMenuFolder(String folderName, String iconCode) {
            this.folderName = folderName;
            this.iconCode = iconCode;

            // A. 构建文件夹的头部可点击控制栏
            // 构建文件夹图标
            FontIcon folderIcon = FXFontIcon.create(iconCode).stylesClass("folder-icon");

            // 构建文件夹名称
            folderLabel = FXLabel.create(folderName).stylesClass("folder-text");

            // 弹簧
            spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // 构建箭头图标
            arrow = FXFontIcon.create(MaterialDesignC.CHEVRON_DOWN).stylesClass("arrow-icon");

            // 构建文件夹头部
            folderHeader = FXHBox.create().stylesClass("nav-folder-row").add(folderIcon, folderLabel, spacer, arrow);

            // B. 构建存放子节点内容的抽屉大方盒
            childrenBox = FXVBox.create().stylesClass("nav-children-box").visible(false).managed(false);

            this.add(folderHeader, childrenBox);

            // C. 点击头部栏控制折叠伸缩，伴随 120ms 的丝滑箭头旋转动画
            folderHeader.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                // 只有在【大容器没有被折叠】时，点击才允许切换展开或收拢状态
                if (parentContainer != null && parentContainer.collapsed.get()) {
                    if (popupMenu == null) {
                        popupMenu = new PopupMenuContainer(this, parentContainer);
                    }
                    popupMenu.show(folderHeader);
                } else if (parentContainer != null && !parentContainer.collapsed.get()) {
                    expanded.set(!expanded.get());
                } else if (parentContainer == null) {
                    // 兜底逻辑：如果尚未绑定容器，默认允许点开
                    expanded.set(!expanded.get());
                }
            });

            expanded.addListener((obs, old, isExp) -> {
                childrenBox.visible(isExp).managed(isExp);

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(120), new KeyValue(arrow.rotateProperty(), isExp ? 180 : 0))
                );
                timeline.play();
            });

            // 【修改点 2】动态构建高级流式绑定
            // 核心原理：让 anyChildSelected 动态监视 childrenBox 的子节点列表
            // 只要子节点列表发生增减，或者任何一个子节点的 active 状态发生改变，都会自动重新计算
            anyChildSelected = Bindings.createBooleanBinding(() -> {
                for (var node : childrenBox.getChildren()) {
                    if (node instanceof NavMenuItem && ((NavMenuItem) node).isActive()) {
                        return true; // 只要有一个叶子节点被激活，整体就是 true
                    }
                    if (node instanceof NavSubMenuFolder && ((NavSubMenuFolder) node).anyChildSelectedProperty().get()) {
                        return true;
                    }
                }
                return false;
            }, childrenBox.getChildren()); // 基础依赖：子节点队列

            // D. 高阶联动选择器支持：当内部盒子有任何一个子项被选中时，让抽屉容器也向 CSS 广播选中伪类
            anyChildSelected.addListener((obs, old, isSel) -> {
                childrenBox.pseudoClassStateChanged(SELECTED_PSEUDO, isSel);
                // 如果你想让文件夹头部行（包含文字图标的那行）也亮起，可以加上下面这行：
                folderHeader.pseudoClassStateChanged(SELECTED_PSEUDO, isSel);
            });
        }

        protected void bindToContainer(SidebarNavContainer container) {
            this.parentContainer = container;

            // 反向绑定：大容器折叠(true) -> 它们隐藏(false)
            folderLabel.visibleProperty().bind(container.collapsed.not());
            folderLabel.managedProperty().bind(container.collapsed.not());

            spacer.visibleProperty().bind(container.collapsed.not());
            spacer.managedProperty().bind(container.collapsed.not());

            arrow.visibleProperty().bind(container.collapsed.not());
            arrow.managedProperty().bind(container.collapsed.not());

            // 2. 高阶联动：如果菜单栏被整体折叠了，强制收拢已经展开的二级子抽屉盒子，保证视觉整洁
            container.collapsed.addListener((obs, old, isCollapsed) -> {
                if (isCollapsed) {
                    expanded.set(false); // 强制关闭折叠
                }
            });

            for (var node : childrenBox.getChildren()) {
                if (node instanceof NavMenuItem) {
                    ((NavMenuItem) node).bindToContainer(container);
                } else if (node instanceof NavSubMenuFolder) {
                    ((NavSubMenuFolder) node).bindToContainer(container);
                }
            }
            // 【修改点 3】绑定建立后，由于子节点状态可能变化，强制刷新一次绑定通知
            anyChildSelected.invalidate();
        }

        public void addMenuItem(NavMenuItem item) {
            if (parentContainer != null) {
                item.bindToContainer(parentContainer);
            }
            childrenBox.add(item);

            // 【修改点 4】每当添加新菜单项时，让 anyChildSelected 顺便监听这个新项的激活状态
            // 这样只要这个具体的 item 被选中，上面的 createBooleanBinding 就会立刻被触发重算
            item.active.addListener((obs, old, val) -> anyChildSelected.invalidate());
            anyChildSelected.invalidate(); // 强制刷新
        }

        public void addSubFolder(NavSubMenuFolder subFolder) {
            if (parentContainer != null) {
                subFolder.bindToContainer(parentContainer);
            }
            childrenBox.add(subFolder);
            // 如果有子文件夹，同理让其监听子文件夹的选中状态
            subFolder.anyChildSelectedProperty().addListener((obs, old, val) -> anyChildSelected.invalidate());
            anyChildSelected.invalidate();
        }

        // 暴露只读属性供上层级联目录递归观察（可选）
        public BooleanBinding anyChildSelectedProperty() {
            return anyChildSelected;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getIconCode() {
            return iconCode;
        }
    }

    private static class PopupMenuContainer {

        private static final PseudoClass SELECTED_PSEUDO = PseudoClass.getPseudoClass("selected");

        private final Popup popup;
        private final FXVBox menuBox;
        private final NavSubMenuFolder ownerFolder;
        private final SidebarNavContainer parentContainer;

        public PopupMenuContainer(NavSubMenuFolder ownerFolder, SidebarNavContainer parentContainer) {
            this.ownerFolder = ownerFolder;
            this.parentContainer = parentContainer;

            popup = new Popup();
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            menuBox = FXVBox.create().padding(4).stylesClass("sidebar");

            popup.getContent().add(menuBox);

            popup.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getTarget() instanceof VBox) {
                    popup.hide();
                }
            });
        }

        public void buildMenuItems() {
            menuBox.getChildren().clear();
            buildMenuItemsRecursive(ownerFolder, 0);
        }

        private void buildMenuItemsRecursive(NavSubMenuFolder folder, int level) {
            for (Node node : folder.childrenBox.getChildren()) {
                if (node instanceof NavMenuItem item) {
                    menuBox.add(createMenuItem(item, level));
                } else if (node instanceof NavSubMenuFolder subFolder) {
                    menuBox.add(createSubFolderItem(subFolder, level));
                    buildMenuItemsRecursive(subFolder, level + 1);
                }
            }
        }

        private Node createMenuItem(NavMenuItem item, int level) {
            HBox menuItem = FXHBox.create(8);
            menuItem.getStyleClass().add("nav-row-box");

            if (level > 0) {
                Region indent = new Region();
                indent.setPrefWidth(level * 16);
                menuItem.getChildren().add(indent);
            }

            FontIcon icon = FXFontIcon.create(item.getIconCode()).stylesClass("nav-icon");

            Label label = new Label(item.getMenuText());
            label.setPadding(new Insets(0, 4, 0, 0));
            label.getStyleClass().add("nav-text");

            menuItem.getChildren().addAll(icon, label);

            if (item.isActive()) {
                menuItem.pseudoClassStateChanged(SELECTED_PSEUDO, true);
            }

            menuItem.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                parentContainer.requestActivation(item);
                popup.hide();
            });

            return menuItem;
        }

        private Node createSubFolderItem(NavSubMenuFolder folder, int level) {
            HBox folderItem = FXHBox.create(8);
            folderItem.getStyleClass().add("nav-folder-row");

            if (level > 0) {
                Region indent = new Region();
                indent.setPrefWidth(level * 16);
                folderItem.getChildren().add(indent);
            }

            FontIcon icon = FXFontIcon.create(folder.getIconCode()).stylesClass("folder-icon");

            Label label = new Label(folder.getFolderName());
            label.setPadding(new Insets(0, 4, 0, 0));
            label.getStyleClass().add("folder-text");

            FontIcon arrow = FXFontIcon.create(MaterialDesignC.CHEVRON_RIGHT).rotate(-90).stylesClass("arrow-icon");

            folderItem.getChildren().addAll(icon, label, arrow);

            return folderItem;
        }

        public void show(Node anchor) {
            buildMenuItems();

            Window window = anchor.getScene().getWindow();
            Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());

            double x = bounds.getMaxX();
            double y = bounds.getMinY();

            popup.show(window, x, y);
        }

        public void hide() {
            popup.hide();
        }
    }
}