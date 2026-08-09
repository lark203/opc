<!-- Redis 信息卡片 -->
<template>
  <ElCard class="redis-info-card" shadow="never">
    <div class="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
      <div v-for="item in infoCards" :key="item.label" class="flex-c gap-3">
        <div
          class="h-10 w-10 flex-shrink-0 rounded-lg flex-cc"
          :style="{ backgroundColor: `color-mix(in srgb, ${primaryColor} 15%, transparent)` }"
        >
          <ArtSvgIcon :icon="item.icon" :style="{ color: primaryColor }" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="truncate text-lg font-semibold text-g-900">{{ item.value }}</div>
          <div class="text-xs text-g-500">{{ item.label }}</div>
        </div>
      </div>
    </div>
  </ElCard>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElCard } from 'element-plus'
  import { useSettingStore } from '@/store/modules/setting'
  import type { RedisInfoVO } from '@/api/monitor/cachekey'

  defineOptions({ name: 'RedisInfoCard' })

  const props = defineProps<{
    info: RedisInfoVO | null
  }>()

  const settingStore = useSettingStore()

  const primaryColor = computed(() => settingStore.systemThemeColor || '#409eff')

  /** 格式化运行时间 */
  const formatUptime = (seconds: number): string => {
    if (!seconds) return '-'
    const days = Math.floor(seconds / 86400)
    const hours = Math.floor((seconds % 86400) / 3600)
    if (days > 0) return `${days}天${hours}小时`
    const minutes = Math.floor((seconds % 3600) / 60)
    if (hours > 0) return `${hours}小时${minutes}分钟`
    return `${minutes}分钟`
  }

  const infoCards = computed(() => {
    const info = props.info
    if (!info) return []
    return [
      { label: 'Redis版本', value: info.version || '-', icon: 'ri:server-line' },
      { label: '运行时间', value: formatUptime(info.uptimeInSeconds), icon: 'ri:time-line' },
      { label: '已用内存', value: info.usedMemoryHuman || '-', icon: 'ri:database-2-line' },
      { label: '键数量', value: String(info.dbSize ?? '-'), icon: 'ri:key-2-line' },
      { label: '命中率', value: info.hitRate || '-', icon: 'ri:focus-3-line' },
      { label: '连接数', value: String(info.connectedClients ?? '-'), icon: 'ri:links-line' }
    ]
  })
</script>

<style lang="scss" scoped>
  .redis-info-card {
    margin-bottom: 12px;
  }
</style>
