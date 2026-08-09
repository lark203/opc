package com.atlantafx.core.event;

import com.atlantafx.core.constant.NotificationLevel;

/**
 * 通知事件载体 - 封装通知所需的信息
 */
public record NotificationEvent(String message, NotificationLevel level) implements AppEvent {
}