<template>
  <div class="demo-page art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :isExpand="true"
      @reset="handleReset"
      @search="handleSearch"
    />
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" v-auth="'demo:demo:add'" @click="handleAdd">新增</ElButton>
            <ElButton
              type="success"
              v-auth="'demo:demo:edit'"
              :disabled="selectedRows.length !== 1"
              @click="() => handleUpdate()"
            >
              修改
            </ElButton>
            <ElButton
              type="danger"
              v-auth="'demo:demo:remove'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
            >
              删除
            </ElButton>
            <ElButton type="info" v-auth="'demo:demo:export'" @click="handleExport">导出</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        row-key="id"
        :data="data"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #action="{ row }">
          <ArtButtonTable type="edit" auth="demo:demo:edit" @click="() => handleUpdate(row)" />
          <ArtButtonTable type="delete" auth="demo:demo:remove" @click="() => handleDelete(row)" />
        </template>
      </ArtTable>
    </ElCard>
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="30%" align-center>
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="80px">
        <ElFormItem label="部门id" prop="deptId">
          <ElInput v-model="form.deptId" placeholder="请输入部门id" />
        </ElFormItem>
        <ElFormItem label="用户id" prop="userId">
          <ElInput v-model="form.userId" placeholder="请输入用户id" />
        </ElFormItem>
        <ElFormItem label="排序号" prop="orderNum">
          <ElInput v-model="form.orderNum" placeholder="请输入排序号" />
        </ElFormItem>
        <ElFormItem label="key键" prop="testKey">
          <ElInput v-model="form.testKey" placeholder="请输入key键" />
        </ElFormItem>
        <ElFormItem label="值" prop="value">
          <ElInput v-model="form.value" placeholder="请输入值" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="buttonLoading" @click="submitForm">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { DemoForm, DemoQuery, DemoVO } from '@/api/demo/demo'
  import { demoApi } from '@/api/demo/demo'
  import { useTable } from '@/hooks/core/useTable'

  defineOptions({ name: 'Demo' })

  const {
    data,
    columns,
    columnChecks,
    pagination,
    loading,
    refreshData,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange
  } = useTable({
    core: {
      apiFn: demoApi.listDemo,
      apiParams: {
        testKey: '',
        value: ''
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', label: '序号', width: 60 },
        { prop: 'id', label: '主键' },
        { prop: 'deptId', label: '部门id' },
        { prop: 'userId', label: '用户id' },
        { prop: 'orderNum', label: '排序号' },
        { prop: 'testKey', label: 'key键' },
        { prop: 'value', label: '值' },
        {
          prop: 'action',
          label: '操作',
          width: 120,
          useSlot: true,
          slotName: 'action',
          fixed: 'right'
        }
      ]
    }
  })

  let searchForm = reactive<DemoQuery>({
    testKey: '',
    value: ''
  })

  const formItems = computed(() => [
    {
      label: 'key键',
      key: 'testKey',
      type: 'input',
      props: { placeholder: '请输入key键', clearable: true }
    },
    {
      label: '值',
      key: 'value',
      type: 'input',
      props: { placeholder: '请输入值', clearable: true }
    }
  ])

  const selectedRows = ref<DemoVO[]>([])
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const buttonLoading = ref(false)
  const formRef = ref<FormInstance>()

  const form = reactive<DemoForm>({
    id: undefined,
    deptId: undefined,
    userId: undefined,
    orderNum: undefined,
    testKey: '',
    value: ''
  })

  const rules: FormRules<DemoForm> = {
    deptId: [{ required: true, message: '部门id不能为空', trigger: 'blur' }],
    userId: [{ required: true, message: '用户id不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '排序号不能为空', trigger: 'blur' }],
    testKey: [{ required: true, message: 'key键不能为空', trigger: 'blur' }],
    value: [{ required: true, message: '值不能为空', trigger: 'blur' }]
  }

  const handleSelectionChange = (rows: DemoVO[]) => {
    selectedRows.value = rows
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.testKey = ''
    searchForm.value = ''
    resetSearchParams()
  }

  const resetForm = () => {
    form.id = undefined
    form.deptId = undefined
    form.userId = undefined
    form.orderNum = undefined
    form.testKey = ''
    form.value = ''
    formRef.value?.resetFields()
  }

  const handleAdd = () => {
    resetForm()
    dialogTitle.value = '添加测试单'
    dialogVisible.value = true
  }

  const handleUpdate = async (row?: DemoVO) => {
    resetForm()
    const id = row ? row.id : selectedRows.value[0]?.id
    if (!id) return
    const res = await demoApi.getDemo(id)
    Object.assign(form, res)
    dialogTitle.value = '修改测试单'
    dialogVisible.value = true
  }

  const submitForm = () => {
    formRef.value?.validate(async (valid: boolean) => {
      if (valid) {
        buttonLoading.value = true
        try {
          if (form.id) {
            await demoApi.updateDemo(form)
          } else {
            await demoApi.addDemo(form)
          }
          dialogVisible.value = false
          refreshData()
          ElMessage.success('操作成功')
        } finally {
          buttonLoading.value = false
        }
      }
    })
  }

  const handleDelete = async (row?: DemoVO) => {
    const ids = row ? [row.id] : selectedRows.value.map((item) => item.id)
    if (ids.length === 0) return
    try {
      await ElMessageBox.confirm('是否确认删除选中的测试单数据?', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await demoApi.delDemo(ids)
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleExport = () => {
    demoApi.exportDemo(searchForm)
  }
</script>
