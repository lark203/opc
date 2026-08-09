package org.dromara.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.dromara.system.domain.SysSmsConfig;
import org.dromara.system.domain.bo.SysSmsConfigBo;
import org.dromara.system.domain.vo.SysSmsConfigVo;
import org.dromara.system.mapper.SysSmsConfigMapper;
import org.dromara.system.service.ISysSmsConfigService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短信配置 服务层实现
 *
 * @author custom
 */
@RequiredArgsConstructor
@Service
public class SysSmsConfigServiceImpl implements ISysSmsConfigService {

    private final SysSmsConfigMapper baseMapper;
    private final ObjectProvider<SmsReadConfig> smsReadConfigProvider;

    @Override
    public PageResult<SysSmsConfigVo> selectPageList(SysSmsConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysSmsConfig> lqw = buildQueryWrapper(bo);
        Page<SysSmsConfigVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    private LambdaQueryWrapper<SysSmsConfig> buildQueryWrapper(SysSmsConfigBo bo) {
        return QueryBuilder.lambda(SysSmsConfig.class)
            .likeIfText(SysSmsConfig::getName, bo.getName())
            .eqIfText(SysSmsConfig::getSupplier, bo.getSupplier())
            .eqIfText(SysSmsConfig::getStatus, bo.getStatus())
            .orderByAsc(SysSmsConfig::getSort)
            .orderByAsc(SysSmsConfig::getSmsId)
            .build();
    }

    @Override
    public SysSmsConfigVo selectById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public void insert(SysSmsConfigBo bo) {
        SysSmsConfig entity = MapstructUtils.convert(bo, SysSmsConfig.class);
        if (entity.getSort() == null) {
            entity.setSort(999);
        }
        if (entity.getStatus() == null) {
            entity.setStatus("1");
        }
        if ("1".equals(entity.getIsDefault())) {
            baseMapper.lambda().set(SysSmsConfig::getIsDefault, "0").update();
        } else if (entity.getIsDefault() == null) {
            entity.setIsDefault("0");
        }
        baseMapper.insert(entity);
        if (entity.getConfigId() == null || entity.getConfigId().isEmpty()) {
            entity.setConfigId("sms_" + entity.getSmsId());
            baseMapper.updateById(entity);
        }
        reloadSmsBlend();
    }

    @Override
    public void update(SysSmsConfigBo bo) {
        if ("1".equals(bo.getIsDefault())) {
            baseMapper.lambda().set(SysSmsConfig::getIsDefault, "0").update();
        }
        SysSmsConfig entity = MapstructUtils.convert(bo, SysSmsConfig.class);
        if (entity.getConfigId() == null || entity.getConfigId().isEmpty()) {
            SysSmsConfig old = baseMapper.selectById(entity.getSmsId());
            entity.setConfigId(old != null && old.getConfigId() != null && !old.getConfigId().isEmpty()
                ? old.getConfigId() : "sms_" + entity.getSmsId());
        }
        baseMapper.updateById(entity);
        reloadSmsBlend();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        baseMapper.deleteByIds(ids);
        reloadSmsBlend();
    }

    @Override
    public void setDefault(Long id) {
        baseMapper.lambda().set(SysSmsConfig::getIsDefault, "0").update();
        SysSmsConfig entity = new SysSmsConfig();
        entity.setSmsId(id);
        entity.setIsDefault("1");
        baseMapper.updateById(entity);
        reloadSmsBlend();
    }

    @Override
    public void changeStatus(Long id, String status) {
        SysSmsConfig entity = new SysSmsConfig();
        entity.setSmsId(id);
        entity.setStatus(status);
        baseMapper.updateById(entity);
        reloadSmsBlend();
    }

    /**
     * 配置变更后重新加载 sms4j 生效配置（仅注册默认且启用的配置）。
     */
    private void reloadSmsBlend() {
        smsReadConfigProvider.ifAvailable(SmsFactory::reloadAll);
    }
}
