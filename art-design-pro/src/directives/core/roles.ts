/**
 * v-roles 角色权限指令
 *
 * 基于用户角色控制 DOM 元素的显示与隐藏，与 RuoYi-Vue-Plus / plus-ui 的角色权限模型一致。
 * 只要用户拥有指定角色中的任意一个，元素就会显示，否则从 DOM 中移除。
 *
 * ## 角色模型
 *
 * - 角色来自 /system/user/getInfo 返回的 roles 数组。
 * - 超级角色 `admin` / `superadmin` 拥有所有角色权限，直接放行（对齐 plus-ui hasRoles）。
 * - 兼容项目自定义角色（如 R_SUPER / R_ADMIN / R_USER）。
 *
 * ## 使用示例
 *
 * ```vue
 * <!-- 单个角色 - 只有超级管理员可见 -->
 * <el-button v-roles="'R_SUPER'">超级管理员功能</el-button>
 *
 * <!-- 多个角色 - 满足其一即可 -->
 * <el-button v-roles="['R_SUPER', 'R_ADMIN']">管理员功能</el-button>
 * ```
 *
 * ## 注意事项
 *
 * - 该指令会直接移除 DOM 元素，而非使用 v-if 隐藏。
 * - 适用于基于角色的粗粒度权限控制；细粒度操作权限请使用 v-auth 指令。
 *
 * @module directives/roles
 * @author 量子科技 Team
 */

import { useUserStore } from '@/store/modules/user'
import { App, Directive, DirectiveBinding } from 'vue'

/** 超级角色，直接放行所有角色校验 */
export const SUPER_ROLES = ['admin', 'superadmin']

/** 指令绑定值：单个角色或角色数组 */
export type RolesDirectiveValue = string | string[]

export type RolesDirective = Directive<HTMLElement, RolesDirectiveValue>

/**
 * 将绑定值规范为角色数组
 */
function normalizeRoles(value: RolesDirectiveValue): string[] {
  if (!value) return []
  return Array.isArray(value) ? value : [value]
}

function removeElement(el: HTMLElement): void {
  if (el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

function checkRolePermission(
  el: HTMLElement,
  binding: DirectiveBinding<RolesDirectiveValue>
): void {
  const userStore = useUserStore()
  const userRoles = userStore.info.roles || []

  // 用户无角色或未登录，移除元素
  if (!userRoles.length) {
    removeElement(el)
    return
  }

  // 超级角色直接放行
  if (userRoles.some((role) => SUPER_ROLES.includes(role))) {
    return
  }

  const requiredRoles = normalizeRoles(binding.value)
  const hasPermission = requiredRoles.some((role) => userRoles.includes(role))

  if (!hasPermission) {
    removeElement(el)
  }
}

const rolesDirective: RolesDirective = {
  mounted: checkRolePermission,
  updated: checkRolePermission
}

export function setupRolesDirective(app: App): void {
  app.directive('roles', rolesDirective)
}
