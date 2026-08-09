package org.dromara.system.service;

import org.dromara.system.domain.bo.SysOptionBo;
import org.dromara.system.domain.vo.SysOptionVo;

import java.util.List;

/**
 * 系统选项配置 服务层
 *
 * @author custom
 */
public interface ISysOptionService {

    /**
     * 查询选项配置列表
     *
     * @param category 分类（SITE/PASSWORD/LOGIN/MAIL），为空则返回全部
     * @return 选项配置集合
     */
    List<SysOptionVo> selectOptionList(String category);

    /**
     * 根据分类和配置键名获取配置值（优先value，为空则回退defaultValue）
     *
     * @param category 分类
     * @param code     配置键名
     * @return 配置值，未找到返回 null
     */
    String getOptionValue(String category, String code);

    /**
     * 根据分类和配置键名获取配置值，返回整数
     *
     * @param category   分类
     * @param code       配置键名
     * @param defaultVal 找不到或解析失败时返回的默认值
     * @return 配置值（整数）
     */
    Integer getOptionInt(String category, String code, Integer defaultVal);

    /**
     * 批量保存选项配置（按 ID 更新值）
     *
     * @param list 选项配置集合
     */
    void saveOptionBatch(List<SysOptionBo> list);

    /**
     * 重置某分类选项为默认值
     *
     * @param category 分类
     */
    void resetOption(String category);
}
