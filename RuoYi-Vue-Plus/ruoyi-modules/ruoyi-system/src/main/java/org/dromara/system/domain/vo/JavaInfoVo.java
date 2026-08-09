package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Java 运行环境信息视图对象
 *
 * @author JunoYi
 */
@Data
public class JavaInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Java 版本
     */
    private String version;

    /**
     * 供应商
     */
    private String vendor;

    /**
     * 安装路径
     */
    private String home;

    /**
     * JVM 名称
     */
    private String jvmName;

    /**
     * JVM 版本
     */
    private String jvmVersion;

    /**
     * 启动参数
     */
    private String args;

}
