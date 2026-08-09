/**
 * useCommon - 通用功能集合
 *
 * 提供常用的页面操作功能，包括页面刷新、滚动控制、路径获取等。
 * 这些功能在多个页面和组件中都会用到，统一封装便于复用。
 *
 * ## 主要功能
 *
 * 1. 首页路径 - 获取系统配置的首页路径
 * 2. 页面刷新 - 刷新当前页面内容
 * 3. 滚动控制 - 提供多种滚动到顶部和指定位置的方法
 * 4. 平滑滚动 - 支持平滑滚动动画效果
 *
 * @module useCommon
 * @author 量子科技 Team
 */

import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/store/modules/menu'
import { useSettingStore } from '@/store/modules/setting'
import { useUserStore } from '@/store/modules/user'
import { getRouteInitFailed, resetRouteInitState } from '@/router/guards/beforeEach'

export function useCommon() {
  const router = useRouter()
  const menuStore = useMenuStore()
  const settingStore = useSettingStore()
  const userStore = useUserStore()

  /**
   * 首页路径
   * 从菜单 store 中获取配置的首页路径
   */
  const homePath = computed(() => menuStore.getHomePath())

  /**
   * 返回首页
   *
   * 兼容路由初始化失败（如接口 500 导致菜单拉取失败）的场景：
   * 此时 homePath 为空且 routeInitFailed 为 sticky 状态，直接跳转 “/” 会因无匹配
   * 路由被守卫再次导向 500 页，使“返回首页”按钮如同失效。这里在跳转前重置初始化
   * 状态，让守卫重新拉取菜单并定位到首页，按钮即可正常返回。
   */
  const goHome = () => {
    const target = menuStore.getHomePath() || '/'

    if (!userStore.isLogin) {
      router.push({ name: 'Login', query: { redirect: target } })
      return
    }

    if (getRouteInitFailed()) {
      resetRouteInitState()
    }

    router.push(target)
  }

  /**
   * 刷新当前页面
   * 通过切换 setting store 中的 refresh 状态触发页面重新渲染
   */
  const refresh = () => {
    settingStore.reload()
  }

  /**
   * 滚动到页面顶部
   * 查找主内容区域并将其滚动位置重置为顶部
   */
  const scrollToTop = () => {
    const scrollContainer = document.getElementById('app-main')
    if (scrollContainer) {
      scrollContainer.scrollTop = 0
    }
  }

  /**
   * 平滑滚动到页面顶部
   * 使用 smooth 行为实现平滑滚动效果
   */
  const smoothScrollToTop = () => {
    const scrollContainer = document.getElementById('app-main')
    if (scrollContainer) {
      scrollContainer.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    }
  }

  /**
   * 滚动到指定位置
   * @param top 目标滚动位置（像素）
   * @param smooth 是否使用平滑滚动
   */
  const scrollTo = (top: number, smooth: boolean = false) => {
    const scrollContainer = document.getElementById('app-main')
    if (scrollContainer) {
      scrollContainer.scrollTo({
        top,
        behavior: smooth ? 'smooth' : 'auto'
      })
    }
  }

  return {
    homePath,
    refresh,
    scrollTo,
    scrollToTop,
    smoothScrollToTop,
    goHome
  }
}
