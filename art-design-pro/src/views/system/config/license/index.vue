<template>
  <div class="art-full-height">
    <ElCard class="license-card">
      <template #header>
        <div class="license-header">
          <span class="license-title">授权管理</span>
          <ElTag :type="state.valid ? 'success' : 'danger'" effect="dark" size="large">
            {{ state.valid ? '授权有效' : '授权无效' }}
          </ElTag>
        </div>
      </template>

      <ElAlert
        v-if="state.message"
        class="license-alert"
        :title="state.message"
        :type="state.valid ? 'success' : 'error'"
        :closable="false"
        show-icon
      />

      <ElDescriptions class="license-desc" :column="1" border>
        <ElDescriptionsItem label="授权状态">
          <ElTag :type="state.valid ? 'success' : 'danger'">
            {{ state.valid ? '有效' : '无效' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="授权类型">{{ state.type || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="授权版本">{{ state.version || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="签发时间">{{ state.issuedAt || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="过期时间">{{ state.expireAt || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="机器指纹">
          <div class="license-fp">
            <span class="license-fp-text">{{ state.fingerprint || '-' }}</span>
            <ElButton
              v-if="state.fingerprint"
              type="primary"
              link
              :icon="CopyDocument"
              @click="copyFingerprint"
            >
              复制
            </ElButton>
          </div>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="最近校验">
          {{ state.lastChecked ? formatTime(state.lastChecked) : '-' }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <div class="license-actions">
        <ElUpload
          ref="uploadRef"
          :auto-upload="false"
          :show-file-list="false"
          accept=".lic"
          :on-change="handleFileChange"
        >
          <ElButton type="primary" :icon="Upload" :loading="uploading"
            >上传授权文件（.lic）</ElButton
          >
        </ElUpload>
        <ElButton :icon="Refresh" @click="loadInfo">刷新状态</ElButton>
        <ElButton :icon="Key" @click="loadFingerprint">获取指纹</ElButton>
      </div>
    </ElCard>

    <ElDialog v-model="fpDialogVisible" title="当前机器指纹" width="600px" align-center>
      <p class="license-fp-tip">
        将下方指纹发给厂商，由其生成授权码（.lic）后，通过「上传授权文件」导入即可激活。
      </p>
      <ElInput :model-value="currentFingerprint" type="textarea" :rows="3" readonly />
      <template #footer>
        <ElButton type="primary" @click="copyText(currentFingerprint)">复制指纹</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import type { UploadFile } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { CopyDocument, Key, Refresh, Upload } from '@element-plus/icons-vue'
  import {
    getLicenseFingerprint,
    getLicenseInfo,
    type LicenseState,
    uploadLicense
  } from '@/api/system/license'

  const state = reactive<LicenseState>({
    valid: false
  })

  const uploading = ref(false)
  const uploadRef = ref()
  const fpDialogVisible = ref(false)
  const currentFingerprint = ref('')

  function formatTime(ts: string | number) {
    const d = new Date(ts)
    const pad = (n: number) => `${n}`.padStart(2, '0')
    return (
      `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ` +
      `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    )
  }

  async function loadInfo() {
    try {
      const data = await getLicenseInfo()
      Object.assign(state, data)
    } catch {
      // 异常时保持页面可用
    }
  }

  async function loadFingerprint() {
    try {
      currentFingerprint.value = await getLicenseFingerprint()
      fpDialogVisible.value = true
    } catch {
      ElMessage.error('获取机器指纹失败')
    }
  }

  async function copyFingerprint() {
    await copyText(state.fingerprint || '')
  }

  async function copyText(text: string) {
    if (!text) return
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success('已复制到剪贴板')
    } catch {
      ElMessage.error('复制失败，请手动选择文本复制')
    }
  }

  async function handleFileChange(uploadFile: UploadFile) {
    const file = uploadFile.raw
    if (!file) return
    if (!file.name.endsWith('.lic')) {
      ElMessage.error('请上传 .lic 授权文件')
      uploadRef.value?.clearFiles()
      return
    }
    uploading.value = true
    try {
      const data = await uploadLicense(file)
      Object.assign(state, data)
      ElMessage.success(data.valid ? '授权已更新，授权有效' : '授权已更新，但当前授权无效')
    } catch {
      // 错误提示由全局拦截器处理
    } finally {
      uploading.value = false
      uploadRef.value?.clearFiles()
    }
  }

  onMounted(loadInfo)
</script>

<style scoped>
  .license-card {
    max-width: 860px;
  }

  .license-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .license-title {
    font-size: 16px;
    font-weight: 600;
  }

  .license-alert {
    margin-bottom: 16px;
  }

  .license-desc {
    margin-bottom: 20px;
  }

  .license-fp {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .license-fp-text {
    word-break: break-all;
  }

  .license-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .license-fp-tip {
    margin-top: 0;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
</style>
