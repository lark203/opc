package org.dromara.system.config;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.tencent.config.TencentConfig;
import org.dromara.system.domain.SysSmsConfig;
import org.dromara.system.mapper.SysSmsConfigMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信配置读取实现（DB 来源）。
 * <p>
 * 配合 sms4j {@code config-type: interface} 使用：框架启动与刷新时会回调本类。
 * 返回所有「启用（status='1'）」的配置，sms4j 会逐个注册为生效 blend，
 * 调用 {@code SmsFactory.getSmsBlend()}（无参）时按 weight 做加权轮询，
 * 即支持多个配置同时生效；也可通过 {@code getSmsBlend(configId)} /
 * {@code getBySupplier(supplier)} 选择指定配置。
 * </p>
 *
 * @author custom
 */
@Component
@RequiredArgsConstructor
public class SysSmsConfigReadConfig implements SmsReadConfig {

    private final SysSmsConfigMapper baseMapper;

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        if (configId == null) {
            return null;
        }
        SysSmsConfig entity = baseMapper.selectOne(new LambdaQueryWrapper<SysSmsConfig>()
            .eq(SysSmsConfig::getConfigId, configId)
            .eq(SysSmsConfig::getStatus, "1"));
        return entity == null ? null : buildConfig(entity);
    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        List<SysSmsConfig> list = baseMapper.selectList(new LambdaQueryWrapper<SysSmsConfig>()
            .eq(SysSmsConfig::getStatus, "1")
            .orderByAsc(SysSmsConfig::getSort)
            .orderByAsc(SysSmsConfig::getSmsId));
        return list.stream().map(this::buildConfig).collect(Collectors.toList());
    }

    private BaseConfig buildConfig(SysSmsConfig entity) {
        BaseConfig config = newConfigBySupplier(entity.getSupplier());
        config.setConfigId(entity.getConfigId());
        config.setAccessKeyId(entity.getAccessKey());
        config.setAccessKeySecret(entity.getSecretKey());
        config.setSignature(entity.getSignature());
        config.setTemplateId(entity.getTemplateId());
        if (entity.getWeight() != null) {
            config.setWeight(entity.getWeight());
        }
        if (entity.getRetryInterval() != null) {
            config.setRetryInterval(entity.getRetryInterval());
        }
        if (entity.getMaxRetries() != null) {
            config.setMaxRetries(entity.getMaxRetries());
        }
        if (entity.getMaximum() != null) {
            config.setMaximum(entity.getMaximum());
        }
        config.setSdkAppId(parseSdkAppId(entity.getSupplierConfig()));
        return config;
    }

    private BaseConfig newConfigBySupplier(String supplier) {
        if ("alibaba".equals(supplier)) {
            return new AlibabaConfig();
        }
        if ("tencent".equals(supplier)) {
            return new TencentConfig();
        }
        throw new ServiceException("不支持的短信供应商：" + supplier + "，仅支持 alibaba / tencent");
    }

    private String parseSdkAppId(String supplierConfig) {
        if (!JSONUtil.isTypeJSON(supplierConfig)) {
            return null;
        }
        return JSONUtil.parseObj(supplierConfig).getStr("sdkAppId");
    }
}
