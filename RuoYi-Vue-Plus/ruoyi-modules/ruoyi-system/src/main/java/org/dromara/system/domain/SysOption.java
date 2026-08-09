package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 系统选项配置表 sys_option
 *
 * @author custom
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_option")
public class SysOption extends BaseEntity {

    /**
     * 配置ID
     */
    @TableId(value = "option_id")
    private Long optionId;

    /**
     * 分类（SITE/PASSWORD/LOGIN/MAIL）
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
    @TableField("default_value")
    private String defaultValue;

    /**
     * 描述
     */
    private String description;
}
