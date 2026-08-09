/**
 * v-auth 权限指令
 *
 * 基于后端返回的权限码集合（userStore.info.permissions）控制 DOM 元素的显示与隐藏，
 * 与 RuoYi-Vue-Plus / plus-ui 的按钮权限模型保持一致。
 * 若用户没有对应权限，元素将从 DOM 中移除。
 *
 * ## 权限模型
 *
 * - 权限码来自 /system/user/getInfo 返回的 permissions 数组，格式为 `module:func:action`，
 *   例如 `system:user:add`。
 * - 超级管理员拥有通配符 `*:*:*`，可绕过所有按钮权限校验。
 * - 权限码由后端统一管控，前端不做硬编码判断。
 *
 * ## 使用示例
 *
 * ```vue
 * <!-- 拥有 system:user:add 权限才显示新增按钮 -->
 * <el-button v-auth="'system:user:add'">新增</el-button>
 *
 * <!-- 拥有其中任意一个权限即显示（数组写法） -->
 * <el-button v-auth="['system:user:edit', 'system:user:remove']">编辑/删除</el-button>
 * ```
 *
 * ## 注意事项
 *
 * - 该指令会直接移除 DOM 元素，而非使用 v-if 隐藏。
 * - 权限校验源为 userStore.info.permissions，前后端模式下均可用（均来自后端 getInfo）。
 *
 * @module directives/auth
 * @author 量子科技 Team
 */

import { useUserStore } from '@/store/modules/user'
import { App, Directive, DirectiveBinding } from 'vue'

/** 超级管理员通配符权限 */
export const SUPER_PERMISSION = '*:*:*'

/** 指令绑定值：单个权限码或权限码数组 */
export type AuthDirectiveValue = string | string[]

export type AuthDirective = Directive<HTMLElement, AuthDirectiveValue>

/**
 * 将绑定值规范为权限码数组
 */
function normalizePermissions(value: AuthDirectiveValue): string[] {
  if (!value) return []
  return Array.isArray(value) ? value : [value]
}

/**
 * 校验是否拥有至少一个权限码
 * @param permissions 待校验的权限码集合
 * @param userPermissions 当前用户拥有的权限码集合
 */
function hasPermission(permissions: string[], userPermissions: string[]): boolean {
  if (!permissions.length) return true
  if (userPermissions.includes(SUPER_PERMISSION)) return true
  return permissions.some((permission) => userPermissions.includes(permission))
}

function removeElement(el: HTMLElement): void {
  if (el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

function checkAuthPermission(el: HTMLElement, binding: DirectiveBinding<AuthDirectiveValue>): void {
  const userStore = useUserStore()
  const userPermissions = userStore.info.permissions || []
  const requiredPermissions = normalizePermissions(binding.value)

  if (!hasPermission(requiredPermissions, userPermissions)) {
    removeElement(el)
  }
}

const authDirective: AuthDirective = {
  mounted: checkAuthPermission,
  updated: checkAuthPermission
}

export function setupAuthDirective(app: App): void {
  app.directive('auth', authDirective)
}
