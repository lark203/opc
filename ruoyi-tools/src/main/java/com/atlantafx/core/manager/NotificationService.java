package com.atlantafx.core.manager;

import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.view.MainLayout;
import com.atlantafx.util.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通知服务：负责全局消息通知的展示
 */
public final class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static MainLayout mainLayout;

    public static void init(MainLayout layout) {
        mainLayout = layout;
    }

    /**
     * 右上角消息通知
     */
    public static void showNotification(String message, NotificationLevel level) {
        TaskRunner.runInFx(() -> {
            if (mainLayout != null) mainLayout.showNotification(message, level);
        });
    }
}
