package com.atlantafx.core.manager;

import com.atlantafx.components.layout.SidebarNavContainer;
import com.atlantafx.core.view.MainLayout;
import com.atlantafx.core.view.ViewFactory;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导航服务：负责页面跳转
 */
public final class NavigationService {

    private static final Logger log = LoggerFactory.getLogger(NavigationService.class);
    private static MainLayout mainLayout;

    public static void init(MainLayout layout) {
        mainLayout = layout;
    }

    public static MainLayout getMainLayout() {
        return mainLayout;
    }

    /**
     * 切换到指定页面（菜单点击方式）
     */
    public static void navigateTo(String pageId) {
        navigateTo(pageId, true);
    }

    /**
     * 切换到指定页面
     *
     * @param pageId       页面 ID
     * @param fromMenuClick 是否是菜单点击（true=菜单点击，false=页面内跳转或返回）
     */
    public static void navigateTo(String pageId, boolean fromMenuClick) {
        if (mainLayout != null) {
            Node view = ViewFactory.showPage(pageId);
            mainLayout.switchPage(pageId, view);

            // 如果不是菜单点击，尝试激活对应菜单项
            if (!fromMenuClick) {
                activateMenuItem(pageId);
            }
        }
    }

    /**
     * 根据页面 ID 激活对应的菜单项
     */
    private static void activateMenuItem(String pageId) {
        if (mainLayout != null && pageId != null) {
            SidebarNavContainer sidebarMenu = mainLayout.getSidebarMenu();
            if (sidebarMenu != null) {
                boolean activated = sidebarMenu.activateMenuItemByPageId(pageId);
                if (!activated) {
                    log.debug("未找到页面 {} 对应的菜单项", pageId);
                }
            }
        }
    }
}
