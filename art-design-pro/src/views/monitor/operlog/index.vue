<template>
  <div class="operlog-page art-full-height">
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
            <ElButton type="danger" :disabled="selectedRows.length === 0" @click="handleDelete"
              >删除</ElButton
            >
            <ElButton type="danger" @click="handleClean">清空</ElButton>
            <ElButton type="info" @click="handleExport">导出</ElButton>
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
    <OperInfoDialog ref="operInfoDialogRef" />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref, toRefs } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import OperInfoDialog from './modules/oper-info-dialog.vue'
  import { useDict } from '@/utils/dict'
  import {
    cleanOperLog,
    delOperLog,
    exportOperLog,
    listOperLog,
    type OperLogQuery,
    type OperLogVO
  } from '@/api/monitor/operlog'

  const { sys_oper_type, sys_common_status, sys_device_type } = toRefs(
    useDict('sys_oper_type', 'sys_common_status', 'sys_device_type')
  )

  let searchForm = reactive<OperLogQuery>({
    operIp: '',
    title: '',
    operName: '',
    clientKey: '',
    deviceType: '',
    browser: '',
    os: '',
    businessType: '',
    status: '',
    startTime: '',
    endTime: ''
  })

  const dateRange = ref<string[]>([])
  const operInfoDialogRef = ref()

  const selectedRows = ref<OperLogVO[]>([])

  const formItems = computed(() => [
    {
      label: '操作地址',
      key: 'operIp',
      type: 'input',
      props: { clearable: true, placeholder: '请输入操作地址' }
    },
    {
      label: '系统模块',
      key: 'title',
      type: 'input',
      props: { clearable: true, placeholder: '请输入系统模块' }
    },
    {
      label: '操作人员',
      key: 'operName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入操作人员' }
    },
    {
      label: '客户端',
      key: 'clientKey',
      type: 'input',
      props: { clearable: true, placeholder: '请输入客户端' }
    },
    {
      label: '设备类型',
      key: 'deviceType',
      type: 'select',
      props: {
        placeholder: '请选择设备类型',
        options: sys_device_type.value,
        clearable: true
      }
    },
    {
      label: '浏览器',
      key: 'browser',
      type: 'input',
      props: { clearable: true, placeholder: '请输入浏览器' }
    },
    {
      label: '操作系统',
      key: 'os',
      type: 'input',
      props: { clearable: true, placeholder: '请输入操作系统' }
    },
    {
      label: '操作类型',
      key: 'businessType',
      type: 'select',
      props: {
        placeholder: '请选择操作类型',
        options: sys_oper_type.value,
        clearable: true
      }
    },
    {
      label: '操作状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择操作状态',
        options: sys_common_status.value,
        clearable: true
      }
    },
    {
      label: '操作时间',
      key: 'dateRange',
      type: 'datetimerange',
      props: {
        vModel: dateRange,
        rangeSeparator: '-',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        valueFormat: 'YYYY-MM-DD HH:mm:ss'
      }
    }
  ])

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
      apiFn: listOperLog,
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
        { prop: 'operId', label: '日志编号', showOverflowTooltip: true },
        { prop: 'title', label: '系统模块', showOverflowTooltip: true },
        {
          prop: 'businessType',
          label: '操作类型',
          align: 'center',
          formatter: (row: OperLogVO) =>
            h(DictTag, { options: sys_oper_type.value, value: row.businessType })
        },
        { prop: 'operName', label: '操作人员' },
        { prop: 'deptName', label: '所属部门' },
        { prop: 'clientKey', label: '客户端', showOverflowTooltip: true },
        {
          prop: 'deviceType',
          label: '设备类型',
          align: 'center',
          formatter: (row: OperLogVO) =>
            h(DictTag, { options: sys_device_type.value, value: row.deviceType })
        },
        { prop: 'browser', label: '浏览器', showOverflowTooltip: true },
        { prop: 'os', label: '操作系统', showOverflowTooltip: true },
        { prop: 'operIp', label: '操作地址' },
        {
          prop: 'status',
          label: '操作状态',
          align: 'center',
          formatter: (row: OperLogVO) =>
            h(DictTag, { options: sys_common_status.value, value: row.status })
        },
        { prop: 'operTime', label: '操作时间' },
        {
          prop: 'costTime',
          label: '消耗时间',
          formatter: (row: OperLogVO) => row.costTime + '毫秒'
        },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row: OperLogVO) =>
            h(ArtButtonTable, {
              type: 'view',
              onClick: () => handleDetail(row)
            })
        }
      ]
    }
  })

  const handleSearch = () => {
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.startTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    } else {
      searchForm.startTime = ''
      searchForm.endTime = ''
    }
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.operIp = ''
    searchForm.title = ''
    searchForm.operName = ''
    searchForm.clientKey = ''
    searchForm.deviceType = ''
    searchForm.browser = ''
    searchForm.os = ''
    searchForm.businessType = ''
    searchForm.status = ''
    searchForm.startTime = ''
    searchForm.endTime = ''
    dateRange.value = []
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: OperLogVO[]) => {
    selectedRows.value = selection
  }

  const handleDetail = (row: OperLogVO) => {
    operInfoDialogRef.value?.openDialog(row)
  }

  const handleDelete = async () => {
    const operIds = selectedRows.value.map((r) => r.operId).join(',')
    if (!operIds) return
    try {
      await ElMessageBox.confirm(`确定要删除日志编号为"${operIds}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delOperLog(operIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleClean = async () => {
    try {
      await ElMessageBox.confirm('确定要清空所有操作日志数据项？', '清空确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await cleanOperLog()
      ElMessage.success('清空成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('清空失败')
      }
    }
  }

  const handleExport = () => {
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.startTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    }
    exportOperLog(searchForm)
  }
</script>
