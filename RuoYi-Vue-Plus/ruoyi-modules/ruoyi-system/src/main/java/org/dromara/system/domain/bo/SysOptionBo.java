package org.dromara.system.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.system.domain.SysOption;

import java.io.Serializable;

/**
 * 系统选项配置业务对象 sys_option
 *
 * @author custom
 */
@Data
@AutoMapper(target = SysOption.class, reverseConvertGenerate = false)
public class SysOptionBo implements Serializable {

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
}
