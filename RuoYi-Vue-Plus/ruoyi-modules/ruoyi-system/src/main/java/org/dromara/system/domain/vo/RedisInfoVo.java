package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Redis 服务器信息视图对象
 *
 * @author JunoYi
 */
@Data
public class RedisInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Redis 版本
     */
    private String version;

    /**
     * 运行模式
     */
    private String mode;

    /**
     * 运行时间（秒）
     */
    private Long uptimeInSeconds;

    /**
     * 已连接客户端数
     */
    private Integer connectedClients;

    /**
     * 已使用内存（字节）
     */
    private String usedMemory;

    /**
     * 已使用内存（可读格式）
     */
    private String usedMemoryHuman;

    /**
     * 内存峰值（字节）
     */
    private String usedMemoryPeak;

    /**
     * 内存峰值（可读格式）
     */
    private String usedMemoryPeakHuman;

    /**
     * 当前库键数量
     */
    private Long dbSize;

    /**
     * 命中次数
     */
    private Long keyspaceHits;

    /**
     * 未命中次数
     */
    private Long keyspaceMisses;

    /**
     * 命中率
     */
    private String hitRate;

    /**
     * 每秒执行命令数
     */
    private Long instantaneousOpsPerSec;

    /**
     * 网络入口流量（可读格式）
     */
    private String totalNetInputBytes;

    /**
     * 网络出口流量（可读格式）
     */
    private String totalNetOutputBytes;

}
