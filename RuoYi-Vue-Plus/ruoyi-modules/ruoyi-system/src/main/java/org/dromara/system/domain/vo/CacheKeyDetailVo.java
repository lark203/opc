package org.dromara.system.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 缓存键详情视图对象（含值内容）
 *
 * @author JunoYi
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CacheKeyDetailVo extends CacheKeyVo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 值内容
     */
    private Object value;

}
