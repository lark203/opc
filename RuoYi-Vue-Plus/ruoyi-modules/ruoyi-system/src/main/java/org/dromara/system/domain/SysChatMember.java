package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 聊天会话成员表 sys_chat_member
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_chat_member")
public class SysChatMember extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶（0否 1是）
     */
    private String isTop;

    /**
     * 最后已读时间
     */
    private LocalDateTime lastReadTime;

}
