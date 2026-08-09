<!-- 缓存键详情抽屉 -->
<template>
  <ElDrawer v-model="visible" title="缓存详情" size="500px" :destroy-on-close="true">
    <div v-if="loading" class="py-10 flex-cc">
      <ElIcon class="is-loading" :size="24"><Loading /></ElIcon>
    </div>
    <div v-else-if="detail" class="space-y-4">
      <!-- 基本信息 -->
      <div class="rounded-lg bg-g-100 p-4">
        <div class="mb-3 border-b-d pb-2 text-sm font-medium text-g-700">基本信息</div>
        <div class="flex items-start gap-3 py-2">
          <span class="w-20 flex-shrink-0 text-sm text-g-500">键名</span>
          <span class="flex-1 break-all font-mono text-sm text-g-800">{{ detail.key }}</span>
        </div>
        <div class="flex items-start gap-3 py-2">
          <span class="w-20 flex-shrink-0 text-sm text-g-500">类型</span>
          <ElTag :type="getTypeTagType(detail.type)" size="small">{{ detail.type }}</ElTag>
        </div>
        <div class="flex items-start gap-3 py-2">
          <span class="w-20 flex-shrink-0 text-sm text-g-500">TTL</span>
          <span class="text-sm text-g-800">{{ formatTTL(detail.ttl) }}</span>
        </div>
        <div v-if="detail.memoryUsage" class="flex items-start gap-3 py-2">
          <span class="w-20 flex-shrink-0 text-sm text-g-500">内存占用</span>
          <span class="text-sm text-g-800">{{ formatBytes(detail.memoryUsage) }}</span>
        </div>
      </div>
      <!-- 值内容 -->
      <div class="rounded-lg bg-g-100 p-4">
        <div class="mb-3 border-b-d pb-2 text-sm font-medium text-g-700 flex-cb">
          <span>值内容</span>
          <ElButton size="small" text @click="copyValue">
            <ArtSvgIcon icon="ri:file-copy-line" class="mr-1" />复制
          </ElButton>
        </div>
        <div class="max-h-96 overflow-auto rounded-lg bg-g-200 p-3">
          <pre class="m-0 break-all whitespace-pre-wrap font-mono text-sm text-g-800">{{
            formatValue(detail.value)
          }}</pre>
        </div>
      </div>
    </div>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { ElButton, ElDrawer, ElIcon, ElMessage, ElTag } from 'element-plus'
  import { Loading } from '@element-plus/icons-vue'
  import { useClipboard } from '@vueuse/core'
  import type { CacheKeyDetailVO } from '@/api/monitor/cachekey'

  defineOptions({ name: 'CacheDetailDrawer' })

  const visible = defineModel<boolean>({ default: false })

  const props = defineProps<{
    detail: CacheKeyDetailVO | null
    loading: boolean
  }>()

  const { copy } = useClipboard()

  /** 格式化 TTL */
  const formatTTL = (ttl: number): string => {
    if (ttl === -1) return '永不过期'
    if (ttl === -2) return '已过期'
    if (ttl < 60) return `${ttl}秒`
    if (ttl < 3600) return `${Math.floor(ttl / 60)}分钟`
    if (ttl < 86400) return `${Math.floor(ttl / 3600)}小时`
    return `${Math.floor(ttl / 86400)}天`
  }

  /** 格式化字节 */
  const formatBytes = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`
    return `${(bytes / 1024 / 1024).toFixed(2)} MB`
  }

  /** 类型对应的标签色 */
  const getTypeTagType = (type: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
      object: 'primary',
      map: 'info',
      list: 'success',
      set: 'warning',
      zset: 'danger'
    }
    return map[type?.toLowerCase()] || 'info'
  }

  /** 格式化值内容 */
  const formatValue = (value: unknown): string => {
    if (value === null || value === undefined) return '-'
    if (typeof value === 'string') {
      try {
        return JSON.stringify(JSON.parse(value), null, 2)
      } catch {
        return value
      }
    }
    return JSON.stringify(value, null, 2)
  }

  /** 复制值内容 */
  const copyValue = () => {
    if (props.detail) {
      copy(formatValue(props.detail.value))
      ElMessage.success('已复制到剪贴板')
    }
  }
</script>
