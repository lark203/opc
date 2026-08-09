<template>
  <div v-loading="loading" class="sysinfo-page art-full-height">
    <ElRow :gutter="12" class="sysinfo-grid">
      <!-- 系统信息 -->
      <ElCol :xs="24" :lg="12">
        <ElCard class="art-table-card">
          <template #header>
            <div class="card-header">
              <ArtSvgIcon icon="ri:information-line" class="mr-2" />
              <span>系统信息</span>
            </div>
          </template>
          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="系统名称">
              <ElTag type="primary">{{ info?.systemInfo?.name || '-' }}</ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="系统版本">
              <ElTag type="success">{{ info?.systemInfo?.version || '-' }}</ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="框架版本">
              {{ info?.systemInfo?.frameworkVersion || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="运行环境">
              {{ info?.systemInfo?.environment || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="启动时间">
              {{ info?.systemInfo?.startTime || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="运行时长">
              <ElTag type="info">{{ info?.systemInfo?.uptime || '-' }}</ElTag>
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </ElCol>

      <!-- 服务器信息 -->
      <ElCol :xs="24" :lg="12">
        <ElCard class="art-table-card">
          <template #header>
            <div class="card-header">
              <ArtSvgIcon icon="ri:server-line" class="mr-2" />
              <span>服务器信息</span>
            </div>
          </template>
          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="服务器名称">
              {{ info?.serverInfo?.name || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="操作系统">
              {{ info?.serverInfo?.os || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="系统架构">
              {{ info?.serverInfo?.arch || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="CPU 核心数">
              {{ info?.serverInfo?.cpuCores ?? '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="服务器IP">
              {{ info?.serverInfo?.ip || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="服务器时间">
              {{ currentTime || '-' }}
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </ElCol>

      <!-- Java 信息 -->
      <ElCol :xs="24" :lg="12">
        <ElCard class="art-table-card">
          <template #header>
            <div class="card-header">
              <ArtSvgIcon icon="ri:code-box-line" class="mr-2" />
              <span>Java 信息</span>
            </div>
          </template>
          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="Java 版本">
              {{ info?.javaInfo?.version || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="Java 供应商">
              {{ info?.javaInfo?.vendor || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="Java Home">
              <div class="text-ellipsis" :title="info?.javaInfo?.home">
                {{ info?.javaInfo?.home || '-' }}
              </div>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="JVM 名称">
              {{ info?.javaInfo?.jvmName || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="JVM 版本">
              {{ info?.javaInfo?.jvmVersion || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="运行参数">
              <div class="text-ellipsis" :title="info?.javaInfo?.args">
                {{ info?.javaInfo?.args || '-' }}
              </div>
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </ElCol>

      <!-- 内存信息 -->
      <ElCol :xs="24" :lg="12">
        <ElCard class="art-table-card">
          <template #header>
            <div class="card-header">
              <ArtSvgIcon icon="ri:database-2-line" class="mr-2" />
              <span>内存信息</span>
            </div>
          </template>
          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="总内存">
              {{ info?.memoryInfo?.total || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="已用内存">
              <ElTag :type="getMemoryTagType(info?.memoryInfo?.usedPercent || 0)">
                {{ info?.memoryInfo?.used || '-' }} ({{ info?.memoryInfo?.usedPercent || 0 }}%)
              </ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="空闲内存">
              {{ info?.memoryInfo?.free || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="JVM 总内存">
              {{ info?.memoryInfo?.jvmTotal || '-' }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="JVM 已用">
              <ElTag :type="getMemoryTagType(info?.memoryInfo?.jvmUsedPercent || 0)">
                {{ info?.memoryInfo?.jvmUsed || '-' }} ({{
                  info?.memoryInfo?.jvmUsedPercent || 0
                }}%)
              </ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="JVM 空闲">
              {{ info?.memoryInfo?.jvmFree || '-' }}
            </ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>
      </ElCol>

      <!-- 磁盘信息 -->
      <ElCol :span="24">
        <ElCard class="art-table-card">
          <template #header>
            <div class="card-header">
              <ArtSvgIcon icon="ri:hard-drive-2-line" class="mr-2" />
              <span>磁盘信息</span>
              <ElButton
                class="ml-auto"
                type="primary"
                size="small"
                :loading="loading"
                @click="loadInfo"
                >刷新</ElButton
              >
            </div>
          </template>
          <ElTable :data="info?.diskInfo || []" border>
            <ElTableColumn prop="path" label="挂载点" min-width="150" />
            <ElTableColumn prop="type" label="文件系统" width="120" />
            <ElTableColumn prop="total" label="总容量" width="120" />
            <ElTableColumn prop="used" label="已用" width="120" />
            <ElTableColumn prop="free" label="可用" width="120" />
            <ElTableColumn prop="usedPercent" label="使用率" min-width="200">
              <template #default="{ row }">
                <ElProgress
                  :percentage="row.usedPercent"
                  :color="getProgressColor(row.usedPercent)"
                  :stroke-width="16"
                />
              </template>
            </ElTableColumn>
          </ElTable>
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, onUnmounted, ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { getSysInfo, type SystemMonitorVO } from '@/api/monitor/sysinfo'

  defineOptions({ name: 'SysInfo' })

  const loading = ref(false)
  const info = ref<SystemMonitorVO | null>(null)
  const currentTime = ref('')

  let serverTime: Date | null = null
  let timer: ReturnType<typeof setInterval> | null = null

  /** 补零 */
  const pad = (value: number) => String(value).padStart(2, '0')

  /** 格式化为 yyyy-MM-dd HH:mm:ss */
  const formatDateTime = (date: Date) =>
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`

  /** 加载系统信息 */
  const loadInfo = async () => {
    loading.value = true
    try {
      const data = await getSysInfo()
      info.value = data
      if (data.serverInfo?.time) {
        // 后端返回 yyyy-MM-dd HH:mm:ss，Safari 等浏览器需替换分隔符才能解析
        serverTime = new Date(data.serverInfo.time.replace(/-/g, '/'))
        currentTime.value = formatDateTime(serverTime)
      }
    } catch (error) {
      console.error('获取系统信息失败:', error)
      ElMessage.error('获取系统信息失败')
    } finally {
      loading.value = false
    }
  }

  /** 内存使用率对应的标签色 */
  const getMemoryTagType = (percent: number): 'success' | 'warning' | 'danger' => {
    if (percent < 60) return 'success'
    if (percent < 80) return 'warning'
    return 'danger'
  }

  /** 使用率对应的进度条颜色 */
  const getProgressColor = (percent: number): string => {
    if (percent < 60) return '#67c23a'
    if (percent < 80) return '#e6a23c'
    return '#f56c6c'
  }

  onMounted(() => {
    loadInfo()
    // 服务器时间本地走秒，避免频繁请求后端
    timer = setInterval(() => {
      if (serverTime) {
        serverTime = new Date(serverTime.getTime() + 1000)
        currentTime.value = formatDateTime(serverTime)
      }
    }, 1000)
  })

  onUnmounted(() => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  })
</script>

<style lang="scss" scoped>
  .sysinfo-grid {
    row-gap: 12px;

    // .art-table-card 自带 margin-top，会与 row-gap 叠加导致间距翻倍，
    // 且使首行卡片顶部多出一段留白，故在本页统一由 row-gap 控制垂直间距
    .art-table-card {
      margin-top: 0;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
  }

  .text-ellipsis {
    max-width: 320px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: help;
  }

  :deep(.el-descriptions__label) {
    width: 120px;
    font-weight: 500;
  }
</style>
