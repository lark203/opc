package org.dromara.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.system.domain.SysUserPasswordHistory;
import org.dromara.system.mapper.SysUserPasswordHistoryMapper;
import org.dromara.system.service.ISysUserPasswordHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户密码历史记录 服务层实现
 *
 * @author custom
 */
@RequiredArgsConstructor
@Service
public class SysUserPasswordHistoryServiceImpl implements ISysUserPasswordHistoryService {

    /**
     * 单用户最多保留的历史条数，避免表无限增长。
     */
    private static final int MAX_KEEP = 32;

    private final SysUserPasswordHistoryMapper baseMapper;

    @Override
    public void recordHistory(Long userId, String passwordHash) {
        SysUserPasswordHistory entity = new SysUserPasswordHistory();
        entity.setUserId(userId);
        entity.setPassword(passwordHash);
        baseMapper.insert(entity);

        // 仅保留最近 MAX_KEEP 条，删除最早的超出部分
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<SysUserPasswordHistory>()
            .eq(SysUserPasswordHistory::getUserId, userId));
        if (count != null && count > MAX_KEEP) {
            List<SysUserPasswordHistory> oldest = baseMapper.selectList(new LambdaQueryWrapper<SysUserPasswordHistory>()
                .eq(SysUserPasswordHistory::getUserId, userId)
                .orderByAsc(SysUserPasswordHistory::getHistoryId)
                .last("LIMIT " + (count - MAX_KEEP)));
            if (!oldest.isEmpty()) {
                baseMapper.deleteBatchIds(oldest.stream()
                    .map(SysUserPasswordHistory::getHistoryId)
                    .collect(Collectors.toList()));
            }
        }
    }

    @Override
    public List<String> selectLastHashes(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<SysUserPasswordHistory> list = baseMapper.selectList(new LambdaQueryWrapper<SysUserPasswordHistory>()
            .eq(SysUserPasswordHistory::getUserId, userId)
            .orderByDesc(SysUserPasswordHistory::getHistoryId)
            .last("LIMIT " + limit));
        return list.stream()
            .map(SysUserPasswordHistory::getPassword)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
