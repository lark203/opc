package org.dromara.common.license.core;

import org.dromara.common.core.exception.base.BaseException;

/**
 * 授权异常，继承通用业务异常基类以便被全局异常处理器统一转换为响应结果。
 *
 * @author your-name
 */
public class LicenseException extends BaseException {

    public LicenseException(String message) {
        super(message);
    }

}
