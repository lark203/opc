package com.atlantafx.core.table;

import com.atlantafx.core.db.Table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 消息实体类，映射 messages 表
 */
@Table("fx_messages")
public class Message {
    private Long id;
    private String title;
    private String content;
    private String targetViewId; // 对应 NavEvent 中的 viewId，如 "SettingsView"
    private Boolean isRead; // 是否已读
    private String createdAt;

    // ========== 新增字段 ==========

    /**
     * 消息类型：NOTIFICATION（普通通知）、ALERT（警告）、ACTION（操作）、LINK（链接）、TASK（任务）
     */
    private String messageType;

    /**
     * 操作类型：NONE（无）、NAVIGATE（跳转页面）、OPEN_URL（打开网页）、SHOW_DIALOG（显示弹窗）、EXECUTE_TASK（执行任务）
     */
    private String actionType;

    /**
     * 操作参数：根据 actionType 不同，存储不同的值
     * - NAVIGATE: 目标页面 ID
     * - OPEN_URL: 目标 URL
     * - SHOW_DIALOG: 弹窗内容或对话框类型
     * - EXECUTE_TASK: 任务标识
     */
    private String actionParam;

    /**
     * 操作按钮文本（可选）
     */
    private String actionButtonText;

    public Message() {
        this.isRead = false;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ========== 便捷构造方法 ==========

    /**
     * 创建普通通知消息
     */
    public static Message notification(String title, String content) {
        Message msg = new Message();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType("NOTIFICATION");
        msg.setActionType("NONE");
        return msg;
    }

    /**
     * 创建警告消息
     */
    public static Message alert(String title, String content) {
        Message msg = new Message();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType("ALERT");
        msg.setActionType("SHOW_DIALOG");
        return msg;
    }

    /**
     * 创建带页面跳转的消息
     */
    public static Message navigate(String title, String content, String targetPageId) {
        Message msg = new Message();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType("ACTION");
        msg.setActionType("NAVIGATE");
        msg.setActionParam(targetPageId);
        msg.setActionButtonText("查看");
        return msg;
    }

    /**
     * 创建带链接的消息
     */
    public static Message link(String title, String content, String url) {
        Message msg = new Message();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType("LINK");
        msg.setActionType("OPEN_URL");
        msg.setActionParam(url);
        msg.setActionButtonText("打开链接");
        return msg;
    }

    /**
     * 创建带任务的消息
     */
    public static Message task(String title, String content, String taskId) {
        Message msg = new Message();
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMessageType("TASK");
        msg.setActionType("EXECUTE_TASK");
        msg.setActionParam(taskId);
        msg.setActionButtonText("执行");
        return msg;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTargetViewId() {
        return targetViewId;
    }

    public void setTargetViewId(String targetViewId) {
        this.targetViewId = targetViewId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionParam() {
        return actionParam;
    }

    public void setActionParam(String actionParam) {
        this.actionParam = actionParam;
    }

    public String getActionButtonText() {
        return actionButtonText;
    }

    public void setActionButtonText(String actionButtonText) {
        this.actionButtonText = actionButtonText;
    }
}
