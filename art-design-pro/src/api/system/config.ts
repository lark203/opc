import request from '@/utils/http'

export interface ConfigVO {
  configId: string | number
  configName: string
  configKey: string
  configValue: string
  configType: string
  remark?: string
  createTime?: string
}

export interface ConfigForm {
  configId?: string | number
  configName: string
  configKey: string
  configValue: string
  configType: string
  remark?: string
}

export interface ConfigQuery {
  configName?: string
  configKey?: string
  configType?: string
  pageNum?: number
  pageSize?: number
}

export function listConfig(query: ConfigQuery) {
  return request.get<{ rows: ConfigVO[]; total: number }>({
    url: '/system/config/list',
    params: query
  })
}

export function getConfig(configId: string | number) {
  return request.get<ConfigForm>({
    url: `/system/config/${configId}`
  })
}

export function getConfigKey(configKey: string) {
  return request.get<string>({
    url: `/system/config/configKey/${configKey}`
  })
}

export function addConfig(data: ConfigForm) {
  return request.post({
    url: '/system/config',
    params: data
  })
}

export function updateConfig(data: ConfigForm) {
  return request.put({
    url: '/system/config',
    params: data
  })
}

export function updateConfigByKey(configKey: string, configValue: any) {
  return request.put({
    url: '/system/config/updateByKey',
    params: { configKey, configValue }
  })
}

export function delConfig(configId: string | number) {
  return request.del({
    url: `/system/config/${configId}`
  })
}

export function refreshCache() {
  return request.put({
    url: '/system/config/refreshCache'
  })
}

export function exportConfig(query: ConfigQuery, fileName?: string) {
  return request.download(
    '/system/config/export',
    query,
    fileName || `config_${new Date().getTime()}.xlsx`
  )
}
