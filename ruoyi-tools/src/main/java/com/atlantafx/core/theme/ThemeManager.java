package com.atlantafx.core.theme;

import atlantafx.base.theme.*;
import com.atlantafx.core.config.AppState;
import com.atlantafx.core.config.ConfigStore;

import java.util.List;
import java.util.Objects;

/**
 * 主题管理：可用主题列表、当前主题获取/切换
 * <p>
 * 主题偏好通过 AppState（自动同步到 ConfigStore）持久化
 */
public final class ThemeManager {
    private static final String DEFAULT_THEME = "Primer Light";

    private static final List<Theme> THEME_LIST = List.of(
            new PrimerLight(), new PrimerDark(),
            new NordLight(), new NordDark(),
            new CupertinoLight(), new CupertinoDark(),
            new Dracula()
    );

    private ThemeManager() {
    }

    /**
     * 所有可用主题
     */
    public static List<Theme> getThemeList() {
        return THEME_LIST;
    }

    /** 当前主题名（从 AppState 读取），若无则返回默认值 */
    public static String getThemeName() {
        String name = AppState.getInstance().getThemeName();
        return name != null ? name : DEFAULT_THEME;
    }

    /** 当前主题名解析为 Theme 对象 */
    public static Theme getTheme() {
        String name = getThemeName();
        return THEME_LIST.stream()
                .filter(t -> Objects.equals(t.getName(), name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 切换主题：更新 AppState + 持久化到磁盘
     * <p>
     * 调用方仍需负责调用 Styles.applyTheme() 更新 UI
     */
    public static void setTheme(String themeName) {
        AppState.getInstance().setThemeName(themeName);
        syncAppStateToStore();
        ConfigStore.save();
    }

    /**
     * 将 AppState 持久化属性同步到 ConfigStore
     */
    private static void syncAppStateToStore() {
        for (var entry : AppState.getInstance().getPersistedProperties().entrySet()) {
            String value = entry.getValue().get();
            if (value != null) {
                ConfigStore.set(entry.getKey(), value);
            }
        }
    }
}
