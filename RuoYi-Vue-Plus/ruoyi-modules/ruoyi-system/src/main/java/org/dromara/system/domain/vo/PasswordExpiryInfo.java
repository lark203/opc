package org.dromara.system.domain.vo;

import java.io.Serializable;

/**
 * 密码过期状态，用于登录后向前端返回强制改密或到期提醒信息。
 *
 * @param expired        是否已过期（前端需强制改密）
 * @param expiringSoon   是否即将过期（前端提示，未过期且在提醒天数内）
 * @param daysLeft       剩余有效天数（已过期为负数；未启用密码过期策略为 -1）
 *
 * @author custom
 */
public record PasswordExpiryInfo(boolean expired, boolean expiringSoon, long daysLeft) implements Serializable {
}
