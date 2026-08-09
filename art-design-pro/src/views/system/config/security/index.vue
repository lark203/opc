<template>
  <div class="security-config">
    <div v-if="loading" class="loading-wrap">
      <ElIcon class="is-loading" :size="32">
        <Loading />
      </ElIcon>
    </div>
    <template v-else>
      <ElForm :model="form" label-position="top" class="config-form">
        <ElFormItem label="密码错误锁定次数">
          <div class="form-item-wrapper">
            <ElInputNumber
              v-model="formEdit.errorLockCount"
              :min="0"
              :max="10"
              :disabled="!editing"
            />
            <span class="form-item-unit">次</span>
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_ERROR_LOCK_COUNT'] }}</div>
        </ElFormItem>

        <ElFormItem label="密码错误锁定时间">
          <div class="form-item-wrapper">
            <ElInputNumber
              v-model="formEdit.errorLockMinutes"
              :min="1"
              :max="1440"
              :disabled="!editing"
            />
            <span class="form-item-unit">分钟</span>
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_ERROR_LOCK_MINUTES'] }}</div>
        </ElFormItem>

        <ElFormItem label="密码有效期">
          <div class="form-item-wrapper">
            <ElInputNumber
              v-model="formEdit.expirationDays"
              :min="0"
              :max="999"
              :disabled="!editing"
            />
            <span class="form-item-unit">天</span>
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_EXPIRATION_DAYS'] }}</div>
        </ElFormItem>

        <ElFormItem label="密码过期提前提醒">
          <div class="form-item-wrapper">
            <ElInputNumber
              v-model="formEdit.warningDays"
              :min="0"
              :max="998"
              :disabled="!editing"
            />
            <span class="form-item-unit">天</span>
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_EXPIRATION_WARNING_DAYS'] }}</div>
        </ElFormItem>

        <ElFormItem label="密码历史不可重复次数">
          <div class="form-item-wrapper">
            <ElInputNumber
              v-model="formEdit.repetitionTimes"
              :min="3"
              :max="32"
              :disabled="!editing"
            />
            <span class="form-item-unit">次</span>
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_REPETITION_TIMES'] }}</div>
        </ElFormItem>

        <ElFormItem label="密码最小长度">
          <div class="form-item-wrapper">
            <ElInputNumber v-model="formEdit.minLength" :min="8" :max="32" :disabled="!editing" />
            <span class="form-item-unit">位</span>
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_MIN_LENGTH'] }}</div>
        </ElFormItem>

        <ElFormItem label="允许包含用户名">
          <ElSwitch v-model="formEdit.allowContainUsername" :disabled="!editing" />
          <div class="form-item-desc">{{ descriptions['PASSWORD_ALLOW_CONTAIN_USERNAME'] }}</div>
        </ElFormItem>

        <ElFormItem label="要求特殊字符">
          <ElSwitch v-model="formEdit.requireSymbols" :disabled="!editing" />
          <div class="form-item-desc">{{ descriptions['PASSWORD_REQUIRE_SYMBOLS'] }}</div>
        </ElFormItem>

        <ElFormItem label="账号初始密码">
          <div class="form-item-wrapper">
            <ElInput
              v-model="form.PASSWORD_INIT"
              type="password"
              show-password
              :disabled="!editing"
              placeholder="Excel 导入用户的初始密码"
            />
          </div>
          <div class="form-item-desc">{{ descriptions['PASSWORD_INIT'] }}</div>
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
  import { Check, Close, Edit, Loading, RefreshLeft } from '@element-plus/icons-vue'
  import { getOption, resetOption, saveOption, SysOptionVO } from '@/api/system/option'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'SecurityConfig' })

  const CATEGORY = 'PASSWORD'
  const loading = ref(false)
  const editing = ref(false)
  const buttonLoading = ref(false)
  const options = ref<SysOptionVO[]>([])
  const form = reactive<Record<string, string>>({})
  const descriptions = reactive<Record<string, string>>({})
  const formEdit = reactive({
    errorLockCount: 5,
    errorLockMinutes: 15,
    expirationDays: 90,
    warningDays: 7,
    repetitionTimes: 3,
    minLength: 8,
    allowContainUsername: false,
    requireSymbols: false
  })

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
    formEdit.errorLockCount = toNum(form.PASSWORD_ERROR_LOCK_COUNT)
    formEdit.errorLockMinutes = toNum(form.PASSWORD_ERROR_LOCK_MINUTES)
    formEdit.expirationDays = toNum(form.PASSWORD_EXPIRATION_DAYS)
    formEdit.warningDays = toNum(form.PASSWORD_EXPIRATION_WARNING_DAYS)
    formEdit.repetitionTimes = toNum(form.PASSWORD_REPETITION_TIMES)
    formEdit.minLength = toNum(form.PASSWORD_MIN_LENGTH)
    formEdit.allowContainUsername = form.PASSWORD_ALLOW_CONTAIN_USERNAME === '1'
    formEdit.requireSymbols = form.PASSWORD_REQUIRE_SYMBOLS === '1'
  }

  function syncFromEdit() {
    form.PASSWORD_ERROR_LOCK_COUNT = String(formEdit.errorLockCount)
    form.PASSWORD_ERROR_LOCK_MINUTES = String(formEdit.errorLockMinutes)
    form.PASSWORD_EXPIRATION_DAYS = String(formEdit.expirationDays)
    form.PASSWORD_EXPIRATION_WARNING_DAYS = String(formEdit.warningDays)
    form.PASSWORD_REPETITION_TIMES = String(formEdit.repetitionTimes)
    form.PASSWORD_MIN_LENGTH = String(formEdit.minLength)
    form.PASSWORD_ALLOW_CONTAIN_USERNAME = formEdit.allowContainUsername ? '1' : '0'
    form.PASSWORD_REQUIRE_SYMBOLS = formEdit.requireSymbols ? '1' : '0'
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
  .security-config {
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
