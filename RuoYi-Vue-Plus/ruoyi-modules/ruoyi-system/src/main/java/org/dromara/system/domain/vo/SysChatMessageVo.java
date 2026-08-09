package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.translation.annotation.Translation;
import org.dromara.common.translation.constant.TransConstant;
import org.dromara.system.domain.SysChatMessage;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息视图对象
 *
 * @author Lion Li
 */
@Data
@AutoMapper(target = SysChatMessage.class)
public class SysChatMessageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
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
     * 发送者昵称
     */
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "senderId")
    private String senderNickName;

    /**
     * 发送者头像OSS ID
     */
    private Long senderAvatar;

    /**
     * 发送者头像地址
     */
    @Translation(type = TransConstant.OSS_ID_TO_URL, mapper = "senderAvatar")
    private String senderAvatarUrl;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息类型
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
