import { watch } from 'vue'
import { ElMessageBox, ElNotification } from 'element-plus'
import { useEventSource } from '@vueuse/core'
import { useNoticeStore } from '@/store/modules/notice'
import { useUserStore } from '@/store/modules/user'
import { mittBus } from '@/utils/sys'
import {
  type NoticeGroup,
  parsePushMessage,
  resolveDisplayStrategy,
  resolveNoticeGroup,
  resolveNoticeTitle,
  resolveNoticeType,
  shouldAppendNotice
} from '@/utils/push-message'

const KICKED_MESSAGE = 'kicked'
let closePushConnection: (() => void) | undefined
let stopPushWatchers: Array<() => void> = []
let pushKicked = false

const formatNoticeTime = (timestamp?: number | string) => {
  const time = timestamp ? new Date(timestamp) : new Date()
  return time.toLocaleString()
}

/**
 * 根据通知类型获取弹窗展示配置。
 * - warning/system: 展示 4 秒
 * - error/alert:    展示 6 秒且显示关闭按钮，确保用户注意到
 * - primary/notice: 展示 3 秒（蓝色）
 * - success/workflow: 展示 3 秒
 */
const buildNotificationConfig = (
  group: NoticeGroup
): { type: 'warning' | 'primary' | 'success' | 'error'; duration: number; showClose: boolean } => {
  const type = resolveNoticeType(group)
  const isAlert = type === 'error'
  return {
    type,
    duration: isAlert ? 6000 : type === 'warning' ? 4000 : 3000,
    showClose: isAlert
  }
}

const appendNotice = (raw: string) => {
  const payload = parsePushMessage(raw)
  if (!shouldAppendNotice(payload)) {
    return
  }
  const group = resolveNoticeGroup(payload)
  const title = resolveNoticeTitle(payload)
  const message = payload.message ?? ''
  const displayStrategy = resolveDisplayStrategy(group, message)

  // 添加到通知 Store
  useNoticeStore().addNotice({
    messageId: payload.messageId,
    title,
    category: group,
    type: payload.type,
    source: payload.source,
    message,
    content: payload.data?.noticeContent,
    data: payload.data,
    path: payload.path,
    read: false,
    timestamp: payload.timestamp ?? Date.now(),
    time: formatNoticeTime(payload.timestamp)
  })

  // 根据展示策略显示消息
  if (displayStrategy === 'modal') {
    // 弹窗显示（公告类）
    ElMessageBox.alert(message, title, {
      confirmButtonText: '我知道了',
      type: 'info',
      draggable: true
    })
  } else if (displayStrategy === 'alert') {
    // 重要弹窗（告警/安全类）
    const config = buildNotificationConfig(group)
    ElNotification({
      title,
      message,
      type: config.type,
      duration: 0, // 不自动关闭
      showClose: true
    })
    // 可选：播放提示音
    // playAlertSound()
  } else {
    // 默认通知面板显示
    const config = buildNotificationConfig(group)
    ElNotification({
      title,
      message,
      type: config.type,
      duration: config.duration,
      showClose: config.showClose
    })
  }
}

const handlePushMessage = (raw: string) => {
  if (raw === KICKED_MESSAGE) {
    pushKicked = true
    closePush()
    return
  }
  // 聊天消息走独立的 mitt 事件，不进入通知盒子
  const payload = parsePushMessage(raw)
  if (payload.type === 'chat') {
    mittBus.emit('chatMessage', payload)
    return
  }
  appendNotice(raw)
}

const buildSseUrl = (path: string) => {
  const { accessToken } = useUserStore()
  const { VITE_API_URL, VITE_APP_CLIENT_ID } = import.meta.env
  return `${VITE_API_URL}${path}?Authorization=Bearer ${accessToken}&clientid=${VITE_APP_CLIENT_ID}`
}

const initSsePush = (url: string) => {
  const { data, error, close } = useEventSource(url, [], {
    autoReconnect: {
      retries: 5,
      delay: 5000,
      onFailed() {
        console.warn('[MessagePush] SSE connection failed after 5 retries')
      }
    }
  })
  closePushConnection = close

  const stopErrorWatch = watch(error, () => {
    console.warn('[MessagePush] SSE connection error:', error.value)
    error.value = null
  })

  const stopDataWatch = watch(data, () => {
    if (!data.value) return
    handlePushMessage(data.value)
    data.value = null
  })

  stopPushWatchers.push(stopErrorWatch, stopDataWatch)
}

export const initPush = () => {
  closePush()
  if (import.meta.env.VITE_APP_MESSAGE_ENABLED === 'false') {
    return
  }
  pushKicked = false
  const path = import.meta.env.VITE_APP_MESSAGE_PATH || '/resource/message'
  initSsePush(buildSseUrl(path))
}

export const closePush = () => {
  closePushConnection?.()
  closePushConnection = undefined
  stopPushWatchers.forEach((stop) => stop())
  stopPushWatchers = []
}

const resumePushIfNeeded = () => {
  const { accessToken } = useUserStore()
  if (!pushKicked || !accessToken || document.visibilityState !== 'visible') {
    return
  }
  setTimeout(() => {
    if (!pushKicked || !accessToken || document.visibilityState !== 'visible') {
      return
    }
    initPush()
  }, 300)
}

window.addEventListener('focus', resumePushIfNeeded)
document.addEventListener('visibilitychange', resumePushIfNeeded)
window.addEventListener('online', resumePushIfNeeded)
