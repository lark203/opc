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
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData" />

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
  import { computed, h, reactive, ref, toRefs } from 'vue'
  import { ElCard } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { routerJump } from '@/api/workflow/workflowCommon'
  import { type FlowTaskVO, pageByTaskCopy, type TaskQuery } from '@/api/workflow/task'

  /** 流程业务状态字典（wf_business_status：cancel/draft/waiting/finish 等） */
  const { wf_business_status } = toRefs(useDict('wf_business_status'))

  /** 已选行（预留批量操作） */
  const selectedRows = ref<FlowTaskVO[]>([])

  const searchForm = reactive<TaskQuery>({
    nodeName: '',
    flowName: '',
    flowCode: ''
  })

  /** 搜索栏配置 */
  const searchItems = computed(() => [
    {
      label: '任务名称',
      key: 'nodeName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入任务名称' }
    },
    {
      label: '流程定义名称',
      key: 'flowName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入流程定义名称' }
    },
    {
      label: '流程定义编码',
      key: 'flowCode',
      type: 'input',
      props: { clearable: true, placeholder: '请输入流程定义编码' }
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
      apiFn: pageByTaskCopy,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { type: 'globalIndex', label: '序号', width: 60 },
        { prop: 'businessCode', label: '业务编码', showOverflowTooltip: true },
        { prop: 'businessTitle', label: '业务标题', showOverflowTooltip: true },
        { prop: 'flowName', label: '流程定义名称', showOverflowTooltip: true },
        { prop: 'flowCode', label: '流程定义编码', showOverflowTooltip: true },
        { prop: 'categoryName', label: '流程分类', showOverflowTooltip: true },
        {
          prop: 'version',
          label: '版本号',
          align: 'center',
          formatter: (row: FlowTaskVO) => (row.version ? `v${row.version}.0` : '-')
        },
        { prop: 'nodeName', label: '任务名称', showOverflowTooltip: true },
        {
          prop: 'flowStatus',
          label: '流程状态',
          align: 'center',
          formatter: (row: FlowTaskVO) =>
            h(DictTag, { options: wf_business_status.value, value: row.flowStatus })
        },
        { prop: 'createTime', label: '创建时间' },
        {
          prop: 'operation',
          label: '操作',
          width: 100,
          fixed: 'right',
          formatter: (row: FlowTaskVO) =>
            h(ArtButtonTable, {
              type: 'view',
              title: '查看',
              onClick: () => handleView(row)
            })
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
    searchForm.nodeName = ''
    searchForm.flowName = ''
    searchForm.flowCode = ''
    resetSearchParams()
    getData()
  }

  /** 表格选中行变化 */
  const handleSelectionChange = (rows: FlowTaskVO[]) => {
    selectedRows.value = rows
  }

  /** 查看任务（跳转到业务表单） */
  const handleView = (row: FlowTaskVO) => {
    routerJump({
      businessId: row.businessId,
      taskId: row.id,
      type: 'view',
      formCustom: row.formCustom,
      formPath: row.formPath
    })
  }
</script>
