<template>
  <div class="client-page art-full-height">
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
            <ElButton type="primary" v-auth="'system:client:add'" @click="showDialog('add')"
              >新增客户端</ElButton
            >
            <ElButton
              type="success"
              v-auth="'system:client:edit'"
              :disabled="selectedRows.length !== 1"
              @click="showDialog('edit')"
              >修改</ElButton
            >
            <ElButton
              type="danger"
              v-auth="'system:client:remove'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
              >删除</ElButton
            >
            <ElButton type="info" v-auth="'system:client:export'" @click="handleExport"
              >导出</ElButton
            >
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
    <ElDialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增客户端' : '修改客户端'"
      width="40%"
      align-center
      @close="handleClose"
    >
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="160px">
        <ElFormItem label="客户端key" prop="clientKey">
          <ElInput
            v-model="formData.clientKey"
            :disabled="formData.id != null"
            placeholder="请输入客户端key"
          />
        </ElFormItem>
        <ElFormItem label="客户端秘钥" prop="clientSecret">
          <ElInput
            v-model="formData.clientSecret"
            :disabled="formData.id != null"
            placeholder="请输入客户端秘钥"
          />
        </ElFormItem>
        <ElFormItem label="授权类型" prop="grantTypeList">
          <ElSelect v-model="formData.grantTypeList" multiple placeholder="请选择授权类型">
            <ElOption
              v-for="item in sys_grant_type"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="设备类型" prop="deviceType">
          <ElSelect v-model="formData.deviceType" placeholder="请选择设备类型">
            <ElOption
              v-for="item in sys_device_type"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem prop="accessPath">
          <template #label>
            <span>
              <ElTooltip
                content="多个路径可按换行、逗号或分号分隔；为空表示允许访问所有接口路径"
                placement="top"
              >
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              允许访问路径
            </span>
          </template>
          <ElInput
            v-model="formData.accessPath"
            type="textarea"
            :rows="4"
            placeholder="示例：/app/**"
          />
        </ElFormItem>
        <ElFormItem prop="ipWhitelist">
          <template #label>
            <span>
              <ElTooltip
                content="支持精确IP、通配符和CIDR；多个规则可按换行、逗号或分号分隔；为空表示允许所有IP"
                placement="top"
              >
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              IP白名单
            </span>
          </template>
          <ElInput
            v-model="formData.ipWhitelist"
            type="textarea"
            :rows="4"
            placeholder="示例：127.0.0.1&#10;192.168.*.*&#10;10.0.0.0/24"
          />
        </ElFormItem>
        <ElFormItem prop="activeTimeout">
          <template #label>
            <span>
              <ElTooltip
                content="指定时间无操作则过期（单位：秒），默认30分钟（1800秒）"
                placement="top"
              >
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              Token活跃超时时间
            </span>
          </template>
          <ElInput v-model="formData.activeTimeout" placeholder="请输入Token活跃超时时间（秒）" />
        </ElFormItem>
        <ElFormItem prop="timeout">
          <template #label>
            <span>
              <ElTooltip
                content="指定时间必定过期（单位：秒），默认七天（604800秒）"
                placement="top"
              >
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              Token固定超时时间
            </span>
          </template>
          <ElInput v-model="formData.timeout" placeholder="请输入Token固定超时时间（秒）" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio v-for="status in sys_normal_disable" :key="status.value" :label="status.value">
              {{ status.label }}
            </ElRadio>
          </ElRadioGroup>
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
  import { computed, reactive, ref } from 'vue'
  import { ElIcon, ElMessage, ElMessageBox, ElSwitch, ElTag, ElTooltip } from 'element-plus'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    addClient,
    changeStatus,
    type ClientForm,
    type ClientQuery,
    type ClientVO,
    delClient,
    exportClient,
    getClient,
    listClient,
    updateClient
  } from '@/api/system/client'
  import { useDict } from '@utils/dict'

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable, sys_grant_type, sys_device_type } = toRefs(
    useDict('sys_normal_disable', 'sys_grant_type', 'sys_device_type')
  )

  let searchForm = reactive<ClientQuery>({
    clientKey: '',
    clientSecret: '',
    status: ''
  })

  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const formRef = ref()

  const selectedRows = ref<ClientVO[]>([])

  const formData = reactive<ClientForm>({
    id: undefined,
    clientId: undefined,
    clientKey: '',
    clientSecret: '',
    grantTypeList: [],
    deviceType: '',
    accessPath: '',
    ipWhitelist: '',
    activeTimeout: 1800,
    timeout: 604800,
    status: '0'
  })

  const rules = reactive({
    clientKey: [{ required: true, message: '客户端key不能为空', trigger: 'blur' }],
    clientSecret: [{ required: true, message: '客户端秘钥不能为空', trigger: 'blur' }],
    grantTypeList: [{ required: true, message: '授权类型不能为空', trigger: 'change' }],
    deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }]
  })

  const formItems = computed(() => [
    {
      label: '客户端key',
      key: 'clientKey',
      type: 'input',
      props: { clearable: true, placeholder: '请输入客户端key' }
    },
    {
      label: '客户端秘钥',
      key: 'clientSecret',
      type: 'input',
      props: { clearable: true, placeholder: '请输入客户端秘钥' }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        options: sys_normal_disable.value || [],
        clearable: true
      }
    }
  ])

  const getRuleList = (ruleList?: string[], ruleValue?: string): string[] => {
    if (Array.isArray(ruleList) && ruleList.length) {
      return ruleList
    }
    if (!ruleValue) {
      return []
    }
    return ruleValue
      .split(/[\n,;]+/)
      .map((item) => item.trim())
      .filter(Boolean)
  }

  const getGrantTypeText = (type: string): string => {
    const found = sys_grant_type.value.find((item) => item.value === type)
    return found?.label || type
  }

  const getDeviceTypeText = (type: string): string => {
    const found = sys_device_type.value.find((item) => item.value === type)
    return found?.label || type
  }

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
      apiFn: listClient,
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
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        { prop: 'clientId', label: '客户端id' },
        { prop: 'clientKey', label: '客户端key' },
        { prop: 'clientSecret', label: '客户端秘钥' },
        {
          prop: 'grantTypeList',
          label: '授权类型',
          formatter: (row: ClientVO) =>
            h('div', { class: 'tag-list' }, [
              ...(row.grantTypeList || []).map((type) =>
                h(ElTag, { type: 'primary', size: 'small', key: type }, () =>
                  getGrantTypeText(type)
                )
              )
            ])
        },
        {
          prop: 'deviceType',
          label: '设备类型',
          align: 'center',
          formatter: (row: ClientVO) => getDeviceTypeText(row.deviceType)
        },
        {
          prop: 'accessPath',
          label: '白名单路径',
          formatter: (row: ClientVO) => {
            const paths = getRuleList(row.accessPathList, row.accessPath)
            if (!paths.length) return '全部路径'
            return h('div', { class: 'tag-list' }, [
              ...paths
                .slice(0, 3)
                .map((path) => h(ElTag, { size: 'small', key: path }, () => path)),
              paths.length > 3 ? h('span', '...') : ''
            ])
          }
        },
        {
          prop: 'ipWhitelist',
          label: '白名单IP',
          formatter: (row: ClientVO) => {
            const ips = getRuleList(row.ipWhitelistList, row.ipWhitelist)
            if (!ips.length) return '全部IP'
            return h('div', { class: 'tag-list' }, [
              ...ips
                .slice(0, 3)
                .map((ip) => h(ElTag, { type: 'success', size: 'small', key: ip }, () => ip)),
              ips.length > 3 ? h('span', '...') : ''
            ])
          }
        },
        { prop: 'activeTimeout', label: '活跃超时' },
        { prop: 'timeout', label: '固定超时' },
        {
          prop: 'status',
          label: '状态',
          align: 'center',
          formatter: (row: ClientVO) =>
            h(ElSwitch, {
              modelValue: row.status === '0',
              'onUpdate:modelValue': (val: boolean) => handleStatusChange(row, val ? '0' : '1')
            })
        },
        {
          prop: 'operation',
          label: '操作',
          fixed: 'right',
          formatter: (row: ClientVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:client:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:client:remove',
                onClick: () => handleDelete(row)
              })
            ])
        }
      ]
    }
  })

  const showDialog = (type: 'add' | 'edit', row?: ClientVO) => {
    dialogType.value = type
    if (row) {
      getClient(row.id).then((data) => {
        Object.assign(formData, data)
      })
    } else {
      Object.assign(formData, {
        id: undefined,
        clientId: undefined,
        clientKey: '',
        clientSecret: '',
        grantTypeList: [],
        deviceType: '',
        accessPath: '',
        ipWhitelist: '',
        activeTimeout: 1800,
        timeout: 604800,
        status: '0'
      })
    }
    dialogVisible.value = true
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (formData.id) {
          updateClient(formData).then(() => {
            ElMessage.success('修改成功')
            dialogVisible.value = false
            refreshData()
          })
        } else {
          addClient(formData).then(() => {
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
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.clientKey = ''
    searchForm.clientSecret = ''
    searchForm.status = ''
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: ClientVO[]) => {
    selectedRows.value = selection
  }

  const handleDelete = async (row?: ClientVO) => {
    const clientIds = row?.id || selectedRows.value.map((r) => r.id).join(',')
    if (!clientIds) return
    try {
      await ElMessageBox.confirm(`确定要删除客户端编号为"${clientIds}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delClient(clientIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleStatusChange = async (row: ClientVO, status: string) => {
    const text = status === '0' ? '启用' : '停用'
    try {
      await ElMessageBox.confirm(`确认要${text}客户端"${row.clientKey}"吗？`, '状态变更', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await changeStatus(row.clientId, status)
      ElMessage.success(`${text}成功`)
    } catch (error) {
      row.status = status === '0' ? '1' : '0'
      if (error !== 'cancel') {
        ElMessage.error(`${text}失败`)
      }
    }
  }

  const handleExport = () => {
    exportClient(searchForm)
  }
</script>

<style lang="scss" scoped>
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
</style>
