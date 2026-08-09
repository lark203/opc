package org.dromara.system.service;

/**
 * 登录验证码动态配置服务接口
 * 从 sys_option 配置中读取验证码相关设置
 *
 * @author custom
 */
public interface ILoginCaptchaService {

    /**
     * 是否启用验证码
     *
     * @return true 表示启用
     */
    boolean isCaptchaEnabled();

    /**
     * 获取验证码类型
     *
     * @return 验证码类型（math/random）
     */
    String getCaptchaType();

    /**
     * 获取数字验证码位数
     *
     * @return 位数
     */
    int getNumberLength();

    /**
     * 获取字符验证码长度
     *
     * @return 长度
     */
    int getCharLength();

    /**
     * 是否允许注册（登录页是否展示注册入口）
     *
     * @return true 表示允许
     */
    boolean isRegisterEnabled();
}
