package org.dromara.common.license.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.license.core.LicenseException;
import org.dromara.common.license.core.LicenseVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 授权拦截器。
 *
 * <p>在业务请求处理前校验授权状态，授权无效时抛出 {@link LicenseException}，
 * 由全局异常处理器转换为统一的错误响应。
 *
 * @author your-name
 */
@Component
@ConditionalOnProperty(value = "license.enabled", havingValue = "true", matchIfMissing = true)
public class LicenseInterceptor implements HandlerInterceptor {

    private final LicenseVerifier verifier;

    public LicenseInterceptor(LicenseVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!verifier.getState().isValid()) {
            throw new LicenseException(verifier.getState().getMessage());
        }
        return true;
    }

}
