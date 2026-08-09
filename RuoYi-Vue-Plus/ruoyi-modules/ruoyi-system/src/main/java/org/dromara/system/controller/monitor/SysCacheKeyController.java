package org.dromara.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.CacheKeyBo;
import org.dromara.system.domain.vo.CacheKeyDetailVo;
import org.dromara.system.domain.vo.CacheKeyVo;
import org.dromara.system.domain.vo.RedisInfoVo;
import org.dromara.system.service.ISysCacheKeyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 缓存键监控
 *
 * @author JunoYi
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/cachekey")
public class SysCacheKeyController extends BaseController {

    private final ISysCacheKeyService cacheKeyService;

    /**
     * 获取 Redis 服务器概览信息。
     *
     * @return Redis 信息
     */
    @SaCheckPermission("monitor:cachekey:list")
    @GetMapping("/info")
    public R<RedisInfoVo> getInfo() {
        return R.ok(cacheKeyService.getRedisInfo());
    }

    /**
     * 分页查询缓存键列表。
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 缓存键分页数据
     */
    @SaCheckPermission("monitor:cachekey:list")
    @GetMapping("/keys")
    public R<PageResult<CacheKeyVo>> keys(CacheKeyBo bo, PageQuery pageQuery) {
        return R.ok(cacheKeyService.getCacheKeyList(bo, pageQuery));
    }

    /**
     * 查询指定缓存键的详情（含值内容）。
     *
     * @param key 键名
     * @return 缓存键详情
     */
    @SaCheckPermission("monitor:cachekey:query")
    @GetMapping("/key")
    public R<CacheKeyDetailVo> getKeyDetail(@NotBlank(message = "键名不能为空") String key) {
        CacheKeyDetailVo detail = cacheKeyService.getCacheKeyDetail(key);
        if (detail == null) {
            return R.fail("缓存键不存在或已过期");
        }
        return R.ok(detail);
    }

    /**
     * 删除指定缓存键。
     *
     * @param key 键名
     * @return 操作结果
     */
    @SaCheckPermission("monitor:cachekey:remove")
    @Log(title = "缓存键监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/key")
    public R<Void> removeKey(@NotBlank(message = "键名不能为空") String key) {
        return toAjax(cacheKeyService.deleteCacheKey(key));
    }

    /**
     * 批量删除缓存键。
     *
     * @param keys 键名列表
     * @return 操作结果
     */
    @SaCheckPermission("monitor:cachekey:remove")
    @Log(title = "缓存键监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public R<Void> removeBatch(@RequestBody @NotEmpty(message = "待删除的键名不能为空") List<String> keys) {
        cacheKeyService.deleteCacheBatch(keys);
        return R.ok();
    }

    /**
     * 清空当前库的所有缓存。
     *
     * @return 操作结果
     */
    @SaCheckPermission("monitor:cachekey:clear")
    @Log(title = "缓存键监控", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clear")
    public R<Void> clear() {
        cacheKeyService.clearAllCache();
        return R.ok();
    }

}
