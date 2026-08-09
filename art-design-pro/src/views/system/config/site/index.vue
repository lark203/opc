<template>
  <div class="site-config">
    <div v-if="loading" class="loading-wrap">
      <ElIcon class="is-loading" :size="32">
        <Loading />
      </ElIcon>
    </div>
    <template v-else>
      <ElForm :model="form" label-position="top" class="config-form">
        <ElFormItem label="系统LOGO">
          <ElUpload
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :disabled="!editing"
            :on-change="(file: any) => onImage(file, 'SITE_LOGO')"
            class="image-upload"
          >
            <img v-if="form.SITE_LOGO" :src="form.SITE_LOGO" class="item-image" />
            <div v-else class="image-upload-placeholder">
              <ElIcon :size="24">
                <Plus />
              </ElIcon>
              <span>上传 Logo</span>
            </div>
          </ElUpload>
          <div class="form-item-desc">{{ descriptions['SITE_LOGO'] }}</div>
        </ElFormItem>

        <ElFormItem label="系统图标">
          <ElUpload
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :disabled="!editing"
            :on-change="(file: any) => onImage(file, 'SITE_FAVICON')"
            class="image-upload"
          >
            <img
              v-if="form.SITE_FAVICON"
              :src="form.SITE_FAVICON"
              class="item-image item-image-sm"
            />
            <div v-else class="image-upload-placeholder">
              <ElIcon :size="24">
                <Plus />
              </ElIcon>
              <span>上传 图标</span>
            </div>
          </ElUpload>
          <div class="form-item-desc">{{ descriptions['SITE_FAVICON'] }}</div>
        </ElFormItem>

        <ElFormItem label="系统名称" required>
          <ElInput
            v-model="form.SITE_TITLE"
            :disabled="!editing"
            maxlength="18"
            show-word-limit
            placeholder="请输入系统名称"
          />
          <div class="form-item-desc">{{ descriptions['SITE_TITLE'] }}</div>
        </ElFormItem>

        <ElFormItem label="系统描述" required>
          <ElInput
            v-model="form.SITE_DESCRIPTION"
            type="textarea"
            :rows="3"
            :disabled="!editing"
            placeholder="请输入系统描述"
          />
          <div class="form-item-desc">{{ descriptions['SITE_DESCRIPTION'] }}</div>
        </ElFormItem>

        <ElFormItem label="系统子描述">
          <ElInput
            v-model="form.SITE_SUB_DESCRIPTION"
            :disabled="!editing"
            placeholder="请输入系统子描述"
          />
          <div class="form-item-desc">{{ descriptions['SITE_SUB_DESCRIPTION'] }}</div>
        </ElFormItem>

        <ElFormItem label="版权声明" required>
          <ElInput
            v-model="form.SITE_COPYRIGHT"
            :disabled="!editing"
            placeholder="请输入版权声明"
          />
          <div class="form-item-desc">{{ descriptions['SITE_COPYRIGHT'] }}</div>
        </ElFormItem>

        <ElFormItem label="备案号">
          <ElInput
            v-model="form.SITE_BEIAN"
            :disabled="!editing"
            maxlength="30"
            show-word-limit
            placeholder="请输入备案号"
          />
          <div class="form-item-desc">{{ descriptions['SITE_BEIAN'] }}</div>
        </ElFormItem>

        <ElFormItem label="显示底部备案区域">
          <ElSwitch
            v-model="form.SITE_SHOW_FOOTER"
            active-value="true"
            inactive-value="false"
            :disabled="!editing"
          />
          <div class="form-item-desc">{{ descriptions['SITE_SHOW_FOOTER'] }}</div>
        </ElFormItem>
      </ElForm>

      <div class="action-bar">
        <template v-if="!editing">
          <ElButton type="primary" @click="handleEdit">
            <ElIcon>
              <Edit />
            </ElIcon>
            修改
          </ElButton>
          <ElButton @click="handleReset">
            <ElIcon>
              <RefreshLeft />
            </ElIcon>
            恢复默认
          </ElButton>
        </template>
        <template v-else>
          <ElButton type="primary" :loading="buttonLoading" @click="handleSave">
            <ElIcon>
              <Check />
            </ElIcon>
            保存
          </ElButton>
          <ElButton :disabled="buttonLoading" @click="handleReset">
            <ElIcon>
              <RefreshLeft />
            </ElIcon>
            重置
          </ElButton>
          <ElButton :disabled="buttonLoading" @click="handleCancel">
            <ElIcon>
              <Close />
            </ElIcon>
            取消
          </ElButton>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import { Check, Close, Edit, Loading, Plus, RefreshLeft } from '@element-plus/icons-vue'
  import { getOption, resetOption, saveOption, SysOptionVO } from '@/api/system/option'
  import { ElMessage } from 'element-plus'
  import { useSiteConfigStore } from '@/store/modules/siteConfig'

  defineOptions({ name: 'SiteConfig' })

  const siteConfigStore = useSiteConfigStore()

  const CATEGORY = 'SITE'
  const loading = ref(false)
  const editing = ref(false)
  const buttonLoading = ref(false)
  const options = ref<SysOptionVO[]>([])
  const form = reactive<Record<string, string>>({})
  const descriptions = reactive<Record<string, string>>({})

  async function load() {
    loading.value = true
    try {
      options.value = await getOption(CATEGORY)
      options.value.forEach((o) => {
        form[o.code] = o.value || o.defaultValue || ''
        descriptions[o.code] = o.description || ''
      })
    } finally {
      loading.value = false
    }
  }

  function toBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.onerror = reject
      reader.readAsDataURL(file)
    })
  }

  async function onImage(file: any, code: string) {
    const raw = file?.raw as File
    if (!raw) return
    form[code] = await toBase64(raw)
  }

  function buildPayload() {
    return options.value.map((o) => ({
      optionId: o.optionId,
      code: o.code,
      value: form[o.code] ?? ''
    }))
  }

  function handleEdit() {
    editing.value = true
  }

  function handleCancel() {
    editing.value = false
    load()
  }

  async function handleSave() {
    buttonLoading.value = true
    try {
      await saveOption(buildPayload())
      // 保存后实时刷新登录页/页头/页脚的网站品牌展示
      siteConfigStore.updateFromForm({ ...form })
      ElMessage.success('保存成功')
      editing.value = false
    } finally {
      buttonLoading.value = false
    }
  }

  async function handleReset() {
    await resetOption(CATEGORY)
    ElMessage.success('已重置为默认值')
    await load()
    // 重置后同步刷新登录页/页头/页脚的网站品牌展示
    await siteConfigStore.load(true)
  }

  onMounted(load)
</script>

<style scoped lang="scss">
  .site-config {
    display: flex;
    flex-direction: column;
  }

  .loading-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    color: var(--el-color-primary);
  }

  .config-form {
    padding: 8px 0;

    :deep(.el-form-item) {
      margin-bottom: 20px;
    }

    :deep(.el-form-item__label) {
      height: auto;
      padding-bottom: 4px;
      font-size: 14px;
      font-weight: 600;
      line-height: 1.5;
      color: var(--el-text-color-primary);
    }

    :deep(.el-form-item__content) {
      display: flex;
      flex-direction: column;
      align-items: stretch;
      line-height: normal;
    }
  }

  .image-upload {
    :deep(.el-upload) {
      display: block;
    }

    &.image-upload-sm {
      :deep(.el-upload) {
        width: 64px;
        height: 64px;
      }
    }
  }

  .image-upload-placeholder {
    display: flex;
    flex-direction: column;
    gap: 4px;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    cursor: pointer;
    background: var(--el-fill-color-light);
    border: 1px dashed var(--el-border-color);
    border-radius: 8px;
    transition: border-color 0.2s;

    &:hover {
      color: var(--el-color-primary);
      border-color: var(--el-color-primary);
    }
  }

  .item-image {
    width: 80px;
    height: 80px;
    cursor: pointer;
    object-fit: contain;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color);
    border-radius: 8px;

    &.item-image-sm {
      width: 64px;
      height: 64px;
    }
  }

  .form-item-desc {
    margin-top: 4px;
    font-size: 12px;
    line-height: 1.5;
    color: var(--el-text-color-placeholder);
  }

  .action-bar {
    display: flex;
    gap: 12px;
    padding-top: 10px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
</style>
