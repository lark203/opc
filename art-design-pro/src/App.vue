<template>
  <ElConfigProvider
    size="default"
    :locale="locales[language]"
    :z-index="3000"
    :card="{
      shadow: 'never'
    }"
  >
    <!-- 错误边界：捕获动态路由页面渲染异常，避免单个页面出错卸载整个应用（白屏传染） -->
    <ErrorBoundary>
      <RouterView></RouterView>
    </ErrorBoundary>
  </ElConfigProvider>
</template>

<script setup lang="ts">
  import { useUserStore } from './store/modules/user'
  import zh from 'element-plus/es/locale/lang/zh-cn'
  import en from 'element-plus/es/locale/lang/en'
  import { systemUpgrade } from './utils/sys'
  import { toggleTransition } from './utils/ui/animation'
  import { checkStorageCompatibility } from './utils/storage'
  import { initializeTheme } from './hooks/core/useTheme'
  import { closePush, initPush } from './hooks/useMessagePush'
  import ErrorBoundary from './components/core/error-boundary/index.vue'
  import { onBeforeMount, onMounted, onUnmounted, watch } from 'vue'

  const userStore = useUserStore()
  const { language, accessToken } = storeToRefs(userStore)

  const locales = {
    zh: zh,
    en: en
  }

  onBeforeMount(() => {
    toggleTransition(true)
    initializeTheme()
  })

  onMounted(() => {
    checkStorageCompatibility()
    toggleTransition(false)
    systemUpgrade()
    // 全局启动消息 SSE（仅在已登录时连接，避免登录页空 token 反复重连）
    if (accessToken.value) {
      initPush()
    }
  })

  // 登录后 token 就绪时启动 SSE
  watch(
    () => accessToken.value,
    (token) => {
      if (token) {
        initPush()
      }
    }
  )

  onUnmounted(() => {
    closePush()
  })
</script>
