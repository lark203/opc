export const PUSH_MESSAGE_TYPE = {
  MESSAGE: 'message',
  NOTICE: 'notice',
  LLM: 'llm',
  CUSTOM: 'custom'
} as const

export const PUSH_MESSAGE_SOURCE = {
  BACKEND: 'backend',
  NOTICE: 'notice',
  WORKFLOW: 'workflow',
  LLM: 'llm',
  CLIENT: 'client'
} as const

export const NOTICE_GROUP = {
  SYSTEM: 'system',
  NOTICE: 'notice',
  WORKFLOW: 'workflow',
  ALERT: 'alert',
  SECURITY: 'security'
} as const

export type NoticeGroup = (typeof NOTICE_GROUP)[keyof typeof NOTICE_GROUP]

/**
 * 消息展示策略
 * - notification: 在通知面板显示（默认）
 * - modal: 弹窗显示，需要用户确认（公告类）
 * - alert: 重要弹窗，带声音提醒（告警/安全类）
 */
export type NoticeDisplayStrategy = 'notification' | 'modal' | 'alert'

/**
 * 通知分组映射到 ElNotification 的 type 类型。
 * - system:    warning  系统消息，需引起注意
 * - notice:    primary  通知公告，蓝色主题（Element Plus 2.9.11+）
 * - workflow:  success  工作流推进，正向反馈
 * - alert:     error    告警/安全，需立即处理
 * - security:  warning  安全告警，特殊提示
 */
export const NOTICE_TYPE_MAP: Record<NoticeGroup, 'warning' | 'primary' | 'success' | 'error'> = {
  [NOTICE_GROUP.SYSTEM]: 'warning',
  [NOTICE_GROUP.NOTICE]: 'primary',
  [NOTICE_GROUP.WORKFLOW]: 'success',
  [NOTICE_GROUP.ALERT]: 'error',
  [NOTICE_GROUP.SECURITY]: 'warning'
}

/**
 * 根据消息分组和内容确定展示策略
 * - 公告类（notice）：如果包含"重要"/"升级"/"维护"等关键词，使用弹窗
 * - 告警/安全类：使用重要弹窗
 * - 其他：使用通知面板
 */
export const resolveDisplayStrategy = (
  group: NoticeGroup,
  message?: string
): NoticeDisplayStrategy => {
  // 告警和安全类消息使用重要弹窗
  if (group === NOTICE_GROUP.ALERT || group === NOTICE_GROUP.SECURITY) {
    return 'alert'
  }

  // 公告类消息检查关键词
  if (group === NOTICE_GROUP.NOTICE && message) {
    const importantKeywords = ['升级', '维护', '停机', '重要', '紧急', '通知', '公告']
    if (importantKeywords.some((keyword) => message.includes(keyword))) {
      return 'modal'
    }
  }

  // 默认在通知面板显示
  return 'notification'
}

/**
 * 根据通知分组获取对应的弹窗类型。
 */
export const resolveNoticeType = (
  group: NoticeGroup
): 'warning' | 'primary' | 'success' | 'error' => {
  return NOTICE_TYPE_MAP[group] ?? 'primary'
}

export interface PushMessagePayload {
  messageId?: string | number
  type?: string
  source?: string
  message?: string
  data?: Record<string, any> | null
  path?: string
  timestamp?: number
}

const MESSAGE_CENTER_TYPES = new Set<string>([
  PUSH_MESSAGE_TYPE.MESSAGE,
  PUSH_MESSAGE_TYPE.NOTICE,
  'alert',
  'security'
])

export const parsePushMessage = (raw: string): PushMessagePayload => {
  try {
    const payload = JSON.parse(raw) as PushMessagePayload
    return {
      type: payload.type ?? PUSH_MESSAGE_TYPE.MESSAGE,
      source: payload.source ?? 'backend',
      messageId: payload.messageId,
      message: payload.message ?? '',
      data: payload.data ?? null,
      path: payload.path,
      timestamp: payload.timestamp ?? Date.now()
    }
  } catch {
    return {
      type: PUSH_MESSAGE_TYPE.MESSAGE,
      source: 'backend',
      messageId: undefined,
      message: raw,
      data: null,
      path: undefined,
      timestamp: Date.now()
    }
  }
}

export const shouldAppendNotice = (payload: PushMessagePayload) => {
  return MESSAGE_CENTER_TYPES.has(payload.type ?? PUSH_MESSAGE_TYPE.MESSAGE)
}

export const resolveNoticeGroup = (payload: PushMessagePayload): NoticeGroup => {
  if (payload.type === 'alert' || payload.source === 'alert') {
    return NOTICE_GROUP.ALERT
  }
  if (payload.type === 'security' || payload.source === 'security') {
    return NOTICE_GROUP.SECURITY
  }
  if (payload.type === PUSH_MESSAGE_TYPE.NOTICE || payload.source === PUSH_MESSAGE_SOURCE.NOTICE) {
    return NOTICE_GROUP.NOTICE
  }
  if (payload.source === PUSH_MESSAGE_SOURCE.WORKFLOW) {
    return NOTICE_GROUP.WORKFLOW
  }
  return NOTICE_GROUP.SYSTEM
}

export const resolveNoticeTitle = (payload: PushMessagePayload) => {
  const group = resolveNoticeGroup(payload)
  if (group === NOTICE_GROUP.ALERT) {
    return '系统告警'
  }
  if (group === NOTICE_GROUP.SECURITY) {
    return '安全告警'
  }
  if (group === NOTICE_GROUP.NOTICE) {
    return '通知公告消息'
  }
  if (group === NOTICE_GROUP.WORKFLOW) {
    return '工作流消息'
  }
  return '系统消息'
}
