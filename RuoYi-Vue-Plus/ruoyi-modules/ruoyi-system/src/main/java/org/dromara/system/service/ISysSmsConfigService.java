package org.dromara.system.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.system.domain.bo.SysSmsConfigBo;
import org.dromara.system.domain.vo.SysSmsConfigVo;

import java.util.List;

/**
 * 短信配置 服务层
 *
 * @author custom
 */
public interface ISysSmsConfigService {

    /**
     * 分页查询短信配置列表
     */
    PageResult<SysSmsConfigVo> selectPageList(SysSmsConfigBo bo, PageQuery pageQuery);

    /**
     * 查询短信配置详情
     */
    SysSmsConfigVo selectById(Long id);

    /**
     * 新增短信配置
     */
    void insert(SysSmsConfigBo bo);

    /**
     * 修改短信配置
     */
    void update(SysSmsConfigBo bo);

    /**
     * 批量删除短信配置
     */
    void deleteByIds(List<Long> ids);

    /**
     * 设为默认
     */
    void setDefault(Long id);

    /**
     * 修改状态并刷新 sms4j 生效配置
     *
     * @param id     配置ID
     * @param status 状态（1正常 2停用）
     */
    void changeStatus(Long id, String status);
}
