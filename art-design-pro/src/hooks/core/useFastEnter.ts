/**
 * useFastEnter - 快速入口管理
 *
 * 管理顶部栏的快速入口功能，提供应用列表和快速链接的配置和过滤。
 * 支持动态启用/禁用、自定义排序、响应式宽度控制等功能。
 *
 * ## 主要功能
 *
 * 1. 应用列表管理 - 获取启用的应用列表，自动按排序权重排序
 * 2. 快速链接管理 - 获取启用的快速链接，支持自定义排序
 * 3. 响应式配置 - 所有配置自动响应变化，无需手动更新
 * 4. 宽度控制 - 提供最小显示宽度配置，支持响应式布局
 *
 * @module useFastEnter
 * @author 量子科技 Team
 */

import { computed } from 'vue'
import { router } from '@/router'
import appConfig from '@/config'
import type { FastEnterApplication, FastEnterQuickLink } from '@/types/config'

export function useFastEnter() {
  // 获取快速入口配置
  const fastEnterConfig = computed(() => appConfig.fastEnter)

  // 仅保留「路由真实存在」的条目。
  // 原因：后台模式（VITE_ACCESS_MODE=backend）下，菜单完全由 sys_menu 控制，
  // 仅在 router/modules 中定义、但后台未下发的演示路由（如 Fireworks/Chat/
  // Pricing/ArticleComment/ChangeLog/Analysis）实际未被注册，点击会 404。
  // 通过 router.hasRoute 过滤后：前端模式下全部保留，后台模式下自动隐藏无效项，
  // 无需为不同模式维护两套配置。
  const isRouteAvailable = (routeName?: string): boolean => {
    if (!routeName) return true // 无 routeName（纯外链）保留
    return router.hasRoute(routeName)
  }

  // 获取启用的应用列表（按排序权重排序，并剔除无效路由）
  const enabledApplications = computed<FastEnterApplication[]>(() => {
    if (!fastEnterConfig.value?.applications) return []

    return fastEnterConfig.value.applications
      .filter((app) => app.enabled !== false)
      .filter((app) => isRouteAvailable(app.routeName))
      .sort((a, b) => (a.order || 0) - (b.order || 0))
  })

  // 获取启用的快速链接（按排序权重排序，并剔除无效路由）
  const enabledQuickLinks = computed<FastEnterQuickLink[]>(() => {
    if (!fastEnterConfig.value?.quickLinks) return []

    return fastEnterConfig.value.quickLinks
      .filter((link) => link.enabled !== false)
      .filter((link) => isRouteAvailable(link.routeName))
      .sort((a, b) => (a.order || 0) - (b.order || 0))
  })

  // 获取最小显示宽度
  const minWidth = computed(() => {
    return fastEnterConfig.value?.minWidth || 1200
  })

  return {
    fastEnterConfig,
    enabledApplications,
    enabledQuickLinks,
    minWidth
  }
}
