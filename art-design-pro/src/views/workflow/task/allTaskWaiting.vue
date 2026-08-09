<template>
  <div class="art-full-height flex flex-col">
    <ArtSearchBar
      v-model="searchForm"
      :items="searchItems"
      label-width="110px"
      @reset="handleReset"
      @search="handleSearch"
    >
      <!-- 申请人选择按钮（带徽章显示已选数量） -->
      <template #createByIds>
        <ElBadge :value="selectedUserCount" :max="99" :hidden="selectedUserCount === 0">
          <ElButton type="primary" @click="openApplyUserSelect">选择申请人</ElButton>
        </ElBadge>
      </template>
    </ArtSearchBar>

    <ElCard class="flex flex-col flex-1 min-h-0 art-table-card" body-class="flex flex-col h-full">
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
        <template #left>
          <!-- 待办任务页签下显示：修改办理人、催办按钮 -->
          <template v-if="activeTab === 'waiting'">
            <ElButton
              type="primary"
              plain
              v-auth="'workflow:task:updateAssignee'"
              :disabled="selectedRows.length === 0"
              @click="openAssigneeSelect"
            >
              修改办理人
            </ElButton>
            <ElButton
              type="warning"
              plain
              v-auth="'workflow:task:urgeTask'"
              :disabled="selectedRows.length === 0"
              @click="openUrgeDialog"
            >
              催办
            </ElButton>
          </template>
        </template>
      </ArtTableHeader>

      <ElTabs v-model="activeTab" @tab-change="handleTabChange">
        <ElTabPane label="待办任务" name="waiting" />
        <ElTabPane label="已办任务" name="finish" />
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

    <!-- 申请人选择弹窗（多选） -->
    <UserSelect
      v-model:visible="applyUserSelectVisible"
      :multiple="true"
      :user-ids="selectedUserIds"
      @confirm-call-back="handleApplyUserConfirm"
    />

    <!-- 修改办理人弹窗（单选） -->
    <UserSelect
      v-model:visible="assigneeSelectVisible"
      :multiple="false"
      @confirm-call-back="handleAssigneeConfirm"
    />

    <!-- 催办弹窗 -->
    <MessageType v-model:visible="urgeVisible" @confirm="handleUrgeConfirm" />

    <!-- 流程干预弹窗 -->
    <ProcessMeddle ref="processMeddleRef" v-model:visible="meddleVisible" @success="refreshData" />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, nextTick, onMounted, reactive, ref, toRefs } from 'vue'
  import {
    ElBadge,
    ElButton,
    ElCard,
    ElMessage,
    ElMessageBox,
    ElTabPane,
    ElTabs
  } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import UserSelect from '@/components/UserSelect/index.vue'
  import MessageType from '@/components/Process/MessageType.vue'
  import ProcessMeddle from '@/components/Process/processMeddle.vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { routerJump } from '@/api/workflow/workflowCommon'
  import type { UserVO } from '@/api/system/user'
  import {
    type FlowTaskVO,
    pageByAllTaskFinish,
    pageByAllTaskWait,
    type TaskQuery,
    updateAssignee,
    urgeTask
  } from '@/api/workflow/task'

  /** 流程业务状态字典（wf_business_status：cancel/draft/waiting/finish 等） */
  const { wf_business_status } = toRefs(useDict('wf_business_status'))
  /** 任务状态字典（wf_task_status：待处理/已处理等，仅已办任务页签使用） */
  const { wf_task_status } = toRefs(useDict('wf_task_status'))

  /** 当前页签：waiting=待办任务 finish=已办任务 */
  const activeTab = ref<'waiting' | 'finish'>('waiting')

  /** 表格选中行 */
  const selectedRows = ref<FlowTaskVO[]>([])

  /** 申请人选择相关状态 */
  const applyUserSelectVisible = ref(false)
  const selectedUserIds = ref<Array<string | number>>([])
  const selectedUserCount = computed(() => selectedUserIds.value.length)

  /** 修改办理人弹窗状态 */
  const assigneeSelectVisible = ref(false)

  /** 催办弹窗状态 */
  const urgeVisible = ref(false)

  /** 流程干预弹窗状态 */
  const meddleVisible = ref(false)
  /** ProcessMeddle 组件引用 */
  const processMeddleRef = ref<InstanceType<typeof ProcessMeddle>>()

  const searchForm = reactive<TaskQuery>({
    nodeName: '',
    flowName: '',
    flowCode: '',
    createByIds: ''
  })

  /** 搜索栏配置：createByIds 使用自定义插槽渲染为"选择申请人"按钮 */
  const searchItems = computed(() => [
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
    }
  ])

  /** 根据页签调用对应接口（待办 / 已办） */
  const fetchTaskList = (params: TaskQuery) =>
    activeTab.value === 'waiting' ? pageByAllTaskWait(params) : pageByAllTaskFinish(params)

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
      apiFn: fetchTaskList,
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
        { prop: 'flowName', label: '流程定义名称', showOverflowTooltip: true },
        { prop: 'flowCode', label: '流程定义编码', showOverflowTooltip: true },
        { prop: 'categoryName', label: '流程分类', showOverflowTooltip: true },
        {
          prop: 'version',
          label: '版本号',
          align: 'center',
          visible: false,
          formatter: (row: FlowTaskVO) => (row.version ? `v${row.version}.0` : '-')
        },
        { prop: 'nodeName', label: '任务名称', showOverflowTooltip: true },
        { prop: 'createByName', label: '申请人', showOverflowTooltip: true },
        {
          prop: 'assigneeNames',
          label: '办理人',
          showOverflowTooltip: true,
          formatter: (row: FlowTaskVO) =>
            activeTab.value === 'waiting' ? row.assigneeNames || '-' : row.approverName || '-'
        },
        {
          prop: 'flowStatus',
          label: '流程状态',
          align: 'center',
          formatter: (row: FlowTaskVO) =>
            h(DictTag, { options: wf_business_status.value, value: row.flowStatus })
        },
        {
          prop: 'flowTaskStatus',
          label: '任务状态',
          align: 'center',
          visible: false,
          formatter: (row: FlowTaskVO) =>
            h(DictTag, { options: wf_task_status.value, value: row.flowTaskStatus })
        },
        { prop: 'createTime', label: '创建时间' },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row: FlowTaskVO) => {
            const buttons: ReturnType<typeof h>[] = []
            // 两个页签都显示：查看按钮
            buttons.push(
              h(ArtButtonTable, {
                type: 'view',
                title: '查看',
                onClick: () => handleView(row)
              })
            )
            // 待办任务页签额外显示：流程干预按钮
            if (activeTab.value === 'waiting') {
              buttons.push(
                h(ArtButtonTable, {
                  icon: 'ri:settings-3-line',
                  iconClass: 'bg-primary/12 text-primary',
                  title: '流程干预',
                  auth: 'workflow:task:meddle',
                  onClick: () => handleMeddle(row)
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
    searchForm.nodeName = ''
    searchForm.flowName = ''
    searchForm.flowCode = ''
    searchForm.createByIds = ''
    selectedUserIds.value = []
    resetSearchParams()
    getData()
  }

  /** 页签切换：切换列可见性并刷新数据 */
  const handleTabChange = () => {
    // 待办页签显示"版本号"列，已办页签显示"任务状态"列
    toggleColumn('version', activeTab.value === 'waiting')
    toggleColumn('flowTaskStatus', activeTab.value === 'finish')
    selectedRows.value = []
    replaceSearchParams(searchForm)
    getData()
  }

  /** 打开申请人选择弹窗 */
  const openApplyUserSelect = () => {
    applyUserSelectVisible.value = true
  }

  /** 确认选择申请人 */
  const handleApplyUserConfirm = (users: UserVO[]) => {
    selectedUserIds.value = users.map((u) => u.userId)
    searchForm.createByIds = selectedUserIds.value.join(',')
  }

  /** 打开修改办理人弹窗 */
  const openAssigneeSelect = () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择任务')
      return
    }
    assigneeSelectVisible.value = true
  }

  /** 确认修改办理人 */
  const handleAssigneeConfirm = async (users: UserVO[]) => {
    if (!users || users.length === 0) {
      ElMessage.warning('请选择用户')
      return
    }
    try {
      await ElMessageBox.confirm('是否确认提交？', '提示', { type: 'warning' })
      const taskIds = selectedRows.value.map((r) => r.id)
      await updateAssignee(taskIds, users[0].userId)
      ElMessage.success('操作成功')
      assigneeSelectVisible.value = false
      selectedRows.value = []
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
      }
    }
  }

  /** 打开催办弹窗 */
  const openUrgeDialog = () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择任务')
      return
    }
    urgeVisible.value = true
  }

  /** 确认催办 */
  const handleUrgeConfirm = async (payload: { messageType: string[]; message: string }) => {
    try {
      await ElMessageBox.confirm('是否确认提交？', '提示', { type: 'warning' })
      const taskIds = selectedRows.value.map((r) => r.id)
      await urgeTask({
        taskIdList: taskIds,
        messageType: payload.messageType,
        message: payload.message
      })
      ElMessage.success('操作成功')
      urgeVisible.value = false
      selectedRows.value = []
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败')
      }
    }
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

  /** 流程干预（调用 ProcessMeddle 组件的 openDialog 方法） */
  const handleMeddle = (row: FlowTaskVO) => {
    nextTick(() => {
      processMeddleRef.value?.openDialog(row.id)
    })
  }

  /** 表格选中行变化 */
  const handleSelectionChange = (rows: FlowTaskVO[]) => {
    selectedRows.value = rows
  }

  onMounted(() => {
    // 初始化为待办页签列显示状态
    toggleColumn('version', true)
    toggleColumn('flowTaskStatus', false)
    replaceSearchParams(searchForm)
    getData()
  })
</script>
