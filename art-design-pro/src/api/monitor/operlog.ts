import request from '@/utils/http'

export interface OperLogVO {
  operId: string | number
  title: string
  businessType: string | number
  method: string
  requestMethod: string
  operatorType: number
  operName: string
  userId?: string | number
  deptId?: string | number
  deptName?: string
  clientKey?: string
  deviceType?: string
  browser?: string
  os?: string
  operUrl: string
  operIp: string
  operLocation?: string
  operParam: string
  jsonResult: string
  status: number
  errorMsg: string
  operTime: string
  costTime: number
}

export interface OperLogQuery {
  operIp?: string
  title?: string
  operName?: string
  clientKey?: string
  deviceType?: string
  browser?: string
  os?: string
  businessType?: string
  status?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export function listOperLog(query: OperLogQuery) {
  return request.get<{ rows: OperLogVO[]; total: number }>({
    url: '/monitor/operlog/list',
    params: query
  })
}

export function delOperLog(operId: Array<string | number> | string | number) {
  return request.del({
    url: `/monitor/operlog/${operId}`
  })
}

export function cleanOperLog() {
  return request.del({
    url: '/monitor/operlog/clean'
  })
}

export function exportOperLog(query: OperLogQuery, fileName?: string) {
  return request.download(
    '/monitor/operlog/export',
    query,
    fileName || `operlog_${new Date().getTime()}.xlsx`
  )
}
