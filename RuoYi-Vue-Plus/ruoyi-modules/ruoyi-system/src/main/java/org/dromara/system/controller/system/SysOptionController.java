package org.dromara.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.system.domain.bo.SysOptionBo;
import org.dromara.system.domain.vo.SysOptionVo;
import org.dromara.system.service.ISysOptionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统选项配置 信息操作处理
 *
 * @author custom
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/option")
public class SysOptionController extends BaseController {

    private final ISysOptionService optionService;

    /**
     * 查询选项配置列表
     *
     * @param category 分类（SITE/PASSWORD/LOGIN/MAIL）
     * @return 选项配置列表
     */
    @SaCheckPermission("system:option:list")
    @GetMapping
    public R<List<SysOptionVo>> list(@RequestParam(required = false) String category) {
        return R.ok(optionService.selectOptionList(category));
    }

    /**
     * 获取网站配置（公开接口，无需登录）
     * <p>用于登录页、系统页头等处展示网站 Logo、名称、描述、备案号等，value 为空时回退 default_value。</p>
     *
     * @return 网站配置选项（SITE 分类）
     */
    @SaIgnore
    @GetMapping("/site")
    public R<List<SysOptionVo>> siteOptions() {
        return R.ok(optionService.selectOptionList("SITE"));
    }

    /**
     * 批量保存选项配置
     *
     * @param list 选项配置集合（含 id、code、value）
     * @return 操作结果
     */
    @SaCheckPermission("system:option:edit")
    @PutMapping
    public R<Void> save(@Validated @RequestBody List<SysOptionBo> list) {
        optionService.saveOptionBatch(list);
        return R.ok();
    }

    /**
     * 重置某分类选项为默认值
     *
     * @param category 分类
     * @return 操作结果
     */
    @SaCheckPermission("system:option:edit")
    @PostMapping("/reset/{category}")
    public R<Void> reset(@PathVariable String category) {
        optionService.resetOption(category);
        return R.ok();
    }
}
