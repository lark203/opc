package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.system.domain.SysChatConversation;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话视图对象
 *
 * @author Lion Li
 */
@Data
@AutoMapper(target = SysChatConversation.class)
public class SysChatConversationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 会话类型
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
     * 对端用户ID（单聊）
     */
    private Long targetUserId;

    /**
     * 对端用户昵称
     */
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "targetUserId")
    private String targetNickName;

    /**
     * 对端用户头像OSS ID
     */
    private Long targetAvatar;

    /**
     * 对端用户头像地址
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "targetAvatar")
    private String targetAvatarUrl;

    /**
     * 最后一条消息摘要
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶
     */
    private String isTop;

}
