package com.atlantafx.core.manager;

import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.view.MainLayout;
import com.atlantafx.util.TaskRunner;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务状态服务：负责全局加载状态（进度条、遮罩层）的管理
 */
public final class TaskStateService {

    private static final Logger log = LoggerFactory.getLogger(TaskStateService.class);
    private static MainLayout mainLayout;

    // 线程安全的引用计数器
    private static final AtomicInteger activeTasks = new AtomicInteger(0);
    // 进度和状态属性
    private static final DoubleProperty globalProgress = new SimpleDoubleProperty(0);
    private static final StringProperty globalStatusText = new SimpleStringProperty("");
    // 追踪所有活跃的遮罩任务
    private static final ConcurrentHashMap<Long, TaskInfo> activeMaskTaskMap = new ConcurrentHashMap<>();
    private static final AtomicInteger allMaskTasks = new AtomicInteger(0);

    public static void init(MainLayout layout) {
        mainLayout = layout;
    }

    // ==================== 普通加载任务（Header 进度条） ====================

    public static void startLoading() {
        startLoading(null);
    }

    public static void startLoading(String message) {
        int count = activeTasks.incrementAndGet();
        if (count == 1) {
            TaskRunner.runInFx(() -> {
                if (mainLayout != null) mainLayout.setProgressBarVisible(true);
                if (StringUtils.isNotBlank(message)) {
                    NotificationService.showNotification(message, NotificationLevel.INFO);
                }
            });
        }
    }

    public static void stopLoading() {
        stopLoading(null);
    }

    public static void stopLoading(String message) {
        int count = activeTasks.decrementAndGet();

        if (count < 0) {
            activeTasks.set(0);
            count = 0;
        }

        if (count == 0) {
            TaskRunner.runInFx(() -> {
                if (mainLayout != null) mainLayout.setProgressBarVisible(false);
            });
        }

        if (StringUtils.isNotBlank(message)) {
            NotificationService.showNotification(message, NotificationLevel.SUCCESS);
        }
    }

    /**
     * 运行一个耗时任务，并指定开始和结束消息
     */
    public static void runTask(String startMessage, String endMessage, Runnable task) {
        startLoading(startMessage);
        try {
            task.run();
        } finally {
            stopLoading(endMessage);
        }
    }

    // ==================== 重要任务（全屏遮罩层） ====================

    /**
     * 任务信息类：记录任务的进度和状态
     */
    private static class TaskInfo {
        String status;
        double progress;
        boolean completed;

        TaskInfo(String status) {
            this.status = status;
            this.progress = 0;
            this.completed = false;
        }
    }

    /**
     * 更新全局遮罩状态（多任务兼容版）
     */
    public static void updateMaskStatus(long taskId, double progress, String status) {
        TaskRunner.runInFx(() -> {
            // 获取任务信息并更新
            TaskInfo info = activeMaskTaskMap.get(taskId);
            if (info != null) {
                info.progress = progress;
                if (status != null) {
                    info.status = status;
                }
                // 标记任务完成（进度为1.0表示完成）
                if (progress >= 1.0) {
                    info.completed = true;
                }
            }
            // 计算总体进度
            updateOverallProgress();
        });
    }

    /**
     * 计算并更新总体进度
     */
    private static void updateOverallProgress() {
        // 剩余任务数
        int remaining = activeMaskTaskMap.size();
        // 总任务数
        int totalTasks = allMaskTasks.get();

        // 否则计算所有活跃任务的平均进度
        if (totalTasks == 1) {
            // 单个任务：直接使用该任务的进度
            for (TaskInfo info : activeMaskTaskMap.values()) {
                globalProgress.set(info.progress);
                globalStatusText.set(info.status);
            }
        } else {
            // 多个任务：计算完成百分比
            long completedCount = totalTasks - remaining;

            double overallProgress = (double) completedCount / totalTasks;
            globalProgress.set(overallProgress);
            globalStatusText.set(String.format("正在处理 %d 项任务，已完成 %d 项",
                    totalTasks, completedCount));
        }
    }

    /**
     * 启动一个重要任务
     */
    public static void startCriticalTask(long taskId, String initialStatus) {
        activeMaskTaskMap.put(taskId, new TaskInfo(initialStatus));
        TaskRunner.runInFx(() -> {
            if (allMaskTasks.incrementAndGet() == 1 && mainLayout != null) {
                mainLayout.setMaskVisible(true);
            }
            updateOverallProgress();
        });
    }

    /**
     * 停止一个重要任务
     */
    public static void stopCriticalTask(long taskId) {
        activeMaskTaskMap.remove(taskId);
        TaskRunner.runInFx(() -> {
            updateOverallProgress();
            if (activeMaskTaskMap.isEmpty() && mainLayout != null) {
                mainLayout.setMaskVisible(false);
                allMaskTasks.set(0);
//                globalProgress.set(0);
//                globalStatusText.set("");
            }
        });
    }

    // ==================== 属性访问 ====================

    public static DoubleProperty globalProgressProperty() {
        return globalProgress;
    }

    public static StringProperty globalStatusTextProperty() {
        return globalStatusText;
    }
}
