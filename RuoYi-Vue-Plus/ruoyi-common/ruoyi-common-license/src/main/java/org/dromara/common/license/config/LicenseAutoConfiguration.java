package org.dromara.common.license.config;

import org.dromara.common.license.core.LicenseVerifier;
import org.dromara.common.license.interceptor.LicenseInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 授权模块自动配置。
 *
 * <p>注册授权拦截器、开启定时重校验，并由 {@code license.enabled} 统一开关。
 *
 * @author your-name
 */
@AutoConfiguration
@ConditionalOnProperty(value = "license.enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class LicenseAutoConfiguration implements WebMvcConfigurer {

    private final LicenseInterceptor licenseInterceptor;
    private final LicenseVerifier licenseVerifier;

    public LicenseAutoConfiguration(LicenseInterceptor licenseInterceptor, LicenseVerifier licenseVerifier) {
        this.licenseInterceptor = licenseInterceptor;
        this.licenseVerifier = licenseVerifier;
    }

    /**
     * 不纳入授权校验的放行路径（登录、验证码、静态资源、授权自身接口等）。
     */
    private static final List<String> EXCLUDES = List.of(
        "/auth/login",
        "/captcha",
        "/error",
        "/favicon.ico",
        "/license/**",
        "/system/option/site",
        "/actuator/**",
        "/**/*.css",
        "/**/*.js",
        "/**/*.png",
        "/**/*.jpg",
        "/**/*.gif",
        "/**/*.ico",
        "/**/*.svg",
        "/**/*.woff",
        "/**/*.woff2",
        "/**/*.ttf",
        "/*.html"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(EXCLUDES);
    }

    /**
     * 定时重新校验授权文件，支持直接替换磁盘文件实现静默续期。
     */
    @Scheduled(fixedDelayString = "${license.checkInterval:3600000}")
    public void scheduledCheck() {
        licenseVerifier.reload();
    }

}
