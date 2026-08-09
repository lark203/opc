package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.dromara.system.domain.SysSmsConfig;

import java.io.Serializable;

/**
 * 短信配置业务对象 sys_sms_config
 *
 * @author custom
 */
@Data
@AutoMapper(target = SysSmsConfig.class, reverseConvertGenerate = false)
public class SysSmsConfigBo implements Serializable {

    private Long smsId;

    /**
     * sms4j 配置ID（唯一，作为 blend 注册键）
     */
    @Size(max = 64, message = "配置ID长度不能超过{max}个字符")
    private String configId;

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过{max}个字符")
    private String name;

    @NotBlank(message = "供应商不能为空")
    @Size(max = 50, message = "供应商长度不能超过{max}个字符")
    private String supplier;

    @NotBlank(message = "accessKey不能为空")
    @Size(max = 200, message = "accessKey长度不能超过{max}个字符")
    private String accessKey;

    @Size(max = 200, message = "secretKey长度不能超过{max}个字符")
    private String secretKey;

    @Size(max = 100, message = "签名长度不能超过{max}个字符")
    private String signature;

    @NotBlank(message = "模板ID不能为空")
    @Size(max = 100, message = "模板ID长度不能超过{max}个字符")
    private String templateId;

    private Integer weight;

    private Integer retryInterval;

    private Integer maxRetries;

    private Integer maximum;

    private String supplierConfig;

    private String status;

    private String isDefault;

    private Integer sort;
}
