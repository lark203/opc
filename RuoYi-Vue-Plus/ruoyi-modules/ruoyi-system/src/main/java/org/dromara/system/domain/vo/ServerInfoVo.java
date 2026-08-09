package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 服务器信息视图对象
 *
 * @author JunoYi
 */
@Data
public class ServerInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主机名称
     */
    private String name;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 系统架构
     */
    private String arch;

    /**
     * CPU 核心数
     */
    private Integer cpuCores;

    /**
     * 服务器 IP
     */
    private String ip;

    /**
     * 服务器当前时间
     */
    private String time;

}
