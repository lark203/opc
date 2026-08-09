package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 短信配置表 sys_sms_config
 *
 * @author custom
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sms_config")
public class SysSmsConfig extends BaseEntity {

    @TableId(value = "sms_id")
    private Long smsId;

    /**
     * sms4j 配置ID（唯一，作为 blend 注册键）
     */
    private String configId;

    /**
     * 名称
     */
    private String name;

    /**
     * 供应商（字典 sms_supplier）
     */
    private String supplier;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 签名
     */
    private String signature;

    /**
     * 模板ID
     */
    private String templateId;

    /**
     * 权重（1-100）
     */
    private Integer weight;

    /**
     * 重试间隔（秒）
     */
    private Integer retryInterval;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 最大发送量
     */
    private Integer maximum;

    /**
     * 供应商扩展配置（JSON）
     */
    private String supplierConfig;

    /**
     * 状态（1正常 2停用）
     */
    private String status;

    /**
     * 是否默认（1是 0否）
     */
    private String isDefault;

    /**
     * 排序
     */
    private Integer sort;
}
