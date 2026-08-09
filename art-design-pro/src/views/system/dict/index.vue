<!-- 字典管理页面 -->
<!-- 左右分栏布局：左侧字典类型，右侧字典数据 -->
<template>
  <div class="dict-page art-full-height">
    <!-- 左右分栏容器 -->
    <div class="dict-layout">
      <!-- 左侧：字典类型 -->
      <div class="dict-type-panel">
        <!-- 搜索栏 -->
        <DictTypeSearch
          v-model="typeSearchForm"
          @search="handleTypeSearch"
          @reset="handleTypeReset"
        />
        <ElCard class="art-table-card">
          <!-- 表格头部 -->
          <ArtTableHeader
            v-model:columns="typeColumnChecks"
            :loading="typeLoading"
            @refresh="refreshTypeData"
          >
            <template #left>
              <ElSpace wrap>
                <ElButton
                  type="primary"
                  v-auth="'system:dict:add'"
                  @click="showTypeDialog('add')"
                  v-ripple
                  >新增类型</ElButton
                >
                <ElButton
                  type="success"
                  v-auth="'system:dict:edit'"
                  :disabled="typeSingle"
                  @click="showTypeDialog('edit')"
                  v-ripple
                >
                  修改
                </ElButton>
                <ElButton
                  type="danger"
                  v-auth="'system:dict:remove'"
                  :disabled="typeMultiple"
                  @click="handleTypeDeleteBatch"
                  v-ripple
                >
                  删除
                </ElButton>
                <ElButton type="warning" @click="handleRefreshCache" v-ripple>刷新缓存</ElButton>
                <ElButton
                  type="info"
                  v-auth="'system:dict:export'"
                  @click="handleTypeExport"
                  v-ripple
                  >导出</ElButton
                >
              </ElSpace>
            </template>
          </ArtTableHeader>

          <!-- 字典类型表格 -->
          <ArtTable
            :loading="typeLoading"
            :data="typeList"
            :columns="typeColumns"
            :pagination="typePagination"
            @selection-change="handleTypeSelectionChange"
            @pagination:size-change="handleTypeSizeChange"
            @pagination:current-change="handleTypeCurrentChange"
            @row-click="handleTypeRowClick"
            highlight-current-row
          />

          <!-- 字典类型弹窗 -->
          <DictTypeDialog
            v-model:visible="typeDialogVisible"
            :type="typeDialogType"
            :data="typeDialogData"
            @submit="handleTypeSubmit"
          />
        </ElCard>
      </div>

      <!-- 右侧：字典数据 -->
      <div class="dict-data-panel">
        <!-- 搜索栏 -->
        <ArtSearchBar
          v-model="dataSearchForm"
          :items="dataSearchItems"
          :span="12"
          @search="handleDataSearch"
          @reset="handleDataReset"
        />
        <ElCard class="art-table-card">
          <!-- 表格头部 -->
          <ArtTableHeader
            v-model:columns="dataColumnChecks"
            :loading="dataLoading"
            @refresh="refreshDataData"
          >
            <template #left>
              <ElSpace wrap>
                <ElButton
                  type="primary"
                  v-auth="'system:dict:add'"
                  :disabled="!hasCurrentDict"
                  @click="showDataDialog('add')"
                  v-ripple
                >
                  新增数据
                </ElButton>
                <ElButton
                  type="success"
                  v-auth="'system:dict:edit'"
                  :disabled="dataSingle || !hasCurrentDict"
                  @click="showDataDialog('edit')"
                  v-ripple
                >
                  修改
                </ElButton>
                <ElButton
                  type="danger"
                  v-auth="'system:dict:remove'"
                  :disabled="dataMultiple || !hasCurrentDict"
                  @click="handleDataDeleteBatch"
                  v-ripple
                >
                  删除
                </ElButton>
                <ElButton
                  type="info"
                  v-auth="'system:dict:export'"
                  :disabled="!hasCurrentDict"
                  @click="handleDataExport"
                  v-ripple
                >
                  导出
                </ElButton>
              </ElSpace>
            </template>
            <template #right>
              <span v-if="hasCurrentDict" class="current-dict-label">
                当前：{{ currentDictLabel }}
              </span>
              <span v-else class="current-dict-label text-g-500">请先选择字典类型</span>
            </template>
          </ArtTableHeader>

          <!-- 字典数据表格 -->
          <ArtTable
            :loading="dataLoading"
            :data="dataList"
            :columns="dataColumns"
            :pagination="dataPagination"
            @selection-change="handleDataSelectionChange"
            @pagination:size-change="handleDataSizeChange"
            @pagination:current-change="handleDataCurrentChange"
          />

          <!-- 字典数据弹窗 -->
          <DictDataDialog
            v-model:visible="dataDialogVisible"
            :type="dataDialogType"
            :data="dataDialogData"
            :dictType="currentDictType"
            @submit="handleDataSubmit"
          />
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  // 导入依赖
  import { computed, h, reactive, ref } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'

  // 导入API
  import {
    addDictData,
    addDictType,
    delDictData,
    delDictType,
    type DictDataForm,
    type DictDataQuery,
    type DictDataVO,
    type DictTypeForm,
    type DictTypeQuery,
    type DictTypeVO,
    exportDictData,
    exportDictType,
    getDictData,
    listDictData,
    listDictType,
    refreshDictCache,
    updateDictData,
    updateDictType
  } from '@/api/system/dict'

  // 导入组件
  import DictTypeSearch from './modules/dict-type-search.vue'
  import DictTypeDialog from './modules/dict-type-dialog.vue'
  import DictDataDialog from './modules/dict-data-dialog.vue'

  // 导入Store
  import { useDictStore } from '@/store/modules/dict'
  import { useTable } from '@/hooks'

  defineOptions({ name: 'Dict' })

  // ==================== 字典类型相关 ====================

  // 字典类型搜索表单
  const typeSearchForm = ref<DictTypeQuery>({
    dictName: '',
    dictType: ''
  })

  const {
    columns: typeColumns,
    columnChecks: typeColumnChecks,
    data: typeList,
    loading: typeLoading,
    pagination: typePagination,
    getData,
    fetchData,
    replaceSearchParams,
    resetSearchParams: handleTypeReset,
    handleSizeChange: handleTypeSizeChange,
    handleCurrentChange: handleTypeCurrentChange,
    refreshData: refreshTypeData
  } = useTable({
    core: {
      apiFn: listDictType,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...typeSearchForm.value
      },
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'dictName',
          label: '字典名称',
          width: 150
        },
        {
          prop: 'dictType',
          label: '字典类型',
          width: 180,
          formatter: (row: DictTypeVO) =>
            h('span', { class: 'link-type', onClick: () => handleTypeRowClick(row) }, row.dictType)
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 150
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 180
        },
        {
          prop: 'operation',
          label: '操作',
          width: 140,
          fixed: 'right',
          formatter: (row: DictTypeVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:dict:edit',
                onClick: () => showTypeDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:dict:remove',
                onClick: () => handleTypeDelete(row)
              })
            ])
        }
      ]
    }
  })

  // 选中的字典类型
  const typeSelectedRows = ref<DictTypeVO[]>([])
  const typeSingle = computed(() => typeSelectedRows.value.length !== 1)
  const typeMultiple = computed(() => typeSelectedRows.value.length === 0)

  // 当前选中的字典类型
  const currentDict = ref<DictTypeVO | null>(null)
  const hasCurrentDict = computed(() => !!currentDict.value)
  const currentDictLabel = computed(() => {
    if (!currentDict.value) return ''
    return `${currentDict.value.dictName}`
  })
  const currentDictType = computed(() => currentDict.value?.dictType || '')

  // 字典类型弹窗
  const typeDialogVisible = ref(false)
  const typeDialogType = ref<'add' | 'edit'>('add')
  const typeDialogData = ref<Partial<DictTypeVO>>({})

  /**
   * 搜索字典类型
   */
  const handleTypeSearch = (params: DictTypeQuery) => {
    replaceSearchParams(params)
    getData()
  }

  /**
   * 字典类型行点击事件
   */
  const handleTypeRowClick = (row: DictTypeVO) => {
    // typeSelectedRows.value = [row]
    currentDict.value = row
    dataSearchForm.dictType = row.dictType
    dataPagination.current = 1
    getDataList()
  }

  /**
   * 字典类型选择变化事件, 这个是checkbox的点击事件
   */
  const handleTypeSelectionChange = (selection: DictTypeVO[]) => {
    typeSelectedRows.value = selection
  }

  /**
   * 显示字典类型弹窗
   */
  const showTypeDialog = (type: 'add' | 'edit', row?: DictTypeVO) => {
    typeDialogType.value = type
    typeDialogData.value = row || {}
    typeDialogVisible.value = true
  }

  /**
   * 字典类型提交
   */
  const handleTypeSubmit = async (formData: DictTypeForm) => {
    try {
      if (formData.dictId) {
        await updateDictType(formData)
        ElMessage.success('修改成功')
      } else {
        await addDictType(formData)
        ElMessage.success('新增成功')
      }
      typeDialogVisible.value = false
      await fetchData()
    } catch (error) {
      console.error('操作字典类型失败:', error)
    }
  }

  /**
   * 删除字典类型
   */
  const handleTypeDelete = async (row?: DictTypeVO) => {
    const dictIds = row?.dictId || typeSelectedRows.value.map((item) => item.dictId)
    if (!dictIds) return

    try {
      await ElMessageBox.confirm('确定要删除选中的字典类型吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delDictType(dictIds)
      ElMessage.success('删除成功')
      await fetchData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 批量删除字典类型
   */
  const handleTypeDeleteBatch = () => {
    handleTypeDelete()
  }

  /**
   * 刷新字典缓存
   */
  const handleRefreshCache = async () => {
    try {
      await refreshDictCache()
      useDictStore().cleanDict()
      ElMessage.success('缓存刷新成功')
    } catch (error) {
      console.error('刷新缓存失败:', error)
    }
  }

  // ==================== 字典数据相关 ====================
  // ==================== 字典数据相关 ====================

  // 字典数据列表
  const dataList = ref<DictDataVO[]>([])
  const dataLoading = ref(false)

  // 选中的字典数据
  const dataSelectedRows = ref<DictDataVO[]>([])
  const dataSingle = computed(() => dataSelectedRows.value.length !== 1)
  const dataMultiple = computed(() => dataSelectedRows.value.length === 0)

  // 字典数据搜索表单
  let dataSearchForm = reactive<DictDataQuery>({
    dictType: '',
    dictLabel: '',
    pageNum: 1,
    pageSize: 10
  })

  // 字典数据搜索项配置
  const dataSearchItems = computed(() => [
    {
      label: '字典标签',
      key: 'dictLabel',
      type: 'input',
      placeholder: '请输入字典标签',
      clearable: true,
      disabled: !hasCurrentDict.value
    }
  ])

  // 字典数据分页
  const dataPagination = reactive({
    current: 1,
    size: 10,
    total: 0,
    pageNum: 1,
    pageSize: 10
  })

  // 字典数据弹窗
  const dataDialogVisible = ref(false)
  const dataDialogType = ref<'add' | 'edit'>('add')
  const dataDialogData = ref<Partial<DictDataVO>>({})

  /**
   * 获取标签类型
   */
  const getTagType = (listClass: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
    if (listClass === 'primary' || listClass === 'default') return 'primary'
    return listClass as any
  }

  /**
   * 字典数据表格列配置
   */
  const { columns: dataColumns, columnChecks: dataColumnChecks } = useTableColumns<DictDataVO>(
    () => [
      { type: 'selection' },
      { type: 'index', width: 60, label: '序号' },
      {
        prop: 'dictLabel',
        label: '字典标签',
        width: 120,
        formatter: (row: DictDataVO) => {
          if (!row.listClass || row.listClass === '' || row.listClass === 'default') {
            return row.dictLabel
          }
          return h(
            ElTag,
            { type: getTagType(row.listClass), class: row.cssClass },
            () => row.dictLabel
          )
        }
      },
      {
        prop: 'dictValue',
        label: '字典键值',
        width: 100
      },
      {
        prop: 'dictSort',
        label: '排序',
        width: 80
      },
      {
        prop: 'remark',
        label: '备注',
        minWidth: 150
      },
      {
        prop: 'createTime',
        label: '创建时间',
        width: 180
      },
      {
        prop: 'operation',
        label: '操作',
        width: 140,
        fixed: 'right',
        formatter: (row: DictDataVO) =>
          h('div', [
            h(ArtButtonTable, {
              type: 'edit',
              auth: 'system:dict:edit',
              onClick: () => showDataDialog('edit', row)
            }),
            h(ArtButtonTable, {
              type: 'delete',
              auth: 'system:dict:remove',
              onClick: () => handleDataDelete(row)
            })
          ])
      }
    ]
  )

  /**
   * 获取字典数据列表
   */
  const getDataList = async () => {
    if (!currentDict.value) {
      dataList.value = []
      dataPagination.total = 0
      dataLoading.value = false
      return
    }
    dataLoading.value = true
    try {
      const res = await listDictData(dataSearchForm)
      dataList.value = res.rows
      dataPagination.total = res.total
      dataPagination.current = res.pageNum
      dataPagination.size = res.pageSize
      dataPagination.pageNum = res.pageNum
      dataPagination.pageSize = res.pageSize
    } catch (error) {
      console.error('获取字典数据列表失败:', error)
    } finally {
      dataLoading.value = false
    }
  }

  /**
   * 刷新字典数据
   */
  const refreshDataData = () => {
    dataSearchForm.pageNum = 1
    getDataList()
  }

  /**
   * 字典数据搜索
   */
  const handleDataSearch = () => {
    dataSearchForm.pageNum = 1
    getDataList()
  }

  /**
   * 字典数据重置搜索
   */
  const handleDataReset = () => {
    dataSearchForm.dictLabel = ''
    dataSearchForm.dictType = currentDict.value?.dictType
    dataSearchForm.pageNum = 1
    getDataList()
  }

  /**
   * 字典数据分页大小变化
   */
  const handleDataSizeChange = (size: number) => {
    dataSearchForm.pageSize = size
    dataSearchForm.pageNum = 1
    getDataList()
  }

  /**
   * 字典数据分页页码变化
   */
  const handleDataCurrentChange = (page: number) => {
    dataSearchForm.pageNum = page
    getDataList()
  }

  /**
   * 字典数据选择变化事件
   */
  const handleDataSelectionChange = (selection: DictDataVO[]) => {
    dataSelectedRows.value = selection
  }

  /**
   * 显示字典数据弹窗
   */
  const showDataDialog = async (type: 'add' | 'edit', row?: DictDataVO) => {
    if (!currentDict.value) {
      ElMessage.warning('请先选择字典类型')
      return
    }
    dataDialogType.value = type
    if (type === 'edit' && row) {
      dataDialogData.value = await getDictData(row.dictCode)
    } else {
      dataDialogData.value = {}
    }
    dataDialogVisible.value = true
  }

  /**
   * 字典数据提交
   */
  const handleDataSubmit = async (formData: DictDataForm) => {
    try {
      if (formData.dictCode) {
        await updateDictData(formData)
        ElMessage.success('修改成功')
      } else {
        await addDictData(formData)
        ElMessage.success('新增成功')
      }
      const submitDictType = dataSearchForm.dictType || ''
      if (submitDictType) {
        useDictStore().removeDict(submitDictType)
      }
      dataDialogVisible.value = false
      getDataList()
    } catch (error) {
      console.error('操作字典数据失败:', error)
    }
  }

  /**
   * 删除字典数据
   */
  const handleDataDelete = async (row?: DictDataVO) => {
    if (!currentDict.value) {
      ElMessage.warning('请先选择字典类型')
      return
    }
    const dictCodes = row?.dictCode || dataSelectedRows.value.map((item) => item.dictCode)
    if (!dictCodes) return

    try {
      await ElMessageBox.confirm('确定要删除选中的字典数据吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delDictData(dictCodes)
      const dictType = dataSearchForm.dictType || ''
      if (dictType) {
        useDictStore().removeDict(dictType)
      }
      ElMessage.success('删除成功')
      getDataList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 批量删除字典数据
   */
  const handleDataDeleteBatch = () => {
    handleDataDelete()
  }

  /**
   * 字典类型导出
   */
  const handleTypeExport = () => {
    exportDictType(typeSearchForm.value)
  }

  /**
   * 字典数据导出
   */
  const handleDataExport = () => {
    exportDictData(dataSearchForm)
  }
</script>

<style lang="scss" scoped>
  .dict-page {
    display: flex;
    flex-direction: column;
    padding: 0;
  }

  .dict-layout {
    display: flex;
    flex: 1;
    gap: 16px;
    overflow: hidden;
  }

  .dict-type-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 400px;
    max-width: 50%;
  }

  .dict-data-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 400px;
    max-width: 50%;
  }

  .current-dict-label {
    padding: 4px 12px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    background: var(--el-fill-color-light);
    border-radius: 4px;
  }

  // 链接样式，deep打破作用域
  :deep(.link-type) {
    color: var(--el-color-primary);
    //text-decoration: underline;
    cursor: pointer;
  }

  :deep(.link-type:hover) {
    color: var(--el-color-primary-light-3);
  }
</style>
