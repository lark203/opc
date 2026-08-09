import { defineStore } from 'pinia'
import { computed, reactive } from 'vue'
import { getMessageBox, type MessageVO } from '@/api/system/message'
import { NOTICE_GROUP } from '@/utils/push-message'

export interface NoticeItem {
  messageId?: string | number
  title?: string
  category?: string
  type?: string
  source?: string
  read: boolean
  message: string
  content?: string
  data?: Record<string, any> | null
  path?: string
  timestamp?: number
  time: string
}

export const useNoticeStore = defineStore('notice', () => {
  const state = reactive({
    notices: [] as NoticeItem[]
  })

  const unreadCount = computed(() => state.notices.filter((item) => !item.read).length)

  const buildNoticeKey = (notice: NoticeItem) => {
    if (notice.messageId !== undefined && notice.messageId !== null) {
      return String(notice.messageId)
    }
    return [notice.type, notice.source, notice.timestamp, notice.message].join(':')
  }

  const sortNotices = () => {
    state.notices.sort((a, b) => Number(b.timestamp || 0) - Number(a.timestamp || 0))
  }

  const setNotices = (notices: NoticeItem[]) => {
    state.notices = [...notices]
    sortNotices()
  }

  const addNotice = (notice: NoticeItem) => {
    const key = buildNoticeKey(notice)
    const index = state.notices.findIndex((item) => buildNoticeKey(item) === key)
    if (index > -1) {
      state.notices[index] = {
        ...state.notices[index],
        ...notice
      }
    } else {
      state.notices.unshift(notice)
    }
    sortNotices()
  }

  const markRead = (messageId?: string | number) => {
    if (messageId === undefined || messageId === null) {
      return
    }
    const target = state.notices.find((item) => String(item.messageId) === String(messageId))
    if (target) {
      target.read = true
    }
  }

  const markReadBatch = (messageIds: Array<string | number>) => {
    const idSet = new Set(messageIds.map((item) => String(item)))
    state.notices.forEach((item) => {
      if (
        item.messageId !== undefined &&
        item.messageId !== null &&
        idSet.has(String(item.messageId))
      ) {
        item.read = true
      }
    })
  }

  const readAll = () => {
    markReadBatch(
      state.notices
        .map((item) => item.messageId)
        .filter((item): item is string | number => item !== undefined && item !== null)
    )
  }

  const clearNotice = () => {
    state.notices = []
  }

  /**
   * 从后端加载当前用户的真实消息（消息盒子），替换本地列表。
   * 用于：应用初始化、打开通知面板时同步已持久化的消息与未读状态。
   */
  const loadMessages = async () => {
    try {
      const box = await getMessageBox()
      const mapItem = (vo: MessageVO, category: string): NoticeItem => ({
        messageId: vo.messageId,
        title: vo.title,
        category,
        type: vo.type,
        source: vo.source,
        message: vo.message || '',
        content: vo.content,
        data: (vo.data as Record<string, any>) ?? null,
        path: vo.path,
        read: vo.readStatus === '1',
        timestamp: vo.createTime ? new Date(vo.createTime.replace(' ', 'T')).getTime() : Date.now(),
        time: vo.createTime || ''
      })
      const list: NoticeItem[] = [
        ...(box.systemList || []).map((vo) => mapItem(vo, NOTICE_GROUP.SYSTEM)),
        ...(box.noticeList || []).map((vo) => mapItem(vo, NOTICE_GROUP.NOTICE)),
        ...(box.workflowList || []).map((vo) => mapItem(vo, NOTICE_GROUP.WORKFLOW))
      ]
      setNotices(list)
    } catch (error) {
      console.error('加载消息失败:', error)
    }
  }

  return {
    state,
    unreadCount,
    setNotices,
    addNotice,
    markRead,
    markReadBatch,
    readAll,
    clearNotice,
    loadMessages
  }
})
