package org.dromara.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.system.domain.vo.SystemMonitorVo;
import org.dromara.system.service.ISysMonitorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统信息监控
 *
 * @author JunoYi
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/sysinfo")
public class SysInfoController {

    private final ISysMonitorService sysMonitorService;

    /**
     * 获取系统信息（系统、服务器、Java、内存、磁盘）。
     *
     * @return 系统监控信息
     */
    @SaCheckPermission("monitor:sysinfo:list")
    @GetMapping()
    public R<SystemMonitorVo> getInfo() {
        return R.ok(sysMonitorService.getSystemMonitorInfo());
    }

}
