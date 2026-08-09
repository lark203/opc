package com.atlantafx.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 View 类上，用于自动注册菜单项和页面路由
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Page(
 *     id = "dashboard",           // 页面唯一标识（用于路由和缓存）
 *     name = "数据大屏",          // 菜单显示名称
 *     title = "项目数据大屏",      // 页面标题（Header显示）
 *     icon = "mdi2v-view-dashboard",
 *     order = 1,
 *     level = 1
 * )
 * public class DashboardView extends BaseView { ... }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page {
    /**
     * 页面唯一标识（用于路由、缓存Key、事件通信）
     * <p>
     * 建议使用英文或拼音，保持全局唯一
     * 例如: "dashboard", "project-list", "system-settings"
     */
    String id();

    /**
     * 菜单显示名称（中文）
     * <p>
     * 在侧边栏菜单中显示的文字
     */
    String name();

    /**
     * 页面标题（Header 显示）
     * <p>
     * 如果为空，则使用 name 作为标题
     */
    String title() default "";

    /**
     * Ikonli 图标代码
     * <p>
     * 例如: "mdi2v-view-dashboard", "mdi2f-folder"
     * 参考: https://ikonli.kordamp.org/
     */
    String icon() default "";

    /**
     * 排序权重
     * <p>
     * 数值越小越靠前，默认为 99
     */
    int order() default 99;

    /**
     * 是否为程序启动后的默认首页
     */
    boolean isDefault() default false;

    /**
     * 父级菜单名称（用于构建多级菜单）
     * <p>
     * 如果为空，表示这是一级菜单
     * 如果不为空，表示这是二级/三级菜单，值为父级菜单的 name
     */
    String parentName() default "";

    /**
     * 菜单层级
     * <p>
     * 1-3级菜单，理论上可以无限层级
     * 用于菜单排序和样式区分
     */
    int level();

    /**
     * 是否隐藏菜单项
     * <p>
     * 隐藏的页面仍然可以通过路由访问，但不会显示在侧边栏
     */
    boolean isHidden() default false;

    /**
     * 是否懒加载
     * <p>
     * true（默认）：首次访问时才创建页面实例
     * false：应用启动时立即预加载到缓存中
     * <p>
     * 建议将高频使用的核心页面设置为 false，提升用户体验
     * 低频或重量级页面保持 true，减少启动时间
     */
    boolean lazyLoad() default true;
}