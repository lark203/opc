<template>
  <div class="oss-config-page art-full-height">
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
            <ElButton type="primary" v-auth="'system:ossConfig:add'" @click="handleAdd"
              >新增</ElButton
            >
            <ElButton
              type="success"
              v-auth="'system:ossConfig:edit'"
              :disabled="selectedRows.length !== 1"
              @click="() => handleUpdate()"
            >
              修改
            </ElButton>
            <ElButton
              type="danger"
              v-auth="'system:ossConfig:remove'"
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
        <template #accessPolicy="{ row }">
          <ElTag :type="getPolicyType(row.accessPolicy)">{{
            getPolicyText(row.accessPolicy)
          }}</ElTag>
        </template>
        <template #status="{ row }">
          <ElSwitch
            v-model="row.status"
            active-value="Y"
            inactive-value="N"
            @change="handleStatusChange(row)"
          />
        </template>
        <template #action="{ row }">
          <ArtButtonTable
            type="edit"
            auth="system:ossConfig:edit"
            @click="() => handleUpdate(row)"
          />
          <ArtButtonTable
            type="delete"
            auth="system:ossConfig:remove"
            @click="() => handleDelete(row)"
          />
        </template>
      </ArtTable>
    </ElCard>
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="35%" align-center>
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="100px">
        <ElFormItem label="配置key" prop="configKey">
          <ElInput v-model="form.configKey" placeholder="请输入配置key" />
        </ElFormItem>
        <ElFormItem label="访问站点" prop="endpoint">
          <ElInput v-model="form.endpoint" placeholder="请输入访问站点">
            <template #prefix>
              <span style="color: #999">{{ protocol }}</span>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem label="自定义域名" prop="domainUrl">
          <ElInput v-model="form.domainUrl" placeholder="请输入自定义域名">
            <template #prefix>
              <span style="color: #999">{{ protocol }}</span>
            </template>
          </ElInput>
        </ElFormItem>
        <ElFormItem label="accessKey" prop="accessKey">
          <ElInput v-model="form.accessKey" placeholder="请输入accessKey" />
        </ElFormItem>
        <ElFormItem label="secretKey" prop="secretKey">
          <ElInput v-model="form.secretKey" placeholder="请输入秘钥" show-password />
        </ElFormItem>
        <ElFormItem label="桶名称" prop="bucketName">
          <ElInput v-model="form.bucketName" placeholder="请输入桶名称" />
        </ElFormItem>
        <ElFormItem label="前缀" prop="prefix">
          <ElInput v-model="form.prefix" placeholder="请输入前缀" />
        </ElFormItem>
        <ElFormItem label="是否HTTPS">
          <ElRadioGroup v-model="form.isHttps">
            <ElRadio v-for="dict in sys_yes_no" :key="dict.value" :value="dict.value">{{
              dict.label
            }}</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="桶权限类型">
          <ElRadioGroup v-model="form.accessPolicy">
            <ElRadio value="0">private</ElRadio>
            <ElRadio value="1">public</ElRadio>
            <ElRadio value="2">custom</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="域" prop="region">
          <ElInput v-model="form.region" placeholder="请输入域" />
        </ElFormItem>
        <ElFormItem label="备注" prop="remark">
          <ElInput v-model="form.remark" type="textarea" placeholder="请输入内容" />
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
  import { computed, reactive, ref, toRefs } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { OssConfigForm, OssConfigQuery, OssConfigVO } from '@/api/system/ossConfig'
  import { ossConfigApi } from '@/api/system/ossConfig'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'

  defineOptions({ name: 'OssConfig' })

  const { sys_yes_no } = toRefs(useDict('sys_yes_no'))

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
      apiFn: ossConfigApi.listOssConfig,
      apiParams: {
        configKey: '',
        bucketName: '',
        status: ''
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', label: '序号', width: 60 },
        { prop: 'configKey', label: '配置key' },
        { prop: 'endpoint', label: '访问站点' },
        { prop: 'domainUrl', label: '自定义域名' },
        { prop: 'bucketName', label: '桶名称' },
        { prop: 'prefix', label: '前缀' },
        { prop: 'region', label: '域' },
        {
          prop: 'accessPolicy',
          label: '桶权限类型',
          useSlot: true,
          slotName: 'accessPolicy'
        },
        { prop: 'status', label: '是否默认', useSlot: true, slotName: 'status' },
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

  let searchForm = reactive<OssConfigQuery>({
    configKey: '',
    bucketName: '',
    status: ''
  })

  const formItems = computed(() => [
    {
      label: '配置key',
      key: 'configKey',
      type: 'input',
      props: { placeholder: '请输入配置key', clearable: true }
    },
    {
      label: '桶名称',
      key: 'bucketName',
      type: 'input',
      props: { placeholder: '请输入桶名称', clearable: true }
    },
    {
      label: '是否默认',
      key: 'status',
      type: 'select',
      props: { placeholder: '请选择状态', clearable: true, options: sys_yes_no.value }
    }
  ])

  const selectedRows = ref<OssConfigVO[]>([])
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const buttonLoading = ref(false)
  const formRef = ref<FormInstance>()

  const form = reactive<OssConfigForm>({
    ossConfigId: undefined,
    configKey: '',
    accessKey: '',
    secretKey: '',
    bucketName: '',
    prefix: '',
    endpoint: '',
    domainUrl: '',
    isHttps: 'N',
    accessPolicy: '1',
    region: '',
    status: 'N',
    remark: ''
  })

  const protocol = computed(() => (form.isHttps === 'Y' ? 'https://' : 'http://'))

  const rules: FormRules<OssConfigForm> = {
    configKey: [{ required: true, message: 'configKey不能为空', trigger: 'blur' }],
    accessKey: [
      { required: true, message: 'accessKey不能为空', trigger: 'blur' },
      { min: 2, max: 200, message: 'accessKey长度必须介于 2 和 200 之间', trigger: 'blur' }
    ],
    secretKey: [
      { required: true, message: 'secretKey不能为空', trigger: 'blur' },
      { min: 2, max: 100, message: 'secretKey长度必须介于 2 和 100 之间', trigger: 'blur' }
    ],
    bucketName: [
      { required: true, message: 'bucketName不能为空', trigger: 'blur' },
      { min: 2, max: 100, message: 'bucketName长度必须介于 2 和 100 之间', trigger: 'blur' }
    ],
    endpoint: [
      { required: true, message: 'endpoint不能为空', trigger: 'blur' },
      { min: 2, max: 100, message: 'endpoint名称长度必须介于 2 和 100 之间', trigger: 'blur' }
    ],
    accessPolicy: [{ required: true, message: 'accessPolicy不能为空', trigger: 'blur' }]
  }

  const handleSelectionChange = (rows: OssConfigVO[]) => {
    selectedRows.value = rows
  }

  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.configKey = ''
    searchForm.bucketName = ''
    searchForm.status = ''
    resetSearchParams()
  }

  const handleAdd = () => {
    resetForm()
    dialogTitle.value = '添加对象存储配置'
    dialogVisible.value = true
  }

  const handleUpdate = async (row?: OssConfigVO) => {
    resetForm()
    const ossConfigId = row ? row.ossConfigId : selectedRows.value[0]?.ossConfigId
    if (!ossConfigId) return
    const res = await ossConfigApi.getOssConfig(ossConfigId)
    Object.assign(form, res)
    dialogTitle.value = '修改对象存储配置'
    dialogVisible.value = true
  }

  const submitForm = () => {
    formRef.value?.validate(async (valid: boolean) => {
      if (valid) {
        buttonLoading.value = true
        try {
          if (form.ossConfigId) {
            await ossConfigApi.updateOssConfig(form)
          } else {
            await ossConfigApi.addOssConfig(form)
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

  const handleStatusChange = async (row: OssConfigVO) => {
    const text = row.status === 'Y' ? '启用' : '停用'
    try {
      await ElMessageBox.confirm(`确认要"${text}""${row.configKey}"配置吗?`)
      await ossConfigApi.changeOssConfigStatus(row.ossConfigId, row.status, row.configKey)
      refreshData()
      ElMessage.success(text + '成功')
    } catch {
      row.status = row.status === 'Y' ? 'N' : 'Y'
    }
  }

  const handleDelete = async (row?: OssConfigVO) => {
    const ossConfigIds = row
      ? [row.ossConfigId]
      : selectedRows.value.map((item) => item.ossConfigId)
    if (ossConfigIds.length === 0) return
    try {
      await ElMessageBox.confirm('是否确认删除选中的配置?', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await ossConfigApi.delOssConfig(ossConfigIds)
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const resetForm = () => {
    form.ossConfigId = undefined
    form.configKey = ''
    form.accessKey = ''
    form.secretKey = ''
    form.bucketName = ''
    form.prefix = ''
    form.endpoint = ''
    form.domainUrl = ''
    form.isHttps = 'N'
    form.accessPolicy = '1'
    form.region = ''
    form.status = 'N'
    form.remark = ''
    formRef.value?.resetFields()
  }

  const getPolicyType = (policy: string) => {
    const types: Record<string, 'warning' | 'success' | 'info' | 'primary'> = {
      '0': 'warning',
      '1': 'success',
      '2': 'info'
    }
    return types[policy] || 'primary'
  }

  const getPolicyText = (policy: string) => {
    const texts: Record<string, string> = {
      '0': 'private',
      '1': 'public',
      '2': 'custom'
    }
    return texts[policy] || policy
  }
</script>
