package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 磁盘信息视图对象
 *
 * @author JunoYi
 */
@Data
public class DiskInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 盘符路径
     */
    private String path;

    /**
     * 文件系统类型
     */
    private String type;

    /**
     * 总容量
     */
    private String total;

    /**
     * 已用容量
     */
    private String used;

    /**
     * 可用容量
     */
    private String free;

    /**
     * 使用率(%)
     */
    private Integer usedPercent;

}
