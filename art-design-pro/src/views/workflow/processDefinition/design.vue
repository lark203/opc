<template>
  <div class="h-full w-full">
    <iframe
      :src="src"
      frameborder="0"
      class="h-full w-full min-h-[calc(100vh-120px)] border-none"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeUnmount, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { buildWarmFlowUrl } from '@/api/workflow/workflowCommon'

  const route = useRoute()
  const router = useRouter()

  const src = computed(() =>
    buildWarmFlowUrl('index.html', {
      id: String(route.query.definitionId || ''),
      onlyDesignShow: 'true',
      disabled: String(route.query.disabled || 'false')
    })
  )

  const handleMessage = (event: MessageEvent) => {
    const data = event.data
    if (data && data.method === 'close') {
      router.push({
        path: '/workflow/processDefinition',
        query: { activeName: route.query.activeName }
      })
    }
  }

  onMounted(() => window.addEventListener('message', handleMessage))
  onBeforeUnmount(() => window.removeEventListener('message', handleMessage))
</script>
