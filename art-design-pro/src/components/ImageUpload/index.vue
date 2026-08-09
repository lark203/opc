<template>
  <div class="component-upload-image">
    <ElUpload
      ref="imageUploadRef"
      multiple
      :action="uploadImgUrl"
      list-type="picture-card"
      :auto-upload="false"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :data="uploadData"
      :limit="limit"
      :accept="fileAccept"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :before-remove="handleDelete"
      :show-file-list="true"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :on-change="handleChange"
      :class="{ hide: fileList.length >= limit }"
    >
      <ElIcon class="avatar-uploader-icon">
        <Plus />
      </ElIcon>
    </ElUpload>
    <div v-if="showTip" class="el-upload__tip">
      请上传
      <template v-if="fileSize">
        大小不超过
        <b style="color: #f56c6c">{{ fileSize }}MB</b>
      </template>
      <template v-if="fileType">
        格式为
        <b style="color: #f56c6c">{{ fileType.join('/') }}</b>
      </template>
      的文件
    </div>
    <div class="upload-footer" v-if="fileList.length > 0">
      <ElButton type="default" @click="handleCancel" :disabled="isUploading">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit" :disabled="isUploading">确定</ElButton>
    </div>

    <ElDialog v-model="dialogVisible" title="预览" width="800px" append-to-body>
      <img :src="dialogImageUrl" style="display: block; max-width: 100%; margin: 0 auto" />
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import type { UploadFile } from 'element-plus'
  import { ElButton, ElDialog, ElIcon, ElLoading, ElMessage, ElUpload } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import type { OssVO } from '@/api/system/oss'
  import { delOss, listByIds } from '@/api/system/oss'
  import { useUserStore } from '@/store/modules/user'

  const props = withDefaults(
    defineProps<{
      modelValue?: string | number | Array<string | number | OssVO>
      limit?: number
      fileSize?: number
      fileType?: string[]
      isShowTip?: boolean
    }>(),
    {
      modelValue: () => [],
      limit: 5,
      fileSize: 5,
      fileType: () => ['png', 'jpg', 'jpeg'],
      isShowTip: true
    }
  )

  const emit = defineEmits(['update:modelValue', 'upload-complete'])

  const number = ref(0)
  const uploadList = ref<any[]>([])
  const dialogImageUrl = ref('')
  const dialogVisible = ref(false)
  const isUploading = ref(false)
  let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

  const uploadImgUrl = computed(() => `${import.meta.env.VITE_API_URL}/resource/oss/upload`)
  const headers = computed(() => ({
    Authorization: `Bearer ${useUserStore().accessToken}`,
    clientid: import.meta.env.VITE_APP_CLIENT_ID || ''
  }))

  const fileList = ref<any[]>([])
  const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize))

  const imageUploadRef = ref<InstanceType<typeof ElUpload>>()

  const uploadData = computed(() => ({}))

  const fileAccept = computed(() => props.fileType.map((type) => `.${type}`).join(','))

  watch(
    () => props.modelValue,
    async (val) => {
      if (val) {
        let list: any[] = []
        if (Array.isArray(val)) {
          list = val
        } else {
          const res = await listByIds(val)
          list = res
        }
        fileList.value = list.map((item) => {
          let itemData: any
          if (typeof item === 'string') {
            itemData = { name: item, url: item }
          } else {
            itemData = { name: item.ossId, url: item.url, ossId: item.ossId }
          }
          itemData.status = 'success'
          return itemData
        })
      } else {
        fileList.value = []
      }
    },
    { deep: true, immediate: true }
  )

  const closeLoading = () => {
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }

  const handleBeforeUpload = (file: any) => {
    let isImg = false
    if (props.fileType.length) {
      let fileExtension = ''
      if (file.name.lastIndexOf('.') > -1) {
        fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1)
      }
      isImg = props.fileType.some((type) => {
        if (file.type.indexOf(type) > -1) return true
        if (fileExtension && fileExtension.indexOf(type) > -1) return true
        return false
      })
    } else {
      isImg = file.type.indexOf('image') > -1
    }
    if (!isImg) {
      ElMessage.error(`文件格式不正确, 请上传${props.fileType.join('/')}图片格式文件!`)
      return false
    }
    if (file.name.includes(',')) {
      ElMessage.error('文件名不正确，不能包含英文逗号!')
      return false
    }
    if (props.fileSize) {
      const isLt = file.size / 1024 / 1024 < props.fileSize
      if (!isLt) {
        ElMessage.error(`上传头像图片大小不能超过 ${props.fileSize} MB!`)
        return false
      }
    }
    return true
  }

  const handleExceed = () => {
    ElMessage.error(`上传文件数量不能超过 ${props.limit} 个!`)
  }

  const handleChange = (file: UploadFile) => {
    const status = file.status as string
    if (status === 'ready') {
      const existingIndex = fileList.value.findIndex((f) => f.name === file.name && !f.ossId)
      if (existingIndex === -1) {
        fileList.value.push({
          uid: file.uid,
          name: file.name,
          url: file.url,
          raw: file.raw
        })
      }
    } else if (status === 'removed') {
      const index = fileList.value.findIndex((f) => f.uid === file.uid)
      if (index > -1) {
        fileList.value.splice(index, 1)
      }
    }
  }

  const handleSubmit = () => {
    isUploading.value = true
    loadingInstance = ElLoading.service({ text: '正在上传图片，请稍候...' })
    number.value = 0
    uploadList.value = []
    imageUploadRef.value?.submit()
  }

  const handleCancel = () => {
    fileList.value = fileList.value.filter((f) => f.ossId)
    emit('update:modelValue', listToString(fileList.value))
  }

  const handleUploadSuccess = (res: any, file: UploadFile) => {
    if (res.code === 200) {
      uploadList.value.push({
        name: res.data.fileName,
        url: res.data.url,
        ossId: res.data.ossId,
        uid: file.uid
      })
      uploadedSuccessfully()
    } else {
      fileList.value = []
      uploadList.value = []
      number.value = 0
      imageUploadRef.value?.clearFiles()
      emit('update:modelValue', '')
      closeLoading()
      isUploading.value = false
      ElMessage.error(res.msg)
      emit('upload-complete')
    }
  }

  const handleDelete = (file: UploadFile): boolean => {
    const findex = fileList.value.map((f) => f.name).indexOf(file.name)
    if (findex > -1) {
      const ossId = fileList.value[findex].ossId
      if (ossId) {
        delOss(ossId)
      }
      fileList.value.splice(findex, 1)
      emit('update:modelValue', listToString(fileList.value))
      return false
    }
    return true
  }

  const uploadedSuccessfully = () => {
    const filesToUpload = fileList.value.filter((f) => !f.ossId)
    if (uploadList.value.length === filesToUpload.length) {
      fileList.value = []
      uploadList.value = []
      number.value = 0
      imageUploadRef.value?.clearFiles()
      emit('update:modelValue', '')
      closeLoading()
      isUploading.value = false
      ElMessage.success('图片上传成功')
      emit('upload-complete')
    }
  }

  const handleUploadError = () => {
    ElMessage.error('上传图片失败')
    fileList.value = []
    uploadList.value = []
    number.value = 0
    imageUploadRef.value?.clearFiles()
    emit('update:modelValue', '')
    closeLoading()
    isUploading.value = false
    emit('upload-complete')
  }

  const handlePictureCardPreview = (file: any) => {
    dialogImageUrl.value = file.url
    dialogVisible.value = true
  }

  const listToString = (list: any[], separator?: string) => {
    let strs = ''
    separator = separator || ','
    for (const i in list) {
      if (undefined !== list[i].ossId && list[i].url.indexOf('blob:') !== 0) {
        strs += list[i].ossId + separator
      }
    }
    return strs != '' ? strs.substring(0, strs.length - 1) : ''
  }
</script>

<style lang="scss" scoped>
  :deep(.hide .el-upload--picture-card) {
    display: none;
  }

  .upload-footer {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    width: 100%;
    margin-top: 10px;
  }
</style>
