package com.atlantafx.core.service;

import com.atlantafx.core.db.DB;
import com.atlantafx.core.db.DatabaseManager;
import com.atlantafx.core.table.Message;
import com.atlantafx.util.TaskRunner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    // UI 绑定的数据源
    private static final ObservableList<Message> DATA = FXCollections.observableArrayList();

    public static ObservableList<Message> getData() {
        return DATA;
    }

    /**
     * 添加消息（异步保存到数据库，并更新UI列表）
     *
     * @param title        消息标题
     * @param content      消息内容
     * @param targetViewId 目标视图ID
     */
    public static void addNavigateMessage(String title, String content, String targetViewId) {
        Message message = Message.navigate(title, content, targetViewId);

        extracted(message);
    }

    /**
     * 添加警告消息（异步保存到数据库，并更新UI列表）
     *
     * @param title   消息标题
     * @param content 消息内容
     */
    public static void addAlertMessage(String title, String content) {
        Message message = Message.alert(title, content);

        extracted(message);
    }

    /**
     * 添加链接消息（异步保存到数据库，并更新UI列表）
     *
     * @param title   消息标题
     * @param content 消息内容
     * @param url     链接地址
     */
    public static void addLinkMessage(String title, String content, String url) {
        Message message = Message.link(title, content, url);

        extracted(message);
    }

    /**
     * 添加任务消息（异步保存到数据库，并更新UI列表）
     *
     * @param title   消息标题
     * @param content 消息内容
     * @param taskId  任务ID
     */
    public static void addTaskMessage(String title, String content, String taskId) {
        Message message = Message.task(title, content, taskId);

        extracted(message);
    }

    private static void extracted(Message message) {
        DB.from(Message.class).insertAsync(message, (id) -> {
            message.setId(id);
            DATA.addFirst(message); // 添加到列表开头
            log.info("UI列表已更新，当前消息数: {}", DATA.size());
        });
    }

    /**
     * 添加通知消息（异步保存到数据库，并更新UI列表）
     *
     * @param title   消息标题
     * @param content 消息内容
     */
    public static void addNotificationMessage(String title, String content) {
        Message message = Message.notification(title, content);

        extracted(message);
    }

    /**
     * 从数据库加载所有未读消息（异步）
     */
    public static void loadMessages() {
        List<Message> messages = DB.from(Message.class).where("isRead", 0).orderBy("id", false).findAll();
        TaskRunner.runInFx(() -> {
            DATA.clear();
            if (messages != null && !messages.isEmpty()) {
                DATA.addAll(messages);
            }
            log.info("从数据库加载未读消息完成，共 {} 条", DATA.size());
        });
    }

    /**
     * 标记消息为已读（异步）
     *
     * @param message 消息ID
     */
    public static void markAsRead(Message message) {
        if (message == null) {
            return;
        }

        message.setIsRead(true);
        DB.from(Message.class).updateAsync(message, () -> {
            DATA.remove(message);
            log.info("消息已标记为已读: {}", message);
        });
    }

    /**
     * 标记所有消息为已读（异步）
     */
    public static void markAllAsRead() {
        TaskRunner.buildSimple(() -> DatabaseManager.executeWithRetry(con -> {
            String sql = "UPDATE fx_messages SET isRead = 1 WHERE isRead = 0";
            int updatedCount = con.createQuery(sql).executeUpdate().getResult();
            log.info("所有消息已标记为已读，共 {} 条", updatedCount);
            return updatedCount;
        })).onSuccess(count -> {
            // 清空UI列表
            DATA.clear();
            log.info("UI列表已清空");
        }).onFailure(e -> {
            log.error("标记所有消息已读失败: ", e);
        }).run();
    }
}