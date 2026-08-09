package com.atlantafx.components.theme;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import com.atlantafx.components.base.FXLabel;
import com.atlantafx.components.base.FXTilePane;
import com.atlantafx.components.base.FXToggleGroup;
import com.atlantafx.components.base.FXVBox;
import com.atlantafx.core.theme.ThemeManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.util.Objects;

/**
 * 创建一个主题选择弹窗
 */
public class ThemeSelectorPopup extends FXVBox {

    private final FXTilePane thumbnailsPane = FXTilePane.create(20, 20);
    private final FXToggleGroup thumbnailsGroup = FXToggleGroup.create();

    public ThemeSelectorPopup(Runnable onThemeChanged) {
        this.spacing(15).padding(20).stylesClass("theme-selector-popup");

        // 标题
        Label title = FXLabel.create("选择主题").stylesClass(Styles.TITLE_3);

        // 配置 TilePane (参考 ThemeDialog)
        thumbnailsPane.align(Pos.TOP_CENTER).prefColumns(3).styleCss("-color-thumbnail-border:-color-border-subtle;");

        // 初始化缩略图
        updateThumbnails();

        // 【核心优化】监听选中变化：实时移除旧标记，添加新标记
        thumbnailsGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val != null && val.getUserData() instanceof Theme theme) {
                // 1. 应用主题
                com.atlantafx.core.theme.Styles.applyTheme(theme);
                // 2. 持久化配置
                ThemeManager.setTheme(theme.getName());

                if (onThemeChanged != null) onThemeChanged.run();
            }
        });

        // 使用 ScrollPane 包裹以防主题过多
        var root = FXVBox.create().add(thumbnailsPane).padding(20);

        getChildren().addAll(title, root);
    }

    private void updateThumbnails() {
        thumbnailsPane.clear();
        String currentThemeName = ThemeManager.getThemeName();

        for (Theme theme : ThemeManager.getThemeList()) {
            // 使用你提供的 ThemeThumbnail 类
            ThemeThumbnail thumbnail = new ThemeThumbnail(theme);
            thumbnail.setToggleGroup(thumbnailsGroup);
            thumbnail.setUserData(theme);

            // 设置初始选中状态
            if (Objects.equals(currentThemeName, theme.getName())) {
                thumbnail.setSelected(true);
            }

            thumbnailsPane.add(thumbnail);
        }
    }
}