import request from '@/utils/http'

/** 测试发送邮件（可选指定收件人，缺省发给自己） */
export function testMail(data?: { to?: string }) {
  return request.post<unknown>({ url: '/system/mail/test', data })
}
