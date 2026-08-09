package org.dromara.system.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.SysOption;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统选项配置视图对象 sys_option
 *
 * @author custom
 */
@Data
@AutoMapper(target = SysOption.class)
public class SysOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    private Long optionId;

    /**
     * 分类
     */
    private String category;

    /**
     * 配置键名
     */
    private String code;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 配置值
     */
    private String value;

    /**
     * 默认值（value 为空时回退使用）
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
