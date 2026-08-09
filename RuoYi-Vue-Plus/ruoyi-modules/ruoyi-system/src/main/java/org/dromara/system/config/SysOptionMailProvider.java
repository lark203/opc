package org.dromara.system.config;

import lombok.RequiredArgsConstructor;
import org.dromara.common.mail.core.MailOptionProvider;
import org.dromara.system.service.ISysOptionService;
import org.springframework.stereotype.Component;

/**
 * 基于 sys_option（MAIL 分类）的邮件配置提供方。
 * <p>
 * 供 common-mail 的 {@link MailAccountResolver} 优先采用数据库中的邮件配置，
 * 使系统配置中的「邮件配置」成为邮件发送的唯一真源。
 *
 * @author custom
 */
@Component
@RequiredArgsConstructor
public class SysOptionMailProvider implements MailOptionProvider {

    private final ISysOptionService optionService;

    @Override
    public String getOptionValue(String category, String key) {
        return optionService.getOptionValue(category, key);
    }
}
