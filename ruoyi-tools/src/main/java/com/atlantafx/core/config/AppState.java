package com.atlantafx.core.config;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AppState {
    private static final AppState INSTANCE = new AppState();

    private final StringProperty projectName = new SimpleStringProperty("量子科学");

    // 1. 基础个性化属性
    private final StringProperty username = new SimpleStringProperty("Gemini User");
    private final StringProperty avatarIcon = new SimpleStringProperty("ACCOUNT_CIRCLE");
    private final StringProperty themeName = new SimpleStringProperty("Primer Light");
    private final StringProperty fontSize = new SimpleStringProperty("14");

    // 2. 核心增量：新增四个全局底层技术开关属性
    private final StringProperty hardwareAcceleration = new SimpleStringProperty("false");
    private final StringProperty showDirtyOpts = new SimpleStringProperty("false");
    private final StringProperty autoReportCrash = new SimpleStringProperty("false");
    private final StringProperty preloadMessages = new SimpleStringProperty("true");

    private final StringProperty collapsed = new SimpleStringProperty("false");

    private final StringProperty monitoring = new SimpleStringProperty("false");

    // 页面默认销毁时间是30分钟
    private final StringProperty idleTime = new SimpleStringProperty("30");

    // 背景图片
    private final StringProperty backgroundImageUrl = new SimpleStringProperty("/assets/images/default_bg.png");
    // 菜单栏透明度
    private final StringProperty sideBarOpacity = new SimpleStringProperty("0.95");
    // 内容透明度
    private final StringProperty contentOpacity = new SimpleStringProperty("0.9");

    private final Map<String, StringProperty> persistedProperties = new LinkedHashMap<>();

    private AppState() {

        persistedProperties.put("projectName", projectName);

        persistedProperties.put("username", username);
        persistedProperties.put("theme", themeName);
        persistedProperties.put("fontSize", fontSize);

        // 将新属性打包进清算注册表，以享受启动自动加载和双向同步福利
        persistedProperties.put("hardwareAcceleration", hardwareAcceleration);
        persistedProperties.put("showDirtyOpts", showDirtyOpts);
        persistedProperties.put("autoReportCrash", autoReportCrash);
        persistedProperties.put("preloadMessages", preloadMessages);

        persistedProperties.put("collapsed", collapsed);
        persistedProperties.put("monitoring", monitoring);

        persistedProperties.put("idleTime", idleTime);

        persistedProperties.put("backgroundImageUrl", backgroundImageUrl);
        persistedProperties.put("sideBarOpacity", sideBarOpacity);
        persistedProperties.put("contentOpacity", contentOpacity);
    }

    public static AppState getInstance() {
        return INSTANCE;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public Map<String, StringProperty> getPersistedProperties() {
        return Collections.unmodifiableMap(persistedProperties);
    }

    // ==================== 属性访问器方法 facts ====================
    public StringProperty themeNameProperty() {
        return themeName;
    }

    public String getThemeName() {
        return themeName.get();
    }

    public void setThemeName(String themeName) {
        this.themeName.set(themeName);
    }

    public StringProperty fontSizeProperty() {
        return fontSize;
    }

    public int getFontSize() {
        try {
            return Integer.parseInt(fontSize.get());
        } catch (NumberFormatException e) {
            return 14;
        }
    }

    public void setFontSize(int size) {
        this.fontSize.set(String.valueOf(size));
    }

    public StringProperty hardwareAccelerationProperty() {
        return hardwareAcceleration;
    }

    public boolean isHardwareAcceleration() {
        return Boolean.parseBoolean(hardwareAcceleration.get());
    }

    public StringProperty showDirtyOptsProperty() {
        return showDirtyOpts;
    }

    public boolean isShowDirtyOpts() {
        return Boolean.parseBoolean(showDirtyOpts.get());
    }

    public StringProperty autoReportCrashProperty() {
        return autoReportCrash;
    }

    public boolean isAutoReportCrash() {
        return Boolean.parseBoolean(autoReportCrash.get());
    }

    public StringProperty preloadMessagesProperty() {
        return preloadMessages;
    }

    public boolean isPreloadMessages() {
        return Boolean.parseBoolean(preloadMessages.get());
    }

    public StringProperty collapsedProperty() {
        return collapsed;
    }

    public boolean isCollapsed() {
        return Boolean.parseBoolean(collapsed.get());
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed.set(String.valueOf(collapsed));
    }

    public StringProperty monitoringProperty() {
        return monitoring;
    }

    public boolean isMonitoring() {
        return Boolean.parseBoolean(monitoring.get());
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring.set(String.valueOf(monitoring));
    }

    public StringProperty idleTimeProperty() {
        return idleTime;
    }

    public int getIdleTime() {
        try {
            return Integer.parseInt(idleTime.get());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void setIdleTime(int idleTime) {
        this.idleTime.set(String.valueOf(idleTime));
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public void setProjectName(String projectName) {
        this.projectName.set(projectName);
    }

    public StringProperty backgroundImageUrlProperty() {
        return backgroundImageUrl;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl.get();
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl.set(backgroundImageUrl);
    }

    public StringProperty sideBarOpacityProperty() {
        return sideBarOpacity;
    }

    public double getSideBarOpacity() {
        try {
            return Double.parseDouble(sideBarOpacity.get());
        } catch (NumberFormatException e) {
            return 0.9;
        }
    }

    public void setSideBarOpacity(double sideBarOpacity) {
        this.sideBarOpacity.set(String.valueOf(sideBarOpacity));
    }

    public StringProperty contentOpacityProperty() {
        return contentOpacity;
    }

    public double getContentOpacity() {
        try {
            return Double.parseDouble(contentOpacity.get());
        } catch (NumberFormatException e) {
            return 0.9;
        }
    }

    public void setContentOpacity(double contentOpacity) {
        this.contentOpacity.set(String.valueOf(contentOpacity));
    }
}