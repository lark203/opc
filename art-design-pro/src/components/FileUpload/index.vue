<template>
  <div class="upload-file">
    <ElUpload
      ref="fileUploadRef"
      multiple
      :action="uploadFileUrl"
      :auto-upload="false"
      :before-upload="handleBeforeUpload"
      :data="uploadData"
      :file-list="fileList"
      :limit="limit"
      :accept="fileAccept"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      :on-change="handleChange"
      class="upload-file-uploader"
      v-if="!disabled"
    >
      <ElButton type="primary">选取文件</ElButton>
    </ElUpload>
    <div v-if="showTip && !disabled" class="el-upload__tip">
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
    <transition-group
      class="upload-file-list el-upload-list el-upload-list--text"
      name="el-fade-in-linear"
      tag="ul"
    >
      <li
        v-for="(file, index) in fileList"
        :key="file.uid"
        class="el-upload-list__item ele-upload-list__item-content"
      >
        <span class="el-icon-document">{{ getFileName(file.name) }}</span>
        <div class="ele-upload-list__item-content-action">
          <ElButton type="danger" v-if="!disabled && !isUploading" link @click="handleDelete(index)"
            >删除
          </ElButton>
        </div>
      </li>
    </transition-group>
    <div class="upload-footer" v-if="fileList.length > 0">
      <ElButton type="default" @click="handleCancel" :disabled="isUploading">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit" :disabled="isUploading">确定</ElButton>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import type { UploadFile } from 'element-plus'
  import { ElButton, ElLoading, ElMessage, ElUpload } from 'element-plus'
  import { delOss, listByIds } from '@/api/system/oss'
  import { useUserStore } from '@/store/modules/user'

  const props = withDefaults(
    defineProps<{
      modelValue?:
        | string
        | number
        | Array<string | number | { name: string; url: string; ossId: number | string }>
      limit?: number
      fileSize?: number
      fileType?: string[]
      isShowTip?: boolean
      disabled?: boolean
    }>(),
    {
      modelValue: () => [],
      limit: 5,
      fileSize: 5,
      fileType: () => ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'pdf'],
      isShowTip: true,
      disabled: false
    }
  )

  const emit = defineEmits(['update:modelValue', 'upload-complete'])

  const number = ref(0)
  const uploadList = ref<any[]>([])
  const isUploading = ref(false)
  let loadingInstance: ReturnType<typeof ElLoading.service> | null = null

  const uploadFileUrl = computed(() => `${import.meta.env.VITE_API_URL}/resource/oss/upload`)
  const headers = computed(() => ({
    Authorization: `Bearer ${useUserStore().accessToken}`,
    clientid: import.meta.env.VITE_APP_CLIENT_ID || ''
  }))

  const fileList = ref<any[]>([])
  const showTip = computed(() => props.isShowTip && (props.fileType || props.fileSize))

  const fileUploadRef = ref<InstanceType<typeof ElUpload>>()

  const uploadData = computed(() => ({}))

  const fileAccept = computed(() => props.fileType.map((type) => `.${type}`).join(','))

  watch(
    () => props.modelValue,
    async (val) => {
      if (val) {
        let temp = 1
        let list: any[] = []
        if (Array.isArray(val)) {
          list = val
        } else if (typeof val === 'string' || typeof val === 'number') {
          const res = await listByIds(val)
          list = res.map((oss) => ({
            name: oss.originalName,
            url: oss.url,
            ossId: oss.ossId
          }))
        } else {
          list = []
        }
        fileList.value = list.map((item) => {
          item = { name: item.name, url: item.url, ossId: item.ossId }
          item.uid = item.uid || new Date().getTime() + temp++
          item.status = 'success'
          return item
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
    if (props.fileType.length) {
      const fileName = file.name.split('.')
      const fileExt = fileName[fileName.length - 1]
      const isTypeOk = props.fileType.indexOf(fileExt) >= 0
      if (!isTypeOk) {
        ElMessage.error(`文件格式不正确, 请上传${props.fileType.join('/')}格式文件!`)
        return false
      }
    }
    if (file.name.includes(',')) {
      ElMessage.error('文件名不正确，不能包含英文逗号!')
      return false
    }
    if (props.fileSize) {
      const isLt = file.size / 1024 / 1024 < props.fileSize
      if (!isLt) {
        ElMessage.error(`上传文件大小不能超过 ${props.fileSize} MB!`)
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
    loadingInstance = ElLoading.service({ text: '正在上传文件，请稍候...' })
    number.value = 0
    uploadList.value = []
    fileUploadRef.value?.submit()
  }

  const handleCancel = () => {
    fileList.value = fileList.value.filter((f) => f.ossId)
    emit('update:modelValue', listToString(fileList.value))
  }

  const handleUploadError = () => {
    ElMessage.error('上传文件失败')
    fileList.value = []
    uploadList.value = []
    number.value = 0
    emit('update:modelValue', '')
    closeLoading()
    isUploading.value = false
    emit('upload-complete')
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
      emit('update:modelValue', '')
      closeLoading()
      isUploading.value = false
      ElMessage.error(res.msg)
      emit('upload-complete')
    }
  }

  const handleDelete = (index: number) => {
    const item = fileList.value[index]
    if (item.ossId) {
      delOss(item.ossId)
    }
    fileList.value.splice(index, 1)
    emit('update:modelValue', listToString(fileList.value))
  }

  const uploadedSuccessfully = () => {
    const filesToUpload = fileList.value.filter((f) => !f.ossId)
    if (uploadList.value.length === filesToUpload.length) {
      fileList.value = []
      uploadList.value = []
      number.value = 0
      emit('update:modelValue', '')
      closeLoading()
      isUploading.value = false
      ElMessage.success('文件上传成功')
      emit('upload-complete')
    }
  }

  const getFileName = (name: string) => {
    if (name.lastIndexOf('/') > -1) {
      return name.slice(name.lastIndexOf('/') + 1)
    } else {
      return name
    }
  }

  const listToString = (list: any[], separator?: string) => {
    let strs = ''
    separator = separator || ','
    list.forEach((item) => {
      if (item.ossId) {
        strs += item.ossId + separator
      }
    })
    return strs != '' ? strs.substring(0, strs.length - 1) : ''
  }
</script>

<style lang="scss" scoped>
  .upload-file-uploader {
    margin-bottom: 5px;
  }

  .upload-file-list .el-upload-list__item {
    position: relative;
    margin-bottom: 10px;
    line-height: 2;
    border: 1px solid #e4e7ed;
  }

  .upload-file-list .ele-upload-list__item-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: inherit;
  }

  .ele-upload-list__item-content-action .el-link {
    margin-right: 10px;
  }

  .upload-footer {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    margin-top: 10px;
  }
</style>
