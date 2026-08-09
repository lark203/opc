/**
 * useAuth - 权限验证管理
 *
 * 提供统一的权限验证功能，用于页面按钮、操作等细粒度权限控制，
 * 与 RuoYi-Vue-Plus / plus-ui 的按钮权限模型保持一致。
 *
 * ## 权限模型
 *
 * - 权限码来自 /system/user/getInfo 返回的 permissions 数组（如 `system:user:add`）。
 * - 超级管理员拥有通配符 `*:*:*`，可绕过所有按钮权限校验。
 * - 角色来自 getInfo 返回的 roles 数组；超级角色 `admin` / `superadmin` 直接放行。
 * - 前后端模式下 permissions / roles 均来自后端 getInfo，因此本 hook 与模式无关。
 *
 * ## 使用示例
 *
 * ```typescript
 * const { hasPermi, hasPermiOr, hasPermiAnd, hasRole } = useAuth()
 *
 * // 单个权限码
 * if (hasPermi('system:user:add')) { ... }
 *
 * // 满足任意一个权限码
 * if (hasPermiOr(['system:user:edit', 'system:user:remove'])) { ... }
 *
 * // 需同时满足多个权限码
 * if (hasPermiAnd(['system:user:add', 'system:user:edit'])) { ... }
 *
 * // 角色校验
 * if (hasRole('admin')) { ... }
 *
 * // 在模板中使用
 * <el-button v-if="hasPermi('system:user:edit')">编辑</el-button>
 * <el-button v-auth="'system:user:add'">新增</el-button>
 * ```
 *
 * @module useAuth
 * @author 量子科技 Team
 */

import { useUserStore } from '@/store/modules/user'

/** 超级管理员通配符权限 */
const SUPER_PERMISSION = '*:*:*'
/** 超级角色，直接放行所有角色校验 */
const SUPER_ROLES = ['admin', 'superadmin']

/**
 * 校验是否拥有指定权限码（支持通配符）
 * @param permission 权限码
 * @param userPermissions 当前用户权限码集合
 */
const checkPermission = (permission: string, userPermissions: string[]): boolean => {
  if (!permission) return true
  if (userPermissions.includes(SUPER_PERMISSION)) return true
  return userPermissions.includes(permission)
}

/**
 * 校验是否拥有指定角色（超级角色直接放行）
 * @param role 角色
 * @param userRoles 当前用户角色集合
 */
const checkRole = (role: string, userRoles: string[]): boolean => {
  if (!role) return true
  if (userRoles.some((item) => SUPER_ROLES.includes(item))) return true
  return userRoles.includes(role)
}

export const useAuth = () => {
  const userStore = useUserStore()

  /**
   * 是否拥有指定权限码
   */
  const hasPermi = (permission: string): boolean => {
    return checkPermission(permission, userStore.info.permissions || [])
  }

  /**
   * 是否拥有其中任意一个权限码
   */
  const hasPermiOr = (permissions: string[]): boolean => {
    const userPermissions = userStore.info.permissions || []
    if (userPermissions.includes(SUPER_PERMISSION)) return true
    return permissions.some((permission) => userPermissions.includes(permission))
  }

  /**
   * 是否同时拥有所有权限码
   */
  const hasPermiAnd = (permissions: string[]): boolean => {
    const userPermissions = userStore.info.permissions || []
    if (userPermissions.includes(SUPER_PERMISSION)) return true
    return permissions.every((permission) => userPermissions.includes(permission))
  }

  /**
   * 是否拥有指定角色
   */
  const hasRole = (role: string): boolean => {
    return checkRole(role, userStore.info.roles || [])
  }

  /**
   * 是否拥有其中任意一个角色
   */
  const hasRoleOr = (roles: string[]): boolean => {
    const userRoles = userStore.info.roles || []
    if (userRoles.some((item) => SUPER_ROLES.includes(item))) return true
    return roles.some((role) => userRoles.includes(role))
  }

  /**
   * 是否同时拥有所有角色
   */
  const hasRoleAnd = (roles: string[]): boolean => {
    const userRoles = userStore.info.roles || []
    if (userRoles.some((item) => SUPER_ROLES.includes(item))) return true
    return roles.every((role) => userRoles.includes(role))
  }

  /**
   * 兼容别名：等价于 hasPermi，供 v-auth 指令与 art-button-more 组件调用
   */
  const hasAuth = (permission: string): boolean => hasPermi(permission)

  return {
    hasPermi,
    hasPermiOr,
    hasPermiAnd,
    hasRole,
    hasRoleOr,
    hasRoleAnd,
    hasAuth
  }
}
