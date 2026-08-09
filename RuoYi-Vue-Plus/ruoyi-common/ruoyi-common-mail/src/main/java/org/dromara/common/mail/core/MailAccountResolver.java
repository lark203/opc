package org.dromara.common.mail.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 统一邮件账户解析器。
 * <p>
 * 完全基于数据库配置（sys_option 的 MAIL 分类）构建邮件账户，由 {@link MailOptionProvider} 提供。
 * 当 sys_option 中 value 为空时，由 sys_option 自身的 default_value 兜底，
 * 不再读取任何 YAML/properties 配置，确保「系统配置 - 邮件配置」为唯一真源。
 *
 * @author custom
 */
@Component
@RequiredArgsConstructor
public class MailAccountResolver {

    private static final String CATEGORY = "MAIL";

    private MailOptionProvider optionProvider;

    @Autowired(required = false)
    public void setOptionProvider(MailOptionProvider optionProvider) {
        this.optionProvider = optionProvider;
    }

    /**
     * 解析邮件账户：仅从数据库配置（sys_option MAIL 分类）构建。
     *
     * @return 邮件账户
     */
    public MailAccount resolve() {
        String host = opt("MAIL_HOST");
        String username = opt("MAIL_USERNAME");
        String password = opt("MAIL_PASSWORD");
        String nickname = opt("MAIL_NICKNAME");
        String sslEnabled = opt("MAIL_SSL_ENABLED");
        String portStr = opt("MAIL_PORT");
        String sslPortStr = opt("MAIL_SSL_PORT");

        boolean ssl = "1".equals(sslEnabled);
        int port = Convert.toInt(portStr, ssl ? 465 : 25);
        int socketFactoryPort = ssl ? Convert.toInt(sslPortStr, port) : port;

        String from;
        if (StrUtil.isNotBlank(nickname) && StrUtil.isNotBlank(username)) {
            from = nickname + " <" + username + ">";
        } else if (StrUtil.isNotBlank(username)) {
            from = username;
        } else {
            from = null;
        }

        MailAccount account = new MailAccount();
        account.setHost(host);
        account.setPort(port);
        account.setAuth(true);
        account.setFrom(from);
        account.setUser(username);
        account.setPass(password);
        account.setSocketFactoryPort(socketFactoryPort);
        account.setStarttlsEnable(!ssl);
        account.setSslEnable(ssl);
        // 超时保持为空，交由 Hutool 默认（不超时）
        return account;
    }

    private String opt(String key) {
        return optionProvider == null ? null : optionProvider.getOptionValue(CATEGORY, key);
    }
}
