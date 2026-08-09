package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.SysMessage;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息记录业务对象 sys_message
 *
 * @author Lion Li
 */
@Data
@AutoMapper(target = SysMessage.class, reverseConvertGenerate = false)
public class SysMessageBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 消息分组
     */
    private String category;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息来源
     */
    private String source;

    /**
     * 是否已读 0未读 1已读
     */
    private String readStatus;
}