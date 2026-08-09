<template>
  <div class="online-page art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap></ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { forceLogout, listOnline, type OnlineQuery, type OnlineVO } from '@/api/monitor/online'
  import { parseTime } from '@/utils/ruoyi'

  let searchForm = reactive<OnlineQuery>({
    ipaddr: '',
    userName: ''
  })

  const formItems = computed(() => [
    {
      label: '登录地址',
      key: 'ipaddr',
      type: 'input',
      props: { clearable: true, placeholder: '请输入登录地址' }
    },
    {
      label: '用户名称',
      key: 'userName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入用户名称' }
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
      apiFn: listOnline,
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
        { type: 'index', width: 60, label: '序号' },
        { prop: 'tokenId', label: '会话编号', showOverflowTooltip: true },
        { prop: 'userName', label: '登录名称', width: 120 },
        { prop: 'clientKey', label: '客户端', width: 110 },
        {
          prop: 'deviceType',
          label: '设备类型',
          width: 100,
          align: 'center',
          formatter: (row: OnlineVO) =>
            h(ElTag, { type: 'primary', size: 'small' }, () =>
              row.deviceType === 'pc'
                ? 'PC端'
                : row.deviceType === 'mobile'
                  ? '移动端'
                  : row.deviceType
            )
        },
        { prop: 'deptName', label: '所属部门', width: 130, showOverflowTooltip: true },
        { prop: 'ipaddr', label: '主机', width: 130 },
        { prop: 'loginLocation', label: '登录地点', width: 150, showOverflowTooltip: true },
        { prop: 'os', label: '操作系统', width: 120 },
        { prop: 'browser', label: '浏览器', width: 120 },
        {
          prop: 'loginTime',
          label: '登录时间',
          width: 180,
          formatter: (row: OnlineVO) => parseTime(row.loginTime)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 80,
          fixed: 'right',
          formatter: (row: OnlineVO) =>
            h(
              'button',
              {
                class: 'el-button el-button--text el-button--small',
                onClick: () => handleForceLogout(row)
              },
              '强退'
            )
        }
      ]
    }
  })

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.ipaddr = ''
    searchForm.userName = ''
    resetSearchParams()
    getData()
  }

  const handleForceLogout = async (row: OnlineVO) => {
    try {
      await ElMessageBox.confirm(`确定要强退用户"${row.userName}"吗？`, '强退确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await forceLogout(row.tokenId)
      ElMessage.success('强退成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('强退失败')
      }
    }
  }
</script>
