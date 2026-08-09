import { router } from '@/router'
import { useUserStore } from '@/store/modules/user'
import type { RouterJumpVo } from './types'

/**
 * 跳转到业务表单（办理/查看）。优先使用流程定义配置的业务表单路径，
 * 否则回落到内置的请假申请表单页。
 */
export function routerJump(vo: RouterJumpVo): void {
  const path = vo.formPath || '/workflow/leaveEdit/index'
  router.push({
    path,
    query: {
      id: vo.businessId,
      type: vo.type,
      taskId: vo.taskId,
      formCustom: vo.formCustom
    }
  })
}

/**
 * 构建 Warm-Flow 设计器 / 流程图 iframe 地址，自动附加鉴权参数。
 */
export function buildWarmFlowUrl(suffix: string, query: Record<string, string> = {}): string {
  const { VITE_API_URL, VITE_APP_CLIENT_ID } = import.meta.env
  const { accessToken } = useUserStore()
  const base = `${window.location.origin}${VITE_API_URL}/warm-flow-ui/${suffix}`
  const params = new URLSearchParams()
  Object.entries(query).forEach(([key, value]) => params.set(key, value))
  params.set('Authorization', 'Bearer ' + accessToken)
  params.set('clientid', VITE_APP_CLIENT_ID || '')
  return `${base}?${params.toString()}`
}
