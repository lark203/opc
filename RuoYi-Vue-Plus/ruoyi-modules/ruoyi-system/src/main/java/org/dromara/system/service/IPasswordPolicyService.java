package org.dromara.system.service;

import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.PasswordExpiryInfo;

import java.time.LocalDateTime;

/**
 * 密码策略服务接口
 * 从 sys_option 配置中读取密码相关策略并进行校验
 *
 * @author custom
 */
public interface IPasswordPolicyService {

    /**
     * 校验密码是否符合策略
     *
     * @param password  待校验的密码（明文）
     * @param userName  用户名（用于检查是否包含用户名）
     * @param oldPwd    旧密码的 BCrypt hash（用于检查是否与旧密码相同），可为 null
     */
    void validatePassword(String password, String userName, String oldPwd);

    /**
     * 校验密码并抛出具体错误信息
     *
     * @param password  待校验的密码（明文）
     * @param userName  用户名（用于检查是否包含用户名）
     * @param oldPwd    旧密码的 BCrypt hash（用于检查是否与旧密码相同），可为 null
     */
    void validatePasswordDetailed(String password, String userName, String oldPwd);

    /**
     * 检查密码是否已过期
     *
     * @param user 用户信息
     * @return true 表示已过期
     */
    boolean isPasswordExpired(SysUser user);

    /**
     * 获取密码过期剩余天数（仅在启用密码过期时有效）
     *
     * @param user 用户信息
     * @return 剩余天数，-1 表示未启用或已过期
     */
    long getPasswordExpireDays(SysUser user);

    /**
     * 计算密码过期状态，用于登录后前端强制改密或到期提醒。
     *
     * @param passwordUpdateTime 密码最后更新时间（为 null 表示未设置，不限制）
     * @return 过期状态（expired / expiringSoon / daysLeft）
     */
    PasswordExpiryInfo getPasswordExpiryInfo(LocalDateTime passwordUpdateTime);

    /**
     * 获取密码最小长度
     *
     * @return 密码最小长度
     */
    int getPasswordMinLength();

    /**
     * 是否要求特殊字符
     *
     * @return true 表示要求
     */
    boolean isRequireSymbols();

    /**
     * 是否允许密码包含用户名
     *
     * @return true 表示允许
     */
    boolean isAllowContainUsername();

    /**
     * 获取密码历史不可重复次数
     *
     * @return 次数（0 表示不检查）
     */
    int getPasswordRepetitionTimes();

    /**
     * 获取密码到期提醒天数
     *
     * @return 提醒天数
     */
    int getPasswordExpirationWarningDays();

    /**
     * 检查新密码是否与最近若干次使用过的密码重复（密码历史不可重复策略）。
     *
     * @param userId       用户ID（新用户为 null，表示无历史可比对）
     * @param newPassword  新密码（明文）
     */
    void checkPasswordHistory(Long userId, String newPassword);
}
