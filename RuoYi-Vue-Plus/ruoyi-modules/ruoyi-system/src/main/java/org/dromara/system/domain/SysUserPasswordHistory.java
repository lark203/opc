package org.dromara.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 用户密码历史记录 sys_user_password_history
 *
 * @author custom
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_password_history")
public class SysUserPasswordHistory extends BaseEntity {

    /**
     * 历史ID
     */
    @TableId(value = "history_id")
    private Long historyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 密码哈希（BCrypt）
     */
    private String password;
}
