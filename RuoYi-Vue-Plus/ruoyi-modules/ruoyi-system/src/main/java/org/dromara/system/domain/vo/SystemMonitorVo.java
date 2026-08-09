package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 系统信息监控视图对象
 *
 * @author JunoYi
 */
@Data
public class SystemMonitorVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统信息
     */
    private SystemBasicInfoVo systemInfo;

    /**
     * 服务器信息
     */
    private ServerInfoVo serverInfo;

    /**
     * Java 信息
     */
    private JavaInfoVo javaInfo;

    /**
     * 内存信息
     */
    private MemoryInfoVo memoryInfo;

    /**
     * 磁盘信息列表
     */
    private List<DiskInfoVo> diskInfo;

}
