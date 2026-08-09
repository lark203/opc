package org.dromara.system.service;

import java.util.List;

/**
 * 用户密码历史记录 服务层
 *
 * @author custom
 */
public interface ISysUserPasswordHistoryService {

    /**
     * 记录一条密码历史（写入后仅保留最近 MAX_KEEP 条，避免无限增长）。
     *
     * @param userId       用户ID
     * @param passwordHash BCrypt 哈希后的密码
     */
    void recordHistory(Long userId, String passwordHash);

    /**
     * 查询用户最近的若干条历史密码哈希（按时间倒序）。
     *
     * @param userId 用户ID
     * @param limit  返回条数
     * @return 密码哈希列表
     */
    List<String> selectLastHashes(Long userId, int limit);
}
