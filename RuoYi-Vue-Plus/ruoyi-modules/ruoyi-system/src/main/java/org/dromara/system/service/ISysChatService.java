package org.dromara.system.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.bo.SysChatMessageBo;
import org.dromara.system.domain.vo.SysChatConversationVo;
import org.dromara.system.domain.vo.SysChatMessageVo;
import org.dromara.system.domain.vo.SysUserVo;

import java.util.List;

/**
 * 聊天服务接口
 *
 * @author Lion Li
 */
public interface ISysChatService {

    /**
     * 获取或创建单聊会话
     *
     * @param userId       当前用户ID
     * @param targetUserId 对端用户ID
     * @return 会话信息
     */
    SysChatConversationVo getOrCreateConversation(Long userId, Long targetUserId);

    /**
     * 分页查询当前用户的会话列表
     * 按置顶、最后消息时间排序，仅填充当前页所需的对端成员与用户信息
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 会话分页结果
     */
    PageResult<SysChatConversationVo> selectConversationList(Long userId, PageQuery pageQuery);

    /**
     * 统计当前用户所有会话的未读消息总数（用于消息角标，轻量查询不加载会话详情）
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    long selectUnreadTotal(Long userId);

    /**
     * 分页查询会话消息列表
     *
     * @param conversationId 会话ID
     * @param pageQuery      分页参数
     * @param userId         当前用户ID
     * @return 消息分页结果
     */
    PageResult<SysChatMessageVo> selectMessageList(Long conversationId, PageQuery pageQuery, Long userId);

    /**
     * 发送消息
     *
     * @param bo       消息参数
     * @param senderId 发送者ID
     * @return 消息信息
     */
    SysChatMessageVo sendMessage(SysChatMessageBo bo, Long senderId);

    /**
     * 标记会话消息为已读
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    void markRead(Long conversationId, Long userId);

    /**
     * 删除会话（当前用户侧删除）
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    void deleteConversation(Long conversationId, Long userId);

    /**
     * 分页查询联系人列表（排除当前用户）
     *
     * @param userId    当前用户ID
     * @param keyword   搜索关键字（用户名/昵称/手机号）
     * @param pageQuery 分页参数
     * @return 联系人分页结果
     */
    PageResult<SysUserVo> selectContactPage(Long userId, String keyword, PageQuery pageQuery);

}
