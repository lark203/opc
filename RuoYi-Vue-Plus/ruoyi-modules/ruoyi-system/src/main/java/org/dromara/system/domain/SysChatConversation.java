package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 聊天会话表 sys_chat_conversation
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_chat_conversation")
public class SysChatConversation extends BaseEntity {

    /**
     * 会话ID
     */
    @TableId(value = "conversation_id")
    private Long conversationId;

    /**
     * 会话类型（single单聊）
     */
    private String type;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话头像
     */
    private String avatar;

    /**
     * 最后一条消息摘要
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 状态（1正常 2删除）
     */
    private String status;

}
