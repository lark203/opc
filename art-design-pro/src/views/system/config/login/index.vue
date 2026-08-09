<template>
  <div class="login-config">
    <div v-if="loading" class="loading-wrap">
      <ElIcon class="is-loading" :size="32">
        <Loading />
      </ElIcon>
    </div>
    <template v-else>
      <ElForm :model="form" label-position="top" class="config-form">
        <ElFormItem label="登录验证码">
          <ElSwitch v-model="formEdit.captchaEnabled" :disabled="!editing" />
          <div class="form-item-desc">{{ descriptions['LOGIN_CAPTCHA_ENABLED'] }}</div>
        </ElFormItem>

        <ElFormItem label="验证码类型">
          <ElSelect v-model="formEdit.captchaType" style="width: 200px" :disabled="!editing">
            <ElOption label="算术验证码" value="1" />
            <ElOption label="字符验证码" value="2" />
          </ElSelect>
          <div class="form-item-desc">{{ descriptions['LOGIN_CAPTCHA_TYPE'] }}</div>
        </ElFormItem>

        <ElFormItem label="验证码字符长度">
          <div class="form-item-wrapper">
            <ElInputNumber
              v-model="formEdit.captchaLength"
              :min="formEdit.captchaType === '1' ? 1 : 2"
              :max="6"
              :disabled="!editing"
            />
            <span class="form-item-unit">位</span>
          </div>
          <div class="form-item-desc">{{ descriptions['LOGIN_CAPTCHA_LENGTH'] }}</div>
        </ElFormItem>

        <ElFormItem label="允许注册">
          <ElSwitch v-model="formEdit.registerEnabled" :disabled="!editing" />
          <div class="form-item-desc">{{ descriptions['LOGIN_REGISTER_ENABLED'] }}</div>
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
  import { onMounted, reactive, ref, watch } from 'vue'
  import { Check, Close, Edit, Loading, RefreshLeft } from '@element-plus/icons-vue'
  import { getOption, resetOption, saveOption, SysOptionVO } from '@/api/system/option'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'LoginConfig' })

  const CATEGORY = 'LOGIN'
  const loading = ref(false)
  const editing = ref(false)
  const buttonLoading = ref(false)
  const options = ref<SysOptionVO[]>([])
  const form = reactive<Record<string, string>>({})
  const descriptions = reactive<Record<string, string>>({})
  const formEdit = reactive({
    captchaEnabled: true,
    captchaType: '1',
    captchaLength: 4,
    registerEnabled: true
  })

  // 验证码类型切换时，保证字符长度满足各自最小限制：算术=1，字符=2
  watch(
    () => formEdit.captchaType,
    () => {
      const min = formEdit.captchaType === '1' ? 1 : 2
      if (formEdit.captchaLength < min) {
        formEdit.captchaLength = min
      }
    }
  )

  function toNum(v?: string): number {
    const n = Number(v)
    return Number.isFinite(n) ? n : 0
  }

  async function load() {
    loading.value = true
    try {
      options.value = await getOption(CATEGORY)
      options.value.forEach((o) => {
        form[o.code] = o.value
        descriptions[o.code] = o.description || ''
      })
      syncToEdit()
    } finally {
      loading.value = false
    }
  }

  function syncToEdit() {
    formEdit.captchaEnabled = form.LOGIN_CAPTCHA_ENABLED === '1'
    formEdit.captchaType = form.LOGIN_CAPTCHA_TYPE || '1'
    formEdit.captchaLength = toNum(form.LOGIN_CAPTCHA_LENGTH) || 4
    formEdit.registerEnabled = form.LOGIN_REGISTER_ENABLED === '1'
  }

  function syncFromEdit() {
    form.LOGIN_CAPTCHA_ENABLED = formEdit.captchaEnabled ? '1' : '0'
    form.LOGIN_CAPTCHA_TYPE = formEdit.captchaType
    form.LOGIN_CAPTCHA_LENGTH = String(formEdit.captchaLength)
    form.LOGIN_REGISTER_ENABLED = formEdit.registerEnabled ? '1' : '0'
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
    syncFromEdit()
    buttonLoading.value = true
    try {
      await saveOption(buildPayload())
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
  }

  onMounted(load)
</script>

<style scoped lang="scss">
  .login-config {
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

  .form-item-wrapper {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .form-item-unit {
    font-size: 13px;
    color: var(--el-text-color-secondary);
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
