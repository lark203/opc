package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.Constants;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.regex.RegexValidator;
import org.dromara.common.mail.core.MailAccountResolver;
import org.dromara.common.mail.core.MailBuilder;
import org.dromara.common.redis.annotation.RateLimiter;
import org.dromara.common.redis.enums.LimitType;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.web.config.properties.CaptchaProperties;
import org.dromara.common.web.core.WaveAndCircleCaptcha;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.system.service.ILoginCaptchaService;
import org.dromara.system.service.ISysOptionService;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.time.Duration;
import java.util.LinkedHashMap;

/**
 * 验证码操作处理
 *
 * @author Lion Li
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
public class CaptchaController {

    private final CaptchaProperties captchaProperties;
    private final MailAccountResolver mailAccountResolver;
    private final ISysOptionService optionService;
    private final ILoginCaptchaService loginCaptchaService;

    /**
     * 发送短信验证码。
     *
     * @param phoneNumber 用户手机号
     * @return 操作结果
     */
    @RateLimiter(key = "#phoneNumber", time = 60, count = 1)
    @GetMapping("/resource/sms/code")
    public R<Void> smsCode(@NotBlank(message = "{user.phonenumber.not.blank}") String phoneNumber) {
        if (!RegexValidator.isMobile(phoneNumber)) {
            return R.fail("请输入正确的手机号！");
        }
        String key = GlobalConstants.CAPTCHA_CODE_KEY + phoneNumber;
        String code = RandomUtil.randomNumbers(4);
        // 验证码模板id 自行处理 (查数据库或写死均可)
        String templateId = "";
        LinkedHashMap<String, String> map = new LinkedHashMap<>(1);
        map.put("code", code);
        SmsBlend smsBlend = SmsFactory.getSmsBlend();
        if (smsBlend == null) {
            return R.fail("短信服务未配置或未启用，请在「系统配置 - 短信配置」中设置生效配置");
        }
        SmsResponse smsResponse = smsBlend.sendMessage(phoneNumber, templateId, map);
        if (!smsResponse.isSuccess()) {
            log.error("验证码短信发送异常 => {}", smsResponse);
            Object data = smsResponse.getData();
            return R.fail(data == null ? "验证码短信发送失败" : data.toString());
        }
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        return R.ok();
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱
     * @return 操作结果
     */
    @GetMapping("/resource/email/code")
    public R<Void> emailCode(@NotBlank(message = "{user.email.not.blank}") String email) {
        // 是否开启邮箱功能：以 sys_option 的 MAIL_ENABLED 为准（1 表示开启）
        String dbEnabled = optionService.getOptionValue("MAIL", "MAIL_ENABLED");
        boolean enabled = "1".equals(dbEnabled);
        if (!enabled) {
            return R.fail("当前系统没有开启邮箱功能！");
        }
        if (!RegexValidator.isEmail(email)) {
            return R.fail("请输入正确的邮箱地址！");
        }
        SpringUtils.getAopProxy(this).emailCodeImpl(email);
        return R.ok();
    }

    /**
     * 发送邮箱验证码的实际执行方法，拆分出来避免开关关闭时仍触发限流。
     *
     * @param email 邮箱
     */
    @RateLimiter(key = "#email", time = 60, count = 1)
    public void emailCodeImpl(String email) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + email;
        String code = RandomUtil.randomNumbers(4);
        try {
            MailBuilder.of(mailAccountResolver.resolve())
                .to(email)
                .subject("登录验证码")
                .text("您本次验证码为：" + code + "，有效性为" + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。")
                .send();
            RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        } catch (Exception e) {
            log.error("验证码短信发送异常 => {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 获取图片验证码。
     *
     * @return 验证码信息
     */
    @GetMapping("/auth/code")
    public R<CaptchaVo> getCode() {
        boolean captchaEnabled = loginCaptchaService.isCaptchaEnabled();
        boolean registerEnabled = loginCaptchaService.isRegisterEnabled();
        if (!captchaEnabled) {
            return R.ok(new CaptchaVo(false, null, null, registerEnabled));
        }
        return R.ok(SpringUtils.getAopProxy(this).getCodeImpl(registerEnabled));
    }

    /**
     * 实际生成图片验证码并缓存结果。
     *
     * @return 验证码信息
     */
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    public CaptchaVo getCodeImpl(boolean registerEnabled) {
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + uuid;
        // 生成验证码
        String captchaType = loginCaptchaService.getCaptchaType();
        CodeGenerator codeGenerator;
        if ("math".equals(captchaType)) {
            codeGenerator = new MathGenerator(loginCaptchaService.getNumberLength(), false);
        } else {
            codeGenerator = new RandomGenerator(loginCaptchaService.getCharLength());
        }
        WaveAndCircleCaptcha captcha = new WaveAndCircleCaptcha(160, 60);
        // captcha.setBackground(Color.WHITE); // 不设置就是透明底
        captcha.setFont(new Font("Arial", Font.BOLD, 45));
        captcha.setGenerator(codeGenerator);
        captcha.createCode();
        // 如果是数学验证码，使用SpEL表达式处理验证码结果
        String code = captcha.getCode();
        if ("math".equals(captchaType)) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        return new CaptchaVo(true, uuid, captcha.getImageBase64(), registerEnabled);
    }

    /**
     * 图片验证码响应对象。
     *
     * @param captchaEnabled   是否启用验证码
     * @param uuid             验证码标识
     * @param img              Base64 图片数据
     * @param registerEnabled  是否允许注册
     */
    public record CaptchaVo(Boolean captchaEnabled, String uuid, String img, Boolean registerEnabled) {
    }

}
