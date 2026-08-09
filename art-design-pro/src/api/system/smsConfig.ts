import request from '@/utils/http'

export interface SmsConfigVO {
  smsId: number
  configId?: string
  name: string
  supplier: string
  accessKey: string
  secretKey?: string
  signature?: string
  templateId: string
  weight?: number
  retryInterval?: number
  maxRetries?: number
  maximum?: number
  supplierConfig?: string
  status?: string
  isDefault?: string
  sort?: number
  createTime?: string
}

export function listSmsConfig(query: any) {
  return request.get<{ rows: SmsConfigVO[]; total: number }>({
    url: '/system/sms/config/list',
    params: query
  })
}

export function getSmsConfig(id: number) {
  return request.get<SmsConfigVO>({ url: `/system/sms/config/${id}` })
}

export function addSmsConfig(data: Partial<SmsConfigVO>) {
  return request.post({ url: '/system/sms/config', params: data })
}

export function updateSmsConfig(data: Partial<SmsConfigVO>) {
  return request.put({ url: `/system/sms/config/${data.smsId}`, params: data })
}

export function delSmsConfig(ids: (number | string)[]) {
  return request.del({ url: `/system/sms/config/${ids.join(',')}` })
}

export function setDefaultSmsConfig(id: number) {
  return request.put({ url: `/system/sms/config/${id}/default` })
}

export function changeSmsConfigStatus(smsId: number, status: string) {
  return request.put({ url: '/system/sms/config/changeStatus', data: { smsId, status } })
}
