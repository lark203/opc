package com.atlantafx.util;

import com.atlantafx.AppContext;
import com.atlantafx.core.constant.NotificationLevel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * 剪贴板工具
 */
public final class ClipboardUtils {

    private ClipboardUtils() {
    }

    /**
     * 复制文本到剪贴板并提示
     *
     * @param text 待复制文本
     */
    public static void copy(String text) {
        copy(text, "已复制到剪贴板");
    }

    /**
     * 复制文本到剪贴板并提示
     *
     * @param text       待复制文本
     * @param successMsg 成功提示语
     */
    public static void copy(String text, String successMsg) {
        if (text == null || text.isEmpty()) {
            AppContext.showNotification("没有可复制的内容", NotificationLevel.WARNING);
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
        AppContext.showNotification(successMsg, NotificationLevel.SUCCESS);
    }
}
