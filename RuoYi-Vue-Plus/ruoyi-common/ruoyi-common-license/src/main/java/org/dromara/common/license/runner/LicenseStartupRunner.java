package org.dromara.common.license.runner;

import org.dromara.common.license.config.properties.LicenseProperties;
import org.dromara.common.license.core.LicenseVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 授权启动校验器。
 *
 * <p>应用启动完成后校验授权状态：有效则打印有效期；无效时根据
 * {@link LicenseProperties#isFailOnInvalid()} 决定是否直接终止启动，
 * 默认不终止，由请求拦截器兜底拒绝业务访问。
 *
 * @author your-name
 */
@Component
@ConditionalOnProperty(value = "license.enabled", havingValue = "true", matchIfMissing = true)
public class LicenseStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LicenseStartupRunner.class);

    private final LicenseVerifier verifier;
    private final LicenseProperties properties;

    public LicenseStartupRunner(LicenseVerifier verifier, LicenseProperties properties) {
        this.verifier = verifier;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (verifier.getState().isValid()) {
            log.info("系统授权校验通过，有效期至 {}", verifier.getState().getExpireAt());
        } else {
            if (properties.isFailOnInvalid()) {
                throw new IllegalStateException("系统授权无效：" + verifier.getState().getMessage());
            }
            log.error("系统授权无效：{}，业务接口将被拒绝访问，请通过后台「授权管理」上传有效授权文件。",
                verifier.getState().getMessage());
        }
    }

}
