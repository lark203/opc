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
        />

        <ElCard
          class="flex flex-col flex-1 min-h-0 art-table-card"
          body-class="flex flex-col h-full"
        >
          <ArtTableHeader
            :loading="loading"
            v-model:columns="columnChecks"
            @refresh="refreshData"
          />

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
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref, toRefs } from 'vue'
  import { ElCard, ElMessage, ElMessageBox, ElScrollbar, ElTag, ElTree } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { routerJump } from '@/api/workflow/workflowCommon'
  import { categoryTree, type CategoryTreeVO } from '@/api/workflow/category'
  import {
    cancelProcessApply,
    deleteByInstanceIds,
    type FlowInstanceQuery,
    type FlowInstanceVO,
    pageByCurrent
  } from '@/api/workflow/instance'

  /** 流程业务状态字典（wf_business_status：cancel/draft/waiting/finish/back 等） */
  const { wf_business_status } = toRefs(useDict('wf_business_status'))

  /** 流程分类树相关状态 */
  const categoryTreeRef = ref()
  const categoryTreeData = ref<CategoryTreeVO[]>([])
  const treeProps = { children: 'children', label: 'label' }

  /** 表格选中行（预留批量操作） */
  const selectedRows = ref<FlowInstanceVO[]>([])

  const searchForm = reactive<FlowInstanceQuery>({
    flowCode: '',
    category: ''
  })

  /** 搜索栏配置 */
  const formItems = computed(() => [
    {
      label: '流程定义编码',
      key: 'flowCode',
      type: 'input',
      props: { clearable: true, placeholder: '请输入流程定义编码' }
    }
  ])

  /** 判断流程状态是否可编辑/删除（草稿、取消、驳回状态） */
  const isEditable = (flowStatus: string) =>
    flowStatus === 'draft' || flowStatus === 'cancel' || flowStatus === 'back'

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
      apiFn: pageByCurrent,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { type: 'globalIndex', label: '序号', width: 60 },
        { prop: 'flowName', label: '流程定义名称', showOverflowTooltip: true },
        { prop: 'flowCode', label: '流程定义编码', showOverflowTooltip: true },
        { prop: 'categoryName', label: '流程分类', showOverflowTooltip: true },
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
          formatter: (row: FlowInstanceVO) =>
            h(
              ElTag,
              {
                type: row.activityStatus === 1 ? 'success' : 'danger',
                size: 'small'
              },
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
        {
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: FlowInstanceVO) => {
            const buttons: ReturnType<typeof h>[] = []
            // 草稿/取消/驳回状态：显示编辑、删除按钮
            if (isEditable(row.flowStatus)) {
              buttons.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  auth: 'workflow:instance:edit',
                  onClick: () => handleOpen(row, 'update')
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
            // 所有状态：显示查看按钮
            buttons.push(
              h(ArtButtonTable, {
                type: 'view',
                title: '查看',
                onClick: () => handleOpen(row, 'view')
              })
            )
            // 审批中状态：显示撤销按钮
            if (row.flowStatus === 'waiting') {
              buttons.push(
                h(ArtButtonTable, {
                  icon: 'ri:notification-line',
                  iconClass: 'bg-warning/12 text-warning',
                  title: '撤销',
                  auth: 'workflow:instance:cancel',
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
    searchForm.flowCode = ''
    searchForm.category = ''
    categoryTreeRef.value?.setCurrentKey(null)
    resetSearchParams()
    getData()
  }

  /** 表格选中行变化 */
  const handleSelectionChange = (rows: FlowInstanceVO[]) => {
    selectedRows.value = rows
  }

  /** 编辑/查看（跳转到业务表单） */
  const handleOpen = (row: FlowInstanceVO, type: string) => {
    routerJump({
      businessId: row.businessId,
      taskId: row.id,
      type,
      formCustom: row.formCustom,
      formPath: row.formPath
    })
  }

  /** 删除单据（仅草稿/取消/驳回状态可删除） */
  const handleDelete = async (row: FlowInstanceVO) => {
    try {
      await ElMessageBox.confirm('是否确认删除？', '删除确认', { type: 'warning' })
      await deleteByInstanceIds(row.id)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /** 撤销流程申请（仅审批中状态可撤销） */
  const handleCancel = async (row: FlowInstanceVO) => {
    try {
      await ElMessageBox.confirm('是否确认撤销当前单据？', '撤销确认', { type: 'warning' })
      await cancelProcessApply({
        businessId: row.businessId,
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

  onMounted(async () => {
    await loadCategoryTree()
    replaceSearchParams(searchForm)
    getData()
  })
</script>
