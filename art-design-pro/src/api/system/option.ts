import request from '@/utils/http'

export interface SysOptionVO {
  optionId: number
  category: string
  code: string
  name: string
  value: string
  defaultValue?: string
  description?: string
  createTime?: string
}

/** 查询某分类选项配置 */
export function getOption(category: string) {
  return request.get<SysOptionVO[]>({ url: '/system/option', params: { category } })
}

/** 获取网站配置（公开接口，无需登录，用于登录页/页头展示，逐行返回 SITE 分类 code/value） */
export function getSiteOption() {
  return request.get<SysOptionVO[]>({ url: '/system/option/site' })
}

/** 批量保存选项配置 */
export function saveOption(data: { optionId: number; code: string; value: string }[]) {
  return request.put({ url: '/system/option', params: data })
}

/** 重置某分类选项为默认值 */
export function resetOption(category: string) {
  return request.post({ url: `/system/option/reset/${category}` })
}
