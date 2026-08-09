package com.atlantafx.util;

import org.kordamp.ikonli.DefaultIkonResolver;
import org.kordamp.ikonli.IkonResolver;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.JavaFXFontLoader;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IconHelper {
    private static final Logger log = LoggerFactory.getLogger(IconHelper.class);

    private static final IkonResolver RESOLVER = DefaultIkonResolver.getInstance(JavaFXFontLoader.getInstance());

    /**
     * 根据图标代码（如 "mzl-home"）创建 FontIcon
     */
    public static FontIcon createIcon(String iconCode) {
        if (iconCode == null || iconCode.isEmpty()) {
            return new FontIcon(MaterialDesignH.HELP_CIRCLE);
        }

        try {
            // 直接使用预取的解析器进行解析
            return new FontIcon(RESOLVER.resolve(iconCode).resolve(iconCode));
        } catch (Exception e) {
            log.error("Ikonli 解析图标失败: [{}], 错误: {}", iconCode, e.getMessage());
            return new FontIcon(MaterialDesignA.ALERT_CIRCLE);
        }
    }
}