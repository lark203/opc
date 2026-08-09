package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统基本信息视图对象
 *
 * @author JunoYi
 */
@Data
public class SystemBasicInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统名称
     */
    private String name;

    /**
     * 系统版本
     */
    private String version;

    /**
     * 框架版本
     */
    private String frameworkVersion;

    /**
     * 运行环境
     */
    private String environment;

    /**
     * 启动时间
     */
    private String startTime;

    /**
     * 运行时长
     */
    private String uptime;

}
