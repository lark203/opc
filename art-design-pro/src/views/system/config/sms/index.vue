<template>
  <div class="sms-config-page art-full-height">
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
            <ElButton type="primary" v-auth="'system:sms:add'" @click="handleAdd">新增</ElButton>
            <ElButton
              type="success"
              v-auth="'system:sms:edit'"
              :disabled="selectedRows.length !== 1"
              @click="() => handleUpdate()"
            >
              修改
            </ElButton>
            <ElButton
              type="danger"
              v-auth="'system:sms:del'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
            >
              删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        :data="data"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #supplier="{ row }">
          <ElTag :type="getSupplierType(row.supplier)">{{ getSupplierLabel(row.supplier) }}</ElTag>
        </template>
        <template #status="{ row }">
          <ElSwitch
            v-model="row.status"
            active-value="1"
            inactive-value="2"
            @change="handleStatusChange(row)"
          />
        </template>
        <template #isDefault="{ row }">
          <ElTag :type="row.isDefault === '1' ? 'success' : 'info'">
            {{ row.isDefault === '1' ? '是' : '否' }}
          </ElTag>
        </template>
        <template #action="{ row }">
          <ElButton
            v-if="row.isDefault !== '1'"
            link
            type="primary"
            v-auth="'system:sms:edit'"
            @click="handleDefault(row)"
          >
            设为默认
          </ElButton>
          <ArtButtonTable type="edit" auth="system:sms:edit" @click="() => handleUpdate(row)" />
          <ArtButtonTable type="delete" auth="system:sms:del" @click="() => handleDelete(row)" />
        </template>
      </ArtTable>
    </ElCard>

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="640px" align-center>
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="120px">
        <ElFormItem label="名称" prop="name">
          <ElInput v-model="form.name" placeholder="请输入名称" />
        </ElFormItem>
        <ElFormItem label="供应商" prop="supplier">
          <ElSelect v-model="form.supplier" placeholder="请选择供应商" style="width: 100%">
            <ElOption v-for="d in sms_supplier" :key="d.value" :label="d.label" :value="d.value" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="配置ID" prop="configId">
          <ElInput v-model="form.configId" placeholder="sms4j 配置键，留空自动生成" />
        </ElFormItem>
        <ElFormItem label="accessKey" prop="accessKey">
          <ElInput v-model="form.accessKey" placeholder="请输入 accessKey" />
        </ElFormItem>
        <ElFormItem label="secretKey" prop="secretKey">
          <ElInput
            v-model="form.secretKey"
            type="password"
            show-password
            placeholder="请输入 secretKey"
          />
        </ElFormItem>
        <ElFormItem label="签名" prop="signature">
          <ElInput v-model="form.signature" placeholder="请输入签名" />
        </ElFormItem>
        <ElFormItem label="模板ID" prop="templateId">
          <ElInput v-model="form.templateId" placeholder="请输入模板ID" />
        </ElFormItem>
        <ElFormItem label="权重">
          <ElInputNumber v-model="form.weight" :min="1" :max="100" />
        </ElFormItem>
        <ElFormItem label="重试间隔(秒)">
          <ElInputNumber v-model="form.retryInterval" :min="0" />
        </ElFormItem>
        <ElFormItem label="最大重试">
          <ElInputNumber v-model="form.maxRetries" :min="0" />
        </ElFormItem>
        <ElFormItem label="最大发送量">
          <ElInputNumber v-model="form.maximum" :min="1" />
        </ElFormItem>
        <ElFormItem label="扩展配置(JSON)">
          <ElInput
            v-model="form.supplierConfig"
            type="textarea"
            :rows="3"
            placeholder='如 {"sdkAppId":""}'
          />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElSwitch v-model="form.status" active-value="1" inactive-value="2" />
        </ElFormItem>
        <ElFormItem label="默认">
          <ElSwitch v-model="form.isDefault" active-value="1" inactive-value="0" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="form.sort" :min="0" />
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
  import {
    addSmsConfig,
    changeSmsConfigStatus,
    delSmsConfig,
    getSmsConfig,
    listSmsConfig,
    setDefaultSmsConfig,
    SmsConfigVO,
    updateSmsConfig
  } from '@/api/system/smsConfig'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'

  defineOptions({ name: 'SmsConfig' })

  const { sms_supplier } = useDict('sms_supplier')

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
      apiFn: listSmsConfig,
      apiParams: {
        name: '',
        supplier: '',
        status: ''
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', label: '序号', width: 60 },
        { prop: 'name', label: '名称' },
        { prop: 'supplier', label: '供应商', useSlot: true, slotName: 'supplier' },
        { prop: 'configId', label: '配置ID' },
        { prop: 'signature', label: '签名' },
        { prop: 'templateId', label: '模板ID' },
        { prop: 'status', label: '状态', useSlot: true, slotName: 'status' },
        { prop: 'isDefault', label: '默认', useSlot: true, slotName: 'isDefault' },
        { prop: 'sort', label: '排序', width: 80 },
        {
          prop: 'action',
          label: '操作',
          width: 200,
          useSlot: true,
          slotName: 'action',
          fixed: 'right'
        }
      ]
    }
  })

  const searchForm = reactive<{ name: string; supplier: string; status: string }>({
    name: '',
    supplier: '',
    status: ''
  })

  const formItems = computed(() => [
    {
      label: '名称',
      key: 'name',
      type: 'input',
      props: { placeholder: '请输入名称', clearable: true }
    },
    {
      label: '供应商',
      key: 'supplier',
      type: 'select',
      props: { placeholder: '请选择供应商', clearable: true, options: sms_supplier }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        clearable: true,
        options: [
          { label: '正常', value: '1' },
          { label: '停用', value: '2' }
        ]
      }
    }
  ])

  const selectedRows = ref<SmsConfigVO[]>([])
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const buttonLoading = ref(false)
  const formRef = ref<FormInstance>()

  const form = reactive<Partial<SmsConfigVO>>({
    smsId: undefined,
    configId: '',
    name: '',
    supplier: '',
    accessKey: '',
    secretKey: '',
    signature: '',
    templateId: '',
    weight: 1,
    retryInterval: 0,
    maxRetries: 0,
    maximum: 1,
    supplierConfig: '',
    status: '1',
    isDefault: '0',
    sort: 999
  })

  const rules: FormRules<Partial<SmsConfigVO>> = {
    name: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    supplier: [{ required: true, message: '供应商不能为空', trigger: 'change' }],
    accessKey: [{ required: true, message: 'accessKey不能为空', trigger: 'blur' }],
    secretKey: [{ required: true, message: 'secretKey不能为空', trigger: 'blur' }],
    templateId: [{ required: true, message: '模板ID不能为空', trigger: 'blur' }]
  }

  const handleSelectionChange = (rows: SmsConfigVO[]) => {
    selectedRows.value = rows
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.name = ''
    searchForm.supplier = ''
    searchForm.status = ''
    resetSearchParams()
  }

  const handleAdd = () => {
    resetForm()
    dialogTitle.value = '新增短信配置'
    dialogVisible.value = true
  }

  const handleUpdate = async (row?: SmsConfigVO) => {
    resetForm()
    const smsId = row ? row.smsId : selectedRows.value[0]?.smsId
    if (!smsId) return
    const res = await getSmsConfig(smsId)
    Object.assign(form, res)
    dialogTitle.value = '修改短信配置'
    dialogVisible.value = true
  }

  const handleDefault = async (row: SmsConfigVO) => {
    await setDefaultSmsConfig(row.smsId)
    ElMessage.success('已设为默认')
    refreshData()
  }

  const handleDelete = async (row?: SmsConfigVO) => {
    const ids = row ? [row.smsId] : selectedRows.value.map((item) => item.smsId)
    if (!ids.length) return
    try {
      await ElMessageBox.confirm('是否确认删除选中的配置?', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delSmsConfig(ids)
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleStatusChange = async (row: SmsConfigVO) => {
    const text = row.status === '1' ? '启用' : '停用'
    try {
      await ElMessageBox.confirm(`确认要"${text}""${row.name}"配置吗?`)
      await changeSmsConfigStatus(row.smsId, row.status as string)
      refreshData()
      ElMessage.success(text + '成功')
    } catch {
      row.status = row.status === '1' ? '2' : '1'
    }
  }

  const submitForm = () => {
    formRef.value?.validate(async (valid: boolean) => {
      if (valid) {
        buttonLoading.value = true
        try {
          if (form.smsId) {
            await updateSmsConfig(form as Partial<SmsConfigVO>)
          } else {
            await addSmsConfig(form as Partial<SmsConfigVO>)
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

  const resetForm = () => {
    Object.assign(form, {
      smsId: undefined,
      configId: '',
      name: '',
      supplier: '',
      accessKey: '',
      secretKey: '',
      signature: '',
      templateId: '',
      weight: 1,
      retryInterval: 0,
      maxRetries: 0,
      maximum: 1,
      supplierConfig: '',
      status: '1',
      isDefault: '0',
      sort: 999
    })
    formRef.value?.resetFields()
  }

  const getSupplierLabel = (supplier: string) => {
    return sms_supplier.find((d) => d.value === supplier)?.label || supplier
  }
  const getSupplierType = (
    supplier: string
  ): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    return (sms_supplier.find((d) => d.value === supplier)?.elTagType || 'primary') as
      | 'primary'
      | 'success'
      | 'warning'
      | 'info'
      | 'danger'
  }
</script>
