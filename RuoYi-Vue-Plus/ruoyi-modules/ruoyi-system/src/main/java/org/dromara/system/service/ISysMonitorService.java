package org.dromara.system.service;

import org.dromara.system.domain.vo.SystemMonitorVo;

/**
 * 系统信息监控服务接口
 *
 * @author JunoYi
 */
public interface ISysMonitorService {

    /**
     * 获取系统监控信息（系统、服务器、Java、内存、磁盘）。
     *
     * @return 系统监控信息
     */
    SystemMonitorVo getSystemMonitorInfo();

}
