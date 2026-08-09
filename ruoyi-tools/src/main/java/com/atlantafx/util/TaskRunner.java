package com.atlantafx.util;

import atlantafx.base.util.Animations;
import com.atlantafx.AppContext;
import com.atlantafx.components.base.FXButton;
import com.atlantafx.components.base.FXDialog;
import com.atlantafx.core.constant.NotificationLevel;
import com.atlantafx.core.event.EventBus;
import com.atlantafx.core.event.ToolbarButtonEvent;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 异步任务运行器
 * static 静态变量 ，在类加载时只创建一次，所有 TaskRunner 实例共享同一个列表
 * <p>
 * 1. 支持取消
 * 2. 支持进度更新
 * 3. 支持结果回调
 * 4. 支持异常处理
 * 5. 支持任务队列限制
 * 6. 支持任务生命周期回调
 * 7. 支持任务取消时更新 UI
 * 8. 支持按钮状态管理（防止重复点击、显示取消按钮）
 */
public class TaskRunner<T> {

    private static final Logger log = LoggerFactory.getLogger(TaskRunner.class);
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    /**
     * 全局任务列表（用于统一管理和取消）
     * 静态变量 - 类加载时创建，所有实例共享一份
     */
    private static final List<TaskRunner<?>> ALL_RUNNING_TASKS = java.util.Collections.synchronizedList(new ArrayList<>());
    private static Node cancelAllButton = null;

    /**
     * 初始化取消全部任务按钮
     */
    public static void initCancelButton() {
        cancelAllButton = FXButton.create(null).icon(MaterialDesignC.CANCEL).tooltip("取消所有任务").danger().circle().flat();

        // 默认隐藏
        cancelAllButton.setVisible(false);
        cancelAllButton.setManaged(false);

        // 点击确认后取消所有任务
        cancelAllButton.setOnMouseClicked(e -> {
            FXDialog.confirm("确认取消", "确定要取消所有正在进行的任务吗？")
                    .confirmCancel(() -> {
                        // 创建副本避免并发修改异常
                        synchronized (ALL_RUNNING_TASKS) {
                            new ArrayList<>(ALL_RUNNING_TASKS).forEach(TaskRunner::cancel);
                        }
                    }, null)
                    .show();
        });

        // 通过 EventBus 添加到工具栏
        EventBus.publish(ToolbarButtonEvent.add(cancelAllButton, -1));
    }

    /**
     * 更新取消按钮可见性
     */
    private static void updateCancelButtonVisibility() {
        runInFx(() -> {
            if (cancelAllButton != null) {
                cancelAllButton.setVisible(!ALL_RUNNING_TASKS.isEmpty());
                cancelAllButton.setManaged(!ALL_RUNNING_TASKS.isEmpty());
            }
        });
    }

    private final long taskId = ID_GENERATOR.incrementAndGet();

    public interface TaskContext {
        void update(double progress, String message);

        boolean isCancelled();
    }

    /**
     * 全局任务队列限制器
     */
    private static final Semaphore GLOBAL_LIMITER = new Semaphore(20);

    /**
     * 后台任务执行逻辑
     */
    private final BiConsumer<TaskContext, Consumer<T>> backgroundAction;
    /**
     * 任务结果缓存
     */
    private final AtomicReference<T> resultRef = new AtomicReference<>();
    /**
     * 当前任务对象
     * <p>
     * 实例变量 - 每次 new 都会创建新的
     */
    private final AtomicReference<Task<T>> activeTask = new AtomicReference<>();
    /**
     * 当前任务是否正在运行
     */
    private final AtomicBoolean isThisTaskRunning = new AtomicBoolean(false);

    /**
     * 任务结果回调
     */
    private Consumer<T> onSuccess;
    /**
     * 任务异常回调
     */
    private Consumer<Throwable> onFailure;
    /**
     * 任务取消回调
     */
    private Runnable onCancelled;
    /**
     * 任务完成回调
     */
    private Runnable onFinal;
    /**
     * 是否使用遮罩
     */
    private boolean useMask = false;
    /**
     * 初始化状态
     */
    private String initialStatus = "处理中...";

    /**
     * 最小执行时长（毫秒）
     */
    private long minimumDuration = 0; // 毫秒

    /**
     * 超时设置（秒）
     */
    private int timeoutSeconds = 0;

    /**
     * 外部传入的并发限制器（可选），用于批量操作时控制同时执行的任务数
     */
    private Semaphore concurrencyLimiter = null;

    /**
     * 防止重复点击的按钮（任务执行时禁用）
     */
    private Node disableButtonOnRun;

    /**
     * 取消按钮（任务执行时显示）
     */
    private Node cancelButton;

    /**
     * 摇摆按钮（任务执行时显示）
     */
    private Node shakeButton;

    private TaskRunner(BiConsumer<TaskContext, Consumer<T>> action) {
        this.backgroundAction = action;
    }

    /**
     * 构建任务
     */
    public static <R> TaskRunner<R> build(BiConsumer<TaskContext, Consumer<R>> action) {
        return new TaskRunner<>(action);
    }

    /**
     * 快捷构建：简单返回值任务
     */
    public static <R> TaskRunner<R> buildSimple(Supplier<R> action) {
        return new TaskRunner<>((ctx, resultSetter) -> resultSetter.accept(action.get()));
    }

    /**
     * 设置最小执行时间，防止 UI 闪烁 (建议 300-500ms)
     */
    public TaskRunner<T> withSmoothDelay(long millis) {
        this.minimumDuration = millis;
        return this;
    }

    /**
     * 设置超时保护
     */
    public TaskRunner<T> withTimeout(int seconds) {
        this.timeoutSeconds = seconds;
        return this;
    }

    /**
     * 设置并发限制器（可选），用于批量操作时控制同时执行的任务数
     * 多个 TaskRunner 共享同一个 Semaphore 即可实现并发控制
     */
    public TaskRunner<T> withConcurrencyLimiter(Semaphore limiter) {
        this.concurrencyLimiter = limiter;
        return this;
    }

    /**
     * 遮罩层
     */
    public TaskRunner<T> withMask(String status) {
        this.useMask = true;
        this.initialStatus = status;
        return this;
    }

    /**
     * 设置防止重复点击的按钮
     * 任务执行期间该按钮将被禁用，任务结束后恢复
     */
    public TaskRunner<T> disableButtonWhileRunning(Node button) {
        this.disableButtonOnRun = button;
        return this;
    }

    /**
     * 设置取消按钮
     * 任务执行期间该按钮将被启用，点击后取消当前任务
     */
    public TaskRunner<T> cancelButton(Node button) {
        this.cancelButton = button;
        return this;
    }

    /**
     * 设置任务成功回调
     */
    public TaskRunner<T> onSuccess(Consumer<T> callback) {
        this.onSuccess = callback;
        return this;
    }

    /**
     * 添加任务异常回调
     */
    public TaskRunner<T> onFailure(Consumer<Throwable> callback) {
        this.onFailure = callback;
        return this;
    }

    /**
     * 添加任务取消回调
     */
    public TaskRunner<T> onCancelled(Runnable callback) {
        this.onCancelled = callback;
        return this;
    }

    /**
     * 添加任务完成回调
     */
    public TaskRunner<T> onFinal(Runnable callback) {
        this.onFinal = callback;
        return this;
    }

    /**
     * 修复后的取消方法：同时发送状态变更和线程中断信号
     */
    public void cancel() {
        Task<T> task = activeTask.get();
        if (task != null) {
            // true 表示如果线程正在 sleep，则强制发出 InterruptedException
            task.cancel(true);
        }
    }

    /**
     * 任务完成，抖动提醒（如输入错误）
     */
    public TaskRunner<T> shake(Node btn) {
        this.shakeButton = btn;
        return this;
    }

    /**
     * 运行任务
     */
    public void run() {
        // 增加运行中的任务计数
        ALL_RUNNING_TASKS.add(this);
        updateCancelButtonVisibility();

        // 禁用防止重复点击的按钮
        if (disableButtonOnRun != null) {
            runInFx(() -> disableButtonOnRun.setDisable(true));
        }
        if (cancelButton != null) {
            cancelButton.setOnMouseClicked(e -> cancel());
        }

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                long startTime = System.currentTimeMillis();
                isThisTaskRunning.set(true);// 仅标记当前实例的状态
                if (concurrencyLimiter != null) {
                    concurrencyLimiter.acquire();
                }
                GLOBAL_LIMITER.acquire();
                final Task<T> runningTask = this;
                // 超时监控线程
                Thread timeoutThread = null;
                if (timeoutSeconds > 0) {
                    timeoutThread = Thread.startVirtualThread(() -> {
                        try {
                            Thread.sleep(timeoutSeconds * 1000L);
                            if (isThisTaskRunning.get()) {
                                cancel();
                                log.warn("任务执行超时，已自动取消");
                                AppContext.showNotification("任务执行超时，已自动取消", NotificationLevel.ERROR);
                            }
                        } catch (InterruptedException ignored) {
                        }
                    });
                }

                try {
                    TaskContext ctx = new TaskContext() {
                        @Override
                        public void update(double progress, String message) {
                            runInFx(() -> AppContext.updateMaskStatus(taskId, progress, message));
                        }

                        @Override
                        public boolean isCancelled() {
                            // 关键修复：获取当前 Task 的真实状态
                            return runningTask.isCancelled();
                        }
                    };

                    backgroundAction.accept(ctx, resultRef::set);

                    // --- 关键：平滑执行逻辑 ---
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < minimumDuration) {
                        Thread.sleep(minimumDuration - elapsed);
                    }

                    return resultRef.get();
                } finally {
                    isThisTaskRunning.set(false);
                    if (timeoutThread != null) timeoutThread.interrupt();
                    GLOBAL_LIMITER.release();
                    if (concurrencyLimiter != null) {
                        concurrencyLimiter.release();
                    }
                }
            }
        };

        activeTask.set(task);

        // 绑定生命周期回调...
        task.setOnSucceeded(e -> {
            cleanup();
            if (shakeButton != null) {
                Animations.shakeX(shakeButton).play();
            }
            if (onSuccess != null) onSuccess.accept(task.getValue());
            if (onFinal != null) onFinal.run();
        });

        task.setOnFailed(e -> {
            cleanup();
            if (onFailure != null) {
                onFailure.accept(task.getException());
            } else {
                log.error("任务执行失败: ", task.getException());
                AppContext.showNotification("错误: " + task.getException().getMessage(), NotificationLevel.ERROR);
            }
            if (onFinal != null) onFinal.run();
        });

        task.setOnCancelled(e -> {
            cleanup();
            log.warn("任务已取消: ", task.getException());
            if (onCancelled != null) onCancelled.run();
            if (onFinal != null) onFinal.run();
        });

        if (useMask) {
            AppContext.startCriticalTask(taskId, initialStatus);
        } else {
            AppContext.startLoading();
        }

        Thread.startVirtualThread(task);
    }

    /**
     * 释放资源
     */
    private void cleanup() {
        isThisTaskRunning.set(false);

        // 减少运行中的任务计数
        ALL_RUNNING_TASKS.remove(this);
        updateCancelButtonVisibility();

        // 恢复防止重复点击的按钮
        if (disableButtonOnRun != null) {
            runInFx(() -> disableButtonOnRun.setDisable(false));
        }

        if (useMask) {
            // 更新批量任务进度
            AppContext.stopCriticalTask(taskId);
        } else {
            AppContext.stopLoading();
        }
    }

    /**
     * 静态简易方法：直接运行
     */
    public static void runAsync(Runnable action) {
        Thread.startVirtualThread(action);
    }

    /**
     * 确保代码在 UI 线程执行
     */
    public static void runInFx(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}