package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存键视图对象
 *
 * @author JunoYi
 */
@Data
public class CacheKeyVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 键名
     */
    private String key;

    /**
     * 值类型（object/list/set/zset/map）
     */
    private String type;

    /**
     * 剩余存活时间（秒），-1 永不过期，-2 不存在
     */
    private Long ttl;

    /**
     * 内存占用（字节）
     */
    private Long memoryUsage;

    /**
     * 元素数量
     */
    private Long size;

}
