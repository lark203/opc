package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 聊天消息表 sys_chat_message
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_chat_message")
public class SysChatMessage extends BaseEntity {

    /**
     * 消息ID
     */
    @TableId(value = "message_id")
    private Long messageId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型（text/image/file）
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 状态（1已送达 2已读 3撤回）
     */
    private String status;

}
