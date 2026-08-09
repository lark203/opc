package org.dromara.system.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存键查询业务对象
 *
 * @author JunoYi
 */
@Data
public class CacheKeyBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 键名匹配模式，支持通配符，如 user:*
     */
    private String pattern;

    /**
     * 值类型（object/list/set/zset/map）
     */
    private String type;

}
