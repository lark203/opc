package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.SysSmsConfig;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信配置视图对象 sys_sms_config
 *
 * @author custom
 */
@Data
@AutoMapper(target = SysSmsConfig.class)
public class SysSmsConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long smsId;

    private String configId;

    private String name;

    private String supplier;

    private String accessKey;

    private String secretKey;

    private String signature;

    private String templateId;

    private Integer weight;

    private Integer retryInterval;

    private Integer maxRetries;

    private Integer maximum;

    private String supplierConfig;

    private String status;

    private String isDefault;

    private Integer sort;

    private LocalDateTime createTime;
}
