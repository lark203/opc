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
          <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton
                  type="primary"
                  v-auth="'workflow:definition:add'"
                  @click="() => showDialog('add')"
                  >新增流程</ElButton
                >
                <ElButton
                  type="success"
                  v-auth="'workflow:definition:edit'"
                  :disabled="selectedRows.length !== 1"
                  @click="() => showDialog('edit')"
                  >修改</ElButton
                >
                <ElButton
                  type="danger"
                  v-auth="'workflow:definition:remove'"
                  :disabled="selectedRows.length === 0"
                  @click="() => handleDelete()"
                  >删除</ElButton
                >
                <ElButton
                  type="primary"
                  v-auth="'workflow:definition:import'"
                  @click="openUploadDialog"
                  >部署流程文件</ElButton
                >
                <ElButton
                  type="info"
                  v-auth="'workflow:definition:export'"
                  :disabled="selectedRows.length !== 1"
                  @click="handleExportDef"
                  >导出</ElButton
                >
              </ElSpace>
            </template>
          </ArtTableHeader>

          <ElTabs v-model="activeName" @tab-change="handleTabChange">
            <ElTabPane label="已发布" name="0" />
            <ElTabPane label="未发布" name="1" />
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

    <!-- 部署流程文件 -->
    <ElDialog v-model="uploadVisible" title="部署流程文件" width="480px" align-center>
      <div v-loading="uploadLoading">
        <div class="mb-3">
          <span class="text-error">*</span>
          请选择部署流程分类：
          <ElTreeSelect
            v-model="uploadCategory"
            :data="categoryTreeData"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            value-key="id"
            filterable
            check-strictly
            :render-after-expand="false"
            placeholder="请选择流程分类"
            style="width: 240px"
          />
        </div>
        <ElUpload
          drag
          multiple
          accept="application/json,application/text"
          :show-file-list="false"
          :before-upload="handleBeforeUpload"
          :http-request="handleImportDefinition"
        >
          <div class="el-upload__text"><em>点击上传，选择 JSON 流程文件</em></div>
          <div class="el-upload__text">仅支持 json 格式文件</div>
          <div class="el-upload__text">PS: 如若部署请部署从本项目流程定义导出的数据</div>
        </ElUpload>
      </div>
    </ElDialog>

    <DefinitionDialog
      v-model:visible="dialogVisible"
      :edit-data="currentData"
      :default-category="searchForm.category"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox, ElSwitch, ElTag, type UploadRequestOptions } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import DefinitionDialog from './modules/definition-dialog.vue'
  import {
    active,
    copyDefinition,
    deleteDefinition,
    exportDef,
    type FlowDefinitionQuery,
    type FlowDefinitionVO,
    importDef,
    listDefinition,
    publish,
    unPublishList
  } from '@/api/workflow/definition'
  import { categoryTree, type CategoryTreeVO } from '@/api/workflow/category'

  const route = useRoute()
  const router = useRouter()

  const categoryTreeRef = ref()
  const categoryTreeData = ref<CategoryTreeVO[]>([])
  const selectedRows = ref<FlowDefinitionVO[]>([])
  const dialogVisible = ref(false)
  const currentData = ref<FlowDefinitionVO>()
  /** 0 已发布 1 未发布 */
  const activeName = ref('0')

  const uploadVisible = ref(false)
  const uploadLoading = ref(false)
  const uploadCategory = ref<string | number>()

  const treeProps = { children: 'children', label: 'label' }

  const searchForm = reactive<FlowDefinitionQuery>({
    flowName: '',
    flowCode: '',
    category: ''
  })

  const formItems = computed(() => [
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

  /** 已发布 / 未发布 共用一套分页表格 */
  const fetchDefinitionList = (params: FlowDefinitionQuery) =>
    activeName.value === '0' ? listDefinition(params) : unPublishList(params)

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
      apiFn: fetchDefinitionList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      immediate: false,
      columnsFactory: () => [
        { type: 'selection', width: 50 },
        { prop: 'flowName', label: '流程定义名称', showOverflowTooltip: true },
        { prop: 'flowCode', label: '标识KEY', showOverflowTooltip: true },
        { prop: 'categoryName', label: '流程分类', showOverflowTooltip: true },
        {
          prop: 'version',
          label: '版本号',
          align: 'center',
          formatter: (row: FlowDefinitionVO) => `v${row.version}.0`
        },
        {
          prop: 'activityStatus',
          label: '激活状态',
          align: 'center',
          formatter: (row: FlowDefinitionVO) =>
            h(ElSwitch, {
              modelValue: Number(row.activityStatus),
              activeValue: 1,
              inactiveValue: 0,
              'onUpdate:modelValue': (val: string | number | boolean) =>
                handleProcessDefState(row, Number(val))
            })
        },
        {
          prop: 'isPublish',
          label: '发布状态',
          align: 'center',
          formatter: (row: FlowDefinitionVO) => {
            const status = Number(row.isPublish)
            if (status === 1) return h(ElTag, { type: 'success' }, () => '已发布')
            if (status === 0) return h(ElTag, { type: 'danger' }, () => '未发布')
            return h(ElTag, { type: 'info' }, () => '失效')
          }
        },
        {
          prop: 'operation',
          label: '操作',
          width: 200,
          fixed: 'right',
          align: 'center',
          formatter: (row: FlowDefinitionVO) => {
            const buttons = [
              Number(row.isPublish) === 0
                ? h(ArtButtonTable, {
                    type: 'edit',
                    title: '流程设计',
                    auth: 'workflow:definition:query',
                    onClick: () => design(row, false)
                  })
                : h(ArtButtonTable, {
                    type: 'view',
                    title: '查看流程',
                    auth: 'workflow:definition:query',
                    onClick: () => design(row, true)
                  })
            ]
            if (Number(row.isPublish) !== 1) {
              buttons.push(
                h(ArtButtonTable, {
                  icon: 'ri:checkbox-circle-line',
                  iconClass: 'bg-success/12 text-success',
                  title: '发布流程',
                  auth: 'workflow:definition:publish',
                  onClick: () => handlePublish(row)
                })
              )
            }
            buttons.push(
              h(ArtButtonTable, {
                icon: 'ri:file-copy-line',
                iconClass: 'bg-info/12 text-info',
                title: '复制流程',
                auth: 'workflow:definition:copy',
                onClick: () => handleCopyDef(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                title: '删除流程',
                auth: 'workflow:definition:remove',
                onClick: () => handleDelete(row)
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
    categoryTreeData.value = await categoryTree()
  }

  /** 流程分类树节点点击 */
  const handleCategoryClick = (node: CategoryTreeVO) => {
    searchForm.category = String(node.id) === '0' ? '' : node.id
    replaceSearchParams(searchForm)
    getData()
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.flowName = ''
    searchForm.flowCode = ''
    searchForm.category = ''
    categoryTreeRef.value?.setCurrentKey(null)
    resetSearchParams()
    getData()
  }

  const handleTabChange = () => {
    getData()
  }

  const handleSelectionChange = (selection: FlowDefinitionVO[]) => {
    selectedRows.value = selection
  }

  const showDialog = (type: 'add' | 'edit', row?: FlowDefinitionVO) => {
    currentData.value = type === 'edit' ? row || selectedRows.value[0] : undefined
    dialogVisible.value = true
  }

  /** 新增成功后跳到未发布页签 */
  const handleDialogSuccess = (isEdit: boolean) => {
    if (!isEdit) activeName.value = '1'
    getData()
  }

  /** 流程设计 / 查看流程 */
  const design = (row: FlowDefinitionVO, disabled: boolean) => {
    router.push({
      path: '/workflow/design/index',
      query: {
        definitionId: String(row.id),
        disabled: String(disabled),
        activeName: activeName.value
      }
    })
  }

  const handlePublish = async (row: FlowDefinitionVO) => {
    try {
      await ElMessageBox.confirm(
        `是否确认发布流程定义编码为【${row.flowCode}】版本为【${row.version}】的数据项？发布后会将已发布流程定义改为失效！`,
        '提示',
        { type: 'warning' }
      )
    } catch {
      return
    }
    await publish(row.id)
    ElMessage.success('发布成功')
    activeName.value = '0'
    getData()
  }

  /** 挂起 / 激活 */
  const handleProcessDefState = async (row: FlowDefinitionVO, status: number) => {
    const msg =
      status === 0
        ? `暂停后，此流程下的所有任务都不允许往后流转，您确定挂起【${row.flowName || row.flowCode}】吗？`
        : `启动后，此流程下的所有任务都允许往后流转，您确定激活【${row.flowName || row.flowCode}】吗？`
    try {
      await ElMessageBox.confirm(msg, '提示', { type: 'warning' })
    } catch {
      return
    }
    await active(row.id, status === 1)
    ElMessage.success('操作成功')
    getData()
  }

  const handleCopyDef = async (row: FlowDefinitionVO) => {
    try {
      await ElMessageBox.confirm(
        `是否确认复制【${row.flowCode}】版本为【${row.version}】的流程定义？`,
        '提示',
        { type: 'warning' }
      )
    } catch {
      return
    }
    await copyDefinition(row.id)
    ElMessage.success('操作成功')
    activeName.value = '1'
    getData()
  }

  const handleDelete = async (row?: FlowDefinitionVO) => {
    const rows = row ? [row] : selectedRows.value
    if (!rows.length) return
    const ids = rows.map((item) => item.id)
    const codes = rows.map((item) => item.flowCode).join('、')
    try {
      await ElMessageBox.confirm(`是否确认删除流程定义编码为【${codes}】的数据项？`, '删除确认', {
        type: 'warning'
      })
    } catch {
      return
    }
    await deleteDefinition(ids.join(','))
    ElMessage.success('删除成功')
    getData()
  }

  const handleExportDef = () => {
    const row = selectedRows.value[0]
    if (!row) return
    exportDef(row.id, `${row.flowCode}.json`)
  }

  const openUploadDialog = () => {
    uploadCategory.value = undefined
    uploadVisible.value = true
  }

  const handleBeforeUpload = () => {
    if (!uploadCategory.value) {
      ElMessage.error('请选择要部署的流程分类！')
      return false
    }
    return true
  }

  const handleImportDefinition = async (options: UploadRequestOptions) => {
    const formData = new FormData()
    formData.append('file', options.file)
    formData.append('category', String(uploadCategory.value))
    uploadLoading.value = true
    try {
      await importDef(formData)
      ElMessage.success('部署成功')
      uploadVisible.value = false
      activeName.value = '1'
      getData()
    } finally {
      uploadLoading.value = false
    }
  }

  onMounted(() => {
    if (route.query.activeName) {
      activeName.value = String(route.query.activeName)
      const query = { ...route.query }
      delete query.activeName
      router.replace({ path: route.path, query })
    }
    loadCategoryTree()
    getData()
  })
</script>
