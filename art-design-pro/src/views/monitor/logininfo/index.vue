<template>
  <div class="art-full-height">
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
            <ElButton
              type="primary"
              :disabled="selectedRows.length !== 1"
              @click="() => handleUnlock()"
              >解锁</ElButton
            >
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
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref, toRefs } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import {
    cleanLoginInfo,
    delLoginInfo,
    exportLoginInfo,
    listLoginInfo,
    type LoginInfoQuery,
    type LoginInfoVO,
    unlockLoginInfo
  } from '@/api/monitor/logininfo'

  const { sys_common_status, sys_device_type } = toRefs(
    useDict('sys_common_status', 'sys_device_type')
  )

  let searchForm = reactive<LoginInfoQuery>({
    userName: '',
    ipaddr: '',
    status: '',
    browser: '',
    os: '',
    deviceType: '',
    clientKey: '',
    startTime: '',
    endTime: ''
  })

  const dateRange = ref<string[]>([])
  const selectedRows = ref<LoginInfoVO[]>([])

  const formItems = computed(() => [
    {
      label: '用户账号',
      key: 'userName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入用户账号' }
    },
    {
      label: '登录地址',
      key: 'ipaddr',
      type: 'input',
      props: { clearable: true, placeholder: '请输入登录地址' }
    },
    {
      label: '浏览器',
      key: 'browser',
      type: 'input',
      props: { clearable: true, placeholder: '请输入浏览器' }
    },
    {
      label: '登录状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择登录状态',
        options: sys_common_status.value,
        clearable: true
      }
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
      label: '登录时间',
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
      apiFn: listLoginInfo,
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
        { prop: 'userName', label: '用户账号' },
        { prop: 'ipaddr', label: '登录地址' },
        { prop: 'loginLocation', label: '登录地点', showOverflowTooltip: true },
        { prop: 'browser', label: '浏览器' },
        { prop: 'os', label: '操作系统', showOverflowTooltip: true },
        { prop: 'clientKey', label: '客户端' },
        {
          prop: 'deviceType',
          label: '设备类型',
          align: 'center',
          formatter: (row: LoginInfoVO) =>
            h(DictTag, { options: sys_device_type.value, value: row.deviceType })
        },
        {
          prop: 'status',
          label: '登录状态',
          align: 'center',
          formatter: (row: LoginInfoVO) =>
            h(DictTag, { options: sys_common_status.value, value: row.status })
        },
        { prop: 'msg', label: '提示消息', showOverflowTooltip: true },
        { prop: 'loginTime', label: '访问时间' },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row: LoginInfoVO) =>
            h(ArtButtonTable, {
              type: 'unlock',
              onClick: () => handleUnlock(row)
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
    searchForm.userName = ''
    searchForm.ipaddr = ''
    searchForm.status = ''
    searchForm.browser = ''
    searchForm.os = ''
    searchForm.deviceType = ''
    searchForm.clientKey = ''
    searchForm.startTime = ''
    searchForm.endTime = ''
    dateRange.value = []
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: LoginInfoVO[]) => {
    selectedRows.value = selection
  }

  const handleDelete = async () => {
    const infoIds = selectedRows.value.map((r) => r.infoId).join(',')
    if (!infoIds) return
    try {
      await ElMessageBox.confirm(`确定要删除日志编号为"${infoIds}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delLoginInfo(infoIds)
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
      await ElMessageBox.confirm('确定要清空所有登录日志数据项？', '清空确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await cleanLoginInfo()
      ElMessage.success('清空成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('清空失败')
      }
    }
  }

  const handleUnlock = async (row?: LoginInfoVO) => {
    const userName = row?.userName || selectedRows.value[0]?.userName
    if (!userName) return
    try {
      await ElMessageBox.confirm(`确定要解锁用户"${userName}"的登录状态？`, '解锁确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await unlockLoginInfo(userName)
      ElMessage.success('解锁成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('解锁失败')
      }
    }
  }

  const handleExport = () => {
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.startTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    }
    exportLoginInfo(searchForm)
  }
</script>
