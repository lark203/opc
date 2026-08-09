package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.regex.RegexValidator;
import org.dromara.common.mail.core.MailAccountResolver;
import org.dromara.common.mail.core.MailBuilder;
import org.dromara.system.service.ISysOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮件配置操作处理
 * <p>
 * 测试发送与是否开启均以数据库 sys_option（MAIL 分类）中的配置为准，不再依赖任何 YAML/properties 配置。
 *
 * @author custom
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/system/mail")
@RequiredArgsConstructor
public class MailController {

    private static final String CATEGORY = "MAIL";

    private final ISysOptionService optionService;
    private final MailAccountResolver mailAccountResolver;

    /**
     * 测试发送邮件。
     * <p>使用 sys_option 中已保存的邮件配置真实发送，便于校验 SMTP/账号/密码是否正确。</p>
     *
     * @param to 收件人邮箱（可选，为空时默认发送给发件人账号自身）
     * @return 操作结果
     */
    @SaCheckPermission("system:mail:test")
    @PostMapping("/test")
    public R<String> testSend(String to) {
        // 1. 是否开启：以数据库 sys_option 的 MAIL_ENABLED 为准（1 表示开启）
        String dbEnabled = optionService.getOptionValue(CATEGORY, "MAIL_ENABLED");
        boolean enabled = "1".equals(dbEnabled);
        if (!enabled) {
            return R.fail("邮件功能未开启");
        }

        // 2. 读取邮件配置（仅来自 sys_option，由 MailAccountResolver 统一处理）
        MailAccount account = mailAccountResolver.resolve();

        // 3. 必填校验（直接修复“无密码却提示成功”的问题）
        if (StrUtil.isBlank(account.getHost()) || StrUtil.isBlank(account.getUser())
            || StrUtil.isBlank(account.getPass())) {
            return R.fail("邮件配置不完整，请检查 SMTP 服务器、发件人账号与密码");
        }

        // 4. 收件人：缺省发给自己
        String recipient = StrUtil.isNotBlank(to) ? to : account.getUser();
        if (!RegexValidator.isEmail(recipient)) {
            return R.fail("收件人邮箱格式不正确");
        }

        // 5. 真实发送
        try {
            MailBuilder.of(account)
                .to(recipient)
                .subject("测试邮件")
                .text("这是一封测试邮件，如果您收到说明邮件配置正确。")
                .send();
            return R.ok("测试邮件已发送，请查收");
        } catch (Exception e) {
            log.error("测试邮件发送失败 => {}", e.getMessage());
            return R.fail("测试邮件发送失败：" + e.getMessage());
        }
    }
}
