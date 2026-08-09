package org.dromara.common.mail.core;

/**
 * 邮件配置选项提供方。
 * <p>
 * 由具体业务模块（如系统模块读取 {@code sys_option} 的 MAIL 分类）实现，
 * 供 {@link MailAccountResolver} 在构建邮件账户时读取数据库配置。不再依赖任何 YAML/properties 配置。
 *
 * @author custom
 */
public interface MailOptionProvider {

    /**
     * 获取指定分类下的配置项值。
     *
     * @param category 分类（如 MAIL）
     * @param key      配置键（如 MAIL_HOST）
     * @return 配置值，未配置时返回 {@code null}
     */
    String getOptionValue(String category, String key);
}
