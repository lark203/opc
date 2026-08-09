package org.dromara.web.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.extra.mail.MailAccount;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mail.core.MailAccountResolver;
import org.dromara.common.mail.core.MailBuilder;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.IPasswordPolicyService;
import org.dromara.system.service.ISysOptionService;
import org.dromara.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 密码重置（忘记密码）服务。
 * <p>提供两步式自助重置：发送验证码到用户已绑定的邮箱/手机号，校验验证码后重置密码。</p>
 *
 * @author custom
 */
@RequiredArgsConstructor
@Service
public class SysPasswordResetService {

    private final ISysUserService userService;
    private final IPasswordPolicyService passwordPolicyService;
    private final ISysOptionService optionService;
    private final MailAccountResolver mailAccountResolver;

    private static final String MAIL_CATEGORY = "MAIL";
    private static final String MAIL_ENABLED_KEY = "MAIL_ENABLED";

    /**
     * 发送重置密码验证码（自动选择通道）。
     *
     * @param username 用户名
     * @return 发送通道与脱敏后的目的地
     */
    public ResetChannelInfo sendResetCode(String username) {
        return sendResetCode(username, null);
    }

    /**
     * 发送重置密码验证码。
     *
     * @param username 用户名
     * @param channel  指定验证通道（email / sms），为空时自动选择
     * @return 发送通道与脱敏后的目的地
     */
    public ResetChannelInfo sendResetCode(String username, String channel) {
        SysUserVo user = userService.selectUserByUserName(username);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        String email = user.getEmail();
        String phone = user.getPhoneNumber();
        // 邮箱是否可用：以 sys_option（MAIL/MAIL_ENABLED）为准
        boolean emailAvailable = StringUtils.isNotBlank(email) && isEmailEnabled();
        boolean smsAvailable = StringUtils.isNotBlank(phone);
        String code = RandomUtil.randomNumbers(4);

        String usedChannel;
        String destination;
        if (StringUtils.isNotBlank(channel)) {
            if ("email".equalsIgnoreCase(channel)) {
                if (!emailAvailable) {
                    throw new ServiceException("邮件服务未启用或该用户未绑定邮箱，请选择短信验证或联系管理员配置邮件服务");
                }
                usedChannel = "email";
                destination = maskEmail(email);
                sendEmail(email, code);
            } else if ("sms".equalsIgnoreCase(channel)) {
                if (!smsAvailable) {
                    throw new ServiceException("该用户未绑定可用手机号");
                }
                usedChannel = "sms";
                destination = maskPhone(phone);
                sendSms(phone, code);
            } else {
                throw new ServiceException("不支持的验证方式");
            }
        } else {
            // 未指定通道时保持原有自动选择逻辑
            if (emailAvailable) {
                usedChannel = "email";
                destination = maskEmail(email);
                sendEmail(email, code);
            } else if (smsAvailable) {
                usedChannel = "sms";
                destination = maskPhone(phone);
                sendSms(phone, code);
            } else {
                throw new ServiceException("该用户未绑定可用的邮箱或手机号，无法重置密码");
            }
        }

        RedisUtils.setCacheObject(
            GlobalConstants.RESET_PWD_CODE_KEY + username,
            code,
            Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION)
        );
        return new ResetChannelInfo(usedChannel, destination);
    }

    /**
     * 查询用户可用的重置密码验证通道。
     *
     * @param username 用户名
     * @return 可用通道列表（含通道类型与脱敏后的目的地）
     */
    public List<ResetChannelInfo> getResetChannels(String username) {
        SysUserVo user = userService.selectUserByUserName(username);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        List<ResetChannelInfo> channels = new ArrayList<>();
        String email = user.getEmail();
        String phone = user.getPhoneNumber();
        // 邮箱通道：用户绑定了邮箱且邮件功能已开启（以 sys_option 为准）
        if (StringUtils.isNotBlank(email) && isEmailEnabled()) {
            channels.add(new ResetChannelInfo("email", maskEmail(email)));
        }
        if (StringUtils.isNotBlank(phone)) {
            channels.add(new ResetChannelInfo("sms", maskPhone(phone)));
        }
        return channels;
    }

    /**
     * 邮件功能是否开启。
     * <p>以 sys_option（MAIL 分类下的 MAIL_ENABLED，值为 "1" 表示开启）为准，不再依赖任何 YAML/properties 配置。</p>
     *
     * @return 是否开启
     */
    private boolean isEmailEnabled() {
        String dbEnabled = optionService.getOptionValue(MAIL_CATEGORY, MAIL_ENABLED_KEY);
        return "1".equals(dbEnabled);
    }

    /**
     * 校验验证码并重置密码。
     *
     * @param username    用户名
     * @param code        验证码
     * @param newPassword 新密码（明文）
     */
    public void resetPassword(String username, String code, String newPassword) {
        String cacheKey = GlobalConstants.RESET_PWD_CODE_KEY + username;
        String cachedCode = RedisUtils.getCacheObject(cacheKey);
        if (StringUtils.isBlank(cachedCode)) {
            throw new ServiceException("验证码已过期，请重新获取");
        }
        if (!StringUtils.equalsIgnoreCase(cachedCode, code)) {
            throw new ServiceException("验证码错误");
        }

        SysUserVo user = userService.selectUserByUserName(username);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 密码策略校验（明文）
        passwordPolicyService.validatePassword(newPassword, username, null);
        // 历史重复密码校验：不能与最近 N 次使用过的密码相同（N 由 PASSWORD_REPETITION_TIMES 决定）
        passwordPolicyService.checkPasswordHistory(user.getUserId(), newPassword);

        int rows = userService.resetUserPwd(user.getUserId(), BCrypt.hashpw(newPassword));
        if (rows <= 0) {
            throw new ServiceException("重置密码失败，请稍后重试");
        }
        RedisUtils.deleteObject(cacheKey);
    }

    /**
     * 发送邮件验证码。
     * <p>与系统「邮件配置-测试发送」一致：使用 sys_option（MAIL 分类）中保存的 SMTP 配置由 MailAccountResolver 构建 MailAccount 发送。</p>
     */
    private void sendEmail(String email, String code) {
        String content = "您正在申请重置密码，本次验证码为：" + code + "，有效性为"
            + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。如非本人操作请忽略。";
        try {
            // 仅使用 sys_option 中的邮件配置（由 MailAccountResolver 统一处理）
            MailAccount account = mailAccountResolver.resolve();
            MailBuilder.of(account)
                .to(email)
                .subject("重置密码验证码")
                .text(content)
                .send();
        } catch (Exception e) {
            throw new ServiceException("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送短信验证码。
     */
    private void sendSms(String phone, String code) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("code", code);
        try {
            SmsBlend smsBlend = SmsFactory.getSmsBlend();
            if (smsBlend == null) {
                throw new ServiceException("短信服务未配置或未启用，请在「系统配置 - 短信配置」中设置生效配置");
            }
            SmsResponse smsResponse = smsBlend.sendMessage(phone, "", map);
            if (!smsResponse.isSuccess()) {
                Object data = smsResponse.getData();
                throw new ServiceException(data == null ? "短信发送失败" : data.toString());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("短信发送失败：" + e.getMessage());
        }
    }

    /**
     * 邮箱脱敏：保留首字符与域名，如 a***@example.com
     */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    /**
     * 手机号脱敏：保留前三位与后四位，如 138****8000
     */
    private String maskPhone(String phone) {
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 重置验证码发送结果。
     *
     * @param channel     发送通道（email / sms）
     * @param destination 脱敏后的目的地
     */
    public record ResetChannelInfo(String channel, String destination) {
    }
}
