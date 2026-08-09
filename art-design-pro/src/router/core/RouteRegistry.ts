/**
 * 路由注册核心类
 *
 * 负责动态路由的注册、验证和管理
 *
 * @module router/core/RouteRegistry
 * @author 量子科技 Team
 */

import type { Router, RouteRecordRaw } from 'vue-router'
import type { AppRouteRecord } from '@/types/router'
import { ComponentLoader } from './ComponentLoader'
import { RouteValidator } from './RouteValidator'
import { RouteTransformer } from './RouteTransformer'

export class RouteRegistry {
  private router: Router
  private componentLoader: ComponentLoader
  private validator: RouteValidator
  private transformer: RouteTransformer
  private removeRouteFns: (() => void)[] = []
  private registered = false

  constructor(router: Router) {
    this.router = router
    this.componentLoader = new ComponentLoader()
    this.validator = new RouteValidator()
    this.transformer = new RouteTransformer(this.componentLoader)
  }

  /**
   * 注册动态路由
   */
  register(menuList: AppRouteRecord[]): void {
    if (this.registered) {
      console.warn('[RouteRegistry] 路由已注册，跳过重复注册')
      return
    }

    // 验证路由配置
    const validationResult = this.validator.validate(menuList)
    if (!validationResult.valid) {
      throw new Error(`路由配置验证失败: ${validationResult.errors.join(', ')}`)
    }

    // 转换并注册路由
    const removeRouteFns: (() => void)[] = []

    menuList.forEach((route) => {
      if (route.name && !this.router.hasRoute(route.name)) {
        const routeConfig = this.transformer.transform(route)
        const removeRouteFn = this.router.addRoute(routeConfig as RouteRecordRaw)
        removeRouteFns.push(removeRouteFn)
      }
    })

    this.removeRouteFns = removeRouteFns

    this.addNotFoundRoute()

    this.registered = true
  }

  /**
   * 添加 404 兜底路由
   * 在动态路由注册完成后追加，避免抢先匹配导致首次导航异常
   */
  private addNotFoundRoute(): void {
    if (this.router.hasRoute('Exception404')) {
      return
    }

    const removeRouteFn = this.router.addRoute({
      path: '/:pathMatch(.*)*',
      name: 'Exception404',
      component: () => import('@views/exception/404/index.vue'),
      meta: { title: '404', isHideTab: true }
    })

    this.removeRouteFns.push(removeRouteFn)
  }

  /**
   * 移除所有动态路由
   */
  unregister(): void {
    this.removeRouteFns.forEach((fn) => fn())
    this.removeRouteFns = []
    this.registered = false
  }

  /**
   * 检查是否已注册
   */
  isRegistered(): boolean {
    return this.registered
  }

  /**
   * 获取移除函数列表（用于 store 管理）
   */
  getRemoveRouteFns(): (() => void)[] {
    return this.removeRouteFns
  }

  /**
   * 标记为已注册（用于错误处理场景，避免重复请求）
   */
  markAsRegistered(): void {
    this.registered = true
  }
}
