package com.atlantafx.core.error;

import com.atlantafx.AppContext;
import com.atlantafx.core.constant.NotificationLevel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局异常处理器：捕获并记录所有线程中未被处理的异常
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 1. 记录日志（包含线程名和详细堆栈）
        log.error("在线程 [{}] 中捕获到未处理的异常:", t.getName(), e);

        // 2. 针对 UI 线程的特殊处理
        if (Platform.isFxApplicationThread()) {
            handleGuiException(e);
        } else {
            log.warn("非 UI 线程发生异常，已静默记录到日志。");
        }
    }

    private void handleGuiException(Throwable e) {
        // 在 UI 线程弹出友好提示，防止程序直接假死
        try {
            AppContext.showNotification(
                    "程序运行出现异常: " + (e.getMessage() != null ? e.getMessage() : "未知错误"),
                    NotificationLevel.ERROR
            );
        } catch (Exception ex) {
            // 如果连 AppContext 都出问题了，则直接输出到控制台
            System.err.println("紧急情况：无法显示 UI 异常通知");
        }
    }

    /**
     * 静态注册方法
     */
    public static void register() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        // 设置所有线程默认的处理器
        Thread.setDefaultUncaughtExceptionHandler(handler);
    }
}