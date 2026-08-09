package org.dromara.common.license.core;

import java.time.LocalDateTime;

/**
 * 授权校验结果状态。
 *
 * @author your-name
 */
public class LicenseState {

    /**
     * 授权是否有效。
     */
    private boolean valid;

    /**
     * 绑定的机器指纹。
     */
    private String fingerprint;

    /**
     * 授权签发时间（ISO-8601）。
     */
    private String issuedAt;

    /**
     * 授权过期时间（ISO-8601）。
     */
    private String expireAt;

    /**
     * 授权版本。
     */
    private String version;

    /**
     * 授权类型。
     */
    private String type;

    /**
     * 校验结果描述信息。
     */
    private String message;

    /**
     * 最近一次校验时间。
     */
    private LocalDateTime lastChecked;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(String issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

}
