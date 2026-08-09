package org.dromara.system.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.PasswordExpiryInfo;
import org.dromara.system.service.ISysOptionService;
import org.dromara.system.service.ISysUserPasswordHistoryService;
import org.dromara.system.service.IPasswordPolicyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 密码策略服务实现
 * 从 sys_option 配置中读取密码相关策略并进行校验
 *
 * @author custom
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordPolicyServiceImpl implements IPasswordPolicyService {

    private final ISysOptionService optionService;
    private final ISysUserPasswordHistoryService passwordHistoryService;

    private static final String CATEGORY = "PASSWORD";

    @Override
    public void validatePassword(String password, String userName, String oldPwd) {
        validatePasswordDetailed(password, userName, oldPwd);
    }

    @Override
    public void validatePasswordDetailed(String password, String userName, String oldPwd) {
        if (password == null || password.isEmpty()) {
            throw new ServiceException("密码不能为空");
        }

        // 1. 检查最小长度
        int minLength = getPasswordMinLength();
        if (password.length() < minLength) {
            throw new ServiceException("密码长度不能少于 " + minLength + " 位");
        }

        // 2. 检查是否与旧密码相同
        if (oldPwd != null && BCrypt.checkpw(password, oldPwd)) {
            throw new ServiceException("新密码不能与旧密码相同");
        }

        // 3. 检查是否包含用户名
        if (!isAllowContainUsername() && userName != null && !userName.isEmpty()) {
            if (password.toLowerCase().contains(userName.toLowerCase())) {
                throw new ServiceException("密码不能包含用户名");
            }
        }

        // 4. 检查是否要求特殊字符
        if (isRequireSymbols()) {
            checkSymbol(password);
        }
    }

    /**
     * 检查密码中是否包含特殊字符（大小写字母、数字、特殊符号中至少满足配置要求）
     */
    private void checkSymbol(String password) {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        int categories = 0;
        if (hasUpper) categories++;
        if (hasLower) categories++;
        if (hasDigit) categories++;
        if (hasSpecial) categories++;

        if (categories < 3) {
            throw new ServiceException("密码必须包含大写字母、小写字母、数字、特殊字符中的至少 3 种类型");
        }
    }

    @Override
    public boolean isPasswordExpired(SysUser user) {
        if (user == null) {
            return false;
        }
        return getPasswordExpiryInfo(user.getPasswordUpdateTime()).expired();
    }

    @Override
    public long getPasswordExpireDays(SysUser user) {
        if (user == null) {
            return -1;
        }
        return getPasswordExpiryInfo(user.getPasswordUpdateTime()).daysLeft();
    }

    @Override
    public PasswordExpiryInfo getPasswordExpiryInfo(LocalDateTime passwordUpdateTime) {
        int expirationDays = getPasswordExpirationDays();
        if (passwordUpdateTime == null || expirationDays <= 0) {
            return new PasswordExpiryInfo(false, false, -1);
        }
        LocalDateTime expireTime = passwordUpdateTime.plusDays(expirationDays);
        long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), expireTime);
        boolean expired = daysLeft <= 0;
        int warningDays = getPasswordExpirationWarningDays();
        boolean expiringSoon = !expired && warningDays > 0 && daysLeft <= warningDays;
        return new PasswordExpiryInfo(expired, expiringSoon, daysLeft);
    }

    @Override
    public int getPasswordMinLength() {
        Integer val = optionService.getOptionInt(CATEGORY, "PASSWORD_MIN_LENGTH", 8);
        return val != null && val >= 6 ? val : 8;
    }

    @Override
    public boolean isRequireSymbols() {
        String val = optionService.getOptionValue(CATEGORY, "PASSWORD_REQUIRE_SYMBOLS");
        return "1".equals(val);
    }

    @Override
    public boolean isAllowContainUsername() {
        String val = optionService.getOptionValue(CATEGORY, "PASSWORD_ALLOW_CONTAIN_USERNAME");
        return "1".equals(val);
    }

    @Override
    public int getPasswordRepetitionTimes() {
        Integer val = optionService.getOptionInt(CATEGORY, "PASSWORD_REPETITION_TIMES", 0);
        return val != null && val >= 0 ? val : 0;
    }

    /**
     * 获取密码有效期天数
     */
    public int getPasswordExpirationDays() {
        Integer val = optionService.getOptionInt(CATEGORY, "PASSWORD_EXPIRATION_DAYS", 0);
        return val != null && val >= 0 ? val : 0;
    }

    /**
     * 获取密码到期提醒天数
     */
    public int getPasswordExpirationWarningDays() {
        Integer val = optionService.getOptionInt(CATEGORY, "PASSWORD_EXPIRATION_WARNING_DAYS", 0);
        return val != null && val >= 0 ? val : 0;
    }

    @Override
    public void checkPasswordHistory(Long userId, String newPassword) {
        if (userId == null) {
            return;
        }
        int times = getPasswordRepetitionTimes();
        if (times <= 0) {
            return;
        }
        List<String> hashes = passwordHistoryService.selectLastHashes(userId, times);
        for (String hash : hashes) {
            if (hash != null && BCrypt.checkpw(newPassword, hash)) {
                throw new ServiceException("新密码不能与最近 " + times + " 次使用过的密码相同");
            }
        }
    }
}
