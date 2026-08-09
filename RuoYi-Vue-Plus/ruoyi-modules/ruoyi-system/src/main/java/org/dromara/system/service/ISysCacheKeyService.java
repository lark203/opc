package org.dromara.system.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.bo.CacheKeyBo;
import org.dromara.system.domain.vo.CacheKeyDetailVo;
import org.dromara.system.domain.vo.CacheKeyVo;
import org.dromara.system.domain.vo.RedisInfoVo;

import java.util.List;

/**
 * 缓存键监控服务接口
 *
 * @author JunoYi
 */
public interface ISysCacheKeyService {

    /**
     * 获取 Redis 服务器信息。
     *
     * @return Redis 信息
     */
    RedisInfoVo getRedisInfo();

    /**
     * 分页查询缓存键列表。
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    PageResult<CacheKeyVo> getCacheKeyList(CacheKeyBo bo, PageQuery pageQuery);

    /**
     * 查询缓存键详情（含值内容）。
     *
     * @param key 键名
     * @return 缓存详情，键不存在时返回 null
     */
    CacheKeyDetailVo getCacheKeyDetail(String key);

    /**
     * 删除指定缓存键。
     *
     * @param key 键名
     * @return 是否删除成功
     */
    boolean deleteCacheKey(String key);

    /**
     * 批量删除缓存键。
     *
     * @param keys 键名列表
     */
    void deleteCacheBatch(List<String> keys);

    /**
     * 清空当前库的所有缓存。
     */
    void clearAllCache();

}
