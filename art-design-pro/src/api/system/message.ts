import request from '@/utils/http'

export interface MessageVO {
  messageId: string | number
  category: string
  type: string
  source: string
  title: string
  message: string
  content: string
  data?: Record<string, any> | null
  path: string
  createTime: string
  readStatus: string
}

export interface MessageBoxVo {
  systemList: MessageVO[]
  noticeList: MessageVO[]
  workflowList: MessageVO[]
}

export interface MessageQuery {
  category?: string
  type?: string
  source?: string
  readStatus?: string
  pageNum?: number
  pageSize?: number
}

export function getMessageBox() {
  return request.get<MessageBoxVo>({
    url: '/resource/message/box'
  })
}

export function listMessage(query: MessageQuery) {
  return request.get<{ rows: MessageVO[]; total: number }>({
    url: '/resource/message/list',
    params: query
  })
}

export function markRead(messageId: string | number) {
  return request.put({
    url: `/resource/message/read/${messageId}`
  })
}

export function markReadBatch(messageIds: Array<string | number>) {
  return request.put({
    url: '/resource/message/read',
    data: messageIds
  })
}

export function markReadAll() {
  return request.put({
    url: '/resource/message/read/all'
  })
}

export function deleteMessage(messageId: string | number) {
  return request.del({
    url: `/resource/message/${messageId}`
  })
}

export function deleteMessageBatch(messageIds: Array<string | number>) {
  return request.del({
    url: '/resource/message',
    data: messageIds
  })
}

/**
 * 消息发送业务对象（系统内 OA 消息）
 */
export interface SysMessageSendBo {
  userIds?: Array<string | number>
  broadcast?: boolean
  title: string
  category: string
  message?: string
  content?: string
  path?: string
  type?: string
  source?: string
}

/**
 * 发送消息（指定用户或全局广播）
 * @param data 发送参数
 */
export function sendMessage(data: SysMessageSendBo) {
  return request.post({
    url: '/resource/message/send',
    data
  })
}
