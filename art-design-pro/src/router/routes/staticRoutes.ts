import { AppRouteRecordRaw } from '@/utils/router'

/**
 * 静态路由配置（始终注册、不依赖后台菜单的路由）
 *
 * 属性说明：
 * isHideTab: true 表示不在标签页中显示
 *
 * 注意事项：
 * 1、path、name 不要和动态/后台路由冲突，否则会导致路由冲突无法访问
 * 2、静态路由「始终注册」，但并不都免登录。是否免登录由路由前置守卫中的
 *    ANONYMOUS_ROUTE_NAMES 白名单决定（目前仅 Login/Register/ForgetPassword/
 *    Exception403/Exception500）。其余静态路由（如 /outside/iframe/*、/system/*）
 *    仍需登录后才能访问，避免未授权用户绕过后台菜单直接打开页面。
 */
export const staticRoutes: AppRouteRecordRaw[] = [
  // 不需要登录就能访问的路由示例
  // {
  //   path: '/welcome',
  //   name: 'WelcomeStatic',
  //   component: () => import('@views/dashboard/console/index.vue'),
  //   meta: { title: 'menus.dashboard.title' }
  // },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@views/auth/login/index.vue'),
    meta: { title: 'menus.login.title', isHideTab: true }
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('@views/auth/register/index.vue'),
    meta: { title: 'menus.register.title', isHideTab: true }
  },
  {
    path: '/auth/forget-password',
    name: 'ForgetPassword',
    component: () => import('@views/auth/forget-password/index.vue'),
    meta: { title: 'menus.forgetPassword.title', isHideTab: true }
  },
  {
    path: '/403',
    name: 'Exception403',
    component: () => import('@views/exception/403/index.vue'),
    meta: { title: '403', isHideTab: true }
  },

  {
    path: '/500',
    name: 'Exception500',
    component: () => import('@views/exception/500/index.vue'),
    meta: { title: '500', isHideTab: true }
  },
  {
    path: '/outside',
    component: () => import('@views/index/index.vue'),
    name: 'Outside',
    meta: { title: 'menus.outside.title' },
    children: [
      {
        path: '/outside/iframe/:path',
        name: 'Iframe',
        component: () => import('@/views/outside/Iframe.vue'),
        meta: { title: 'iframe' }
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@views/index/index.vue'),
    name: 'System',
    meta: { title: '系统管理', isHide: true },
    children: [
      {
        path: 'user-center',
        name: 'UserCenter',
        component: () => import('@views/system/user-center/index.vue'),
        meta: { title: '个人中心', icon: 'ri:user-line', isHide: true, isHideTab: true }
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@views/system/message/index.vue'),
        meta: { title: '消息中心', icon: 'ri:message-line', isHide: true }
      }
    ]
  }
]
