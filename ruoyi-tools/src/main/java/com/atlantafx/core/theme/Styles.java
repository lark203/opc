package com.atlantafx.core.theme;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import javafx.application.Application;

public class Styles {

    public static void setupTheme() {
        // 设置初始主题为 Primer Light (GitHub 风格)
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    /**
     * 切换主题
     *
     * @param isDarkMode 是否为深色模式
     */
    public static void toggleDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
    }

    public static void applyTheme(Theme theme) {
        // 1. 设置基础 UserAgent 样式
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());

        // 2. 如果你有自定义的 CSS (app.css)，需要确保它在切换后依然排在最后，以保持优先级
//        if (AppContext.getScene() != null) {
//            var stylesheets = AppContext.getScene().getStylesheets();
//            String customCss = AppLauncher.class.getResource("/css/app.css").toExternalForm();
//
//            // 先移除再添加，确保 app.css 始终在列表末尾覆盖默认样式
//            stylesheets.remove(customCss);
//            stylesheets.add(customCss);
//        }
    }
}
