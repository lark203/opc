package org.dromara.common.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 跨域配置属性。
 */
@Data
@ConfigurationProperties(prefix = "web.cors")
public class CorsProperties {

    /**
     * 是否允许携带凭证。
     * <p>注意：开启凭证(cookie/token)时必须配合具体的来源(allowedOriginPatterns)，
     * 严禁使用通配符 *，否则任意网站都可携带用户会话发起跨站请求(CSRF)并读取响应。</p>
     */
    private Boolean allowCredentials = true;

    /**
     * 允许的来源匹配规则。
     * <p>默认仅允许本地开发前端(与 Vite 开发服务器端口一致)，
     * 生产环境必须在 application-prod.yml 中通过 web.cors.allowed-origin-patterns 指定真实前端域名，
     * 禁止使用通配符 *。</p>
     */
    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
        "http://localhost:5173", "http://127.0.0.1:5173"));

    /**
     * 允许的请求头（禁止使用通配符 *，仅开放实际需要的头）。
     */
    private List<String> allowedHeaders = new ArrayList<>(List.of(
        "Authorization", "Content-Type", "Accept", "clientid", "X-Requested-With"));

    /**
     * 允许的请求方法（禁止使用通配符 *）。
     */
    private List<String> allowedMethods = new ArrayList<>(List.of(
        "GET", "POST", "PUT", "DELETE", "OPTIONS"));

    /**
     * 预检请求缓存时间，单位秒。
     */
    private Long maxAge = 1800L;

}
