package org.dromara.web.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登录成功后的令牌信息返回对象。
 *
 * @author Michelle.Chung
 */
@Data
public class LoginVo {

    /**
     * 授权令牌
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 授权令牌 access_token 的有效期
     */
    @JsonProperty("expire_in")
    private Long expireIn;

    /**
     * 刷新令牌 refresh_token 的有效期
     */
    @JsonProperty("refresh_expire_in")
    private Long refreshExpireIn;

    /**
     * 应用id
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 令牌权限
     */
    private String scope;

    /**
     * 用户 openid
     */
    private String openid;

    /**
     * 密码是否已过期（前端需强制改密）
     */
    @JsonProperty("password_expired")
    private Boolean passwordExpired;

    /**
     * 密码是否即将过期（前端提示）
     */
    @JsonProperty("password_expiring_soon")
    private Boolean passwordExpiringSoon;

    /**
     * 密码剩余有效天数（null 表示不限制）
     */
    @JsonProperty("password_expire_in_days")
    private Long passwordExpireInDays;

}
