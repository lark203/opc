import request from '@/utils/http'

export interface LoginInfoVO {
  infoId: string | number
  userName: string
  clientKey?: string
  deviceType?: string
  status: string
  ipaddr: string
  loginLocation?: string
  browser?: string
  os?: string
  msg?: string
  loginTime: string
}

export interface LoginInfoQuery {
  userName?: string
  ipaddr?: string
  status?: string
  browser?: string
  os?: string
  deviceType?: string
  clientKey?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export function listLoginInfo(query: LoginInfoQuery) {
  return request.get<{ rows: LoginInfoVO[]; total: number }>({
    url: '/monitor/loginInfo/list',
    params: query
  })
}

export function delLoginInfo(infoIds: Array<string | number> | string | number) {
  return request.del({
    url: `/monitor/loginInfo/${infoIds}`
  })
}

export function cleanLoginInfo() {
  return request.del({
    url: '/monitor/loginInfo/clean'
  })
}

export function unlockLoginInfo(userName: string) {
  return request.get({
    url: `/monitor/loginInfo/unlock/${userName}`
  })
}

export function exportLoginInfo(query: LoginInfoQuery, fileName?: string) {
  return request.download(
    '/monitor/loginInfo/export',
    query,
    fileName || `logininfo_${new Date().getTime()}.xlsx`
  )
}
