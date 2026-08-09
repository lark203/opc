package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 内存信息视图对象
 *
 * @author JunoYi
 */
@Data
public class MemoryInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 运行时内存总量
     */
    private String total;

    /**
     * 运行时已用内存
     */
    private String used;

    /**
     * 运行时空闲内存
     */
    private String free;

    /**
     * 运行时内存使用率(%)
     */
    private Integer usedPercent;

    /**
     * JVM 堆内存上限
     */
    private String jvmTotal;

    /**
     * JVM 堆内存已用
     */
    private String jvmUsed;

    /**
     * JVM 堆内存空闲
     */
    private String jvmFree;

    /**
     * JVM 堆内存使用率(%)
     */
    private Integer jvmUsedPercent;

}
