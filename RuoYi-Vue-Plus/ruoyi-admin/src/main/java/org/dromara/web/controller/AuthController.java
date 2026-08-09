package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.domain.model.LoginBody;
import org.dromara.common.core.enums.PushSourceEnum;
import org.dromara.common.core.enums.PushTypeEnum;
import org.dromara.common.core.utils.DateUtils;
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.encrypt.annotation.ApiEncrypt;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.redis.annotation.RateLimiter;
import org.dromara.common.redis.enums.LimitType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.social.config.properties.SocialLoginConfigProperties;
import org.dromara.common.social.config.properties.SocialProperties;
import org.dromara.common.social.utils.SocialUtils;
import org.dromara.system.api.MessageService;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.dromara.system.api.model.RegisterBody;
import org.dromara.system.api.model.SocialLoginBody;
import org.dromara.system.domain.vo.SysClientVo;
import org.dromara.system.domain.vo.SysOptionVo;
import org.dromara.system.service.ILoginCaptchaService;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysOptionService;
import org.dromara.system.service.ISysSocialService;
import org.dromara.web.domain.vo.LoginVo;
import org.dromara.web.service.IAuthStrategy;
import org.dromara.web.service.SysLoginService;
import org.dromara.web.service.SysPasswordResetService;
import org.dromara.web.service.SysRegisterService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器，提供登录、注册、社交绑定和退出能力。
 *
 * @author Lion Li
 */
@Slf4j
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SocialProperties socialProperties;
    private final SysLoginService loginService;
    private final SysRegisterService registerService;
    private final SysPasswordResetService resetService;
    private final ILoginCaptchaService loginCaptchaService;
    private final ISysSocialService socialUserService;
    private final ISysClientService clientService;
    private final ISysOptionService optionService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final MessageService messageService;


    /**
     * 登录方法
     *
     * @param body 登录信息
     * @return 结果
     */
    @ApiEncrypt
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    @PostMapping("/login")
    public R<LoginVo> login(@RequestBody String body) {
        LoginBody loginBody = JsonUtils.parseObject(body, LoginBody.class);
        ValidatorUtils.validate(loginBody);
        // 授权类型和客户端id
        String clientId = loginBody.getClientId();
        String grantType = loginBody.getGrantType();
        SysClientVo client = clientService.queryByClientId(clientId);
        // 查询不到 client 或 client 内不包含 grantType
        if (ObjectUtil.isNull(client) || !StringUtils.contains(client.getGrantType(), grantType)) {
            log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
            return R.fail(MessageUtils.message("auth.grant.type.error"));
        } else if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            return R.fail(MessageUtils.message("auth.grant.type.blocked"));
        }
        // 登录
        LoginVo loginVo = IAuthStrategy.login(body, client, grantType);

        Long userId = LoginHelper.getUserId();
        scheduledExecutorService.schedule(() -> {
            messageService.publishMessage(
                List.of(userId),
                PushPayloadDTO.of(
                    PushTypeEnum.MESSAGE,
                    PushSourceEnum.BACKEND,
                    DateUtils.getTodayHour(new Date()) + "好，欢迎登录 " + getSystemName() + " 后台管理系统",
                    null
                )
            );
        }, 5, TimeUnit.SECONDS);
        return R.ok(loginVo);
    }

    /**
     * 获取系统名称（用于登录欢迎语）。
     * <p>优先取 sys_option（SITE 分类）中 SITE_TITLE 的 value；为空时回退 defaultValue；
     * 两者皆为空则使用内置默认名称。</p>
     *
     * @return 系统名称
     */
    private String getSystemName() {
        try {
            List<SysOptionVo> options = optionService.selectOptionList("SITE");
            for (SysOptionVo vo : options) {
                if ("SITE_TITLE".equals(vo.getCode())) {
                    if (StrUtil.isNotBlank(vo.getValue())) {
                        return vo.getValue();
                    }
                    return vo.getDefaultValue();
                }
            }
        } catch (Exception e) {
            log.warn("读取系统名称失败，使用默认值", e);
        }
        return "RuoYi-Vue-Plus 后台管理系统";
    }

    /**
     * 获取第三方绑定跳转地址。
     *
     * @param source 登录来源
     * @return 跳转地址
     */
    @GetMapping("/binding/{source}")
    public R<String> authBinding(@PathVariable("source") String source) {
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        if (ObjectUtil.isNull(obj)) {
            return R.fail(source + "平台账号暂不支持");
        }
        AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        return R.data(authorizeUrl);
    }

    /**
     * 处理前端回调后的社交账号绑定。
     *
     * @param loginBody 请求体
     * @return 操作结果
     */
    @PostMapping("/social/callback")
    public R<Void> socialCallback(@RequestBody SocialLoginBody loginBody) {
        // 校验token
        StpUtil.checkLogin();
        // 获取第三方登录信息
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
            loginBody.getSource(), loginBody.getSocialCode(),
            loginBody.getSocialState(), socialProperties);
        AuthUser authUserData = response.getData();
        // 判断授权响应是否成功
        if (!response.ok()) {
            return R.fail(response.getMsg());
        }
        loginService.socialRegister(authUserData);
        return R.ok();
    }


    /**
     * 取消当前用户的社交账号授权。
     *
     * @param socialId socialId
     * @return 操作结果
     */
    @DeleteMapping(value = "/unlock/{socialId}")
    public R<Void> unlockSocial(@PathVariable Long socialId) {
        // 校验token
        StpUtil.checkLogin();
        Boolean rows = socialUserService.deleteWithValidById(socialId);
        return rows ? R.ok() : R.fail("取消授权失败");
    }


    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }

    /**
     * 用户注册。
     *
     * @param user 注册信息
     * @return 操作结果
     */
    @ApiEncrypt
    @RateLimiter(time = 60, count = 5, limitType = LimitType.IP)
    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody RegisterBody user) {
        if (!loginCaptchaService.isRegisterEnabled()) {
            return R.fail("当前系统没有开启注册功能！");
        }
        registerService.register(user);
        return R.ok();
    }

    /**
     * 发送重置密码验证码。
     * <p>根据用户名查找用户，将验证码发送到其绑定的邮箱或手机号。</p>
     *
     * @param body 请求体（用户名）
     * @return 发送通道与脱敏后的目的地
     */
    @RateLimiter(key = "#body.username", time = 60, count = 1)
    @PostMapping("/reset-password/send-code")
    public R<ResetCodeVo> sendResetCode(@Validated @RequestBody ResetCodeBody body) {
        SysPasswordResetService.ResetChannelInfo info = resetService.sendResetCode(body.username(), body.channel());
        return R.ok(new ResetCodeVo(info.channel(), info.destination()));
    }

    /**
     * 查询用户可用的重置密码验证通道（邮箱 / 短信）。
     *
     * @param body 请求体（用户名）
     * @return 可用通道列表（含通道类型与脱敏后的目的地）
     */
    @PostMapping("/reset-password/channels")
    public R<List<SysPasswordResetService.ResetChannelInfo>> getResetChannels(
        @Validated @RequestBody ResetCodeBody body) {
        return R.ok(resetService.getResetChannels(body.username()));
    }

    /**
     * 校验验证码并重置密码。
     *
     * @param body 请求体（用户名、验证码、新密码）
     * @return 操作结果
     */
    @ApiEncrypt
    @PostMapping("/reset-password")
    public R<Void> resetPassword(@Validated @RequestBody ResetPwdBody body) {
        resetService.resetPassword(body.username(), body.code(), body.newPassword());
        return R.ok();
    }

    /**
     * 发送重置验证码请求体。
     *
     * @param username 用户名
     */
    public record ResetCodeBody(
        @NotBlank(message = "{user.username.not.blank}") String username,
        String channel
    ) {
    }

    /**
     * 重置密码请求体。
     *
     * @param username    用户名
     * @param code        验证码
     * @param newPassword 新密码
     */
    public record ResetPwdBody(
        @NotBlank(message = "{user.username.not.blank}") String username,
        @NotBlank(message = "{user.captcha.not.blank}") String code,
        @NotBlank(message = "{user.password.not.blank}") String newPassword
    ) {
    }

    /**
     * 重置验证码发送结果响应。
     *
     * @param channel     发送通道（email / sms）
     * @param destination 脱敏后的目的地
     */
    public record ResetCodeVo(String channel, String destination) {
    }

}
