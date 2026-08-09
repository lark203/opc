<template>
  <div class="art-full-height">
    <div class="box-border flex gap-4 h-full max-md:block max-md:gap-0 max-md:h-auto">
      <!-- 左侧流程分类树 -->
      <div class="flex-shrink-0 w-58 h-full max-md:w-full max-md:h-auto max-md:mb-5">
        <ElCard class="tree-card art-card-xs flex flex-col h-full mt-0">
          <template #header>
            <b>流程分类</b>
          </template>
          <ElScrollbar>
            <ElTree
              ref="categoryTreeRef"
              :data="categoryTreeData"
              :props="treeProps"
              :expand-on-click-node="false"
              node-key="id"
              default-expand-all
              highlight-current
              @node-click="handleCategoryClick"
            />
          </ElScrollbar>
        </ElCard>
      </div>

      <!-- 右侧内容区域 -->
      <div class="flex flex-col flex-grow min-w-0">
        <ArtSearchBar
          v-model="searchForm"
          :items="formItems"
          label-width="110px"
          @reset="handleReset"
          @search="handleSearch"
        >
          <!-- 申请人选择按钮（带徽章显示已选数量） -->
          <template #createByIds>
            <ElBadge :value="selectedUserCount" :max="99" :hidden="selectedUserCount === 0">
              <ElButton type="primary" @click="openUserSelect">选择申请人</ElButton>
            </ElBadge>
          </template>
        </ArtSearchBar>

        <ElCard
          class="flex flex-col flex-1 min-h-0 art-table-card"
          body-class="flex flex-col h-full"
        >
          <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
            <template #left>
              <ElButton
                type="danger"
                v-auth="'workflow:instance:remove'"
                :disabled="selectedRows.length === 0"
                @click="handleBatchDelete"
              >
                删除
              </ElButton>
            </template>
          </ArtTableHeader>

          <ElTabs v-model="activeTab" @tab-change="handleTabChange">
            <ElTabPane label="运行中" name="running" />
            <ElTabPane label="已完成" name="finish" />
          </ElTabs>

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
    </div>

    <!-- 流程变量弹窗 -->
    <VariableDialog
      v-model:visible="variableVisible"
      :instance-id="currentInstanceId"
      :flow-name="currentFlowName"
    />

    <!-- 申请人选择弹窗 -->
    <UserSelect
      v-model:visible="userSelectVisible"
      :multiple="true"
      :user-ids="selectedUserIds"
      @confirm-call-back="handleUserSelectConfirm"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref, toRefs } from 'vue'
  import {
    ElBadge,
    ElButton,
    ElCard,
    ElMessage,
    ElMessageBox,
    ElScrollbar,
    ElTabPane,
    ElTabs,
    ElTag,
    ElTree
  } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import UserSelect from '@/components/UserSelect/index.vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { routerJump } from '@/api/workflow/workflowCommon'
  import { categoryTree, type CategoryTreeVO } from '@/api/workflow/category'
  import type { UserVO } from '@/api/system/user'
  import {
    deleteByInstanceIds,
    deleteHisByInstanceIds,
    type FlowInstanceQuery,
    type FlowInstanceVO,
    invalid,
    pageByFinish,
    pageByRunning
  } from '@/api/workflow/instance'
  import VariableDialog from './modules/variable-dialog.vue'

  /** 流程业务状态字典（wf_business_status：cancel/draft/waiting/finish 等） */
  const { wf_business_status } = toRefs(useDict('wf_business_status'))

  const categoryTreeRef = ref()
  const categoryTreeData = ref<CategoryTreeVO[]>([])
  const treeProps = { children: 'children', label: 'label' }

  /** 当前页签：running=运行中 finish=已完成 */
  const activeTab = ref<'running' | 'finish'>('running')
  const selectedRows = ref<FlowInstanceVO[]>([])

  /** 申请人选择相关状态 */
  const userSelectVisible = ref(false)
  const selectedUserIds = ref<Array<string | number>>([])
  const selectedUserCount = computed(() => selectedUserIds.value.length)

  /** 流程变量弹窗相关状态 */
  const variableVisible = ref(false)
  const currentInstanceId = ref<string | number>('')
  const currentFlowName = ref('')

  const searchForm = reactive<FlowInstanceQuery>({
    nodeName: '',
    flowName: '',
    flowCode: '',
    createByIds: '',
    category: ''
  })

  /** 搜索栏配置：createByIds 使用自定义插槽渲染为"选择申请人"按钮 */
  const formItems = computed(() => [
    { label: '', key: 'createByIds', type: 'input', props: { placeholder: '' } },
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

  /** 根据页签调用对应接口（运行中 / 已完成） */
  const fetchInstanceList = (params: FlowInstanceQuery) =>
    activeTab.value === 'running' ? pageByRunning(params) : pageByFinish(params)

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
    refreshData,
    toggleColumn
  } = useTable({
    core: {
      apiFn: fetchInstanceList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      immediate: false,
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { type: 'globalIndex', label: '序号', width: 60 },
        { prop: 'businessCode', label: '业务编码', showOverflowTooltip: true },
        { prop: 'businessTitle', label: '业务标题', showOverflowTooltip: true },
        {
          prop: 'flowName',
          label: '流程定义名称',
          showOverflowTooltip: true,
          formatter: (row: FlowInstanceVO) => `${row.flowName}v${row.version}`
        },
        { prop: 'flowCode', label: '流程定义编码', showOverflowTooltip: true },
        { prop: 'categoryName', label: '流程分类', showOverflowTooltip: true },
        { prop: 'nodeName', label: '任务名称', showOverflowTooltip: true },
        { prop: 'createByName', label: '申请人', showOverflowTooltip: true },
        {
          prop: 'version',
          label: '版本号',
          align: 'center',
          formatter: (row: FlowInstanceVO) => `v${row.version}.0`
        },
        {
          prop: 'activityStatus',
          label: '状态',
          align: 'center',
          visible: true,
          formatter: (row: FlowInstanceVO) =>
            h(
              ElTag,
              { type: row.activityStatus === 1 ? 'success' : 'danger', size: 'small' },
              () => (row.activityStatus === 1 ? '激活' : '挂起')
            )
        },
        {
          prop: 'flowStatus',
          label: '流程状态',
          align: 'center',
          formatter: (row: FlowInstanceVO) =>
            h(DictTag, { options: wf_business_status.value, value: row.flowStatus })
        },
        { prop: 'createTime', label: '启动时间' },
        { prop: 'updateTime', label: '结束时间', visible: false },
        {
          prop: 'operation',
          label: '操作',
          width: 200,
          fixed: 'right',
          formatter: (row: FlowInstanceVO) => {
            const buttons: ReturnType<typeof h>[] = []
            // 运行中页签：显示作废、删除按钮
            if (activeTab.value === 'running') {
              buttons.push(
                h(ArtButtonTable, {
                  type: 'invalid',
                  auth: 'workflow:instance:invalid',
                  onClick: () => handleInvalid(row)
                })
              )
              buttons.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  auth: 'workflow:instance:remove',
                  onClick: () => handleDelete(row)
                })
              )
            }
            // 两个页签都显示：查看、变量按钮
            buttons.push(
              h(ArtButtonTable, {
                type: 'view',
                title: '查看流程',
                auth: 'workflow:instance:query',
                onClick: () => handleView(row)
              })
            )
            buttons.push(
              h(ArtButtonTable, {
                icon: 'ri:code-line',
                iconClass: 'bg-info/12 text-info',
                title: '流程变量',
                auth: 'workflow:instance:variableQuery',
                onClick: () => handleVariable(row)
              })
            )
            return h('div', buttons)
          }
        }
      ]
    }
  })

  /** 加载流程分类树 */
  const loadCategoryTree = async () => {
    const res = await categoryTree()
    categoryTreeData.value = res
  }

  /** 点击分类树节点筛选 */
  const handleCategoryClick = (data: CategoryTreeVO) => {
    searchForm.category = data.id === '0' ? '' : String(data.id)
    handleSearch()
  }

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
    searchForm.createByIds = ''
    searchForm.category = ''
    selectedUserIds.value = []
    categoryTreeRef.value?.setCurrentKey(null)
    resetSearchParams()
    getData()
  }

  /** 页签切换：切换数据源 + 切换列可见性 */
  const handleTabChange = () => {
    // 运行中显示"状态"列，已完成显示"结束时间"列
    toggleColumn('activityStatus', activeTab.value === 'running')
    toggleColumn('updateTime', activeTab.value === 'finish')
    selectedRows.value = []
    replaceSearchParams(searchForm)
    getData()
  }

  /** 打开申请人选择弹窗 */
  const openUserSelect = () => {
    userSelectVisible.value = true
  }

  /** 确认选择申请人 */
  const handleUserSelectConfirm = (users: UserVO[]) => {
    selectedUserIds.value = users.map((u) => u.userId)
    searchForm.createByIds = selectedUserIds.value.join(',')
  }

  /** 表格选中行变化 */
  const handleSelectionChange = (rows: FlowInstanceVO[]) => {
    selectedRows.value = rows
  }

  /** 作废流程（弹窗输入作废原因） */
  const handleInvalid = async (row: FlowInstanceVO) => {
    try {
      const { value } = await ElMessageBox.prompt('请输入作废原因', '作废确认', {
        inputType: 'textarea',
        inputPlaceholder: '请输入作废原因',
        inputValidator: (val: string) => !!val?.trim() || '作废原因不能为空',
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await invalid({ id: row.id, comment: value })
      ElMessage.success('作废成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('作废失败')
      }
    }
  }

  /** 删除单个流程实例 */
  const handleDelete = async (row: FlowInstanceVO) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除流程实例「${row.businessTitle || row.flowName}」吗？`,
        '删除确认',
        { type: 'warning' }
      )
      if (activeTab.value === 'running') {
        await deleteByInstanceIds(row.id)
      } else {
        await deleteHisByInstanceIds(row.id)
      }
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /** 批量删除 */
  const handleBatchDelete = async () => {
    if (selectedRows.value.length === 0) return
    const ids = selectedRows.value.map((r) => r.id)
    try {
      await ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 个流程实例吗？`, '批量删除确认', {
        type: 'warning'
      })
      if (activeTab.value === 'running') {
        await deleteByInstanceIds(ids.join(','))
      } else {
        await deleteHisByInstanceIds(ids.join(','))
      }
      ElMessage.success('删除成功')
      selectedRows.value = []
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /** 查看流程（跳转到业务表单） */
  const handleView = (row: FlowInstanceVO) => {
    routerJump({
      businessId: row.businessId,
      taskId: row.id,
      type: 'view',
      formCustom: row.formCustom,
      formPath: row.formPath
    })
  }

  /** 查看流程变量 */
  const handleVariable = (row: FlowInstanceVO) => {
    currentInstanceId.value = row.id
    currentFlowName.value = row.flowName
    variableVisible.value = true
  }

  onMounted(async () => {
    await loadCategoryTree()
    replaceSearchParams(searchForm)
    getData()
  })
</script>
