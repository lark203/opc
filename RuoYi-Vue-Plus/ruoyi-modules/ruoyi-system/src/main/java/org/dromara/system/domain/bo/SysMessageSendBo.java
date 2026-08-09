package org.dromara.system.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息发送业务对象
 *
 * @author Lion Li
 */
@Data
public class SysMessageSendBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接收用户ID集合（broadcast 为 true 时忽略）
     */
    private List<Long> userIds;

    /**
     * 是否全局广播
     */
    private Boolean broadcast;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息分组（system/notice/workflow）
     */
    private String category;

    /**
     * 内容摘要
     */
    private String message;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 前端跳转路径
     */
    private String path;

    /**
     * 消息类型（缺省按 category 推导）
     */
    private String type;

    /**
     * 消息来源（缺省按 category 推导）
     */
    private String source;
}
