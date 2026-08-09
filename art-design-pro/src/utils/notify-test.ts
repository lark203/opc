/**
 * 消息测试工具
 * 在浏览器控制台中使用 window.notifyTest.testXXX() 测试不同类型的消息
 */

import { useNoticeStore } from '@/store/modules/notice'
import { NOTICE_GROUP, type NoticeGroup } from '@/utils/push-message'

interface TestMessage {
  title: string
  message: string
  category: NoticeGroup
  type?: string
  path?: string
}

/**
 * 模拟推送消息
 */
const simulatePush = (raw: string) => {
  const event = new MessageEvent('message', { data: raw })
  // 触发 SSE 消息处理
  window.dispatchEvent(event)
}

/**
 * 直接添加测试消息到 Store（用于前端测试，不依赖后端）
 */
const addTestNotice = (options: TestMessage) => {
  const noticeStore = useNoticeStore()

  noticeStore.addNotice({
    messageId: `test-${Date.now()}`,
    title: options.title,
    category: options.category,
    type: options.type || options.category,
    source: 'test',
    message: options.message,
    read: false,
    timestamp: Date.now(),
    time: new Date().toLocaleString(),
    path: options.path
  })

  console.log(`[NotifyTest] 已添加测试消息:`, options)
}

export const notifyTest = {
  /**
   * 测试系统消息
   */
  testSystem: (message = '系统将于今晚22:00进行例行维护') => {
    addTestNotice({
      title: '系统消息',
      message,
      category: NOTICE_GROUP.SYSTEM
    })
  },

  /**
   * 测试公告消息（会弹窗显示）
   */
  testNotice: (message = '今晚系统升级，请提前保存工作内容') => {
    addTestNotice({
      title: '通知公告',
      message,
      category: NOTICE_GROUP.NOTICE
    })
  },

  /**
   * 测试普通公告（不会弹窗）
   */
  testNoticeNormal: (message = '本周五下午部门例会') => {
    addTestNotice({
      title: '通知公告',
      message,
      category: NOTICE_GROUP.NOTICE
    })
  },

  /**
   * 测试工作流消息
   */
  testWorkflow: (message = '您有一条待审批的请假申请', path = '/workflow/leave') => {
    addTestNotice({
      title: '工作流消息',
      message,
      category: NOTICE_GROUP.WORKFLOW,
      path
    })
  },

  /**
   * 测试告警消息
   */
  testAlert: (message = '服务器CPU使用率超过90%') => {
    addTestNotice({
      title: '系统告警',
      message,
      category: NOTICE_GROUP.ALERT
    })
  },

  /**
   * 测试安全告警
   */
  testSecurity: (message = '检测到异常登录尝试') => {
    addTestNotice({
      title: '安全告警',
      message,
      category: NOTICE_GROUP.SECURITY
    })
  },

  /**
   * 批量测试：添加各类型消息
   */
  testAll: () => {
    notifyTest.testSystem()
    notifyTest.testNotice()
    notifyTest.testWorkflow()
    notifyTest.testAlert()
    notifyTest.testSecurity()
    console.log('[NotifyTest] 已添加所有类型的测试消息')
  },

  /**
   * 清空所有消息
   */
  clear: () => {
    const noticeStore = useNoticeStore()
    noticeStore.clearNotice()
    console.log('[NotifyTest] 已清空所有消息')
  },

  /**
   * 查看当前消息列表
   */
  list: () => {
    const noticeStore = useNoticeStore()
    console.table(
      noticeStore.state.notices.map((n) => ({
        分类: n.category,
        标题: n.title,
        消息: n.message,
        已读: n.read ? '是' : '否',
        时间: n.time
      }))
    )
  }
}

// 挂载到 window 方便测试
if (typeof window !== 'undefined') {
  ;(window as any).notifyTest = notifyTest
}

console.log(`
╔══════════════════════════════════════════╗
║        消息测试工具已加载                ║
║   使用 window.notifyTest 测试消息功能    ║
╠══════════════════════════════════════════╣
║  notifyTest.testSystem()     - 系统消息  ║
║  notifyTest.testNotice()     - 公告消息  ║
║  notifyTest.testWorkflow()   - 工作流    ║
║  notifyTest.testAlert()      - 告警消息  ║
║  notifyTest.testSecurity()   - 安全告警  ║
║  notifyTest.testAll()        - 全部测试  ║
║  notifyTest.clear()          - 清空消息  ║
║  notifyTest.list()           - 查看消息  ║
╚══════════════════════════════════════════╝
`)
