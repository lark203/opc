<template>
  <div class="cachekey-page art-full-height">
    <!-- Redis 概览 -->
    <RedisInfoCard :info="redisInfo" />

    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :show-expand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="handleRefreshAll">
        <template #left>
          <ElSpace wrap>
            <ElButton
              v-auth="'monitor:cachekey:remove'"
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              >批量删除</ElButton
            >
            <ElButton v-auth="'monitor:cachekey:clear'" type="danger" plain @click="handleClearAll"
              >清空缓存</ElButton
            >
            <ElDivider direction="vertical" />
            <span class="text-sm text-g-600">
              共 <b :style="{ color: primaryColor }">{{ pagination.total }}</b> 个键
            </span>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- 键值详情抽屉 -->
    <CacheDetailDrawer
      v-model="detailDrawerVisible"
      :detail="currentDetail"
      :loading="detailLoading"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref, type VNode } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useSettingStore } from '@/store/modules/setting'
  import RedisInfoCard from './modules/redis-info-card.vue'
  import CacheDetailDrawer from './modules/cache-detail-drawer.vue'
  import {
    type CacheKeyDetailVO,
    type CacheKeyQuery,
    type CacheKeyVO,
    clearCacheKey,
    delCacheKey,
    delCacheKeyBatch,
    getCacheKeyDetail,
    getRedisInfo,
    listCacheKey,
    type RedisInfoVO
  } from '@/api/monitor/cachekey'

  defineOptions({ name: 'CacheKey' })

  const { hasPermi } = useAuth()
  const settingStore = useSettingStore()

  const primaryColor = computed(() => settingStore.systemThemeColor || '#409eff')

  const redisInfo = ref<RedisInfoVO | null>(null)

  const searchForm = reactive<CacheKeyQuery>({
    pattern: '',
    type: ''
  })

  // 类型选项（对应后端 RType 枚举）
  const typeOptions = [
    { label: 'OBJECT', value: 'object' },
    { label: 'MAP', value: 'map' },
    { label: 'LIST', value: 'list' },
    { label: 'SET', value: 'set' },
    { label: 'ZSET', value: 'zset' }
  ]

  const formItems = computed(() => [
    {
      label: '键名',
      key: 'pattern',
      type: 'input',
      props: { clearable: true, placeholder: '支持通配符，如 user:*' }
    },
    {
      label: '类型',
      key: 'type',
      type: 'select',
      props: { clearable: true, placeholder: '请选择类型', options: typeOptions }
    }
  ])

  const selectedRows = ref<CacheKeyVO[]>([])

  const detailDrawerVisible = ref(false)
  const detailLoading = ref(false)
  const currentDetail = ref<CacheKeyDetailVO | null>(null)

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: listCacheKey,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' },
        {
          prop: 'key',
          label: '键名',
          minWidth: 300,
          showOverflowTooltip: true,
          formatter: (row: CacheKeyVO) =>
            h(
              'span',
              {
                class: 'font-mono text-sm c-p hover:underline',
                style: { color: primaryColor.value },
                onClick: () => showDetail(row)
              },
              row.key
            )
        },
        {
          prop: 'type',
          label: '类型',
          width: 100,
          align: 'center',
          formatter: (row: CacheKeyVO) =>
            h(ElTag, { type: getTypeTagType(row.type), size: 'small' }, () => row.type)
        },
        {
          prop: 'ttl',
          label: 'TTL',
          width: 140,
          align: 'center',
          formatter: (row: CacheKeyVO) =>
            h('span', { style: { color: getTtlColor(row.ttl) } }, formatTTL(row.ttl))
        },
        {
          prop: 'memoryUsage',
          label: '内存占用',
          width: 120,
          align: 'center',
          formatter: (row: CacheKeyVO) => (row.memoryUsage ? formatBytes(row.memoryUsage) : '-')
        },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          align: 'center',
          fixed: 'right',
          formatter: (row: CacheKeyVO) => {
            const buttons: VNode[] = []
            if (hasPermi('monitor:cachekey:query')) {
              buttons.push(h(ArtButtonTable, { type: 'view', onClick: () => showDetail(row) }))
            }
            if (hasPermi('monitor:cachekey:remove')) {
              buttons.push(h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) }))
            }
            return buttons.length ? h('div', { style: 'text-align: center' }, buttons) : '-'
          }
        }
      ]
    }
  })

  /** 加载 Redis 概览信息 */
  const loadRedisInfo = async () => {
    try {
      redisInfo.value = await getRedisInfo()
    } catch (error) {
      console.error('加载 Redis 信息失败:', error)
    }
  }

  /** 刷新概览与列表 */
  const handleRefreshAll = () => {
    loadRedisInfo()
    refreshData()
  }

  /** 格式化 TTL */
  const formatTTL = (ttl: number): string => {
    if (ttl === -1) return '永不过期'
    if (ttl === -2) return '已过期'
    if (ttl < 60) return `${ttl}秒`
    if (ttl < 3600) return `${Math.floor(ttl / 60)}分钟`
    if (ttl < 86400) return `${Math.floor(ttl / 3600)}小时`
    return `${Math.floor(ttl / 86400)}天`
  }

  /** TTL 对应的展示颜色 */
  const getTtlColor = (ttl: number): string => {
    if (ttl === -1) return 'var(--art-gray-500)'
    if (ttl < 60) return '#f56c6c'
    if (ttl < 3600) return '#e6a23c'
    return '#67c23a'
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

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.pattern = ''
    searchForm.type = ''
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: CacheKeyVO[]) => {
    selectedRows.value = selection
  }

  /** 查看详情 */
  const showDetail = async (row: CacheKeyVO) => {
    detailDrawerVisible.value = true
    detailLoading.value = true
    currentDetail.value = null
    try {
      currentDetail.value = await getCacheKeyDetail(row.key)
    } catch (error) {
      console.error('加载缓存详情失败:', error)
    } finally {
      detailLoading.value = false
    }
  }

  /** 删除单个缓存键 */
  const handleDelete = async (row: CacheKeyVO) => {
    try {
      await ElMessageBox.confirm(`确定要删除键"${row.key}"吗？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delCacheKey(row.key)
      ElMessage.success('删除成功')
      handleRefreshAll()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /** 批量删除缓存键 */
  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) return
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 个缓存键吗？`,
        '批量删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await delCacheKeyBatch(selectedRows.value.map((item) => item.key))
      selectedRows.value = []
      ElMessage.success('删除成功')
      handleRefreshAll()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /** 清空所有缓存 */
  const handleClearAll = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有缓存吗？此操作不可恢复！', '清空缓存', {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await clearCacheKey()
      ElMessage.success('清空成功')
      handleRefreshAll()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('清空失败')
      }
    }
  }

  onMounted(() => {
    loadRedisInfo()
  })
</script>
