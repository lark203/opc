package org.dromara.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.system.service.ILoginCaptchaService;
import org.dromara.system.service.ISysOptionService;
import org.springframework.stereotype.Service;

/**
 * 登录验证码动态配置服务实现
 * 优先从 sys_option 读取配置，未配置时回退到 application.yml
 *
 * @author custom
 */
@Service
@RequiredArgsConstructor
public class LoginCaptchaServiceImpl implements ILoginCaptchaService {

    private final ISysOptionService optionService;
    private final CaptchaProperties captchaProperties;

    private static final String CATEGORY = "LOGIN";

    @Override
    public boolean isCaptchaEnabled() {
        String val = optionService.getOptionValue(CATEGORY, "LOGIN_CAPTCHA_ENABLED");
        if (val != null) {
            return "1".equals(val);
        }
        // 回退到 application.yml 配置
        return Boolean.TRUE.equals(captchaProperties.getEnable());
    }

    @Override
    public String getCaptchaType() {
        String val = optionService.getOptionValue(CATEGORY, "LOGIN_CAPTCHA_TYPE");
        if (val != null && !val.isEmpty()) {
            return "1".equals(val) ? "math" : "random";
        }
        // 回退到 application.yml 配置
        String type = captchaProperties.getType();
        return type != null ? type : "math";
    }

    @Override
    public int getNumberLength() {
        Integer val = optionService.getOptionInt(CATEGORY, "LOGIN_CAPTCHA_LENGTH", null);
        if (val != null && val > 0) {
            return val;
        }
        Integer length = captchaProperties.getNumberLength();
        return length != null ? length : 3;
    }

    @Override
    public int getCharLength() {
        Integer val = optionService.getOptionInt(CATEGORY, "LOGIN_CAPTCHA_LENGTH", null);
        if (val != null && val > 0) {
            return val;
        }
        Integer length = captchaProperties.getCharLength();
        return length != null ? length : 4;
    }

    @Override
    public boolean isRegisterEnabled() {
        // 注册开关统一由系统配置(sys_option) LOGIN_REGISTER_ENABLED 控制
        String val = optionService.getOptionValue(CATEGORY, "LOGIN_REGISTER_ENABLED");
        return "1".equals(val);
    }
}
