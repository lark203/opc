package com.atlantafx.core.event;

import java.util.function.Consumer; /**
 * 确认对话框事件：用于弹出带回调的 Modal 窗口
 */
public record ConfirmDialogEvent(
        String title,
        String message,
        Consumer<Boolean> onResult
) implements AppEvent {}
