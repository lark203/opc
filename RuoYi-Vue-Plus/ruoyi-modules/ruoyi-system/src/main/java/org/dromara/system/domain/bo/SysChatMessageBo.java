package org.dromara.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天消息发送业务对象
 *
 * @author Lion Li
 */
@Data
public class SysChatMessageBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接收者用户ID
     */
    @NotNull(message = "接收者不能为空")
    private Long receiverId;

    /**
     * 消息类型（text/image/file）
     */
    private String type;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

}
