package com.atlantafx.core.service;

import com.atlantafx.AppContext;
import com.atlantafx.util.ClientIdGenerator;
import com.atlantafx.util.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 心跳服务，用于检查用户是否在登录状态。
 * <p>
 * 心跳的主要目的是：
 * <p>
 * 防止多开：服务器检查同一 ClientID 是否有多个活跃连接。
 * <p>
 * 强制下线：如果后台禁用了该账号，心跳返回指令可让客户端强制退出。
 */
public class HeartbeatService {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "HeartbeatService-Thread");
        t.setDaemon(true); // 设置为守护线程，主程序关闭时它会自动停止
        return t;
    });

    private final String clientId = ClientIdGenerator.generateId();

    public void start() {
        // 每 60 秒执行一次心跳
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 0, 60, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        try {
            log.info("发送心跳, ClientID: {}", clientId);

            // 模拟 API 请求
            // boolean isAuthorized = apiClient.checkHeartbeat(clientId, token);
            boolean isAuthorized = true; // 假设请求成功

            if (!isAuthorized) {
                stopAndLogout("您的账号已在其他地方登录，或授权已过期。");
            }

        } catch (Exception e) {
            log.error("心跳发送失败: {}", e.getMessage());
        }
    }

    private void stopAndLogout(String message) {
        stop();
        // 回到 UI 线程执行弹窗和退出
        TaskRunner.runInFx(() -> {
            // 这里弹出 Alert 或跳转回登录界面
            log.info("强制下线: {}", message);
            AppContext.exitApp();
        });
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}