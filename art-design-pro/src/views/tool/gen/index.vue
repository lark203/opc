<template>
  <div class="gen-page art-full-height">
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
            <ElButton
              type="primary"
              v-auth="'tool:gen:code'"
              :disabled="selectedRows.length === 0"
              @click="() => handleGenTable()"
            >
              生成
            </ElButton>
            <ElButton type="info" v-auth="'tool:gen:import'" @click="openImportTable"
              >导入</ElButton
            >
            <ElButton
              type="success"
              v-auth="'tool:gen:edit'"
              :disabled="selectedRows.length !== 1"
              @click="() => handleEditTable()"
            >
              修改
            </ElButton>
            <ElButton
              type="danger"
              v-auth="'tool:gen:remove'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
            >
              删除
            </ElButton>
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
    <ElDialog v-model="previewDialogVisible" title="代码预览" width="80%" top="5vh" append-to-body>
      <ElTabs v-model="previewActiveName" class="gen-preview-tabs">
        <ElTabPane
          v-for="(value, key) in previewData"
          :key="key"
          :label="getTabLabel(key)"
          :name="key"
        >
          <div class="gen-preview-toolbar">
            <ElButton size="small" @click="copyCode(value)">复制</ElButton>
          </div>
          <pre class="code-preview"><code class="hljs" v-html="highlightCode(value)"></code></pre>
        </ElTabPane>
      </ElTabs>
    </ElDialog>
    <ImportTable ref="importRef" @ok="handleSearch" />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import hljs from 'highlight.js/lib/common'
  import 'highlight.js/styles/atom-one-dark.css'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import request from '@/utils/http'
  import ImportTable from '@/views/tool/gen/modules/import-table.vue'
  import {
    delTable,
    getDataNames,
    listTable,
    previewTable,
    synchDb,
    type TableQuery,
    type TableVO
  } from '@/api/tool/gen'

  const router = useRouter()

  let searchForm = reactive<TableQuery>({
    tableName: '',
    tableComment: '',
    dataName: ''
  })

  const dataNameList = ref<string[]>([])
  const selectedRows = ref<TableVO[]>([])
  const previewDialogVisible = ref(false)
  const previewActiveName = ref('')
  const previewData = ref<Record<string, string>>({})
  const importRef = ref<InstanceType<typeof ImportTable>>()

  const formItems = computed(() => [
    {
      label: '数据源',
      key: 'dataName',
      type: 'select',
      props: {
        placeholder: '请选择/输入数据源名称',
        options: dataNameList.value.map((item) => ({ label: item, value: item })),
        clearable: true,
        filterable: true
      }
    },
    {
      label: '表名称',
      key: 'tableName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入表名称' }
    },
    {
      label: '表描述',
      key: 'tableComment',
      type: 'input',
      props: { clearable: true, placeholder: '请输入表描述' }
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
      apiFn: listTable,
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
        { type: 'selection', width: 55 },
        { type: 'index', label: '序号' },
        { prop: 'dataName', label: '数据源', showOverflowTooltip: true },
        { prop: 'tableName', label: '表名称', showOverflowTooltip: true },
        { prop: 'tableComment', label: '表描述', showOverflowTooltip: true },
        { prop: 'className', label: '实体', showOverflowTooltip: true },
        { prop: 'createTime', label: '创建时间', showOverflowTooltip: true },
        { prop: 'updateTime', label: '更新时间', showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 280,
          fixed: 'right',
          formatter: (row: TableVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'view',
                title: '预览',
                auth: 'tool:gen:preview',
                onClick: () => handlePreview(row)
              }),
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'tool:gen:edit',
                onClick: () => handleEditTable(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'tool:gen:remove',
                onClick: () => handleDelete(row)
              }),
              h(ArtButtonTable, {
                icon: 'ri:refresh-line',
                title: '同步',
                auth: 'tool:gen:edit',
                onClick: () => handleSynchDb(row)
              }),
              h(ArtButtonTable, {
                type: 'download',
                title: '生成代码',
                auth: 'tool:gen:code',
                onClick: () => handleGenTable(row)
              })
            ])
        }
      ]
    }
  })

  const getTabLabel = (key: string): string => {
    const dotIndex = key.indexOf('.ftl')
    const lastSlash = key.lastIndexOf('/')
    if (dotIndex !== -1 && lastSlash !== -1) {
      return key.substring(lastSlash + 1, dotIndex)
    }
    return key
  }

  const highlightCode = (code: string): string => {
    try {
      return hljs.highlightAuto(code).value
    } catch {
      return code.replace(/</g, '&lt;').replace(/>/g, '&gt;')
    }
  }

  const copyCode = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code)
      ElMessage.success('复制成功')
    } catch {
      ElMessage.error('复制失败')
    }
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.tableName = ''
    searchForm.tableComment = ''
    searchForm.dataName = ''
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: TableVO[]) => {
    selectedRows.value = selection
  }

  const handlePreview = async (row: TableVO) => {
    const res = await previewTable(row.tableId)
    previewData.value = res
    previewActiveName.value = Object.keys(res)[0] || ''
    previewDialogVisible.value = true
  }

  const handleEditTable = (row?: TableVO) => {
    const tableId = row?.tableId || selectedRows.value[0]?.tableId
    if (!tableId) return
    router.push(`/tool/gen-edit/index/${tableId}`)
  }

  const handleDelete = async (row?: TableVO) => {
    const tableIds = row?.tableId || selectedRows.value.map((r) => r.tableId).join(',')
    if (!tableIds) return
    try {
      await ElMessageBox.confirm(`确定要删除表编号为"${tableIds}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delTable(tableIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleSynchDb = async (row: TableVO) => {
    try {
      await ElMessageBox.confirm(`确认要强制同步"${row.tableName}"表结构吗？`, '同步确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await synchDb(row.tableId)
      ElMessage.success('同步成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('同步失败')
      }
    }
  }

  const handleGenTable = async (row?: TableVO) => {
    const currentRows = row ? [row] : selectedRows.value
    if (!currentRows.length) {
      ElMessage.error('请选择要生成的数据')
      return
    }
    const tableIdStr = currentRows.map((item) => item.tableId).join(',')
    request.download(`/tool/gen/batchGenCode?tableIdStr=${tableIdStr}`, {}, 'ruoyi.zip')
  }

  const openImportTable = () => {
    importRef.value?.show(searchForm.dataName || '')
  }

  const loadDataNames = async () => {
    const res = await getDataNames()
    dataNameList.value = res
  }

  loadDataNames()
</script>

<style lang="scss" scoped>
  .gen-preview-tabs {
    :deep(.el-tabs__content) {
      max-height: 60vh;
      overflow: auto;
    }
  }

  .gen-preview-toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 8px;
  }

  .code-preview {
    max-height: 55vh;
    padding: 12px;
    margin: 0;
    overflow: auto;
    font-family: Monaco, Menlo, 'Ubuntu Mono', Consolas, monospace;
    font-size: 12px;
    line-height: 1.5;
    border-radius: 6px;
  }
</style>
