package org.dromara.system.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.system.domain.bo.SysMessageBo;
import org.dromara.system.domain.vo.SysMessageBoxVo;
import org.dromara.system.domain.vo.SysMessageVo;

import java.util.List;

/**
 * 消息记录服务接口
 *
 * @author Lion Li
 */
public interface ISysMessageService {

    /**
     * 查询当前用户消息盒子数据
     * 按系统消息、通知公告、工作流消息分类返回
     *
     * @param userId 用户ID
     * @return 消息盒子数据
     */
    SysMessageBoxVo queryMessageBox(Long userId);

    /**
     * 发送指定用户文本消息
     *
     * @param userId  目标用户ID
     * @param message 文本消息内容
     */
    void sendMessage(Long userId, String message);

    /**
     * 全局广播文本消息
     *
     * @param message 文本消息内容
     */
    void sendMessage(String message);

    /**
     * 发送指定用户自定义消息体
     *
     * @param userId  目标用户ID
     * @param payload 消息推送体
     */
    void sendMessage(Long userId, PushPayloadDTO payload);

    /**
     * 全局广播自定义消息体
     *
     * @param payload 消息推送体
     */
    void sendMessage(PushPayloadDTO payload);

    /**
     * 批量发布消息给指定用户列表
     *
     * @param userIds 用户ID集合
     * @param payload 消息推送体
     */
    void publishMessage(List<Long> userIds, PushPayloadDTO payload);

    /**
     * 发布全局广播文本消息
     *
     * @param message 文本消息内容
     */
    void publishAll(String message);

    /**
     * 发布全局广播自定义消息体
     *
     * @param payload 消息推送体
     */
    void publishAll(PushPayloadDTO payload);

    /**
     * 存储全局广播消息到数据库
     *
     * @param payload 消息推送体
     * @return 回填消息ID后的消息体
     */
    PushPayloadDTO storeAll(PushPayloadDTO payload);

    /**
     * 存储指定用户消息到数据库
     *
     * @param userIds 用户ID集合
     * @param payload 消息推送体
     * @return 回填消息ID后的消息体
     */
    PushPayloadDTO storeUsers(List<Long> userIds, PushPayloadDTO payload);

    /**
     * 标记消息为已读
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     */
    void markRead(Long userId, Long messageId);

    /**
     * 批量标记消息为已读
     *
     * @param userId     用户ID
     * @param messageIds 消息ID集合
     */
    void markReadBatch(Long userId, List<Long> messageIds);

    /**
     * 标记所有消息为已读
     *
     * @param userId 用户ID
     */
    void markReadAll(Long userId);

    /**
     * 删除消息
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     */
    void deleteMessage(Long userId, Long messageId);

    /**
     * 批量删除消息
     *
     * @param userId     用户ID
     * @param messageIds 消息ID集合
     */
    void deleteMessageBatch(Long userId, List<Long> messageIds);

    /**
     * 分页查询当前用户消息列表
     *
     * @param notice    查询条件
     * @param pageQuery 分页参数
     * @param userId    用户ID
     * @return 消息分页结果
     */
    PageResult<SysMessageVo> selectPageMessageList(SysMessageBo notice, PageQuery pageQuery, Long userId);

}
