<!-- 通知组件 -->
<template>
  <div
    class="art-notification-panel art-card-sm !shadow-xl"
    :style="{
      transform: show ? 'scaleY(1)' : 'scaleY(0.9)',
      opacity: show ? 1 : 0
    }"
    v-show="visible"
    @click.stop
  >
    <div class="flex-cb px-3.5 mt-3.5">
      <span class="text-base font-medium text-g-800">{{ $t('notice.title') }}</span>
      <span
        class="text-xs text-g-800 px-1.5 py-1 c-p select-none rounded hover:bg-g-200"
        @click="readAll"
      >
        {{ $t('notice.btnRead') }}
      </span>
    </div>

    <ul class="box-border flex items-end w-full h-12.5 px-3.5 border-b-d">
      <li
        v-for="(item, index) in barList"
        :key="index"
        class="h-12 leading-12 mr-5 overflow-hidden text-[13px] text-g-700 c-p select-none transition-colors relative"
        :class="[{ 'bar-active': barActiveIndex === index }, item.className]"
        @click="changeBar(index)"
      >
        <span>{{ item.name }}</span>
        <span
          v-if="item.num > 0"
          class="ml-1 px-1.5 py-0.5 text-xs rounded-full text-white"
          :class="item.badgeClass"
        >
          {{ item.num }}
        </span>
      </li>
    </ul>

    <div class="w-full h-[calc(100%-95px)]">
      <div class="h-[calc(100%-60px)] overflow-y-scroll scrollbar-thin">
        <!-- 系统消息 -->
        <ul v-show="barActiveIndex === 0">
          <li
            v-for="(item, index) in systemList"
            :key="index"
            class="notice-item notice-item-warning box-border flex-c px-3.5 py-3.5 c-p last:border-b-0"
            :class="{ 'notice-item-unread': !item.read }"
            @click="onNewsClick(item)"
          >
            <div class="notice-icon size-9 leading-9 text-center rounded-lg flex-cc">
              <ArtSvgIcon class="text-lg !bg-transparent" icon="ri:notification-3-line" />
            </div>
            <div class="w-[calc(100%-55px)] ml-3.5">
              <h4
                class="text-sm font-normal leading-5.5 text-g-900 notice-message"
                :class="{ 'notice-message-collapsed': !isExpanded(item) }"
                @mouseenter="expandNotice(item)"
                @mouseleave="collapseNotice(item)"
              >
                {{ item.message }}
              </h4>
              <p class="mt-1.5 text-xs text-g-500">{{ formatTime(item.timestamp) }}</p>
            </div>
            <span v-if="item.read" class="text-xs text-g-400">已读</span>
            <span v-else class="notice-badge">
              <span class="pulse-dot"></span>
              <span class="text-xs text-danger">未读</span>
            </span>
          </li>
        </ul>

        <!-- 通知公告 -->
        <ul v-show="barActiveIndex === 1">
          <li
            v-for="(item, index) in noticeList"
            :key="index"
            class="notice-item notice-item-primary box-border flex-c px-3.5 py-3.5 c-p last:border-b-0"
            :class="{ 'notice-item-unread': !item.read }"
            @click="onNewsClick(item)"
          >
            <div class="notice-icon size-9 leading-9 text-center rounded-lg flex-cc">
              <ArtSvgIcon class="text-lg !bg-transparent" icon="ri:mail-line" />
            </div>
            <div class="w-[calc(100%-55px)] ml-3.5">
              <h4
                class="text-sm font-normal leading-5.5 text-g-900 notice-message"
                :class="{ 'notice-message-collapsed': !isExpanded(item) }"
                @mouseenter="expandNotice(item)"
                @mouseleave="collapseNotice(item)"
              >
                {{ item.message }}
              </h4>
              <p class="mt-1.5 text-xs text-g-500">{{ formatTime(item.timestamp) }}</p>
            </div>
            <span v-if="item.read" class="text-xs text-g-400">已读</span>
            <span v-else class="notice-badge">
              <span class="pulse-dot"></span>
              <span class="text-xs text-danger">未读</span>
            </span>
          </li>
        </ul>

        <!-- 工作流消息 -->
        <ul v-show="barActiveIndex === 2">
          <li
            v-for="(item, index) in workflowList"
            :key="index"
            class="notice-item notice-item-success box-border flex-c px-3.5 py-3.5 c-p last:border-b-0"
            :class="{ 'notice-item-unread': !item.read }"
          >
            <div
              class="notice-icon size-9 leading-9 text-center rounded-lg flex-cc"
              @click="onNewsClick(item)"
            >
              <ArtSvgIcon class="text-lg !bg-transparent" icon="ri:file-text-line" />
            </div>
            <div class="w-[calc(100%-55px)] ml-3.5" @click="onNewsClick(item)">
              <h4
                class="text-sm font-normal leading-5.5 text-g-900 notice-message"
                :class="{ 'notice-message-collapsed': !isExpanded(item) }"
                @mouseenter="expandNotice(item)"
                @mouseleave="collapseNotice(item)"
              >
                {{ item.message }}
              </h4>
              <p class="mt-1.5 text-xs text-g-500">{{ formatTime(item.timestamp) }}</p>
            </div>
            <div class="flex flex-col items-end gap-1">
              <ElButton
                v-if="item.path"
                type="success"
                size="small"
                @click.stop="handleQuickAction(item)"
                >立即处理</ElButton
              >
              <span v-if="item.read" class="text-xs text-g-400">已读</span>
              <span v-else class="notice-badge">
                <span class="pulse-dot"></span>
                <span class="text-xs text-danger">未读</span>
              </span>
            </div>
          </li>
        </ul>

        <!-- 告警消息 -->
        <ul v-show="barActiveIndex === 3">
          <li
            v-for="(item, index) in alertList"
            :key="index"
            class="notice-item notice-item-danger box-border flex-c px-3.5 py-3.5 c-p last:border-b-0"
            :class="{ 'notice-item-unread': !item.read }"
            @click="onNewsClick(item)"
          >
            <div class="notice-icon size-9 leading-9 text-center rounded-lg flex-cc">
              <ArtSvgIcon class="text-lg !bg-transparent" icon="ri:alarm-warning-line" />
            </div>
            <div class="w-[calc(100%-55px)] ml-3.5">
              <h4
                class="text-sm font-normal leading-5.5 text-g-900 notice-message"
                :class="{ 'notice-message-collapsed': !isExpanded(item) }"
                @mouseenter="expandNotice(item)"
                @mouseleave="collapseNotice(item)"
              >
                {{ item.message }}
              </h4>
              <p class="mt-1.5 text-xs text-g-500">{{ formatTime(item.timestamp) }}</p>
            </div>
            <span v-if="item.read" class="text-xs text-g-400">已读</span>
            <span v-else class="notice-badge">
              <span class="pulse-dot pulse-dot-urgent"></span>
              <span class="text-xs text-danger font-medium">未读</span>
            </span>
          </li>
        </ul>

        <!-- 安全告警 -->
        <ul v-show="barActiveIndex === 4">
          <li
            v-for="(item, index) in securityList"
            :key="index"
            class="notice-item notice-item-security box-border flex-c px-3.5 py-3.5 c-p last:border-b-0"
            :class="{ 'notice-item-unread': !item.read }"
            @click="onNewsClick(item)"
          >
            <div class="notice-icon size-9 leading-9 text-center rounded-lg flex-cc">
              <ArtSvgIcon class="text-lg !bg-transparent" icon="ri:shield-check-line" />
            </div>
            <div class="w-[calc(100%-55px)] ml-3.5">
              <h4
                class="text-sm font-normal leading-5.5 text-g-900 notice-message"
                :class="{ 'notice-message-collapsed': !isExpanded(item) }"
                @mouseenter="expandNotice(item)"
                @mouseleave="collapseNotice(item)"
              >
                {{ item.message }}
              </h4>
              <p class="mt-1.5 text-xs text-g-500">{{ formatTime(item.timestamp) }}</p>
            </div>
            <span v-if="item.read" class="text-xs text-g-400">已读</span>
            <span v-else class="notice-badge">
              <span class="pulse-dot pulse-dot-urgent"></span>
              <span class="text-xs text-danger font-medium">未读</span>
            </span>
          </li>
        </ul>

        <!-- 空状态 -->
        <div
          v-show="currentTabIsEmpty"
          class="empty-state relative top-25 h-full text-center !bg-transparent"
        >
          <ArtSvgIcon :icon="getEmptyIcon(barActiveIndex)" class="text-5xl text-g-400" />
          <p class="mt-3.5 text-xs text-g-500 !bg-transparent">
            暂无{{ barList[barActiveIndex]?.name || '' }}消息
          </p>
        </div>
      </div>

      <div class="relative box-border w-full px-3.5">
        <ElButton class="w-full mt-3" @click="handleViewAll" v-ripple>
          {{ $t('notice.viewAll') }}
        </ElButton>
      </div>
    </div>

    <div class="h-25"></div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useRouter } from 'vue-router'
  import { useNoticeStore } from '@/store/modules/notice'
  import { NOTICE_GROUP } from '@/utils/push-message'
  import { markRead } from '@/api/system/message'

  defineOptions({ name: 'ArtNotification' })

  const { t } = useI18n()
  const router = useRouter()
  const noticeStore = useNoticeStore()

  const props = defineProps<{
    value: boolean
  }>()

  const emit = defineEmits<{
    'update:value': [value: boolean]
  }>()

  const show = ref(false)
  const visible = ref(false)
  const barActiveIndex = ref(0)
  const expandedNotices = ref(new Set<string | number>())

  const newsList = computed(() => noticeStore.state.notices)

  const systemList = computed(() =>
    newsList.value.filter(
      (item: any) => (item.category || NOTICE_GROUP.SYSTEM) === NOTICE_GROUP.SYSTEM
    )
  )

  const noticeList = computed(() =>
    newsList.value.filter((item: any) => item.category === NOTICE_GROUP.NOTICE)
  )

  const workflowList = computed(() =>
    newsList.value.filter((item: any) => item.category === NOTICE_GROUP.WORKFLOW)
  )

  const alertList = computed(() =>
    newsList.value.filter((item: any) => item.category === NOTICE_GROUP.ALERT)
  )

  const securityList = computed(() =>
    newsList.value.filter((item: any) => item.category === NOTICE_GROUP.SECURITY)
  )

  const countUnread = (list: any[]) => list.filter((item: any) => !item.read).length

  const barList = computed(() => [
    {
      name: t('notice.bar[0]'),
      num: countUnread(systemList.value),
      className: 'bar-type-warning',
      badgeClass: 'bg-warning'
    },
    {
      name: t('notice.bar[1]'),
      num: countUnread(noticeList.value),
      className: 'bar-type-primary',
      badgeClass: 'bg-primary'
    },
    {
      name: t('notice.bar[2]'),
      num: countUnread(workflowList.value),
      className: 'bar-type-success',
      badgeClass: 'bg-success'
    },
    {
      name: t('notice.bar[3]'),
      num: countUnread(alertList.value),
      className: 'bar-type-danger',
      badgeClass: 'bg-danger'
    },
    {
      name: '安全',
      num: countUnread(securityList.value),
      className: 'bar-type-security',
      badgeClass: 'bg-orange-500'
    }
  ])

  const currentTabIsEmpty = computed(() => {
    const tabDataMap = [
      systemList.value,
      noticeList.value,
      workflowList.value,
      alertList.value,
      securityList.value
    ]
    const currentData = tabDataMap[barActiveIndex.value]
    return currentData && currentData.length === 0
  })

  /**
   * 智能时间显示
   */
  const formatTime = (timestamp?: number | string) => {
    if (!timestamp) return ''

    const now = Date.now()
    const time = typeof timestamp === 'string' ? new Date(timestamp).getTime() : timestamp
    const diff = now - time

    if (diff < 60 * 1000) return '刚刚'
    if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
    if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))}小时前`

    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const messageDate = new Date(time)

    if (
      messageDate.getFullYear() === yesterday.getFullYear() &&
      messageDate.getMonth() === yesterday.getMonth() &&
      messageDate.getDate() === yesterday.getDate()
    ) {
      const hours = messageDate.getHours().toString().padStart(2, '0')
      const minutes = messageDate.getMinutes().toString().padStart(2, '0')
      return `昨天 ${hours}:${minutes}`
    }

    const year = messageDate.getFullYear()
    const month = (messageDate.getMonth() + 1).toString().padStart(2, '0')
    const day = messageDate.getDate().toString().padStart(2, '0')
    const hours = messageDate.getHours().toString().padStart(2, '0')
    const minutes = messageDate.getMinutes().toString().padStart(2, '0')

    if (year === today.getFullYear()) {
      return `${month}-${day} ${hours}:${minutes}`
    }

    return `${year}-${month}-${day} ${hours}:${minutes}`
  }

  /**
   * 长文本折叠/展开
   */
  const isExpanded = (item: any) => {
    return expandedNotices.value.has(item.messageId)
  }

  const expandNotice = (item: any) => {
    if (item.message && item.message.length > 30) {
      expandedNotices.value.add(item.messageId)
    }
  }

  const collapseNotice = (item: any) => {
    expandedNotices.value.delete(item.messageId)
  }

  /**
   * 获取空状态图标
   */
  const getEmptyIcon = (index: number) => {
    const icons = [
      'ri:notification-off-line',
      'ri:mail-close-line',
      'ri:file-paper-2-line',
      'ri:alarm-line',
      'ri:shield-line'
    ]
    return icons[index] || 'system-uicons:inbox'
  }

  const showNotice = (open: boolean) => {
    if (open) {
      visible.value = true
      setTimeout(() => {
        show.value = true
      }, 5)
    } else {
      show.value = false
      setTimeout(() => {
        visible.value = false
      }, 350)
    }
  }

  const changeBar = (index: number) => {
    barActiveIndex.value = index
  }

  const onNewsClick = async (item: any) => {
    if (item?.messageId) {
      noticeStore.markRead(item.messageId)
      await markRead(item.messageId)
    }
    if (item?.path) {
      await router.push(item.path)
    }
  }

  /**
   * 快捷操作：立即处理工作流
   */
  const handleQuickAction = async (item: any) => {
    if (item?.messageId) {
      noticeStore.markRead(item.messageId)
      await markRead(item.messageId)
    }
    if (item?.path) {
      await router.push(item.path)
    }
  }

  const readAll = () => {
    const ids = newsList.value
      .map((item: any) => item.messageId)
      .filter((item: string | number | undefined) => item !== undefined && item !== null)
    noticeStore.markReadBatch(ids)
  }

  const handleViewAll = () => {
    emit('update:value', false)
    router.push('/system/message')
  }

  watch(
    () => props.value,
    (newValue) => {
      showNotice(newValue)
      // 打开面板时从后端同步真实消息（含未读状态）
      if (newValue) {
        noticeStore.loadMessages()
      }
    }
  )
</script>

<style scoped>
  @reference '@styles/core/tailwind.css';

  .art-notification-panel {
    @apply absolute 
    top-14.5 
    right-5 
    w-90 
    h-125 
    overflow-hidden 
    transition-all 
    duration-300
    origin-top 
    will-change-[top,left] 
    max-[640px]:top-[65px]
    max-[640px]:right-0
    max-[640px]:w-full 
    max-[640px]:h-[80vh];
  }

  /* Tab 选中态颜色 */
  .bar-active {
    color: var(--theme-color) !important;
    border-bottom: 2px solid var(--theme-color);
  }

  .bar-type-warning.bar-active {
    color: var(--el-color-warning) !important;
    border-bottom-color: var(--el-color-warning);
  }

  .bar-type-primary.bar-active {
    color: var(--el-color-primary) !important;
    border-bottom-color: var(--el-color-primary);
  }

  .bar-type-success.bar-active {
    color: var(--el-color-success) !important;
    border-bottom-color: var(--el-color-success);
  }

  .bar-type-danger.bar-active {
    color: var(--el-color-danger) !important;
    border-bottom-color: var(--el-color-danger);
  }

  .bar-type-security.bar-active {
    color: #ff9800 !important;
    border-bottom-color: #ff9800;
  }

  /* 通知项基础样式 */
  .notice-item {
    position: relative;
    border-left: 3px solid transparent;
    transition: all 0.2s ease;
  }

  /* 系统消息 - 橙色主题 */
  .notice-item-warning {
    border-left-color: var(--el-color-warning);
  }

  .notice-item-warning .notice-icon {
    color: var(--el-color-warning);
    background-color: oklch(78% 0.14 75.5deg / 12%);
  }

  .notice-item-warning:hover {
    background-color: oklch(78% 0.14 75.5deg / 8%);
  }

  /* 通知公告 - 蓝色主题 */
  .notice-item-primary {
    border-left-color: var(--el-color-primary);
  }

  .notice-item-primary .notice-icon {
    color: var(--el-color-primary);
    background-color: oklch(70% 0.23 260deg / 12%);
  }

  .notice-item-primary:hover {
    background-color: oklch(70% 0.23 260deg / 8%);
  }

  /* 工作流消息 - 绿色主题 */
  .notice-item-success {
    border-left-color: var(--el-color-success);
  }

  .notice-item-success .notice-icon {
    color: var(--el-color-success);
    background-color: oklch(78% 0.17 166.1deg / 12%);
  }

  .notice-item-success:hover {
    background-color: oklch(78% 0.17 166.1deg / 8%);
  }

  /* 告警消息 - 红色主题 */
  .notice-item-danger {
    border-left-color: var(--el-color-danger);
  }

  .notice-item-danger .notice-icon {
    color: var(--el-color-danger);
    background-color: oklch(68% 0.22 25.3deg / 12%);
  }

  .notice-item-danger:hover {
    background-color: oklch(68% 0.22 25.3deg / 8%);
  }

  /* 安全告警 - 橙色紫色主题 */
  .notice-item-security {
    border-left-color: #ff9800;
  }

  .notice-item-security .notice-icon {
    color: #ff9800;
    background-color: oklch(75% 0.15 55deg / 12%);
  }

  .notice-item-security:hover {
    background-color: oklch(75% 0.15 55deg / 8%);
  }

  /* 未读消息样式增强 */
  .notice-item-unread.notice-item-warning {
    background-color: oklch(78% 0.14 75.5deg / 5%);
  }

  .notice-item-unread.notice-item-primary {
    background-color: oklch(70% 0.23 260deg / 5%);
  }

  .notice-item-unread.notice-item-success {
    background-color: oklch(78% 0.17 166.1deg / 5%);
  }

  .notice-item-unread.notice-item-danger {
    background-color: oklch(68% 0.22 25.3deg / 8%);
  }

  .notice-item-unread.notice-item-security {
    background-color: oklch(75% 0.15 55deg / 5%);
  }

  /* 未读标记 */
  .notice-badge {
    display: flex;
    gap: 4px;
    align-items: center;
  }

  /* 脉冲动画红点 */
  .pulse-dot {
    width: 6px;
    height: 6px;
    background-color: var(--el-color-danger);
    border-radius: 50%;
    animation: pulse 2s infinite;
  }

  .pulse-dot-urgent {
    background-color: #f56c6c;
    animation: pulse-urgent 1.5s infinite;
  }

  @keyframes pulse {
    0%,
    100% {
      opacity: 1;
      transform: scale(1);
    }

    50% {
      opacity: 0.5;
      transform: scale(1.2);
    }
  }

  @keyframes pulse-urgent {
    0%,
    100% {
      box-shadow: 0 0 0 0 rgb(245 108 108 / 40%);
      opacity: 1;
      transform: scale(1);
    }

    50% {
      box-shadow: 0 0 0 4px rgb(245 108 108 / 0%);
      opacity: 0.8;
      transform: scale(1.3);
    }
  }

  /* 消息折叠 */
  .notice-message {
    overflow: hidden;
    text-overflow: ellipsis;
    transition: all 0.2s ease;
  }

  .notice-message-collapsed {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    white-space: normal;
  }

  /* 空状态 */
  .empty-state {
    padding: 40px 20px;
  }

  /* 滚动条样式 */
  .scrollbar-thin::-webkit-scrollbar {
    width: 5px !important;
  }

  .dark .scrollbar-thin::-webkit-scrollbar-track {
    background-color: var(--default-box-color);
  }

  .dark .scrollbar-thin::-webkit-scrollbar-thumb {
    background-color: #222 !important;
  }

  /* 暗黑模式适配 */
  .dark .notice-item-warning:hover {
    background-color: oklch(50% 0.12 75.5deg / 15%);
  }

  .dark .notice-item-primary:hover {
    background-color: oklch(45% 0.2 260deg / 15%);
  }

  .dark .notice-item-success:hover {
    background-color: oklch(50% 0.15 166.1deg / 15%);
  }

  .dark .notice-item-danger:hover {
    background-color: oklch(42% 0.2 25.3deg / 15%);
  }

  .dark .notice-item-security:hover {
    background-color: oklch(50% 0.12 55deg / 15%);
  }

  .dark .notice-item-warning .notice-icon {
    background-color: oklch(50% 0.12 75.5deg / 20%);
  }

  .dark .notice-item-primary .notice-icon {
    background-color: oklch(45% 0.2 260deg / 20%);
  }

  .dark .notice-item-success .notice-icon {
    background-color: oklch(50% 0.15 166.1deg / 20%);
  }

  .dark .notice-item-danger .notice-icon {
    background-color: oklch(42% 0.2 25.3deg / 20%);
  }

  .dark .notice-item-security .notice-icon {
    background-color: oklch(50% 0.12 55deg / 20%);
  }

  .dark .notice-item-unread.notice-item-warning {
    background-color: oklch(50% 0.12 75.5deg / 10%);
  }

  .dark .notice-item-unread.notice-item-primary {
    background-color: oklch(45% 0.2 260deg / 10%);
  }

  .dark .notice-item-unread.notice-item-success {
    background-color: oklch(50% 0.15 166.1deg / 10%);
  }

  .dark .notice-item-unread.notice-item-danger {
    background-color: oklch(42% 0.2 25.3deg / 12%);
  }

  .dark .notice-item-unread.notice-item-security {
    background-color: oklch(50% 0.12 55deg / 10%);
  }
</style>
