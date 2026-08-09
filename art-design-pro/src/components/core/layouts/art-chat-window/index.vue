<template>
  <div>
    <ElDrawer
      v-model="isDrawerVisible"
      :size="isMobile ? '100%' : '480px'"
      :with-header="false"
      :close-on-click-modal="false"
    >
      <div class="relative flex h-full flex-col">
        <!-- 头部 -->
        <div class="flex-cb border-b-d px-4 py-3">
          <div class="flex-c gap-2">
            <!-- 返回按钮（在会话详情时显示） -->
            <ElIcon
              v-if="currentConversation"
              class="c-p text-lg text-g-600 hover:text-g-900"
              @click="backToList"
            >
              <ArrowLeft />
            </ElIcon>
            <template v-if="currentConversation">
              <ElAvatar :size="36" :src="resolveAvatar(currentConversation.targetAvatar)">
                {{ getAvatarText(currentConversation.targetNickName) }}
              </ElAvatar>
              <div>
                <div class="text-base font-medium text-g-900">
                  {{ currentConversation.targetNickName || '未知用户' }}
                </div>
                <div class="text-xs text-g-500">
                  {{
                    currentConversation.unreadCount
                      ? `未读 ${currentConversation.unreadCount} 条`
                      : '暂无新消息'
                  }}
                </div>
              </div>
            </template>
            <template v-else>
              <div class="text-base font-medium text-g-900">消息</div>
            </template>
          </div>
          <div class="flex-c gap-2">
            <ElButton :icon="Plus" circle size="small" plain @click="openContactDialog" />
            <ElIcon class="c-p text-lg text-g-600 hover:text-g-900" @click="closeChat">
              <Close />
            </ElIcon>
          </div>
        </div>

        <!-- 会话列表 -->
        <div
          v-if="!currentConversation"
          class="flex-1 overflow-y-auto px-2 py-2 [&::-webkit-scrollbar]:!w-1"
          @scroll="onConversationScroll"
        >
          <template v-if="conversations.length > 0">
            <div
              v-for="conv in conversations"
              :key="conv.conversationId"
              class="mb-1 flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2.5 transition-all hover:bg-g-300/40"
              @click="openConversation(conv)"
            >
              <ElAvatar :size="42" :src="resolveAvatar(conv.targetAvatar)">
                {{ getAvatarText(conv.targetNickName) }}
              </ElAvatar>
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-2">
                  <span class="truncate font-medium text-g-900">{{
                    conv.targetNickName || '未知用户'
                  }}</span>
                </div>
                <div class="mt-0.5 truncate text-xs text-g-500">
                  {{ conv.lastMessage || '暂无消息' }}
                </div>
              </div>
              <div class="flex flex-col items-end gap-1">
                <span class="text-xs text-g-400">{{ formatTime(conv.lastMessageTime) }}</span>
                <ElBadge
                  v-if="conv.unreadCount && conv.unreadCount > 0"
                  :value="conv.unreadCount > 99 ? '99+' : conv.unreadCount"
                  type="danger"
                  :max="99"
                />
              </div>
            </div>
            <!-- 向下滚动加载更多会话 -->
            <div v-if="convHasMore" class="py-2 text-center text-xs text-g-400">
              {{ convLoadingMore ? '加载中...' : '向下滚动加载更多' }}
            </div>
          </template>
          <!-- 空状态 -->
          <div v-else class="flex h-full flex-col items-center justify-center px-4 text-center">
            <ElIcon :size="48" class="mb-3 text-g-400">
              <ChatDotRound />
            </ElIcon>
            <div class="text-sm text-g-500">暂无会话</div>
            <ElButton type="primary" size="small" class="mt-3" @click="openContactDialog">
              开始新对话
            </ElButton>
          </div>
        </div>

        <!-- 聊天消息区域（虚拟滚动，仅渲染视口附近消息，避免长会话 DOM 膨胀） -->
        <div
          v-else
          class="relative flex-1 overflow-y-auto px-4 py-4 [&::-webkit-scrollbar]:!w-1"
          ref="messageContainer"
          @scroll="onMessageScroll"
          @dragenter.prevent="onMessageDragEnter"
          @dragover.prevent="onMessageDragOver"
          @dragleave="onMessageDragLeave"
          @drop.prevent="onMessageDrop"
        >
          <!-- 向上滚动加载更早消息的提示 -->
          <div v-if="messageHasMore" class="py-2 text-center text-xs text-g-400">
            {{ messageLoadingMore ? '加载中...' : '向上滚动加载更多' }}
          </div>
          <div :style="{ height: `${messageTotalSize}px`, position: 'relative', width: '100%' }">
            <div
              v-for="vItem in messageVirtualItems"
              :key="messages[vItem.index].messageId"
              :data-index="vItem.index"
              :ref="(el: any) => messageVirtualizer.measureElement(el)"
              :style="{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                transform: `translateY(${vItem.start}px)`
              }"
              :class="[
                'flex w-full items-start gap-2 pb-4',
                isMe(messages[vItem.index]) ? 'flex-row-reverse' : 'flex-row'
              ]"
            >
              <ElAvatar :size="32" :src="resolveAvatar(messages[vItem.index].senderAvatar)">
                {{ getAvatarText(messages[vItem.index].senderNickName) }}
              </ElAvatar>
              <div
                :class="[
                  'flex max-w-[70%] flex-col',
                  isMe(messages[vItem.index]) ? 'items-end' : 'items-start'
                ]"
              >
                <div
                  :class="[
                    'mb-1 flex gap-2 text-xs',
                    isMe(messages[vItem.index]) ? 'flex-row-reverse' : 'flex-row'
                  ]"
                >
                  <span class="font-medium text-g-700">{{
                    messages[vItem.index].senderNickName
                  }}</span>
                  <span class="text-g-400">{{ formatTime(messages[vItem.index].createTime) }}</span>
                </div>
                <!-- 图片消息 -->
                <img
                  v-if="messages[vItem.index].type === 'image'"
                  :src="resolveAvatar(messages[vItem.index].content)"
                  class="block max-h-60 max-w-[70%] cursor-zoom-in rounded-lg object-cover"
                  @load="onImageLoad"
                  @click="previewImage(resolveAvatar(messages[vItem.index].content))"
                />
                <!-- 文件消息 -->
                <a
                  v-else-if="messages[vItem.index].type === 'file'"
                  :href="fileUrl(messages[vItem.index].content)"
                  target="_blank"
                  rel="noopener"
                  :class="[
                    'flex max-w-full items-center gap-2 rounded-lg px-3.5 py-2 text-sm leading-relaxed hover:underline',
                    isMe(messages[vItem.index]) ? 'bg-theme text-white' : 'bg-g-300/60 text-g-900'
                  ]"
                >
                  <ElIcon>
                    <Paperclip />
                  </ElIcon>
                  <span class="truncate">{{ resolveFileName(messages[vItem.index].content) }}</span>
                </a>
                <!-- 文本消息 -->
                <div
                  v-else
                  :class="[
                    'max-w-full break-words rounded-lg px-3.5 py-2 text-sm leading-relaxed',
                    isMe(messages[vItem.index]) ? 'bg-theme text-white' : 'bg-g-300/60 text-g-900'
                  ]"
                >
                  {{ messages[vItem.index].content }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天输入区域 -->
        <div v-if="currentConversation" class="border-t-d px-4 py-3">
          <ElInput
            ref="messageInputRef"
            v-model="messageText"
            type="textarea"
            :rows="2"
            placeholder="输入消息，Enter发送"
            resize="none"
            @keyup.enter.prevent="handleSend"
          />
          <div class="mt-2 flex-cb">
            <div class="flex-c gap-2 text-g-500">
              <span
                class="c-p flex-c gap-1 text-base hover:text-g-700"
                title="发送图片"
                @click="imageInputRef?.click()"
              >
                <ArtSvgIcon icon="ri:image-line" class="text-lg" />
                <span class="text-xs">图片</span>
              </span>
              <ElPopover
                v-model:visible="showEmoji"
                :width="280"
                trigger="click"
                placement="top-start"
              >
                <template #reference>
                  <span class="c-p flex-c gap-1 text-base hover:text-g-700" title="发送表情">
                    <ArtSvgIcon icon="ri:emotion-happy-line" class="text-lg" />
                    <span class="text-xs">表情</span>
                  </span>
                </template>
                <div class="grid grid-cols-8 gap-1">
                  <span
                    v-for="e in emojiList"
                    :key="e"
                    class="c-p cursor-pointer rounded p-1 text-center text-lg hover:bg-g-300/60"
                    @click="insertEmoji(e)"
                    >{{ e }}</span
                  >
                </div>
              </ElPopover>
              <span
                class="c-p flex-c gap-1 text-base hover:text-g-700"
                title="发送文件"
                @click="fileInputRef?.click()"
              >
                <ArtSvgIcon icon="ri:attachment-line" class="text-lg" />
                <span class="text-xs">文件</span>
              </span>
              <ElIcon v-if="uploading" class="animate-spin text-base text-g-400">
                <Loading />
              </ElIcon>
            </div>
            <ElButton
              type="primary"
              size="small"
              :icon="Promotion"
              :loading="sending"
              @click="handleSend"
              v-ripple
            >
              发送
            </ElButton>
          </div>
        </div>

        <!-- 拖拽上传遮罩：在聊天区域内拖入图片/文件即可发送 -->
        <div
          v-if="isDragOver"
          class="pointer-events-none absolute inset-0 z-20 flex items-center justify-center bg-white/70"
        >
          <div
            class="rounded-xl border-2 border-dashed border-theme px-6 py-4 text-base font-medium text-theme"
          >
            松开鼠标即可发送图片 / 文件
          </div>
        </div>
      </div>
    </ElDrawer>

    <!-- 联系人选择弹窗 -->
    <ElDialog
      v-model="showContactDialog"
      title="选择聊天对象"
      :width="isMobile ? '90%' : '420px'"
      :close-on-click-modal="false"
      align-center
      append-to-body
    >
      <div class="py-2">
        <ElInput
          v-model="contactKeyword"
          placeholder="搜索用户名/昵称/手机号"
          clearable
          :prefix-icon="Search"
          @keyup.enter="searchContacts"
          @clear="searchContacts"
        />
        <div
          v-loading="contactLoading"
          class="mt-3 max-h-80 overflow-y-auto [&::-webkit-scrollbar]:!w-1"
          @scroll="onContactScroll"
          ref="contactListEl"
        >
          <div
            v-for="user in contacts"
            :key="user.userId"
            class="mb-1 flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-all hover:bg-g-300/40"
            @click="selectContact(user)"
          >
            <ElAvatar :size="36" :src="resolveAvatar(user.avatar)">
              {{ getAvatarText(user.nickName) }}
            </ElAvatar>
            <div class="min-w-0 flex-1">
              <div class="truncate font-medium text-g-900">{{ user.nickName }}</div>
              <div class="truncate text-xs text-g-500">
                {{ user.userName
                }}<template v-if="user.phoneNumber"> · {{ user.phoneNumber }}</template>
              </div>
            </div>
            <ElIcon class="text-g-400"><ArrowRight /></ElIcon>
          </div>
          <ElEmpty v-if="!contactLoading && contacts.length === 0" description="未找到联系人" />
          <div
            v-if="contactHasMore && contacts.length > 0"
            class="py-2 text-center text-xs text-g-400"
          >
            {{ contactLoadingMore ? '加载中...' : '向下滚动加载更多' }}
          </div>
        </div>
      </div>
    </ElDialog>

    <!-- 隐藏的文件选择器：图片与通用文件分开，便于限制类型 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      class="hidden"
      @change="onImageSelected"
    />
    <input ref="fileInputRef" type="file" class="hidden" @change="onFileSelected" />

    <!-- 图片预览遮罩 -->
    <div
      v-if="previewImageUrl"
      class="fixed inset-0 z-[3000] flex items-center justify-center bg-black/80"
      @click="previewImageUrl = null"
    >
      <img :src="previewImageUrl" class="max-h-[90%] max-w-[90%] object-contain" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import {
    ArrowLeft,
    ArrowRight,
    ChatDotRound,
    Close,
    Loading,
    Paperclip,
    Plus,
    Promotion,
    Search
  } from '@element-plus/icons-vue'
  import { useVirtualizer } from '@tanstack/vue-virtual'
  import { mittBus } from '@/utils/sys'
  import { useUserStore } from '@/store/modules/user'
  import { buildAuthUrl } from '@/utils/auth-url'
  import {
    type ChatConversationVo,
    type ChatMessageVo,
    type ChatUserVo,
    getChatOssInfo,
    getContacts,
    getConversationList,
    getConversationUnread,
    getMessageList,
    getOrCreateConversation,
    markChatRead,
    sendChatMessage,
    uploadChatFile
  } from '@/api/system/chat'

  defineOptions({ name: 'ArtChatWindow' })

  const MOBILE_BREAKPOINT = 640
  const SCROLL_DELAY = 80
  const CONTACT_PAGE_SIZE = 10
  const MESSAGE_PAGE_SIZE = 10
  const CONVERSATION_PAGE_SIZE = 10

  // 轻量内置表情面板（作为纯文本发送，无需后端改动）
  const emojiList = [
    '😀',
    '😁',
    '😂',
    '🤣',
    '😊',
    '😍',
    '😘',
    '😎',
    '🤔',
    '😴',
    '😭',
    '😡',
    '👍',
    '👎',
    '👏',
    '🙏',
    '💪',
    '🎉',
    '❤️',
    '💔',
    '🔥',
    '⭐',
    '🌹',
    '🍻',
    '😅',
    '🥰',
    '😜',
    '🤩',
    '😏',
    '😱',
    '😢',
    '😤',
    '😇',
    '🥳',
    '😋',
    '😬',
    '🤯',
    '😳',
    '🥺',
    '😈',
    '🤨',
    '🤝',
    '🐶',
    '🐱',
    '🐰',
    '🦊',
    '🐼',
    '🐸',
    '🍎',
    '🍔',
    '🍕',
    '🍟',
    '🍦',
    '🍓',
    '⚽',
    '🏀',
    '🎮',
    '🎵',
    '📷',
    '💡',
    '✅',
    '❌',
    '❓',
    '⚠️',
    '💯',
    '✨',
    '🚀',
    '🌈',
    '☀️',
    '🌙',
    '⚡',
    '🎁'
  ]

  const { width } = useWindowSize()
  const isMobile = computed(() => width.value < MOBILE_BREAKPOINT)

  const userStore = useUserStore()
  const currentUserId = computed(() => userStore.getUserId)

  const isDrawerVisible = ref(false)
  const showContactDialog = ref(false)
  const contactKeyword = ref('')
  const contactLoading = ref(false)
  const contactLoadingMore = ref(false)
  const contacts = ref<ChatUserVo[]>([])
  const contactPageNum = ref(1)
  const contactTotal = ref(0)
  const contactListEl = ref<HTMLElement | null>(null)

  const conversations = ref<ChatConversationVo[]>([])
  // 会话列表分页加载状态（向下滚动加载更多，避免一次性加载全部会话）
  const convPageNum = ref(1)
  const convTotal = ref(0)
  const convLoadingMore = ref(false)
  const convHasMore = computed(() => conversations.value.length < convTotal.value)
  const currentConversation = ref<ChatConversationVo | null>(null)
  const messages = ref<ChatMessageVo[]>([])
  const messageText = ref('')
  const messageContainer = ref<HTMLElement | null>(null)
  const sending = ref(false)

  // 图片/文件发送相关状态
  const imageInputRef = ref<HTMLInputElement | null>(null)
  const fileInputRef = ref<HTMLInputElement | null>(null)
  const messageInputRef = ref<any>(null) // ElInput 实例，用于表情插入时定位光标
  const uploading = ref(false)
  const showEmoji = ref(false)
  // 拖拽上传时是否处于消息区上方（用于显示遮罩）
  const isDragOver = ref(false)
  // 文件消息（content 为 OSS id）的元信息缓存：ossId -> 文件名，避免重复请求
  const fileMetaCache = reactive(new Map<string, string>())
  // 已发起异步拉取的文件 id，防止重复请求
  const fileMetaFetching = new Set<string>()
  // 图片预览
  const previewImageUrl = ref<string | null>(null)

  // 消息分片加载状态（前端仅展示最近 N 条，向上滚动加载更早的消息）
  const messagePageNum = ref(1)
  const messageTotal = ref(0)
  const messageLoadingMore = ref(false)
  const messageHasMore = computed(() => messages.value.length < messageTotal.value)

  // 消息虚拟滚动：动态测量每条高度，仅渲染视口附近的项，避免长会话 DOM 膨胀。
  // options 用 computed 传入（MaybeRef），使 count 随消息数变化而响应式更新。
  const messageVirtualizerOptions = computed(() => ({
    count: messages.value.length,
    getScrollElement: () => messageContainer.value,
    estimateSize: () => 80,
    overscan: 6
  }))
  const messageVirtualizer = useVirtualizer(messageVirtualizerOptions)
  const messageVirtualItems = computed(() => messageVirtualizer.value.getVirtualItems())
  const messageTotalSize = computed(() => messageVirtualizer.value.getTotalSize())

  const contactHasMore = computed(() => contacts.value.length < contactTotal.value)

  /** 判断消息是否为当前用户发送 */
  const isMe = (msg: ChatMessageVo) => {
    const uid = currentUserId.value
    return uid != null && String(msg.senderId) === String(uid)
  }

  /**
   * 获取昵称首字（汉字取第一个字，英文取首字母大写）
   */
  const getAvatarText = (nickName?: string) => {
    if (!nickName) return '?'
    return nickName.charAt(0).toUpperCase()
  }

  /**
   * 头像 URL 解析结果缓存：同一头像 OSS ID 的预览地址是确定的，
   * 避免每次重渲染（含虚拟列表高频重算）重复拼接 /resource/oss/preview/{id}。
   * 浏览器仍按 URL 做 HTTP 缓存，相同头像不会重复请求 OSS。
   */
  const avatarUrlCache = new Map<string, string | undefined>()

  /**
   * 根据头像 OSS ID 构建可访问的预览地址
   * 参照 user-detail-drawer.vue 的实现：直接用 OSS ID 通过 /resource/oss/preview/{id} 代理接口访问
   * 不使用 @Translation 翻译的 avatarUrl，因为该值是 MinIO 直连地址，浏览器通常不可达
   */
  const resolveAvatar = (avatar?: string | number | null): string | undefined => {
    if (avatar === null || avatar === undefined || avatar === '') return undefined
    const avatarStr = String(avatar)
    if (avatarStr === 'null' || avatarStr === 'undefined' || avatarStr === '') return undefined
    if (avatarUrlCache.has(avatarStr)) return avatarUrlCache.get(avatarStr)
    let url: string | undefined
    if (avatarStr.startsWith('http://') || avatarStr.startsWith('https://')) {
      url = avatarStr
    } else if (/^\d+$/.test(avatarStr)) {
      url = buildAuthUrl('/resource/oss/preview/' + avatarStr)
    }
    avatarUrlCache.set(avatarStr, url)
    return url
  }

  /** 智能时间格式化 */
  const formatTime = (time?: string) => {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minute = 60 * 1000
    const hour = 60 * minute
    const day = 24 * hour
    if (diff < minute) return '刚刚'
    if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
    if (diff < day) return `${Math.floor(diff / hour)}小时前`
    if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const hh = String(date.getHours()).padStart(2, '0')
    const mm = String(date.getMinutes()).padStart(2, '0')
    return `${m}-${d} ${hh}:${mm}`
  }

  const scrollToBottom = () => {
    nextTick(() => {
      setTimeout(() => {
        if (messageContainer.value) {
          messageContainer.value.scrollTop = messageContainer.value.scrollHeight
        }
      }, SCROLL_DELAY)
    })
  }

  /** 刷新未读消息角标（轻量：仅拉取未读总数，不加载会话详情） */
  const refreshUnreadBadge = async () => {
    try {
      const total = await getConversationUnread()
      mittBus.emit('chatUnreadChange', total || 0)
    } catch {
      mittBus.emit('chatUnreadChange', 0)
    }
  }

  /**
   * 加载会话列表（分页）
   * @param reset true 时重置为第一页并清空，用于打开抽屉/返回列表/新建会话；
   *              false 时追加下一页（向下滚动加载更多）
   */
  const loadConversations = async (reset = false) => {
    if (reset) {
      convPageNum.value = 1
      conversations.value = []
    }
    try {
      const res = await getConversationList({
        pageNum: convPageNum.value,
        pageSize: CONVERSATION_PAGE_SIZE
      })
      const rows = res.rows || []
      if (reset) {
        conversations.value = rows
      } else {
        conversations.value.push(...rows)
      }
      convTotal.value = res.total || 0
      if (rows.length > 0) {
        convPageNum.value++
      }
    } catch {
      if (reset) {
        conversations.value = []
        convTotal.value = 0
      }
    } finally {
      await refreshUnreadBadge()
    }
  }

  /** 会话列表向下滚动加载更多 */
  const loadMoreConversations = async () => {
    if (convLoadingMore.value || !convHasMore.value) return
    convLoadingMore.value = true
    try {
      await loadConversations(false)
    } finally {
      convLoadingMore.value = false
    }
  }

  /** 会话列表滚动事件 */
  const onConversationScroll = (e: Event) => {
    const target = e.target as HTMLElement
    if (target.scrollHeight - target.scrollTop - target.clientHeight < 50) {
      loadMoreConversations()
    }
  }

  /** 收到实时消息时，若对应会话已在列表中则就地更新其最后消息与时间（不重置整列） */
  const updateConversationInList = (data: any) => {
    if (!data || data.conversationId == null) return
    const idx = conversations.value.findIndex(
      (c) => String(c.conversationId) === String(data.conversationId)
    )
    if (idx >= 0) {
      const c = conversations.value[idx]
      if (data.content != null) {
        // 图片/文件消息在会话列表里展示为占位摘要，而非 OSS id 原文
        if (data.type === 'image') {
          c.lastMessage = '[图片]'
        } else if (data.type === 'file') {
          c.lastMessage = '[文件]'
        } else {
          c.lastMessage = String(data.content).substring(0, 100)
        }
      }
      if (data.createTime != null) {
        c.lastMessageTime = data.createTime
      }
    }
  }

  /** 加载会话消息（仅展示最近 N 条，后端按创建时间倒序返回，前端反转为旧→新展示） */
  const loadMessages = async (conversationId: string | number) => {
    try {
      const res = await getMessageList(conversationId, { pageNum: 1, pageSize: MESSAGE_PAGE_SIZE })
      const rows = res.rows || []
      // 后端倒序（最新在前），反转为旧→新便于从底部向上阅读
      messages.value = rows.slice().reverse()
      messageTotal.value = res.total || 0
      messagePageNum.value = 2
      scrollToBottom()
      await nextTick()
      // 兜底：若内容未撑出滚动条（如刚打开对话框且 10 条未占满视口），
      // 因没有滚动条 onMessageScroll 不会触发，用户将无法加载更早消息，这里自动续加载到可滚动
      await ensureScrollableOrExhausted()
    } catch {
      messages.value = []
      messageTotal.value = 0
    }
  }

  /** 判断消息区是否已接近底部（用于决定是否自动滚动到底部） */
  const isNearBottom = () => {
    const el = messageContainer.value
    if (!el) return true
    return el.scrollHeight - el.scrollTop - el.clientHeight < 60
  }

  /**
   * 兜底加载：当还有更早消息（messageHasMore）但当前内容未撑出滚动条时，
   * 继续自动加载更早的一页，直到内容可滚动或已无更多。
   * 否则在“刚打开对话框且 10 条未占满视口”的场景下，因无滚动条、onMessageScroll 不会触发，
   * 用户将永远无法加载更早消息。
   */
  const ensureScrollableOrExhausted = async () => {
    const el = messageContainer.value
    if (!el || !messageHasMore.value) return
    if (el.scrollHeight > el.clientHeight + 1) return // 已可滚动，交给用户手动滚动
    const before = messages.value.length
    await loadOlderMessages()
    await nextTick()
    if (messages.value.length > before) {
      await ensureScrollableOrExhausted()
    }
  }

  /**
   * 向上滚动到顶部时加载更早的消息（后端按倒序分页，page 越大越早）。
   * 前置插入并维持当前滚动位置，避免视口跳动。
   */
  const loadOlderMessages = async () => {
    const conv = currentConversation.value
    if (!conv || messageLoadingMore.value || !messageHasMore.value) return
    messageLoadingMore.value = true
    try {
      const res = await getMessageList(conv.conversationId, {
        pageNum: messagePageNum.value,
        pageSize: MESSAGE_PAGE_SIZE
      })
      const older = (res.rows || []).slice().reverse() // 反转为旧→新，更早的在前
      if (older.length > 0) {
        const el = messageContainer.value!
        const prevHeight = el.scrollHeight
        const prevTop = el.scrollTop
        messages.value = older.concat(messages.value)
        messagePageNum.value++
        await nextTick()
        // 前置了新内容，补偿滚动高度以保持视口稳定
        el.scrollTop = el.scrollHeight - prevHeight + prevTop
      }
    } catch {
      // 忽略加载更早消息时的异常
    } finally {
      messageLoadingMore.value = false
    }
    // 若加载一页后仍未撑出滚动条（视口很高/消息很短），继续自动加载，保证可滚动
    await nextTick()
    await ensureScrollableOrExhausted()
  }

  /** 消息区滚动事件：滚到顶部附近时加载更早的消息 */
  const onMessageScroll = (e: Event) => {
    const target = e.target as HTMLElement
    if (target.scrollTop < 50) {
      loadOlderMessages()
    }
  }

  /**
   * 轮询当前会话的新消息（SSE 推送不可用时的兜底，保证接收方无需手动刷新即可看到新消息）。
   * 后端按倒序返回最新一批，反转为旧→新后仅追加本地尚不存在的新消息，避免重复与滚动抖动。
   * 仅当用户已接近底部时才自动滚动到底部，避免把正在查看历史消息的用户拽下去。
   */
  let pollTimer: ReturnType<typeof setInterval> | null = null
  const POLL_INTERVAL = 300000

  const pollMessages = async () => {
    const conv = currentConversation.value
    if (!conv) return
    try {
      const res = await getMessageList(conv.conversationId, {
        pageNum: 1,
        pageSize: MESSAGE_PAGE_SIZE
      })
      const rows = (res.rows || []).slice().reverse() // 反转为旧→新
      const existingIds = new Set(messages.value.map((m) => String(m.messageId)))
      const incoming = rows.filter((r) => !existingIds.has(String(r.messageId)))
      if (incoming.length > 0) {
        const nearBottom = isNearBottom()
        messages.value.push(...incoming)
        if (nearBottom) scrollToBottom()
        // 正在查看该会话，顺手标记已读
        markChatRead(conv.conversationId).catch(() => {})
        conv.unreadCount = 0
      }
      // 同步会话列表未读角标（轻量，不重置整列）
      await refreshUnreadBadge()
    } catch {
      // 忽略轮询异常
    }
  }

  const startPolling = () => {
    stopPolling()
    pollTimer = setInterval(pollMessages, POLL_INTERVAL)
  }

  const stopPolling = () => {
    if (pollTimer !== null) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  /** 打开会话 */
  const openConversation = async (conv: ChatConversationVo) => {
    currentConversation.value = conv
    if (conv.unreadCount && conv.unreadCount > 0) {
      markChatRead(conv.conversationId).catch(() => {})
      conv.unreadCount = 0
    }
    await loadMessages(conv.conversationId)
    await refreshUnreadBadge()
    startPolling()
  }

  /** 返回会话列表 */
  const backToList = () => {
    stopPolling()
    currentConversation.value = null
    messages.value = []
    loadConversations(true)
  }

  /** 打开联系人弹窗 */
  const openContactDialog = () => {
    showContactDialog.value = true
    contactKeyword.value = ''
    contacts.value = []
    contactPageNum.value = 1
    contactTotal.value = 0
    searchContacts()
  }

  /** 选择联系人 */
  const selectContact = async (user: ChatUserVo) => {
    try {
      const conv = await getOrCreateConversation(user.userId)
      showContactDialog.value = false
      currentConversation.value = conv
      await loadMessages(conv.conversationId)
      startPolling()
      await loadConversations(true)
    } catch {
      ElMessage.error('无法创建会话，请重试')
    }
  }

  /** 搜索联系人（重置到第一页） */
  const searchContacts = async () => {
    contactLoading.value = true
    contactPageNum.value = 1
    contacts.value = []
    try {
      const res = await getContacts(contactKeyword.value.trim(), 1, CONTACT_PAGE_SIZE)
      contacts.value = res.rows || []
      contactTotal.value = res.total || 0
    } catch {
      contacts.value = []
      contactTotal.value = 0
    } finally {
      contactLoading.value = false
    }
  }

  /** 加载更多联系人 */
  const loadMoreContacts = async () => {
    if (contactLoadingMore.value || !contactHasMore.value) return
    contactLoadingMore.value = true
    try {
      const nextPage = contactPageNum.value + 1
      const res = await getContacts(contactKeyword.value.trim(), nextPage, CONTACT_PAGE_SIZE)
      contacts.value.push(...(res.rows || []))
      contactPageNum.value = nextPage
      contactTotal.value = res.total || 0
    } catch {
      // 忽略错误
    } finally {
      contactLoadingMore.value = false
    }
  }

  /** 联系人列表滚动事件 */
  const onContactScroll = (e: Event) => {
    const target = e.target as HTMLElement
    if (target.scrollHeight - target.scrollTop - target.clientHeight < 50) {
      loadMoreContacts()
    }
  }

  /** 发送消息 */
  const handleSend = async () => {
    const text = messageText.value.trim()
    if (!text || !currentConversation.value || sending.value) return
    sending.value = true
    try {
      const msg = await sendChatMessage({
        receiverId: currentConversation.value.targetUserId,
        content: text,
        type: 'text'
      })
      // 直接追加到消息列表，实时展示
      messages.value.push(msg)
      messageText.value = ''
      scrollToBottom()
      // 更新会话列表的最后消息
      const conv = conversations.value.find(
        (c) => c.conversationId === currentConversation.value?.conversationId
      )
      if (conv) {
        conv.lastMessage = text.substring(0, 100)
        conv.lastMessageTime = msg.createTime
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '发送失败')
    } finally {
      sending.value = false
    }
  }

  /** 选择图片后上传并发送 */
  const onImageSelected = async (e: Event) => {
    const input = e.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = '' // 重置，确保可重复选择同一文件
    if (!file) return
    await uploadAndSend(file, 'image')
  }

  /** 选择文件后上传并发送 */
  const onFileSelected = async (e: Event) => {
    const input = e.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) return
    await uploadAndSend(file, 'file')
  }

  /** 拖拽事件是否携带文件（用于过滤非文件拖拽，如文本） */
  const dragHasFiles = (e: DragEvent): boolean =>
    !!e.dataTransfer && Array.from(e.dataTransfer.types || []).includes('Files')

  /** 拖拽进入消息区 */
  const onMessageDragEnter = (e: DragEvent) => {
    if (dragHasFiles(e)) isDragOver.value = true
  }

  /** 拖拽在消息区移动（preventDefault 才能触发 drop 事件） */
  const onMessageDragOver = (e: DragEvent) => {
    if (dragHasFiles(e)) isDragOver.value = true
  }

  /** 拖拽离开消息区：仅当真正离开容器（而非移动到子元素）时隐藏遮罩 */
  const onMessageDragLeave = (e: DragEvent) => {
    const related = e.relatedTarget as Node | null
    const el = messageContainer.value
    if (!related || !el || !el.contains(related)) {
      isDragOver.value = false
    }
  }

  /** 在消息区松开拖拽：逐张上传并发送图片/文件 */
  const onMessageDrop = async (e: DragEvent) => {
    isDragOver.value = false
    const dt = e.dataTransfer
    if (!dt) return
    const files = Array.from(dt.files || [])
    for (const file of files) {
      const type = file.type.startsWith('image/') ? 'image' : 'file'
      await uploadAndSend(file, type)
    }
  }

  /** 上传文件到 OSS，再用返回的 ossId 作为消息内容发送 */
  const uploadAndSend = async (file: File, type: 'image' | 'file') => {
    if (!currentConversation.value || uploading.value) return
    uploading.value = true
    try {
      const form = new FormData()
      form.append('file', file)
      const res = await uploadChatFile(form)
      await sendFileMessage(type, res.ossId, res.fileName)
    } catch (e: any) {
      ElMessage.error(e?.message || '上传失败')
    } finally {
      uploading.value = false
    }
  }

  /** 发送图片/文件消息（content 存 OSS id，由接收方按 type 渲染） */
  const sendFileMessage = async (
    type: 'image' | 'file',
    ossId: string | number,
    fileName?: string
  ) => {
    if (!currentConversation.value) return
    const msg = await sendChatMessage({
      receiverId: currentConversation.value.targetUserId,
      content: String(ossId),
      type
    })
    messages.value.push(msg)
    scrollToBottom()
    if (fileName) fileMetaCache.set(String(ossId), fileName)
    const conv = conversations.value.find(
      (c) => c.conversationId === currentConversation.value?.conversationId
    )
    if (conv) {
      conv.lastMessage = type === 'image' ? '[图片]' : '[文件]'
      conv.lastMessageTime = msg.createTime
    }
  }

  /** 在输入框光标处插入表情（作为纯文本） */
  const insertEmoji = (emoji: string) => {
    const input = messageInputRef.value
    const textarea = input?.textarea as HTMLTextAreaElement | undefined
    const val = messageText.value
    if (textarea) {
      const start = textarea.selectionStart ?? val.length
      const end = textarea.selectionEnd ?? val.length
      messageText.value = val.slice(0, start) + emoji + val.slice(end)
      nextTick(() => {
        textarea.focus()
        const pos = start + emoji.length
        textarea.setSelectionRange(pos, pos)
      })
    } else {
      messageText.value = val + emoji
    }
    showEmoji.value = false
  }

  /** 文件下载地址（走聊天专用下载接口，仅需登录，附带鉴权 token） */
  const fileUrl = (ossId: string | number) => buildAuthUrl('/resource/chat/download/' + ossId)

  /** 解析文件消息的文件名：优先读缓存，未命中则异步拉取并回填，触发重渲染 */
  const resolveFileName = (ossId: string | number): string => {
    const key = String(ossId)
    if (fileMetaCache.has(key)) return fileMetaCache.get(key)!
    if (!fileMetaFetching.has(key)) {
      fileMetaFetching.add(key)
      getChatOssInfo(key)
        .then((list) => {
          const item = (list || []).find((o) => String(o.ossId) === key)
          fileMetaCache.set(key, item ? item.originalName || item.fileName : '文件')
        })
        .catch(() => {
          fileMetaCache.set(key, '文件')
        })
    }
    return '文件'
  }

  /** 图片加载完成后尺寸才确定，通知虚拟列表重新测量，避免后续消息与图片重叠 */
  const onImageLoad = () => {
    requestAnimationFrame(() => messageVirtualizer.value?.measure())
  }

  /** 打开图片预览 */
  const previewImage = (url?: string | undefined) => {
    if (url) previewImageUrl.value = url
  }

  /** 打开聊天窗口 */
  const openChat = () => {
    isDrawerVisible.value = true
    currentConversation.value = null
    messages.value = []
    loadConversations(true)
  }

  /** 关闭聊天窗口 */
  const closeChat = () => {
    stopPolling()
    isDrawerVisible.value = false
    currentConversation.value = null
    messages.value = []
  }

  /**
   * 处理收到的实时聊天推送（来自全局 SSE 通道，经 useMessagePush 转发的 chatMessage 事件）。
   * 若当前正打开对应会话则直接追加消息并自动标记已读；否则仅刷新会话列表以更新未读角标。
   */
  const onChatMessage = async (payload: any) => {
    const data = payload?.data
    if (!data || data.conversationId == null) return
    // 推送仅发往接收方，这里再做一次防御性校验，避免其它用户收到时误处理
    if (data.receiverId != null && String(data.receiverId) !== String(currentUserId.value)) return

    const convId = currentConversation.value?.conversationId
    if (convId != null && String(convId) === String(data.conversationId)) {
      // 防御：若本地已存在该消息（SSE 与轮询可能重复推送），则跳过
      const exists = messages.value.some((m) => String(m.messageId) === String(data.messageId))
      if (!exists) {
        messages.value.push({
          messageId: data.messageId,
          conversationId: data.conversationId,
          senderId: data.senderId,
          senderNickName: data.senderNickName,
          senderAvatar: data.senderAvatar,
          receiverId: data.receiverId,
          type: data.type || 'text',
          content: data.content,
          createTime: data.createTime,
          status: '1'
        } as ChatMessageVo)
        // 仅当用户已接近底部时才自动滚动，避免打断正在查看历史的用户
        if (isNearBottom()) scrollToBottom()
      }
      // 正在查看该会话，自动标记已读
      await markChatRead(convId).catch(() => {})
      currentConversation.value!.unreadCount = 0
    }
    // 若对应会话已在列表中，就地更新最后消息；并刷新未读角标（轻量，不重置整列）
    updateConversationInList(data)
    await refreshUnreadBadge()
  }

  onMounted(() => {
    mittBus.on('openChat', openChat)
    mittBus.on('chatMessage', onChatMessage)
  })

  onUnmounted(() => {
    stopPolling()
    mittBus.off('openChat', openChat)
    mittBus.off('chatMessage', onChatMessage)
  })
</script>
