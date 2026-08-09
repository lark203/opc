<template>
  <div class="message-page art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="handleReadAll">全部已读</ElButton>
            <ElButton type="success" @click="sendDialogVisible = true">发送消息</ElButton>
            <ElButton type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete"
              >批量删除</ElButton
            >
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
    <ElDialog v-model="detailDialogVisible" title="消息详情" width="40%" align-center>
      <div v-if="currentDetail">
        <div class="flex items-center gap-2 mb-4">
          <div
            class="size-8 leading-8 text-center rounded-lg flex-cc"
            :class="getCategoryClass(currentDetail.category)"
          >
            <ArtSvgIcon
              class="text-base !bg-transparent"
              :icon="getCategoryIcon(currentDetail.category)"
            />
          </div>
          <h4 class="text-base font-medium text-g-900">{{ currentDetail.title }}</h4>
        </div>
        <div class="text-sm text-g-600 leading-relaxed whitespace-pre-wrap">
          {{ currentDetail.content || currentDetail.message }}
        </div>
        <div class="mt-4 pt-4 border-t text-xs text-g-400">
          {{ formatTime(currentDetail.createTime) }}
        </div>
      </div>
    </ElDialog>
    <MessageSendDialog v-model:visible="sendDialogVisible" @success="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { NOTICE_GROUP } from '@/utils/push-message'
  import {
    deleteMessage,
    deleteMessageBatch,
    listMessage,
    markRead,
    markReadAll,
    type MessageQuery,
    type MessageVO
  } from '@/api/system/message'
  import MessageSendDialog from './modules/message-send-dialog.vue'

  let searchForm = reactive<MessageQuery>({
    category: '',
    readStatus: ''
  })

  const detailDialogVisible = ref(false)
  const sendDialogVisible = ref(false)
  const currentDetail = reactive<MessageVO>({} as MessageVO)

  const selectedRows = ref<MessageVO[]>([])

  const formItems = computed(() => [
    {
      label: '消息分类',
      key: 'category',
      type: 'select',
      props: {
        placeholder: '请选择消息分类',
        options: [
          { label: '系统消息', value: 'system' },
          { label: '通知公告', value: 'notice' },
          { label: '工作流', value: 'workflow' },
          { label: '系统告警', value: 'alert' },
          { label: '安全告警', value: 'security' }
        ],
        clearable: true
      }
    },
    {
      label: '阅读状态',
      key: 'readStatus',
      type: 'select',
      props: {
        placeholder: '请选择阅读状态',
        options: [
          { label: '未读', value: '0' },
          { label: '已读', value: '1' }
        ],
        clearable: true
      }
    }
  ])

  const getCategoryClass = (category: string) => {
    const map: Record<string, string> = {
      [NOTICE_GROUP.SYSTEM]: 'bg-warning/12 text-warning',
      [NOTICE_GROUP.NOTICE]: 'bg-info/12 text-info',
      [NOTICE_GROUP.WORKFLOW]: 'bg-success/12 text-success',
      [NOTICE_GROUP.ALERT]: 'bg-danger/12 text-danger',
      [NOTICE_GROUP.SECURITY]: 'bg-orange-500/12 text-orange-500'
    }
    return map[category] || 'bg-g-200 text-g-600'
  }

  const getCategoryIcon = (category: string) => {
    const map: Record<string, string> = {
      [NOTICE_GROUP.SYSTEM]: 'ri:notification-3-line',
      [NOTICE_GROUP.NOTICE]: 'ri:mail-line',
      [NOTICE_GROUP.WORKFLOW]: 'ri:file-text-line',
      [NOTICE_GROUP.ALERT]: 'ri:alarm-warning-line',
      [NOTICE_GROUP.SECURITY]: 'ri:shield-check-line'
    }
    return map[category] || 'ri:message-line'
  }

  const getCategoryLabel = (category: string) => {
    const map: Record<string, string> = {
      [NOTICE_GROUP.SYSTEM]: '系统消息',
      [NOTICE_GROUP.NOTICE]: '通知公告',
      [NOTICE_GROUP.WORKFLOW]: '工作流',
      [NOTICE_GROUP.ALERT]: '系统告警',
      [NOTICE_GROUP.SECURITY]: '安全告警'
    }
    return map[category] || category
  }

  const formatTime = (timeStr: string) => {
    if (!timeStr) return ''
    return timeStr.substring(0, 19).replace('T', ' ')
  }

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
      apiFn: listMessage,
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
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'category',
          label: '分类',
          align: 'center',
          formatter: (row: MessageVO) =>
            h(ElTag, { type: getCategoryTagType(row.category), size: 'small' }, () =>
              getCategoryLabel(row.category)
            )
        },
        { prop: 'title', label: '标题', showOverflowTooltip: true },
        {
          prop: 'message',
          label: '内容摘要',
          showOverflowTooltip: true,
          formatter: (row: MessageVO) =>
            row.message?.substring(0, 50) + (row.message?.length > 50 ? '...' : '')
        },
        {
          prop: 'readStatus',
          label: '状态',
          align: 'center',
          formatter: (row: MessageVO) =>
            h(ElTag, { type: row.readStatus === '1' ? 'success' : 'warning', size: 'small' }, () =>
              row.readStatus === '1' ? '已读' : '未读'
            )
        },
        { prop: 'createTime', label: '创建时间' },
        {
          prop: 'operation',
          label: '操作',
          width: 200,
          fixed: 'right',
          formatter: (row: MessageVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'detail',
                onClick: () => handleDetail(row)
              }),
              h(ArtButtonTable, {
                type: 'edit',
                title: '标记为已读',
                disabled: row.readStatus === '1',
                onClick: () => handleMarkRead(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => handleDelete(row)
              })
            ])
        }
      ]
    }
  })

  const getCategoryTagType = (
    category: string
  ): 'primary' | 'success' | 'warning' | 'info' | 'danger' | undefined => {
    const map: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
      [NOTICE_GROUP.SYSTEM]: 'warning',
      [NOTICE_GROUP.NOTICE]: 'info',
      [NOTICE_GROUP.WORKFLOW]: 'success',
      [NOTICE_GROUP.ALERT]: 'danger',
      [NOTICE_GROUP.SECURITY]: 'warning'
    }
    return map[category]
  }

  const handleDetail = async (row: MessageVO) => {
    if (row.readStatus === '0') {
      await markRead(row.messageId)
      row.readStatus = '1'
    }
    Object.assign(currentDetail, row)
    detailDialogVisible.value = true
  }

  const handleMarkRead = async (row: MessageVO) => {
    if (row.readStatus === '1') {
      ElMessage.info('该消息已读')
      return
    }
    await markRead(row.messageId)
    row.readStatus = '1'
    ElMessage.success('已标记为已读')
  }

  const handleReadAll = async () => {
    try {
      await ElMessageBox.confirm('确定将所有消息标记为已读？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await markReadAll()
      ElMessage.success('操作成功')
      refreshData()
    } catch {
      ElMessage.error('操作失败')
    }
  }

  const handleDelete = async (row: MessageVO) => {
    try {
      await ElMessageBox.confirm('确定删除这条消息？', '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await deleteMessage(row.messageId)
      ElMessage.success('删除成功')
      refreshData()
    } catch {
      ElMessage.error('操作失败')
    }
  }

  const handleBatchDelete = async () => {
    try {
      await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 条消息？`, '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const messageIds = selectedRows.value.map((r) => r.messageId)
      await deleteMessageBatch(messageIds)
      ElMessage.success('删除成功')
      selectedRows.value = []
      refreshData()
    } catch {
      ElMessage.error('操作失败')
    }
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.category = ''
    searchForm.readStatus = ''
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: MessageVO[]) => {
    selectedRows.value = selection
  }
</script>
