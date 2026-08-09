<template>
  <div class="flex w-full h-screen">
    <LoginLeftView />

    <div class="relative flex-1">
      <AuthTopBar />

      <div class="auth-right-wrap">
        <div class="form">
          <h3 class="title">{{ $t('login.title') }}</h3>
          <p class="sub-title">{{ $t('login.subTitle') }}</p>
          <ElForm
            ref="formRef"
            :model="formData"
            :rules="rules"
            :key="formKey"
            @keyup.enter="handleSubmit"
            style="margin-top: 25px"
          >
            <ElFormItem prop="username">
              <ElInput
                class="custom-height"
                :placeholder="$t('login.placeholder.username')"
                v-model.trim="formData.username"
              />
            </ElFormItem>
            <ElFormItem prop="password">
              <ElInput
                class="custom-height"
                :placeholder="$t('login.placeholder.password')"
                v-model.trim="formData.password"
                type="password"
                autocomplete="off"
                show-password
              />
            </ElFormItem>

            <ElFormItem prop="code" v-if="captchaEnabled">
              <div class="flex gap-3 w-full">
                <ElInput
                  class="custom-height flex-1"
                  :placeholder="$t('login.placeholder.code')"
                  v-model.trim="formData.code"
                />
                <div class="w-24 h-10 cursor-pointer flex-shrink-0" @click="refreshCode">
                  <img
                    v-if="codeUrl"
                    :src="codeUrl"
                    alt="验证码"
                    class="w-full h-full object-contain"
                  />
                </div>
              </div>
            </ElFormItem>

            <div class="flex-cb mt-2 text-sm">
              <ElCheckbox v-model="formData.rememberPassword">{{
                $t('login.rememberPwd')
              }}</ElCheckbox>
              <RouterLink class="text-theme" :to="{ name: 'ForgetPassword' }">{{
                $t('login.forgetPwd')
              }}</RouterLink>
            </div>

            <div style="margin-top: 30px">
              <ElButton
                class="w-full custom-height"
                type="primary"
                @click="handleSubmit"
                :loading="loading"
                v-ripple
              >
                {{ $t('login.btnText') }}
              </ElButton>
            </div>

            <div class="mt-5 text-sm text-gray-600" v-if="registerEnabled">
              <span>{{ $t('login.noAccount') }}</span>
              <RouterLink class="text-theme" :to="{ name: 'Register' }">{{
                $t('login.register')
              }}</RouterLink>
            </div>
          </ElForm>
        </div>
      </div>

      <div v-if="site.copyright || site.beian" class="auth-footer">
        <span v-if="site.copyright">{{ site.copyright }}</span>
        <template v-if="site.beian">
          <span class="sep">·</span>
          <a :href="BEIAN_URL" target="_blank" rel="noopener noreferrer">{{ site.beian }}</a>
        </template>
      </div>
    </div>

    <ElDialog
      v-model="showForcePwdModal"
      :title="t('login.forceChangeTitle')"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <p class="pwd-tip">{{ t('login.pwdExpiredTip') }}</p>
      <ElForm ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="0">
        <ElFormItem prop="newPassword">
          <ElInput
            v-model.trim="pwdForm.newPassword"
            type="password"
            :placeholder="t('login.newPassword')"
            show-password
            autocomplete="off"
          />
        </ElFormItem>
        <ElFormItem prop="confirmPassword">
          <ElInput
            v-model.trim="pwdForm.confirmPassword"
            type="password"
            :placeholder="t('login.confirmPassword')"
            show-password
            autocomplete="off"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton type="primary" :loading="pwdLoading" @click="handleForceChange">
          {{ t('login.changePwdBtn') }}
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { useI18n } from 'vue-i18n'
  import { useSiteConfigStore } from '@/store/modules/siteConfig'
  import { HttpError } from '@/utils/http/error'
  import { getCodeImg, getInfo, login, type LoginResult } from '@/api/auth'
  import { profileApi } from '@/api/system/profile'
  import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'

  defineOptions({ name: 'Login' })

  const { t, locale } = useI18n()
  const formKey = ref(0)

  watch(locale, () => {
    formKey.value++
  })

  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const site = useSiteConfigStore()
  const BEIAN_URL = 'https://beian.miit.gov.cn/'

  const formRef = ref<FormInstance>()

  const formData = reactive({
    username: '',
    password: '',
    code: '',
    uuid: '',
    rememberPassword: true
  })

  const codeUrl = ref('')
  const captchaEnabled = ref(true)
  const registerEnabled = ref(true)

  const rules = computed<FormRules>(() => ({
    username: [{ required: true, message: t('login.placeholder.username'), trigger: 'blur' }],
    password: [{ required: true, message: t('login.placeholder.password'), trigger: 'blur' }],
    code: captchaEnabled.value
      ? [{ required: true, message: t('login.placeholder.code'), trigger: 'blur' }]
      : []
  }))

  const loading = ref(false)

  // 密码过期相关状态
  const showForcePwdModal = ref(false)
  const pwdLoading = ref(false)
  const pwdFormRef = ref<FormInstance>()
  const pendingLoginResult = ref<LoginResult | null>(null)
  const pwdForm = reactive({
    newPassword: '',
    confirmPassword: ''
  })
  const pwdRules = computed<FormRules>(() => ({
    newPassword: [
      { required: true, message: t('login.newPassword'), trigger: 'blur' },
      { min: 6, message: t('login.pwdMinTip', { n: 6 }), trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: t('login.confirmPassword'), trigger: 'blur' },
      {
        validator: (_rule, value, callback) =>
          value === pwdForm.newPassword ? callback() : callback(new Error(t('login.pwdNotMatch'))),
        trigger: 'blur'
      }
    ]
  }))

  onMounted(() => {
    refreshCode()
    site.load()
    const savedUsername = localStorage.getItem('login_username')
    if (savedUsername) {
      formData.username = savedUsername
    }
  })

  interface CodeResult {
    img: string
    uuid: string
    captchaEnabled?: boolean
    registerEnabled?: boolean
  }

  const refreshCode = async () => {
    try {
      const result = (await getCodeImg()) as CodeResult
      captchaEnabled.value = result.captchaEnabled !== false
      registerEnabled.value = result.registerEnabled !== false
      if (captchaEnabled.value) {
        codeUrl.value = 'data:image/png;base64,' + result.img
        formData.uuid = result.uuid
        formData.code = ''
      } else {
        codeUrl.value = ''
        formData.uuid = ''
        formData.code = ''
      }
    } catch (error) {
      console.error('获取验证码失败:', error)
    }
  }

  const handleSubmit = async () => {
    if (!formRef.value) return

    try {
      const valid = await formRef.value.validate()
      if (!valid) return

      loading.value = true

      const { username, password, code, uuid } = formData

      if (formData.rememberPassword) {
        localStorage.setItem('login_username', username)
      } else {
        localStorage.removeItem('login_username')
      }

      const loginResult = await login({
        username,
        password,
        code,
        uuid
      })

      if (!loginResult.access_token) {
        throw new Error('Login failed - no token received')
      }

      userStore.setToken(loginResult.access_token, loginResult.refresh_token)

      // 密码已过期：强制改密后才能进入系统
      if (loginResult.password_expired) {
        pendingLoginResult.value = loginResult
        pwdForm.newPassword = ''
        pwdForm.confirmPassword = ''
        showForcePwdModal.value = true
        return
      }

      await proceed(loginResult)
    } catch (error) {
      if (error instanceof HttpError) {
        refreshCode()
      } else {
        console.error('[Login] Unexpected error:', error)
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 登录成功后的后续处理（获取用户信息并跳转）。
   */
  const proceed = async (loginResult: LoginResult) => {
    const userInfo = await getInfo()
    userStore.setUserInfo(userInfo)
    userStore.setLoginStatus(true)

    userStore.checkAndClearWorktabs()

    const redirect = route.query.redirect as string
    router.push(redirect || '/')

    if (loginResult.password_expiring_soon) {
      ElMessage.warning(
        t('login.pwdExpiringSoon', { days: loginResult.password_expire_in_days ?? 0 })
      )
    }

    // 无角色权限：仅提示联系管理员分配，可停留
    if (!userInfo.roles || userInfo.roles.length === 0) {
      ElMessage.warning(t('login.noRoleTip'))
    }

    // 资料不完善（缺失邮箱或手机号）：弹窗引导去个人中心补全
    const missingProfile = !userInfo.user.email || !userInfo.user.phoneNumber
    if (missingProfile) {
      ElMessageBox.confirm(t('login.profileIncompleteTip'), t('login.profileIncompleteTitle'), {
        confirmButtonText: t('login.goToProfile'),
        cancelButtonText: t('login.laterBtn'),
        type: 'warning',
        closeOnClickModal: false,
        closeOnPressEscape: false
      })
        .then(() => {
          router.push({ name: 'UserCenter' })
        })
        .catch(() => {
          // 用户选择稍后处理，停留当前页面
        })
    }
  }

  /**
   * 密码过期后的强制改密提交。
   */
  const handleForceChange = async () => {
    if (!pwdFormRef.value) return
    try {
      await pwdFormRef.value.validate()
      pwdLoading.value = true
      await profileApi.updatePwd({
        oldPassword: formData.password,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success(t('login.changePwdSuccess'))
      showForcePwdModal.value = false
      if (pendingLoginResult.value) {
        await proceed(pendingLoginResult.value)
      }
    } catch (error) {
      if (!(error instanceof HttpError)) {
        console.error('[Login] force change password error:', error)
      }
    } finally {
      pwdLoading.value = false
    }
  }
</script>

<style scoped>
  @import './style.css';

  .pwd-tip {
    margin: 0 0 16px;
    font-size: 13px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }
</style>
