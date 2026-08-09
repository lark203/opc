import request from '@/utils/http'

export interface OnlineVO {
  tokenId: string
  deptName: string
  userName: string
  clientKey?: string
  deviceType?: string
  ipaddr: string
  loginLocation: string
  browser: string
  os: string
  loginTime: number
}

export interface OnlineQuery {
  ipaddr?: string
  userName?: string
  pageNum?: number
  pageSize?: number
}

export function listOnline(query: OnlineQuery) {
  return request.get<{ rows: OnlineVO[]; total: number }>({
    url: '/monitor/online/list',
    params: query
  })
}

export function forceLogout(tokenId: string) {
  return request.del({
    url: `/monitor/online/${tokenId}`
  })
}

export function getOnline() {
  return request.get<{ rows: OnlineVO[]; total: number }>({
    url: '/monitor/online'
  })
}

export function delOnline(tokenId: string) {
  return request.del({
    url: `/monitor/online/myself/${tokenId}`
  })
}
