<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <h3 class="title">{{ $t('forgetPassword.title') }}</h3>
          <p class="sub-title">{{ $t('forgetPassword.subTitle') }}</p>

          <!-- 第一步：输入账号 -->
          <template v-if="step === 1">
            <div class="mt-5">
              <span class="input-label" v-if="showInputLabel">账号</span>
              <ElInput
                class="custom-height"
                :placeholder="$t('forgetPassword.usernamePlaceholder')"
                v-model.trim="username"
                @keyup.enter="nextStep"
              />
            </div>

            <div style="margin-top: 15px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                :loading="loading"
                @click="nextStep"
                v-ripple
              >
                {{ $t('forgetPassword.nextStep') }}
              </ElButton>
            </div>
          </template>

          <!-- 第二步：选择验证方式 -->
          <template v-else-if="step === 2">
            <p class="tip">{{ $t('forgetPassword.chooseChannel') }}</p>

            <div class="channel-list">
              <div
                v-for="item in channels"
                :key="item.channel"
                class="channel-item"
                :class="{ active: selectedChannel === item.channel }"
                @click="selectedChannel = item.channel"
              >
                <div class="channel-name">
                  {{
                    item.channel === 'email'
                      ? $t('forgetPassword.channelEmail')
                      : $t('forgetPassword.channelSms')
                  }}
                </div>
                <div class="channel-dest">{{ item.destination }}</div>
              </div>
            </div>

            <div style="margin-top: 15px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                :loading="loading"
                @click="sendCode"
                v-ripple
              >
                {{ $t('forgetPassword.sendCode') }}
              </ElButton>
            </div>

            <div style="margin-top: 15px">
              <ElButton class="w-full custom-height" plain @click="backToUsername" v-ripple>
                {{ $t('forgetPassword.backBtnText') }}
              </ElButton>
            </div>
          </template>

          <!-- 第三步：输入验证码与新密码 -->
          <template v-else>
            <p class="tip" v-if="destination">
              {{ $t('forgetPassword.codeSentTo', { dest: destination }) }}
            </p>

            <div class="account-bar" v-if="username">
              <span class="account-label">{{ $t('forgetPassword.accountLabel') }}</span>
              <span class="account-value">{{ username }}</span>
            </div>

            <div class="mt-5">
              <ElInput
                class="custom-height"
                :placeholder="$t('forgetPassword.codePlaceholder')"
                v-model.trim="code"
              />
            </div>

            <div style="margin-top: 15px">
              <ElInput
                class="custom-height"
                type="password"
                show-password
                autocomplete="off"
                :placeholder="$t('forgetPassword.newPasswordPlaceholder')"
                v-model.trim="newPassword"
              />
            </div>

            <div style="margin-top: 15px">
              <ElInput
                class="custom-height"
                type="password"
                show-password
                autocomplete="off"
                :placeholder="$t('forgetPassword.confirmPasswordPlaceholder')"
                v-model.trim="confirmPassword"
                @keyup.enter="onSubmit"
              />
            </div>

            <div style="margin-top: 15px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                :loading="loading"
                @click="onSubmit"
                v-ripple
              >
                {{ $t('forgetPassword.submitBtnText') }}
              </ElButton>
            </div>

            <div style="margin-top: 15px">
              <ElButton
                class="w-full custom-height"
                plain
                :disabled="countdown > 0"
                @click="sendCode"
                v-ripple
              >
                {{
                  countdown > 0
                    ? $t('forgetPassword.resendIn', { s: countdown })
                    : $t('forgetPassword.resend')
                }}
              </ElButton>
            </div>
          </template>

          <div style="margin-top: 15px">
            <ElButton class="w-full custom-height" plain @click="toLogin">
              {{ $t('forgetPassword.backToLogin') }}
            </ElButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { getResetChannels, resetPassword, sendResetCode } from '@/api/auth'

  defineOptions({ name: 'ForgetPassword' })

  const router = useRouter()
  const { t } = useI18n()

  type Channel = 'email' | 'sms'
  interface ChannelInfo {
    channel: Channel
    destination: string
  }

  const step = ref(1)
  const username = ref('')
  const channels = ref<ChannelInfo[]>([])
  const selectedChannel = ref<Channel | ''>('')
  const code = ref('')
  const newPassword = ref('')
  const confirmPassword = ref('')
  const destination = ref('')
  const loading = ref(false)
  const countdown = ref(0)
  const showInputLabel = ref(false)
  let timer: ReturnType<typeof setInterval> | null = null

  const startCountdown = () => {
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  }

  /** 第一步：根据账号查询可用验证通道 */
  const nextStep = async () => {
    if (!username.value) {
      ElMessage.warning(t('forgetPassword.enterUsernameFirst'))
      return
    }
    try {
      loading.value = true
      const res = (await getResetChannels({ username: username.value })) as ChannelInfo[]
      if (!res || res.length === 0) {
        ElMessage.warning(t('forgetPassword.noChannelBound'))
        return
      }
      channels.value = res
      // 仅有一种通道时默认选中
      if (res.length === 1) {
        selectedChannel.value = res[0].channel
      } else {
        selectedChannel.value = ''
      }
      step.value = 2
    } catch (error) {
      console.error('查询验证通道失败:', error)
    } finally {
      loading.value = false
    }
  }

  /** 第二/三步：发送验证码（使用已选通道） */
  const sendCode = async () => {
    if (!username.value) {
      ElMessage.warning(t('forgetPassword.enterUsernameFirst'))
      return
    }
    if (!selectedChannel.value) {
      ElMessage.warning(t('forgetPassword.chooseChannelFirst'))
      return
    }
    try {
      loading.value = true
      const res = (await sendResetCode({
        username: username.value,
        channel: selectedChannel.value
      })) as { channel: Channel; destination: string }
      destination.value = res.destination
      step.value = 3
      startCountdown()
      ElMessage.success(t('forgetPassword.codeSent'))
    } catch (error) {
      console.error('发送验证码失败:', error)
    } finally {
      loading.value = false
    }
  }

  const backToUsername = () => {
    step.value = 1
    selectedChannel.value = ''
  }

  const onSubmit = async () => {
    if (!code.value) {
      ElMessage.warning(t('forgetPassword.enterCodeFirst'))
      return
    }
    if (!newPassword.value) {
      ElMessage.warning(t('forgetPassword.enterNewPasswordFirst'))
      return
    }
    if (newPassword.value !== confirmPassword.value) {
      ElMessage.warning(t('forgetPassword.newPasswordMismatch'))
      return
    }
    try {
      loading.value = true
      await resetPassword({
        username: username.value,
        code: code.value,
        newPassword: newPassword.value
      })
      ElMessage.success(t('forgetPassword.success'))
      router.push({ name: 'Login' })
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      loading.value = false
    }
  }

  const toLogin = () => {
    router.push({ name: 'Login' })
  }

  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })
</script>

<style scoped>
  @import '../login/style.css';

  .tip {
    margin: 0 0 4px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--el-color-primary);
  }

  .account-bar {
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 10px 14px;
    margin-top: 10px;
    font-size: 14px;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
  }

  .account-label {
    color: var(--el-text-color-secondary);
  }

  .account-value {
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .channel-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 10px;
  }

  .channel-item {
    padding: 12px 16px;
    cursor: pointer;
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    transition: all 0.2s;
  }

  .channel-item:hover {
    border-color: var(--el-color-primary);
  }

  .channel-item.active {
    background: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary);
  }

  .channel-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .channel-dest {
    margin-top: 4px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
</style>
