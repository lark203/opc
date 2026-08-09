<template>
  <div class="cache-page art-full-height">
    <ElRow :gutter="12" class="cache-grid">
      <ElCol :span="24">
        <ElCard class="art-table-card">
          <template #header>
            <div class="toolbar-shell">
              <div class="table-heading">
                <h3>缓存概览</h3>
                <p>Redis 运行状态、资源消耗与实时统计。</p>
              </div>
            </div>
          </template>
          <div class="cache-table">
            <table style="width: 100%">
              <tbody>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">Redis版本</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">{{ cache.info.redis_version }}</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">运行模式</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">
                      {{ cache.info.redis_mode === 'standalone' ? '单机' : '集群' }}
                    </div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">端口</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">{{ cache.info.tcp_port }}</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">客户端数</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">{{ cache.info.connected_clients }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">运行时间(天)</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">{{ cache.info.uptime_in_days }}</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">使用内存</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">{{ cache.info.used_memory_human }}</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">使用CPU</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">
                      {{ parseFloat(cache.info.used_cpu_user_children || '0').toFixed(2) }}
                    </div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">内存配置</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">{{
                      cache.info.maxmemory_human || '-'
                    }}</div>
                  </td>
                </tr>
                <tr>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">AOF是否开启</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">
                      {{ cache.info.aof_enabled === '0' ? '否' : '是' }}
                    </div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">RDB是否成功</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">
                      {{ cache.info.rdb_last_bgsave_status || '-' }}
                    </div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">Key数量</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.dbSize" class="cell">{{ cache.dbSize }}</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div class="cell">网络入口/出口</div>
                  </td>
                  <td class="el-table__cell is-leaf">
                    <div v-if="cache.info" class="cell">
                      {{ cache.info.instantaneous_input_kbps || '0' }}kps/{{
                        cache.info.instantaneous_output_kbps || '0'
                      }}kps
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :lg="12">
        <ElCard class="art-table-card">
          <template #header>
            <div class="toolbar-shell">
              <div class="table-heading">
                <h3>命令统计</h3>
              </div>
            </div>
          </template>
          <div ref="commandstats" style="height: 420px" />
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :lg="12">
        <ElCard class="art-table-card">
          <template #header>
            <div class="toolbar-shell">
              <div class="table-heading">
                <h3>内存信息</h3>
              </div>
            </div>
          </template>
          <div ref="usedmemory" style="height: 420px" />
        </ElCard>
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import { onBeforeUnmount, onMounted, ref } from 'vue'
  import * as echarts from 'echarts'
  import { type CacheVO, getCache } from '@/api/monitor/cache'

  const cache = ref<Partial<CacheVO>>({})
  const commandstats = ref<HTMLElement>()
  const usedmemory = ref<HTMLElement>()
  let commandstatsInstance: echarts.ECharts | undefined
  let usedmemoryInstance: echarts.ECharts | undefined

  const handleResize = () => {
    commandstatsInstance?.resize()
    usedmemoryInstance?.resize()
  }

  const disposeCharts = () => {
    commandstatsInstance?.dispose()
    usedmemoryInstance?.dispose()
    commandstatsInstance = undefined
    usedmemoryInstance = undefined
  }

  const getList = async () => {
    try {
      const res = await getCache()
      cache.value = res
      disposeCharts()
      if (commandstats.value) {
        commandstatsInstance = echarts.init(commandstats.value)
        commandstatsInstance.setOption({
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b} : {c} ({d}%)'
          },
          series: [
            {
              name: '命令',
              type: 'pie',
              roseType: 'radius',
              radius: [15, 95],
              center: ['50%', '38%'],
              data: res.commandStats || [],
              animationEasing: 'cubicInOut',
              animationDuration: 1000
            }
          ]
        })
      }
      if (usedmemory.value && res.info) {
        usedmemoryInstance = echarts.init(usedmemory.value)
        usedmemoryInstance.setOption({
          tooltip: {
            formatter: '{b} <br/>{a} : ' + res.info.used_memory_human
          },
          series: [
            {
              name: '峰值',
              type: 'gauge',
              min: 0,
              max: 1000,
              detail: {
                formatter: res.info.used_memory_human
              },
              data: [
                {
                  value: parseFloat(res.info.used_memory_human || '0'),
                  name: '内存消耗'
                }
              ]
            }
          ]
        })
      }
    } catch (error) {
      console.error('加载缓存监控数据失败:', error)
    }
  }

  onMounted(() => {
    window.addEventListener('resize', handleResize)
    getList()
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    disposeCharts()
  })
</script>

<style lang="scss" scoped>
  .cache-grid {
    row-gap: 12px;

    // .art-table-card 自带 margin-top，会与 row-gap 叠加导致间距翻倍，
    // 且使首行卡片顶部多出一段留白，故在本页统一由 row-gap 控制垂直间距
    .art-table-card {
      margin-top: 0;
    }
  }

  .cache-table {
    overflow: hidden;
    border: 1px solid var(--el-border-color);
    border-radius: 10px;

    table {
      width: 100%;
      border-collapse: collapse;
    }

    td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid var(--el-border-color-light);
    }

    .cell {
      font-size: 14px;
    }
  }
</style>
