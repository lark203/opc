<template>
  <div class="art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="flex flex-col flex-1 min-h-0 art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" v-auth="'system:config:add'" @click="showDialog('add')"
              >新增参数</ElButton
            >
            <ElButton type="danger" v-auth="'system:config:refresh'" @click="handleRefreshCache"
              >刷新缓存</ElButton
            >
            <ElButton type="info" v-auth="'system:config:export'" @click="handleExport"
              >导出</ElButton
            >
          </ElSpace>
        </template>
        <template #right>
          <ElTabs
            v-model="activeTab"
            type="card"
            size="small"
            class="config-tabs"
            @tab-change="handleTabChange"
          >
            <ElTabPane label="全部" name="" />
            <ElTabPane label="系统内置" name="Y" />
            <ElTabPane label="自定义配置" name="N" />
          </ElTabs>
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

    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增参数' : '修改参数'"
      width="20%"
      align-center
      @close="handleClose"
    >
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <ElFormItem label="参数名称" prop="configName">
          <ElInput v-model="formData.configName" placeholder="请输入参数名称" />
        </ElFormItem>
        <ElFormItem label="参数键名" prop="configKey">
          <ElInput v-model="formData.configKey" placeholder="请输入参数键名" />
        </ElFormItem>
        <ElFormItem label="参数键值" prop="configValue">
          <ElInput v-model="formData.configValue" type="textarea" placeholder="请输入参数键值" />
        </ElFormItem>
        <ElFormItem label="系统内置" prop="configType">
          <ElRadioGroup v-model="formData.configType">
            <ElRadio v-for="dict in sys_yes_no" :key="dict.value" :value="dict.value">
              {{ dict.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput v-model="formData.remark" type="textarea" placeholder="请输入备注" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="handleClose">取 消</ElButton>
          <ElButton type="primary" @click="handleSubmit">确 定</ElButton>
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, reactive, ref } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import {
    addConfig,
    type ConfigForm,
    type ConfigQuery,
    type ConfigVO,
    delConfig,
    exportConfig,
    getConfig,
    listConfig,
    refreshCache,
    updateConfig
  } from '@/api/system/config'

  const { sys_yes_no } = toRefs(useDict('sys_yes_no'))

  const searchForm = ref<ConfigQuery>({
    configName: '',
    configKey: '',
    configType: ''
  })

  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const formRef = ref()
  const activeTab = ref('')

  const formData = reactive<ConfigForm>({
    configId: undefined,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'Y',
    remark: ''
  })

  const rules = reactive({
    configName: [{ required: true, message: '参数名称不能为空', trigger: 'blur' }],
    configKey: [{ required: true, message: '参数键名不能为空', trigger: 'blur' }],
    configValue: [{ required: true, message: '参数键值不能为空', trigger: 'blur' }]
  })

  const formItems = computed(() => [
    {
      label: '参数名称',
      key: 'configName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入参数名称' }
    },
    {
      label: '参数键名',
      key: 'configKey',
      type: 'input',
      props: { clearable: true, placeholder: '请输入参数键名' }
    },
    {
      label: '系统内置',
      key: 'configType',
      type: 'select',
      props: {
        placeholder: '请选择',
        options: sys_yes_no.value || [],
        clearable: true
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
      apiFn: listConfig,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { prop: 'configName', label: '参数名称', minWidth: 160 },
        { prop: 'configKey', label: '参数键名', minWidth: 160 },
        {
          prop: 'configValue',
          label: '参数键值',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'configType',
          label: '系统内置',
          width: 100,
          align: 'center',
          formatter: (row: ConfigVO) =>
            h(DictTag, { options: sys_yes_no.value, value: row.configType })
        },
        { prop: 'remark', label: '备注', showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 120,
          fixed: 'right',
          formatter: (row: ConfigVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:config:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:config:remove',
                onClick: () => handleDelete(row)
              })
            ])
        }
      ]
    }
  })

  const showDialog = (type: 'add' | 'edit', row?: ConfigVO) => {
    dialogType.value = type
    if (row) {
      getConfig(row.configId).then((data) => {
        Object.assign(formData, data)
      })
    } else {
      Object.assign(formData, {
        configId: undefined,
        configName: '',
        configKey: '',
        configValue: '',
        configType: 'Y',
        remark: ''
      })
    }
    dialogVisible.value = true
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (formData.configId) {
          updateConfig(formData).then(() => {
            ElMessage.success('修改成功')
            dialogVisible.value = false
            refreshData()
          })
        } else {
          addConfig(formData).then(() => {
            ElMessage.success('新增成功')
            dialogVisible.value = false
            refreshData()
          })
        }
      }
    })
  }

  const handleClose = () => {
    dialogVisible.value = false
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm.value)
    getData()
  }

  const handleReset = () => {
    searchForm.value.configName = ''
    searchForm.value.configKey = ''
    searchForm.value.configType = ''
    activeTab.value = ''
    resetSearchParams()
    getData()
  }

  const handleTabChange = (tab: string) => {
    searchForm.value.configType = tab
    replaceSearchParams(searchForm.value)
    getData()
  }

  /*const handleInlineSave = async (row: ConfigVO) => {
    if (!row.configKey) return
    try {
      await ElMessageBox.confirm('确认要保存对参数"' + row.configKey + '"的修改吗？')
      await updateConfigByKey(row.configKey, row.configValue)
      ElMessage.success('修改成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('修改失败')
      }
    }
  }*/

  const handleDelete = async (row: ConfigVO) => {
    try {
      await ElMessageBox.confirm(`确定要删除参数编号为"${row.configId}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delConfig(row.configId)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleRefreshCache = async () => {
    try {
      await refreshCache()
      ElMessage.success('刷新缓存成功')
    } catch {
      ElMessage.error('刷新缓存失败')
    }
  }

  const handleExport = () => {
    exportConfig(searchForm.value)
  }
</script>

<style scoped>
  .config-tabs :deep(.el-tabs__header) {
    margin: 0;
    border-bottom: none;
  }

  .config-tabs :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  .config-tabs :deep(.el-tabs__nav-wrap) {
    border-bottom: none;
  }

  .config-tabs :deep(.el-tabs__item) {
    height: 32px;
    padding: 0 16px;
    margin-right: 8px;
    font-size: 14px;
    line-height: 32px;
  }

  .config-tabs :deep(.el-tabs__item.is-active) {
    height: 32px;
  }
</style>
