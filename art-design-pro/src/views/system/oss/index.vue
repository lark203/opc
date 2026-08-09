<template>
  <div class="oss-page art-full-height">
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
            <ElButton type="primary" v-auth="'system:oss:upload'" @click="handleFile"
              >上传文件</ElButton
            >
            <ElButton type="primary" v-auth="'system:oss:upload'" @click="handleImage"
              >上传图片</ElButton
            >
            <ElButton
              type="danger"
              v-auth="'system:oss:remove'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
            >
              删除
            </ElButton>
            <ElButton
              :type="previewListResource ? 'danger' : 'warning'"
              @click="handlePreviewListResource(!previewListResource)"
            >
              预览开关 : {{ previewListResource ? '禁用' : '启用' }}
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
        <template #filePreview="{ row }">
          <ElImage
            v-if="previewListResource && checkFileSuffix(row.fileSuffix)"
            :src="buildAuthUrl(`/resource/oss/preview/${row.ossId}`)"
            :preview-src-list="[buildAuthUrl(`/resource/oss/preview/${row.ossId}`)]"
            preview-teleported
            class="w-20 h-20 object-cover rounded cursor-pointer"
            fit="cover"
          >
            <template #error>
              <div class="flex items-center justify-center w-full h-full text-gray-400">
                <ElIcon class="w-8 h-8">
                  <PictureFilled />
                </ElIcon>
              </div>
            </template>
          </ElImage>
          <span v-else class="text-sm text-gray-600 truncate max-w-40">{{ row.url }}</span>
        </template>
        <template #createTime="{ row }">
          <span>{{ row.createTime }}</span>
        </template>
        <template #action="{ row }">
          <ArtButtonTable
            type="download"
            auth="system:oss:download"
            @click="() => handleDownload(row)"
          />
          <ArtButtonTable type="delete" auth="system:oss:remove" @click="() => handleDelete(row)" />
        </template>
      </ArtTable>
    </ElCard>
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="500px" align-center>
      <ElForm :model="uploadForm" label-width="80px">
        <ElFormItem label="文件">
          <FileUpload
            v-if="uploadType === 0"
            v-model="uploadForm.file"
            @upload-complete="handleUploadComplete"
          />
          <ImageUpload
            v-if="uploadType === 1"
            v-model="uploadForm.file"
            @upload-complete="handleUploadComplete"
          />
        </ElFormItem>
      </ElForm>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import { PictureFilled } from '@element-plus/icons-vue'
  import type { OssQuery, OssVO } from '@/api/system/oss'
  import { ossApi } from '@/api/system/oss'
  import { getConfigKey, updateConfigByKey } from '@/api/system/config'
  import { useTable } from '@/hooks/core/useTable'
  import FileUpload from '@/components/FileUpload/index.vue'
  import ImageUpload from '@/components/ImageUpload/index.vue'
  import { buildAuthUrl } from '@/utils/auth-url'

  defineOptions({ name: 'Oss' })

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
      apiFn: ossApi.listOss,
      apiParams: {
        fileName: '',
        originalName: '',
        fileSuffix: '',
        createTime: '',
        service: ''
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', label: '序号', width: 60 },
        { prop: 'fileName', label: '文件名', showOverflowTooltip: true },
        { prop: 'originalName', label: '原名', showOverflowTooltip: true },
        { prop: 'fileSuffix', label: '文件后缀' },
        {
          prop: 'filePreview',
          label: '文件展示',
          useSlot: true,
          slotName: 'filePreview'
        },
        {
          prop: 'createTime',
          label: '创建时间',
          useSlot: true,
          slotName: 'createTime'
        },
        { prop: 'createByName', label: '上传人' },
        { prop: 'service', label: '服务商' },
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

  let searchForm = reactive<OssQuery>({
    fileName: '',
    originalName: '',
    fileSuffix: '',
    createTime: '',
    service: ''
  })

  const dateRange = ref<string[]>([])

  const formItems = computed(() => [
    {
      label: '文件名',
      key: 'fileName',
      type: 'input',
      props: { placeholder: '请输入文件名', clearable: true }
    },
    {
      label: '原名',
      key: 'originalName',
      type: 'input',
      props: { placeholder: '请输入原名', clearable: true }
    },
    {
      label: '文件后缀',
      key: 'fileSuffix',
      type: 'input',
      props: { placeholder: '请输入文件后缀', clearable: true }
    },
    {
      label: '创建时间',
      key: 'dateRange',
      type: 'datetimerange',
      props: {
        vModel: dateRange,
        rangeSeparator: '-',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        valueFormat: 'YYYY-MM-DD HH:mm:ss'
      }
    },
    {
      label: '服务商',
      key: 'service',
      type: 'input',
      props: { placeholder: '请输入服务商', clearable: true }
    }
  ])

  const selectedRows = ref<OssVO[]>([])
  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const uploadType = ref(0)
  const previewListResource = ref(true)
  const uploadForm = reactive({ file: '' })

  const handleSelectionChange = (rows: OssVO[]) => {
    selectedRows.value = rows
  }

  const handleSearch = () => {
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.createTime = `${dateRange.value[0]},${dateRange.value[1]}`
    }
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    dateRange.value = []
    searchForm.fileName = ''
    searchForm.originalName = ''
    searchForm.fileSuffix = ''
    searchForm.createTime = ''
    searchForm.service = ''
    resetSearchParams()
  }

  const handleFile = () => {
    uploadType.value = 0
    dialogTitle.value = '上传文件'
    dialogVisible.value = true
  }

  const handleImage = () => {
    uploadType.value = 1
    dialogTitle.value = '上传图片'
    dialogVisible.value = true
  }

  const handleUploadComplete = () => {
    dialogVisible.value = false
    uploadForm.file = ''
    refreshData()
  }

  const handleDownload = (row: OssVO) => {
    ossApi.downloadOss(row.ossId)
  }

  const handleDelete = async (row?: OssVO) => {
    const ossIds = row ? [row.ossId] : selectedRows.value.map((item) => item.ossId)
    if (ossIds.length === 0) return
    try {
      await ElMessageBox.confirm('是否确认删除选中的文件?', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await ossApi.delOss(ossIds)
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handlePreviewListResource = async (preview: boolean) => {
    try {
      await updateConfigByKey('sys.oss.previewListResource', preview)
      previewListResource.value = preview
      refreshData()
      ElMessage.success((preview ? '启用' : '停用') + '成功')
    } catch {
      return
    }
  }

  const checkFileSuffix = (fileSuffix: string | string[]) => {
    const arr = ['.png', '.jpg', '.jpeg', '.gif', '.bmp', '.webp']
    const suffixArray = Array.isArray(fileSuffix) ? fileSuffix : [fileSuffix]
    return suffixArray.some((suffix) => arr.includes(suffix.toLowerCase()))
  }

  onMounted(async () => {
    const res = await getConfigKey('sys.oss.previewListResource')
    previewListResource.value = res === undefined ? true : res === 'true'
  })
</script>
