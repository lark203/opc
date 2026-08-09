package org.dromara.common.license.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 授权码配置属性。
 *
 * @author your-name
 */
@Component
@ConfigurationProperties(prefix = "license")
public class LicenseProperties {

    /**
     * 是否启用授权校验，默认开启。
     */
    private boolean enabled = true;

    /**
     * 授权文件落地路径（相对于应用工作目录）。
     */
    private String licensePath = "config/license.lic";

    /**
     * 验签公钥资源路径，支持 classpath: 或 file: 前缀。
     */
    private String publicKeyPath = "classpath:META-INF/license/public.key";

    /**
     * 是否绑定机器指纹，开启后授权文件仅可在签发时所在服务器使用。
     */
    private boolean bindFingerprint = true;

    /**
     * 定时重校验间隔（毫秒）。
     */
    private long checkInterval = 3600000L;

    /**
     * 启动校验不通过时是否直接终止应用启动，默认 false（由请求拦截器兜底拒绝）。
     */
    private boolean failOnInvalid = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLicensePath() {
        return licensePath;
    }

    public void setLicensePath(String licensePath) {
        this.licensePath = licensePath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public boolean isBindFingerprint() {
        return bindFingerprint;
    }

    public void setBindFingerprint(boolean bindFingerprint) {
        this.bindFingerprint = bindFingerprint;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean isFailOnInvalid() {
        return failOnInvalid;
    }

    public void setFailOnInvalid(boolean failOnInvalid) {
        this.failOnInvalid = failOnInvalid;
    }

}
