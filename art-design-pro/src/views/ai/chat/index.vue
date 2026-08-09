<template>
  <div v-loading="loading" class="box-border w-full h-full min-h-[calc(100vh-120px)]">
    <iframe
      v-if="chatUrl"
      :src="chatUrl"
      frameborder="0"
      title="Snail AI"
      allow="clipboard-read; clipboard-write"
      class="w-full h-full min-h-[calc(100vh-120px)] border-none"
    ></iframe>
    <ElEmpty
      v-else-if="!loading"
      class="w-full h-full min-h-[calc(100vh-120px)]"
      :description="loadError || '正在加载 AI 会话'"
    >
      <ElButton v-if="loadError" type="primary" @click="loadChat">重新加载</ElButton>
    </ElEmpty>
  </div>
</template>

<script setup lang="ts">
  import { registerCurrentSnailUser } from '@/api/ai/agent'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'AiChat' })

  const userStore = useUserStore()

  const chatUrl = ref('')
  const loadError = ref('')
  const loading = ref(false)

  const buildChatUrl = (openId: string, trustedCredential: string): string => {
    const params = new URLSearchParams({ openId, trustedCredential })
    return `${import.meta.env.VITE_API_URL}/snail-chat/?${params.toString()}`
  }

  const loadChat = async (): Promise<void> => {
    loading.value = true
    loadError.value = ''
    chatUrl.value = ''
    try {
      const token = userStore.accessToken
      if (!token) {
        loadError.value = '登录凭证不存在，请重新登录后再试'
        return
      }
      const user = await registerCurrentSnailUser()
      if (!user?.openId) {
        loadError.value = '获取 AI 用户身份失败'
        return
      }
      chatUrl.value = buildChatUrl(user.openId, token)
    } catch {
      loadError.value = '加载 AI 会话失败，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    loadChat()
  })
</script>
