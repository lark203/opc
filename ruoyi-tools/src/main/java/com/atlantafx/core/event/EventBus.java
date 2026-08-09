package com.atlantafx.core.event;

import com.atlantafx.util.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 增强版事件总线
 * <p>
 * 特性：
 * 1. 支持按事件类型订阅（类型安全）
 * 2. 支持同步/异步发布
 * 3. 支持订阅生命周期管理
 * 4. 异常隔离机制
 * 5. 事件分发监控
 */
public class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    /**
     * 按事件类型分组的订阅者列表
     * Key: 事件类型 Class
     * Value: 该类型事件的订阅者列表
     */
    private static final Map<Class<? extends AppEvent>, List<SubscriberWrapper<? extends AppEvent>>> SUBSCRIBERS =
            new ConcurrentHashMap<>();

    /**
     * 全局订阅者（接收所有事件）
     */
    private static final List<SubscriberWrapper<AppEvent>> GLOBAL_SUBSCRIBERS = new CopyOnWriteArrayList<>();

    /**
     * 订阅者包装类，包含订阅者信息和配置
     */
    private static class SubscriberWrapper<T extends AppEvent> {
        final Consumer<T> handler;
        final boolean async;
        final int priority;

        SubscriberWrapper(Consumer<T> handler, boolean async, int priority) {
            this.handler = handler;
            this.async = async;
            this.priority = priority;
        }
    }

    /**
     * 订阅指定类型的事件（同步模式，默认优先级）
     *
     * @param eventType 事件类型
     * @param handler   事件处理器
     * @param <T>       事件类型泛型
     */
    public static <T extends AppEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribe(eventType, handler, false, 0);
    }

    /**
     * 订阅指定类型的事件（可配置同步/异步和优先级）
     *
     * @param eventType 事件类型
     * @param handler   事件处理器
     * @param async     是否异步处理
     * @param priority  优先级（数值越大优先级越高）
     * @param <T>       事件类型泛型
     */
    public static <T extends AppEvent> void subscribe(Class<T> eventType, Consumer<T> handler, boolean async, int priority) {
        SUBSCRIBERS.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new SubscriberWrapper<>(handler, async, priority));
        log.debug("订阅事件: type={}, async={}, priority={}", eventType.getSimpleName(), async, priority);
    }

    /**
     * 订阅所有类型的事件（全局订阅者）
     *
     * @param handler 事件处理器
     */
    public static void subscribe(Consumer<AppEvent> handler) {
        subscribe(handler, false, 0);
    }

    /**
     * 订阅所有类型的事件（全局订阅者，可配置）
     *
     * @param handler  事件处理器
     * @param async    是否异步处理
     * @param priority 优先级
     */
    public static void subscribe(Consumer<AppEvent> handler, boolean async, int priority) {
        GLOBAL_SUBSCRIBERS.add(new SubscriberWrapper<>(handler, async, priority));
        log.debug("添加全局订阅者, async={}, priority={}", async, priority);
    }

    /**
     * 取消订阅指定类型的事件
     *
     * @param eventType 事件类型
     * @param handler   事件处理器
     * @param <T>       事件类型泛型
     * @return 是否取消成功
     */
    public static <T extends AppEvent> boolean unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<SubscriberWrapper<? extends AppEvent>> subscribers = SUBSCRIBERS.get(eventType);
        if (subscribers != null) {
            boolean removed = subscribers.removeIf(wrapper -> wrapper.handler == handler);
            if (removed) {
                log.debug("取消订阅事件: type={}", eventType.getSimpleName());
            }
            return removed;
        }
        return false;
    }

    /**
     * 取消全局订阅
     *
     * @param handler 事件处理器
     * @return 是否取消成功
     */
    public static boolean unsubscribe(Consumer<AppEvent> handler) {
        boolean removed = GLOBAL_SUBSCRIBERS.removeIf(wrapper -> wrapper.handler == handler);
        if (removed) {
            log.debug("取消全局订阅");
        }
        return removed;
    }

    /**
     * 发布事件（同步模式）
     *
     * @param event 事件对象
     */
    public static void publish(AppEvent event) {
        publish(event, false);
    }

    /**
     * 发布事件（可配置同步/异步）
     *
     * @param event 事件对象
     * @param async 是否异步发布
     */
    public static void publish(AppEvent event, boolean async) {
        if (event == null) {
            log.warn("尝试发布 null 事件，已忽略");
            return;
        }

        Class<? extends AppEvent> eventType = event.getClass();
        log.debug("发布事件: type={}, async={}", eventType.getSimpleName(), async);

        if (async) {
            TaskRunner.runAsync(() -> doPublish(event, eventType));
        } else {
            doPublish(event, eventType);
        }
    }

    /**
     * 执行事件发布
     */
    @SuppressWarnings("unchecked")
    private static void doPublish(AppEvent event, Class<? extends AppEvent> eventType) {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        int failedCount = 0;

        // 1. 先处理该类型事件的订阅者
        List<SubscriberWrapper<? extends AppEvent>> typeSubscribers = SUBSCRIBERS.get(eventType);
        if (typeSubscribers != null && !typeSubscribers.isEmpty()) {
            // 按优先级排序（降序）
            List<SubscriberWrapper<? extends AppEvent>> sorted = typeSubscribers.stream()
                    .sorted((a, b) -> Integer.compare(b.priority, a.priority))
                    .toList();

            for (SubscriberWrapper<? extends AppEvent> wrapper : sorted) {
                processedCount++;
                try {
                    if (wrapper.async) {
                        TaskRunner.runAsync(() -> ((Consumer<AppEvent>) wrapper.handler).accept(event));
                    } else {
                        ((Consumer<AppEvent>) wrapper.handler).accept(event);
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("事件处理失败: type={}, error={}", eventType.getSimpleName(), e.getMessage(), e);
                }
            }
        }

        // 2. 处理全局订阅者
        for (SubscriberWrapper<AppEvent> wrapper : GLOBAL_SUBSCRIBERS) {
            processedCount++;
            try {
                if (wrapper.async) {
                    TaskRunner.runAsync(() -> wrapper.handler.accept(event));
                } else {
                    wrapper.handler.accept(event);
                }
            } catch (Exception e) {
                failedCount++;
                log.error("全局订阅者处理失败: type={}, error={}", eventType.getSimpleName(), e.getMessage(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        if (duration > 100) {
            log.warn("事件分发耗时较长: type={}, duration={}ms, processed={}, failed={}",
                    eventType.getSimpleName(), duration, processedCount, failedCount);
        } else if (failedCount > 0) {
            log.info("事件分发完成: type={}, duration={}ms, processed={}, failed={}",
                    eventType.getSimpleName(), duration, processedCount, failedCount);
        }
    }

    /**
     * 获取指定类型事件的订阅者数量
     *
     * @param eventType 事件类型
     * @return 订阅者数量
     */
    public static int getSubscriberCount(Class<? extends AppEvent> eventType) {
        List<SubscriberWrapper<? extends AppEvent>> subscribers = SUBSCRIBERS.get(eventType);
        return subscribers != null ? subscribers.size() : 0;
    }

    /**
     * 获取全局订阅者数量
     *
     * @return 全局订阅者数量
     */
    public static int getGlobalSubscriberCount() {
        return GLOBAL_SUBSCRIBERS.size();
    }

    /**
     * 清空所有订阅者（主要用于测试）
     */
    public static void clear() {
        SUBSCRIBERS.clear();
        GLOBAL_SUBSCRIBERS.clear();
        log.info("已清空所有订阅者");
    }
}
