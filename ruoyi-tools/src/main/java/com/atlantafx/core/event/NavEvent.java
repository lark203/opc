package com.atlantafx.core.event;

/**
 * 导航事件：用于切换主界面的视图
 */
public record NavEvent(String viewId) implements AppEvent {}
