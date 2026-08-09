<template>
  <div class="mail-config">
    <div v-if="loading" class="loading-wrap">
      <ElIcon class="is-loading" :size="32">
        <Loading />
      </ElIcon>
    </div>
    <template v-else>
      <ElForm :model="form" label-position="top" class="config-form">
        <ElFormItem label="邮件功能开启">
          <ElSwitch v-model="formEdit.mailEnabled" :disabled="!editing" />
          <div class="form-item-desc">{{ descriptions['MAIL_ENABLED'] }}</div>
        </ElFormItem>

        <ElFormItem label="邮件协议">
          <ElSelect v-model="form.MAIL_PROTOCOL" style="width: 200px" :disabled="!editing">
            <ElOption label="SMTP" value="smtp" />
            <ElOption label="IMAP" value="imap" />
            <ElOption label="POP3" value="pop3" />
          </ElSelect>
          <div class="form-item-desc">{{ descriptions['MAIL_PROTOCOL'] }}</div>
        </ElFormItem>

        <ElFormItem label="SMTP 服务器">
          <ElInput
            v-model="form.MAIL_HOST"
            :disabled="!editing"
            placeholder="如 smtp.qq.com"
            style="max-width: 400px"
          />
          <div class="form-item-desc">{{ descriptions['MAIL_HOST'] }}</div>
        </ElFormItem>

        <ElFormItem label="SMTP 端口">
          <div class="form-item-wrapper">
            <ElInputNumber v-model="formEdit.port" :min="1" :max="65535" :disabled="!editing" />
            <span class="form-item-unit">端口</span>
          </div>
          <div class="form-item-desc">{{ descriptions['MAIL_PORT'] }}</div>
        </ElFormItem>

        <ElFormItem label="发件人账号">
          <ElInput
            v-model="form.MAIL_USERNAME"
            :disabled="!editing"
            placeholder="邮箱账号"
            style="max-width: 400px"
          />
          <div class="form-item-desc">{{ descriptions['MAIL_USERNAME'] }}</div>
        </ElFormItem>

        <ElFormItem label="发件人密码">
          <ElInput
            v-model="form.MAIL_PASSWORD"
            type="password"
            show-password
            :disabled="!editing"
            placeholder="邮箱密码/授权码"
            style="max-width: 400px"
          />
          <div class="form-item-desc">{{ descriptions['MAIL_PASSWORD'] }}</div>
        </ElFormItem>

        <ElFormItem label="启用 SSL">
          <ElSwitch v-model="formEdit.sslEnabled" :disabled="!editing" />
          <div class="form-item-desc">{{ descriptions['MAIL_SSL_ENABLED'] }}</div>
        </ElFormItem>

        <ElFormItem v-if="form.MAIL_SSL_ENABLED === '1'" label="SSL 端口">
          <div class="form-item-wrapper">
            <ElInputNumber v-model="formEdit.sslPort" :min="1" :max="65535" :disabled="!editing" />
            <span class="form-item-unit">端口</span>
          </div>
          <div class="form-item-desc">{{ descriptions['MAIL_SSL_PORT'] }}</div>
        </ElFormItem>

        <ElFormItem label="发件人昵称">
          <ElInput
            v-model="form.MAIL_NICKNAME"
            :disabled="!editing"
            placeholder="发件人昵称"
            style="max-width: 400px"
          />
          <div class="form-item-desc">{{ descriptions['MAIL_NICKNAME'] }}</div>
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
          <ElButton v-auth="'system:mail:test'" @click="handleTest" :disabled="!form.MAIL_HOST">
            <ElIcon>
              <Promotion />
            </ElIcon>
            测试发送
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
  import { Check, Close, Edit, Loading, Promotion, RefreshLeft } from '@element-plus/icons-vue'
  import { getOption, resetOption, saveOption, SysOptionVO } from '@/api/system/option'
  import { testMail } from '@/api/system/mail'
  import { ElMessage } from 'element-plus'

  defineOptions({ name: 'MailConfig' })

  const CATEGORY = 'MAIL'
  const loading = ref(false)
  const editing = ref(false)
  const buttonLoading = ref(false)
  const options = ref<SysOptionVO[]>([])
  const form = reactive<Record<string, string>>({})
  const descriptions = reactive<Record<string, string>>({})
  const formEdit = reactive({
    port: 465,
    sslPort: 465,
    sslEnabled: true,
    mailEnabled: false
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
      formEdit.port = toNum(form.MAIL_PORT) || 465
      formEdit.sslPort = toNum(form.MAIL_SSL_PORT) || 465
      formEdit.sslEnabled = form.MAIL_SSL_ENABLED === '1'
      formEdit.mailEnabled = form.MAIL_ENABLED === '1'
    } finally {
      loading.value = false
    }
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
    form.MAIL_PORT = String(formEdit.port)
    form.MAIL_SSL_PORT = String(formEdit.sslPort)
    form.MAIL_SSL_ENABLED = formEdit.sslEnabled ? '1' : '0'
    form.MAIL_ENABLED = formEdit.mailEnabled ? '1' : '0'
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

  async function handleTest() {
    if (!form.MAIL_HOST || !form.MAIL_USERNAME || !form.MAIL_PASSWORD) {
      ElMessage.warning('请先填写 SMTP 服务器、发件人账号与密码（建议先保存再测试）')
      return
    }
    buttonLoading.value = true
    try {
      await testMail()
      ElMessage.success('测试邮件已发送，请查收')
    } catch (e: any) {
      ElMessage.error(e?.message || '测试邮件发送失败，请检查配置')
    } finally {
      buttonLoading.value = false
    }
  }

  onMounted(load)
</script>

<style scoped lang="scss">
  .mail-config {
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
