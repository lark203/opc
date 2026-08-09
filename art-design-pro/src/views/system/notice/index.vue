<template>
  <div class="notice-page art-full-height">
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
            <ElButton type="primary" v-auth="'system:notice:add'" @click="showDialog('add')"
              >新增公告</ElButton
            >
            <ElButton
              type="success"
              v-auth="'system:notice:edit'"
              :disabled="selectedRows.length !== 1"
              @click="showDialog('edit')"
              >修改</ElButton
            >
            <ElButton
              type="danger"
              v-auth="'system:notice:remove'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
              >删除</ElButton
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
      :title="dialogType === 'add' ? '新增公告' : '修改公告'"
      width="45%"
      align-center
      @close="handleClose"
    >
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <ElRow :gutter="20">
          <ElCol :span="12">
            <ElFormItem label="公告标题" prop="noticeTitle">
              <ElInput v-model="formData.noticeTitle" placeholder="请输入公告标题" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="公告类型" prop="noticeType">
              <ElSelect v-model="formData.noticeType" placeholder="请选择公告类型">
                <ElOption label="通知" value="1" />
                <ElOption label="公告" value="2" />
                <ElOption label="消息" value="3" />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :span="24">
            <ElFormItem label="状态">
              <ElRadioGroup v-model="formData.status">
                <ElRadio value="0">正常</ElRadio>
                <ElRadio value="1">停用</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol :span="24">
            <ElFormItem label="内容">
              <ArtWangEditor
                v-model="formData.noticeContent"
                height="250px"
                placeholder="请输入公告内容..."
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElForm>
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="handleClose">取 消</ElButton>
          <ElButton type="primary" @click="handleSubmit">确 定</ElButton>
        </span>
      </template>
    </ElDialog>
    <ElDialog v-model="detailDialogVisible" title="公告详情" width="35%" align-center>
      <div class="notice-detail">
        <div class="notice-detail__header">
          <div class="notice-detail__title">{{ detailForm.noticeTitle || '-' }}</div>
          <div class="notice-detail__meta">
            <div class="notice-detail__meta-item">
              <span class="notice-detail__meta-label">类型：</span>
              <ElTag type="primary" size="small">
                {{ getNoticeTypeText(detailForm.noticeType) }}
              </ElTag>
            </div>
            <div class="notice-detail__meta-item">
              <span class="notice-detail__meta-label">状态：</span>
              <ElTag :type="detailForm.status === '0' ? 'success' : 'danger'" size="small">
                {{ detailForm.status === '0' ? '正常' : '停用' }}
              </ElTag>
            </div>
            <div class="notice-detail__meta-item">
              <span class="notice-detail__meta-label">创建者：</span>
              <span>{{ detailForm.createByName || '-' }}</span>
            </div>
            <div class="notice-detail__meta-item">
              <span class="notice-detail__meta-label">创建时间：</span>
              <span>{{ detailForm.createTime || '-' }}</span>
            </div>
          </div>
        </div>
        <ElDivider />
        <div class="notice-detail__content" v-html="detailForm.noticeContent || '暂无内容'"></div>
      </div>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtWangEditor from '@/components/core/forms/art-wang-editor/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import {
    addNotice,
    delNotice,
    getNotice,
    listNotice,
    type NoticeForm,
    type NoticeQuery,
    type NoticeVO,
    updateNotice
  } from '@/api/system/notice'

  let searchForm = reactive<NoticeQuery>({
    noticeTitle: '',
    createByName: '',
    noticeType: '',
    status: ''
  })

  const dialogVisible = ref(false)
  const detailDialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const formRef = ref()

  const selectedRows = ref<NoticeVO[]>([])
  const detailForm = ref<NoticeVO>({} as NoticeVO)

  const formData = reactive<NoticeForm>({
    noticeId: undefined,
    noticeTitle: '',
    noticeType: '',
    noticeContent: '',
    status: '0'
  })

  const rules = reactive({
    noticeTitle: [{ required: true, message: '公告标题不能为空', trigger: 'blur' }],
    noticeType: [{ required: true, message: '公告类型不能为空', trigger: 'change' }]
  })

  const formItems = computed(() => [
    {
      label: '公告标题',
      key: 'noticeTitle',
      type: 'input',
      props: { clearable: true, placeholder: '请输入公告标题' }
    },
    {
      label: '操作人员',
      key: 'createByName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入操作人员' }
    },
    {
      label: '公告类型',
      key: 'noticeType',
      type: 'select',
      props: {
        placeholder: '请选择公告类型',
        options: [
          { label: '通知', value: '1' },
          { label: '公告', value: '2' },
          { label: '消息', value: '3' }
        ],
        clearable: true
      }
    }
  ])

  const getNoticeTypeText = (type: string): string => {
    const map: Record<string, string> = {
      '1': '通知',
      '2': '公告',
      '3': '消息'
    }
    return map[type] || type
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
      apiFn: listNotice,
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
        { prop: 'noticeTitle', label: '公告标题', showOverflowTooltip: true },
        {
          prop: 'noticeType',
          label: '公告类型',
          width: 100,
          align: 'center',
          formatter: (row: NoticeVO) =>
            h(ElTag, { type: 'primary', size: 'small' }, () => getNoticeTypeText(row.noticeType))
        },
        {
          prop: 'status',
          label: '状态',
          width: 80,
          align: 'center',
          formatter: (row: NoticeVO) =>
            h(ElTag, { type: row.status === '0' ? 'success' : 'danger', size: 'small' }, () =>
              row.status === '0' ? '正常' : '停用'
            )
        },
        { prop: 'createByName', label: '创建者', width: 100 },
        { prop: 'createTime', label: '创建时间', width: 180 },
        {
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right',
          formatter: (row: NoticeVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'detail',
                onClick: () => handleDetail(row)
              }),
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:notice:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:notice:remove',
                onClick: () => handleDelete(row)
              })
            ])
        }
      ]
    }
  })

  const showDialog = (type: 'add' | 'edit', row?: NoticeVO) => {
    dialogType.value = type
    if (row) {
      getNotice(row.noticeId).then((data) => {
        Object.assign(formData, data)
      })
    } else {
      Object.assign(formData, {
        noticeId: undefined,
        noticeTitle: '',
        noticeType: '',
        noticeContent: '',
        status: '0'
      })
    }
    dialogVisible.value = true
  }

  const handleDetail = async (row: NoticeVO) => {
    const data = await getNotice(row.noticeId)
    detailForm.value = data
    detailDialogVisible.value = true
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (formData.noticeId) {
          updateNotice(formData).then(() => {
            ElMessage.success('修改成功')
            dialogVisible.value = false
            refreshData()
          })
        } else {
          addNotice(formData).then(() => {
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
    searchForm.noticeTitle = ''
    searchForm.createByName = ''
    searchForm.noticeType = ''
    searchForm.status = ''
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: NoticeVO[]) => {
    selectedRows.value = selection
  }

  const handleDelete = async (row?: NoticeVO) => {
    const noticeIds = row?.noticeId || selectedRows.value.map((r) => r.noticeId).join(',')
    if (!noticeIds) return
    try {
      await ElMessageBox.confirm(`确定要删除公告编号为"${noticeIds}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delNotice(noticeIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
</script>

<style lang="scss" scoped>
  .notice-detail {
    &__header {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    &__title {
      font-size: 22px;
      font-weight: 700;
      line-height: 1.4;
      color: var(--el-text-color-primary);
    }

    &__meta {
      display: flex;
      flex-wrap: wrap;
      gap: 12px 20px;
      font-size: 13px;
      line-height: 1.6;
      color: var(--el-text-color-secondary);
    }

    &__meta-item {
      display: inline-flex;
      gap: 4px;
      align-items: center;
    }

    &__meta-label {
      color: var(--el-text-color-secondary);
      white-space: nowrap;
    }

    &__content {
      max-height: 60vh;
      overflow: auto;
      line-height: 1.8;
      color: var(--el-text-color-primary);
      word-break: break-word;
    }
  }
</style>
