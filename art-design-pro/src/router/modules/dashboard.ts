import { AppRouteRecord } from '@/types/router'

const consoleRoute: AppRouteRecord = {
  path: 'console',
  name: 'Console',
  component: '/dashboard/console',
  meta: {
    title: '工作台',
    icon: 'ri:home-smile-2-line',
    keepAlive: false,
    fixedTab: true
  }
}

/**
 * backend 模式下的仪表盘兜底路由
 *
 * sys_menu 中没有仪表盘，需要一个落地页供登录后跳转。
 * 仅注入工作台，分析页/电子商务属于演示页面，不应对所有角色强制可见。
 */
export const dashboardFallbackRoutes: AppRouteRecord = {
  name: 'Dashboard',
  path: '/dashboard',
  component: '/index/index',
  meta: {
    title: '仪表盘',
    icon: 'ri:pie-chart-line'
  },
  children: [consoleRoute]
}

export const dashboardRoutes: AppRouteRecord = {
  name: 'Dashboard',
  path: '/dashboard',
  component: '/index/index',
  meta: {
    title: '仪表盘',
    icon: 'ri:pie-chart-line'
  },
  children: [
    consoleRoute,
    {
      path: 'analysis',
      name: 'Analysis',
      component: '/dashboard/analysis',
      meta: {
        title: '分析页',
        icon: 'ri:align-item-bottom-line',
        keepAlive: false
      }
    },
    {
      path: 'ecommerce',
      name: 'Ecommerce',
      component: '/dashboard/ecommerce',
      meta: {
        title: '电子商务',
        icon: 'ri:bar-chart-box-line',
        keepAlive: false
      }
    }
  ]
}
