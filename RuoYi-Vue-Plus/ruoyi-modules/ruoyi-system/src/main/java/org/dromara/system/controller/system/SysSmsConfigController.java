package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.SysSmsConfigBo;
import org.dromara.system.domain.vo.SysSmsConfigVo;
import org.dromara.system.service.ISysSmsConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 短信配置 信息操作处理
 *
 * @author custom
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sms/config")
public class SysSmsConfigController extends BaseController {

    private final ISysSmsConfigService smsConfigService;

    /**
     * 分页查询短信配置列表
     */
    @SaCheckPermission("system:sms:list")
    @GetMapping("/list")
    public R<PageResult<SysSmsConfigVo>> list(SysSmsConfigBo bo, PageQuery pageQuery) {
        return R.ok(smsConfigService.selectPageList(bo, pageQuery));
    }

    /**
     * 查询短信配置详情
     */
    @SaCheckPermission("system:sms:query")
    @GetMapping("/{id}")
    public R<SysSmsConfigVo> getInfo(@PathVariable Long id) {
        return R.ok(smsConfigService.selectById(id));
    }

    /**
     * 新增短信配置
     */
    @SaCheckPermission("system:sms:add")
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysSmsConfigBo bo) {
        smsConfigService.insert(bo);
        return R.ok();
    }

    /**
     * 修改短信配置
     */
    @SaCheckPermission("system:sms:edit")
    @PutMapping("/{id}")
    public R<Void> edit(@Validated @RequestBody SysSmsConfigBo bo, @PathVariable Long id) {
        bo.setSmsId(id);
        smsConfigService.update(bo);
        return R.ok();
    }

    /**
     * 删除短信配置
     */
    @SaCheckPermission("system:sms:del")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        smsConfigService.deleteByIds(Arrays.asList(ids));
        return R.ok();
    }

    /**
     * 设为默认
     */
    @SaCheckPermission("system:sms:edit")
    @PutMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        smsConfigService.setDefault(id);
        return R.ok();
    }

    /**
     * 修改短信配置状态
     */
    @SaCheckPermission("system:sms:edit")
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysSmsConfigBo bo) {
        smsConfigService.changeStatus(bo.getSmsId(), bo.getStatus());
        return R.ok();
    }
}
