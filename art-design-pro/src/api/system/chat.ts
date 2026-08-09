import request from '@/utils/http'
import type { OssUploadVO, OssVO } from '@/api/system/oss'

export interface ChatConversationVo {
  conversationId: string | number
  type: string
  title: string
  avatar: string
  targetUserId: string | number
  targetNickName: string
  targetAvatar: string
  targetAvatarUrl: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
  isTop: string
}

export interface ChatMessageVo {
  messageId: string | number
  conversationId: string | number
  senderId: string | number
  senderNickName: string
  senderAvatar: string
  senderAvatarUrl: string
  receiverId: string | number
  type: string
  content: string
  status: string
  createTime: string
}

export interface ChatUserVo {
  userId: string | number
  userName: string
  nickName: string
  avatar: string
  avatarUrl: string
  email: string
  phoneNumber: string
}

export interface ChatMessageBo {
  receiverId: string | number
  type?: string
  content: string
}

export interface MessageQuery {
  pageNum?: number
  pageSize?: number
}

export function getOrCreateConversation(targetUserId: string | number) {
  return request.get<ChatConversationVo>({
    url: `/resource/chat/conversation/${targetUserId}`
  })
}

export function getConversationList(query?: MessageQuery) {
  return request.get<{ rows: ChatConversationVo[]; total: number }>({
    url: '/resource/chat/conversation/list',
    params: query
  })
}

export function getConversationUnread() {
  return request.get<number>({
    url: '/resource/chat/conversation/unread'
  })
}

export function getMessageList(conversationId: string | number, query?: MessageQuery) {
  return request.get<{ rows: ChatMessageVo[]; total: number }>({
    url: `/resource/chat/message/list/${conversationId}`,
    params: query
  })
}

export function sendChatMessage(data: ChatMessageBo) {
  return request.post<ChatMessageVo>({
    url: '/resource/chat/message/send',
    data
  })
}

export function markChatRead(conversationId: string | number) {
  return request.put({
    url: `/resource/chat/message/read/${conversationId}`
  })
}

export function deleteConversation(conversationId: string | number) {
  return request.del({
    url: `/resource/chat/conversation/${conversationId}`
  })
}

export function getContacts(keyword?: string, pageNum = 1, pageSize = 10) {
  return request.get<{ rows: ChatUserVo[]; total: number }>({
    url: '/resource/chat/contacts',
    params: { keyword, pageNum, pageSize }
  })
}

/**
 * 聊天内文件/图片上传（仅登录即可，无需 system:oss:upload 管理员权限）
 * 返回结构与 OSS 上传一致：{ url, fileName, ossId }
 */
export function uploadChatFile(data: FormData) {
  return request.post<OssUploadVO>({
    url: '/resource/chat/upload',
    data
  })
}

/**
 * 解析聊天消息中图片/文件对应的 OSS 原始信息（文件名等）
 * 仅需登录即可访问（区别于 /resource/oss/listByIds 的 system:oss:query 权限），
 * 使普通用户也能显示文件名。
 */
export function getChatOssInfo(ossIds: string | number) {
  return request.get<OssVO[]>({
    url: `/resource/chat/oss/info/${ossIds}`
  })
}
