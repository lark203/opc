package org.dromara.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.system.domain.SysOption;
import org.dromara.system.domain.bo.SysOptionBo;
import org.dromara.system.domain.vo.SysOptionVo;
import org.dromara.system.mapper.SysOptionMapper;
import org.dromara.system.service.ISysOptionService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统选项配置 服务层实现
 *
 * @author custom
 */
@RequiredArgsConstructor
@Service
public class SysOptionServiceImpl implements ISysOptionService {

    private final SysOptionMapper baseMapper;

    private static final String CATEGORY_CACHE_PREFIX = CacheNames.SYS_OPTION_CATEGORY + ":";

    private List<SysOptionVo> loadCategoryWithCache(String category) {
        String cacheKey = CATEGORY_CACHE_PREFIX + category;
        List<SysOptionVo> cached = RedisUtils.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }
        LambdaQueryWrapper<SysOption> lqw = new LambdaQueryWrapper<>();
        lqw.eq(category != null, SysOption::getCategory, category)
            .orderByAsc(SysOption::getOptionId);
        List<SysOptionVo> list = baseMapper.selectVoList(lqw);
        for (SysOptionVo vo : list) {
            if (vo.getValue() == null || vo.getValue().isBlank()) {
                vo.setValue(vo.getDefaultValue());
            }
        }
        RedisUtils.setCacheObject(cacheKey, list, Duration.ofMinutes(30));
        return list;
    }

    private void evictCategoryCache(String category) {
        // 删除某分类缓存；若传空则清除全部 sys_option 分类缓存
        if (category != null) {
            RedisUtils.deleteObject(CATEGORY_CACHE_PREFIX + category);
        } else {
            Collection<String> keys = RedisUtils.keys(CATEGORY_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                RedisUtils.deleteObject(keys);
            }
        }
    }

    @Override
    public List<SysOptionVo> selectOptionList(String category) {
        return loadCategoryWithCache(category);
    }

    @Override
    public String getOptionValue(String category, String code) {
        if (category == null || code == null) {
            return null;
        }
        List<SysOptionVo> list = loadCategoryWithCache(category);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Map<String, SysOptionVo> map = list.stream()
            .collect(Collectors.toMap(SysOptionVo::getCode, Function.identity(), (a, b) -> a));
        SysOptionVo vo = map.get(code);
        return vo == null ? null : vo.getValue();
    }

    @Override
    public Integer getOptionInt(String category, String code, Integer defaultVal) {
        String val = getOptionValue(category, code);
        if (val == null || val.isBlank()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    @Override
    public void saveOptionBatch(List<SysOptionBo> list) {
        String dirtyCategory = null;
        for (SysOptionBo bo : list) {
            if (bo.getOptionId() == null) {
                continue;
            }
            SysOption entity = new SysOption();
            entity.setOptionId(bo.getOptionId());
            entity.setValue(bo.getValue());
            baseMapper.updateById(entity);
            // 通过回查 DB 找到对应 category，用于后续清除缓存
            if (dirtyCategory == null) {
                SysOption db = baseMapper.selectById(bo.getOptionId());
                if (db != null) {
                    dirtyCategory = db.getCategory();
                }
            }
        }
        if (ObjectUtil.isAllNotEmpty(dirtyCategory)) {
            evictCategoryCache(dirtyCategory);
        } else {
            evictCategoryCache(null);
        }
    }

    @Override
    public void resetOption(String category) {
        List<SysOptionVo> list = selectOptionList(category);
        for (SysOptionVo vo : list) {
            SysOption entity = new SysOption();
            entity.setOptionId(vo.getOptionId());
            entity.setValue(vo.getDefaultValue());
            baseMapper.updateById(entity);
        }
        evictCategoryCache(category);
    }
}
