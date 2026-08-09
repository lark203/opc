<template>
  <div class="art-full-height flex flex-col">
    <ArtSearchBar
      v-model="searchForm"
      :items="searchItems"
      label-width="110px"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="flex flex-col flex-1 min-h-0 art-table-card" body-class="flex flex-col h-full">
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
        <template #left>
          <ElButton type="primary" plain v-auth="'workflow:leave:add'" @click="handleAdd">
            新增
          </ElButton>
          <ElButton type="warning" plain v-auth="'workflow:leave:export'" @click="handleExport">
            导出
          </ElButton>
        </template>
      </ArtTableHeader>

      <div class="flex-1 min-h-0">
        <ArtTable
          :loading="loading"
          :data="data"
          :columns="columns"
          :pagination="pagination"
          :show-table-header="false"
          @selection-change="handleSelectionChange"
          @pagination:size-change="handleSizeChange"
          @pagination:current-change="handleCurrentChange"
        />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onActivated, reactive, ref, toRefs } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElButton, ElCard, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import http from '@/utils/http'
  import { cancelProcessApply } from '@/api/workflow/instance'
  import {
    delLeave,
    type LeaveForm,
    type LeaveQuery,
    type LeaveVO,
    listLeave
  } from '@/api/workflow/leave'

  const router = useRouter()

  /** 流程业务状态字典（wf_business_status：cancel/draft/waiting/finish/back 等） */
  const { wf_business_status } = toRefs(useDict('wf_business_status'))

  /** 请假类型选项（后端未提供字典，使用固定选项） */
  const leaveTypeOptions = [
    { value: '1', label: '事假' },
    { value: '2', label: '调休' },
    { value: '3', label: '病假' },
    { value: '4', label: '婚假' }
  ]

  /** 获取请假类型标签文本 */
  const getLeaveTypeLabel = (value: string) =>
    leaveTypeOptions.find((e) => e.value === value)?.label || value

  /** 表格选中行 */
  const selectedRows = ref<LeaveVO[]>([])

  const searchForm = reactive<LeaveQuery>({
    startLeaveDays: undefined,
    endLeaveDays: undefined
  })

  /** 搜索栏配置 */
  const searchItems = computed(() => [
    {
      label: '请假天数',
      key: 'startLeaveDays',
      type: 'input',
      props: { clearable: true, placeholder: '请输入请假天数' }
    },
    {
      label: '至',
      key: 'endLeaveDays',
      type: 'input',
      props: { clearable: true, placeholder: '请输入请假天数' }
    }
  ])

  /** 判断流程状态是否可编辑/删除（草稿、取消、驳回状态） */
  const isEditable = (status?: string) =>
    status === 'draft' || status === 'cancel' || status === 'back'

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
      apiFn: listLeave,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      immediate: false,
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { type: 'globalIndex', label: '序号', width: 60 },
        {
          prop: 'leaveType',
          label: '请假类型',
          align: 'center',
          formatter: (row: LeaveVO) =>
            h(ElTag, { type: 'info' }, () => getLeaveTypeLabel(row.leaveType))
        },
        { prop: 'startDate', label: '开始时间', showOverflowTooltip: true },
        { prop: 'endDate', label: '结束时间', showOverflowTooltip: true },
        { prop: 'leaveDays', label: '请假天数', align: 'center' },
        { prop: 'remark', label: '请假原因', showOverflowTooltip: true },
        {
          prop: 'status',
          label: '流程状态',
          align: 'center',
          formatter: (row: LeaveVO) =>
            h(DictTag, { options: wf_business_status.value, value: row.status })
        },
        {
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: LeaveVO) => {
            const buttons: ReturnType<typeof h>[] = []
            // 草稿/取消/驳回状态：显示编辑、删除按钮
            if (isEditable(row.status)) {
              buttons.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  auth: 'workflow:leave:edit',
                  onClick: () => handleUpdate(row)
                })
              )
              buttons.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  auth: 'workflow:leave:remove',
                  onClick: () => handleDelete(row)
                })
              )
            }
            // 所有状态：显示查看按钮
            buttons.push(
              h(ArtButtonTable, {
                type: 'view',
                title: '查看',
                onClick: () => handleView(row)
              })
            )
            // 审批中状态：显示撤销按钮
            if (row.status === 'waiting') {
              buttons.push(
                h(ArtButtonTable, {
                  icon: 'ri:notification-line',
                  iconClass: 'bg-warning/12 text-warning',
                  title: '撤销',
                  onClick: () => handleCancel(row)
                })
              )
            }
            return h('div', buttons)
          }
        }
      ]
    }
  })

  /** 搜索 */
  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  /** 重置搜索 */
  const handleReset = () => {
    searchForm.startLeaveDays = undefined
    searchForm.endLeaveDays = undefined
    resetSearchParams()
    getData()
  }

  /** 表格选中行变化 */
  const handleSelectionChange = (rows: LeaveVO[]) => {
    selectedRows.value = rows
  }

  /** 新增请假（跳转到编辑页） */
  const handleAdd = () => {
    router.push({ path: '/workflow/leaveEdit/index', query: { type: 'add' } })
  }

  /** 编辑请假（跳转到编辑页） */
  const handleUpdate = (row: LeaveVO) => {
    router.push({
      path: '/workflow/leaveEdit/index',
      query: { id: String(row.id), type: 'update' }
    })
  }

  /** 查看请假（跳转到编辑页） */
  const handleView = (row: LeaveVO) => {
    router.push({
      path: '/workflow/leaveEdit/index',
      query: { id: String(row.id), type: 'view' }
    })
  }

  /** 删除请假 */
  const handleDelete = async (row: LeaveVO) => {
    try {
      await ElMessageBox.confirm('是否确认删除？', '删除确认', { type: 'warning' })
      await delLeave(row.id)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /** 撤销流程申请 */
  const handleCancel = async (row: LeaveVO) => {
    try {
      await ElMessageBox.confirm('是否确认撤销当前单据？', '撤销确认', { type: 'warning' })
      await cancelProcessApply({
        businessId: String(row.id),
        message: '申请人撤销流程！'
      })
      ElMessage.success('撤销成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('撤销失败')
      }
    }
  }

  /** 导出请假列表 */
  const handleExport = () => {
    http.download('workflow/leave/export', { ...searchForm }, `leave_${new Date().getTime()}.xlsx`)
  }

  /** 页面激活时刷新数据（keep-alive 下从编辑页返回时触发） */
  onActivated(() => {
    getData()
  })
</script>
