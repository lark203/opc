package org.dromara.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.push.helper.PushHelper;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.system.domain.SysChatConversation;
import org.dromara.system.domain.SysChatMember;
import org.dromara.system.domain.SysChatMessage;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.bo.SysChatMessageBo;
import org.dromara.system.domain.vo.SysChatConversationVo;
import org.dromara.system.domain.vo.SysChatMessageVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysChatConversationMapper;
import org.dromara.system.mapper.SysChatMemberMapper;
import org.dromara.system.mapper.SysChatMessageMapper;
import org.dromara.system.mapper.SysUserMapper;
import org.dromara.system.service.ISysChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 聊天服务实现
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysChatServiceImpl implements ISysChatService {

    /**
     * 会话类型：单聊
     */
    private static final String CONVERSATION_TYPE_SINGLE = "single";

    /**
     * 消息类型：文本
     */
    private static final String MESSAGE_TYPE_TEXT = "text";

    /**
     * 状态：正常/已送达
     */
    private static final String STATUS_NORMAL = "1";

    /**
     * 状态：已读
     */
    private static final String STATUS_READ = "2";

    /**
     * 状态：删除
     */
    private static final String STATUS_DELETED = "2";

    /**
     * 是否置顶：是
     */
    private static final String IS_TOP_YES = "1";

    /**
     * 最后消息摘要最大长度
     */
    private static final int LAST_MESSAGE_MAX_LENGTH = 100;

    private final SysChatConversationMapper conversationMapper;
    private final SysChatMemberMapper memberMapper;
    private final SysChatMessageMapper messageMapper;
    private final SysUserMapper userMapper;

    /**
     * 获取或创建单聊会话
     * 先查找两个用户是否已有共同会话，没有则新建
     *
     * @param userId       当前用户ID
     * @param targetUserId 对端用户ID
     * @return 会话信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysChatConversationVo getOrCreateConversation(Long userId, Long targetUserId) {
        // 查找已有会话
        Long existConvId = findExistingConversation(userId, targetUserId);
        if (existConvId != null) {
            return buildConversationVo(existConvId, userId);
        }

        // 创建新会话
        Long conversationId = IdGeneratorUtil.nextLongId();
        SysChatConversation conversation = new SysChatConversation();
        conversation.setConversationId(conversationId);
        conversation.setType(CONVERSATION_TYPE_SINGLE);
        conversation.setStatus(STATUS_NORMAL);
        conversationMapper.insert(conversation);

        // 创建双方成员记录
        createMember(conversationId, userId);
        createMember(conversationId, targetUserId);

        return buildConversationVo(conversationId, userId);
    }

    /**
     * 分页查询当前用户的会话列表
     * 按置顶、最后消息时间排序；仅对当前页所需的会话填充对端成员与用户信息，避免一次性加载全部
     *
     * @param userId    用户ID
     * @param pageQuery 分页参数
     * @return 会话分页结果
     */
    @Override
    public PageResult<SysChatConversationVo> selectConversationList(Long userId, PageQuery pageQuery) {
        // 安全上限：防止调用方不传 pageSize 时按 PageQuery 默认值拉取全量
        if (pageQuery.getPageSize() == null || pageQuery.getPageSize() > 100) {
            pageQuery.setPageSize(100);
        }
        if (pageQuery.getPageNum() == null || pageQuery.getPageNum() < 1) {
            pageQuery.setPageNum(1);
        }

        // 分页查询当前用户所属的会话：用子查询按 membership 过滤，避免先全量加载成员再 IN 大列表
        Page<SysChatConversationVo> page = conversationMapper.selectVoPage(pageQuery.build(),
            new LambdaQueryWrapper<SysChatConversation>()
                .eq(SysChatConversation::getStatus, STATUS_NORMAL)
                .apply("conversation_id IN (SELECT conversation_id FROM sys_chat_member WHERE user_id = {0})", userId)
                .orderByDesc(SysChatConversation::getLastMessageTime)
        );
        List<SysChatConversationVo> conversations = page.getRecords();
        if (CollUtil.isEmpty(conversations)) {
            return PageResult.build(Collections.emptyList(), 0);
        }

        // 仅查询当前页会话的成员记录（每会话最多一条），用于填充未读/置顶
        List<Long> convIds = conversations.stream()
            .map(SysChatConversationVo::getConversationId)
            .toList();
        List<SysChatMember> myMembers = memberMapper.lambda()
            .eq(SysChatMember::getUserId, userId)
            .in(SysChatMember::getConversationId, convIds)
            .list();
        Map<Long, SysChatMember> convIdToMyMember = myMembers.stream()
            .collect(Collectors.toMap(SysChatMember::getConversationId, m -> m, (a, b) -> a));

        // 仅批量查询当前页会话的对端成员
        List<SysChatMember> targetMembers = memberMapper.lambda()
            .in(SysChatMember::getConversationId, convIds)
            .ne(SysChatMember::getUserId, userId)
            .list();

        Map<Long, Long> convIdToTargetUserId = targetMembers.stream()
            .collect(Collectors.toMap(SysChatMember::getConversationId, SysChatMember::getUserId, (a, b) -> a));

        // 批量查询对端用户信息（头像OSS ID）
        Set<Long> targetUserIds = new HashSet<>(convIdToTargetUserId.values());
        Map<Long, SysUserVo> userMap = batchQueryUsers(targetUserIds);

        // 填充会话VO
        for (SysChatConversationVo vo : conversations) {
            SysChatMember myMember = convIdToMyMember.get(vo.getConversationId());
            if (myMember != null) {
                vo.setUnreadCount(myMember.getUnreadCount());
                vo.setIsTop(myMember.getIsTop());
            }
            Long targetUserId = convIdToTargetUserId.get(vo.getConversationId());
            if (targetUserId != null) {
                vo.setTargetUserId(targetUserId);
                SysUserVo targetUser = userMap.get(targetUserId);
                if (targetUser != null) {
                    vo.setTargetAvatar(targetUser.getAvatar());
                }
            }
        }

        // 排序：置顶优先，其次按最后消息时间倒序（仅作用于当前页）
        conversations.sort(this::compareConversation);
        return PageResult.build(conversations, page.getTotal());
    }

    /**
     * 统计当前用户所有会话的未读消息总数
     * 轻量查询（仅 SUM 未读数列），用于消息角标，避免加载会话详情
     *
     * @param userId 用户ID
     * @return 未读消息总数
     */
    @Override
    public long selectUnreadTotal(Long userId) {
        List<SysChatMember> members = memberMapper.lambda()
            .eq(SysChatMember::getUserId, userId)
            .select(SysChatMember::getUnreadCount)
            .list();
        return members.stream()
            .mapToLong(m -> m.getUnreadCount() == null ? 0 : m.getUnreadCount())
            .sum();
    }

    /**
     * 分页查询会话消息列表
     * 按创建时间倒序，返回时附带发送者头像信息
     *
     * @param conversationId 会话ID
     * @param pageQuery      分页参数
     * @param userId         当前用户ID
     * @return 消息分页结果
     */
    @Override
    public PageResult<SysChatMessageVo> selectMessageList(Long conversationId, PageQuery pageQuery, Long userId) {
        // 校验当前用户是否为会话成员
        SysChatMember member = memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .eq(SysChatMember::getUserId, userId)
            .one();
        if (member == null) {
            return PageResult.build(Collections.emptyList(), 0);
        }

        // 安全上限：防止调用方不传 pageSize 时按 PageQuery 默认值拉取全量历史
        if (pageQuery.getPageSize() == null || pageQuery.getPageSize() > 100) {
            pageQuery.setPageSize(100);
        }
        if (pageQuery.getPageNum() == null || pageQuery.getPageNum() < 1) {
            pageQuery.setPageNum(1);
        }

        // 分页查询消息（按创建时间倒序，page 1 即最新一批，便于前端仅展示最近 N 条并向上翻页加载更早消息）
        Page<SysChatMessageVo> page = messageMapper.selectVoPage(pageQuery.build(),
            new LambdaQueryWrapper<SysChatMessage>()
                .eq(SysChatMessage::getConversationId, conversationId)
                .orderByDesc(SysChatMessage::getCreateTime)
        );

        // 批量填充发送者头像OSS ID
        Set<Long> senderIds = page.getRecords().stream()
            .map(SysChatMessageVo::getSenderId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (!senderIds.isEmpty()) {
            Map<Long, SysUserVo> userMap = batchQueryUsers(senderIds);
            for (SysChatMessageVo vo : page.getRecords()) {
                SysUserVo sender = userMap.get(vo.getSenderId());
                if (sender != null) {
                    vo.setSenderAvatar(sender.getAvatar());
                }
            }
        }

        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 发送消息
     * 自动获取或创建会话，保存消息，更新会话摘要和接收方未读数
     *
     * @param bo       消息参数
     * @param senderId 发送者ID
     * @return 消息信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysChatMessageVo sendMessage(SysChatMessageBo bo, Long senderId) {
        // 获取或创建会话
        SysChatConversationVo convVo = getOrCreateConversation(senderId, bo.getReceiverId());
        Long conversationId = convVo.getConversationId();

        // 保存消息
        SysChatMessage message = new SysChatMessage();
        message.setMessageId(IdGeneratorUtil.nextLongId());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(bo.getReceiverId());
        message.setType(StringUtils.isBlank(bo.getType()) ? MESSAGE_TYPE_TEXT : bo.getType());
        message.setContent(bo.getContent());
        message.setStatus(STATUS_NORMAL);
        messageMapper.insert(message);

        // 更新会话最后消息摘要和时间
        SysChatConversation update = new SysChatConversation();
        update.setConversationId(conversationId);
        update.setLastMessage(StringUtils.substring(bo.getContent(), 0, LAST_MESSAGE_MAX_LENGTH));
        update.setLastMessageTime(LocalDateTime.now());
        conversationMapper.updateById(update);

        // 接收方未读消息数+1
        memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .eq(SysChatMember::getUserId, bo.getReceiverId())
            .setSql("unread_count = unread_count + 1")
            .update();

        // 构建返回VO
        SysChatMessageVo vo = MapstructUtils.convert(message, SysChatMessageVo.class);
        SysUserVo sender = userMapper.selectVoById(senderId);
        if (sender != null) {
            vo.setSenderAvatar(sender.getAvatar());
        }

        // 实时推送给接收方（复用全局 SSE 通道），推送失败不影响消息落库
        try {
            PushPayloadDTO pushPayload = new PushPayloadDTO();
            pushPayload.setType("chat");
            pushPayload.setSource("chat");
            pushPayload.setMessage(message.getContent());
            pushPayload.setMessageId(message.getMessageId());
            pushPayload.setTimestamp(System.currentTimeMillis());
            Map<String, Object> chatData = new HashMap<>(8);
            chatData.put("conversationId", conversationId);
            chatData.put("messageId", message.getMessageId());
            chatData.put("senderId", senderId);
            chatData.put("senderNickName", sender != null ? sender.getNickName() : null);
            chatData.put("senderAvatar", sender != null ? sender.getAvatar() : null);
            chatData.put("receiverId", bo.getReceiverId());
            chatData.put("content", message.getContent());
            chatData.put("createTime", message.getCreateTime());
            chatData.put("type", message.getType());
            pushPayload.setData(chatData);
            PushHelper.publishMessage(List.of(bo.getReceiverId()), pushPayload);
        } catch (Exception e) {
            log.warn("聊天消息实时推送失败：{}", e.getMessage());
        }

        return vo;
    }

    /**
     * 标记会话消息为已读
     * 重置当前用户未读数，并将对端发送的消息状态更新为已读
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long conversationId, Long userId) {
        // 读取当前用户未读数，用于限制后续消息 UPDATE 的行数
        SysChatMember myMember = memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .eq(SysChatMember::getUserId, userId)
            .one();
        if (myMember == null) {
            return;
        }
        int unread = myMember.getUnreadCount() == null ? 0 : myMember.getUnreadCount();

        // 重置未读数（仅更新 1 行）
        memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .eq(SysChatMember::getUserId, userId)
            .set(SysChatMember::getUnreadCount, 0)
            .set(SysChatMember::getLastReadTime, LocalDateTime.now())
            .update();

        // 仅将最近的 unread 条对端未读消息标记为已读：先按时间倒序取最近 unread 条的主键，
        // 再 UPDATE … IN (ids)。避免原来无上限地更新会话内全部消息（深历史会话下代价极高）。
        if (unread > 0) {
            List<Long> unreadIds = messageMapper.lambda()
                .eq(SysChatMessage::getConversationId, conversationId)
                .ne(SysChatMessage::getSenderId, userId)
                .eq(SysChatMessage::getStatus, STATUS_NORMAL)
                .orderByDesc(SysChatMessage::getCreateTime)
                .last("LIMIT " + unread)
                .select(SysChatMessage::getMessageId)
                .list()
                .stream()
                .map(SysChatMessage::getMessageId)
                .toList();
            if (!unreadIds.isEmpty()) {
                messageMapper.lambda()
                    .in(SysChatMessage::getMessageId, unreadIds)
                    .set(SysChatMessage::getStatus, STATUS_READ)
                    .update();
            }
        }
    }

    /**
     * 删除会话（当前用户侧）
     * 移除当前用户的成员记录，若会话无成员则标记会话为已删除
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, Long userId) {
        // 删除当前用户的成员记录
        memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .eq(SysChatMember::getUserId, userId)
            .delete();

        // 若会话已无成员，标记为已删除
        Long count = memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .count();
        if (count == null || count == 0) {
            conversationMapper.lambda()
                .eq(SysChatConversation::getConversationId, conversationId)
                .set(SysChatConversation::getStatus, STATUS_DELETED)
                .update();
        }
    }

    /**
     * 分页查询联系人列表（排除当前用户）
     * 支持按用户名或昵称关键字搜索
     *
     * @param userId    当前用户ID
     * @param keyword   搜索关键字
     * @param pageQuery 分页参数
     * @return 联系人分页结果
     */
    @Override
    public PageResult<SysUserVo> selectContactPage(Long userId, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SysUser::getUserId, userId)
            .eq(SysUser::getStatus, "0")
            .eq(SysUser::getDelFlag, "0");
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUserName, keyword)
                .or().like(SysUser::getNickName, keyword)
                .or().like(SysUser::getPhoneNumber, keyword));
        }
        wrapper.orderByAsc(SysUser::getNickName);
        Page<SysUserVo> page = userMapper.selectVoPage(pageQuery.build(), wrapper);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    // ==================== 私有方法 ====================

    /**
     * 查找两个用户之间已有的单聊会话
     *
     * @param userId       用户ID
     * @param targetUserId 对端用户ID
     * @return 会话ID，不存在返回null
     */
    /**
     * 查找两个用户之间已有的单聊会话
     * <p>
     * 优化：用一条子查询 {@code conversation_id IN (SELECT conversation_id FROM sys_chat_member WHERE user_id = ?)}
     * 替代原先“先 list 出当前用户全部会话成员、再 IN 大列表”的方式，避免把发送方全部成员行拉进内存，
     * 并借助 {@code (user_id, conversation_id)} 复合索引完成索引覆盖扫描。
     *
     * @param userId       用户ID
     * @param targetUserId 对端用户ID
     * @return 会话ID，不存在返回null
     */
    private Long findExistingConversation(Long userId, Long targetUserId) {
        SysChatMember targetMember = memberMapper.lambda()
            .eq(SysChatMember::getUserId, targetUserId)
            .apply("conversation_id IN (SELECT conversation_id FROM sys_chat_member WHERE user_id = {0})", userId)
            .one();
        return targetMember != null ? targetMember.getConversationId() : null;
    }

    /**
     * 创建会话成员记录
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    private void createMember(Long conversationId, Long userId) {
        SysChatMember member = new SysChatMember();
        member.setId(IdGeneratorUtil.nextLongId());
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setUnreadCount(0);
        member.setIsTop("0");
        memberMapper.insert(member);
    }

    /**
     * 构建会话VO（包含对端用户信息和当前用户未读数）
     *
     * @param conversationId 会话ID
     * @param userId         当前用户ID
     * @return 会话VO
     */
    private SysChatConversationVo buildConversationVo(Long conversationId, Long userId) {
        SysChatConversationVo vo = conversationMapper.selectVoById(conversationId);
        if (vo == null) {
            return null;
        }

        // 查询对端成员
        SysChatMember targetMember = memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .ne(SysChatMember::getUserId, userId)
            .one();
        if (targetMember != null) {
            vo.setTargetUserId(targetMember.getUserId());
            SysUserVo targetUser = userMapper.selectVoById(targetMember.getUserId());
            if (targetUser != null) {
                vo.setTargetAvatar(targetUser.getAvatar());
            }
        }

        // 查询当前用户成员记录
        SysChatMember myMember = memberMapper.lambda()
            .eq(SysChatMember::getConversationId, conversationId)
            .eq(SysChatMember::getUserId, userId)
            .one();
        if (myMember != null) {
            vo.setUnreadCount(myMember.getUnreadCount());
            vo.setIsTop(myMember.getIsTop());
        }
        return vo;
    }

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户ID到用户VO的映射
     */
    private Map<Long, SysUserVo> batchQueryUsers(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SysUserVo> users = userMapper.selectVoByIds(userIds);
        return users.stream()
            .collect(Collectors.toMap(SysUserVo::getUserId, u -> u, (a, b) -> a));
    }

    /**
     * 会话排序比较器：置顶优先，其次按最后消息时间倒序
     *
     * @param a 会话A
     * @param b 会话B
     * @return 比较结果
     */
    private int compareConversation(SysChatConversationVo a, SysChatConversationVo b) {
        boolean aTop = IS_TOP_YES.equals(a.getIsTop());
        boolean bTop = IS_TOP_YES.equals(b.getIsTop());
        if (aTop != bTop) {
            return aTop ? -1 : 1;
        }
        LocalDateTime aTime = a.getLastMessageTime();
        LocalDateTime bTime = b.getLastMessageTime();
        if (aTime == null && bTime == null) {
            return 0;
        }
        if (aTime == null) {
            return 1;
        }
        if (bTime == null) {
            return -1;
        }
        return bTime.compareTo(aTime);
    }

}
