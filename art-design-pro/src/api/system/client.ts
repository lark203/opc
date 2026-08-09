import request from '@/utils/http'

export interface ClientVO {
  id: string | number
  clientId: string
  clientKey: string
  clientSecret: string
  grantTypeList: string[]
  deviceType: string
  accessPath: string
  accessPathList: string[]
  ipWhitelist: string
  ipWhitelistList: string[]
  activeTimeout: number
  timeout: number
  status: string
}

export interface ClientForm {
  id?: string | number
  clientId?: string
  clientKey: string
  clientSecret: string
  grantTypeList: string[]
  deviceType: string
  accessPath?: string
  accessPathList?: string[]
  ipWhitelist?: string
  ipWhitelistList?: string[]
  activeTimeout?: number
  timeout?: number
  status: string
}

export interface ClientQuery {
  clientId?: string
  clientKey?: string
  clientSecret?: string
  grantType?: string
  deviceType?: string
  accessPath?: string
  ipWhitelist?: string
  activeTimeout?: number
  timeout?: number
  status?: string
  pageNum?: number
  pageSize?: number
}

export function listClient(query: ClientQuery) {
  return request.get<{ rows: ClientVO[]; total: number }>({
    url: '/system/client/list',
    params: query
  })
}

export function getClient(clientId: string | number) {
  return request.get<ClientForm>({
    url: `/system/client/${clientId}`
  })
}

export function addClient(data: ClientForm) {
  return request.post({
    url: '/system/client',
    params: data
  })
}

export function updateClient(data: ClientForm) {
  return request.put({
    url: '/system/client',
    params: data
  })
}

export function delClient(clientId: Array<string | number> | string | number) {
  return request.del({
    url: `/system/client/${clientId}`
  })
}

export function changeStatus(clientId: string, status: string) {
  return request.put({
    url: '/system/client/changeStatus',
    params: { clientId, status }
  })
}

export function exportClient(query: ClientQuery, fileName?: string) {
  return request.download(
    '/system/client/export',
    query,
    fileName || `client_${new Date().getTime()}.xlsx`
  )
}
