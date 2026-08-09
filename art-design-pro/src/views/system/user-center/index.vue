<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="group art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img
            class="absolute top-0 left-0 w-full h-50 object-cover cursor-pointer"
            :src="bgImageUrl"
            @click="handleChangeBg"
          />
          <div
            class="absolute top-1 right-2 cursor-pointer text-white bg-black/50 px-2 py-1 rounded text-xs opacity-0 transition-opacity duration-300 group-hover:opacity-100 hover:bg-black/70 z-10"
            @click="handleChangeBg"
          >
            更换背景
          </div>
          <div class="relative z-10">
            <div class="group relative w-20 h-20 mt-30 mx-auto">
              <img
                class="w-full h-full object-cover border-2 border-white rounded-full cursor-pointer hover:opacity-90"
                :src="getAvatarUrl(profileData?.user?.avatar)"
                @click="handleChangeAvatar"
              />
              <div
                class="absolute inset-0 rounded-full bg-black/40 opacity-0 flex items-center justify-center cursor-pointer transition-opacity duration-300 group-hover:opacity-100 hover:bg-black/50"
                @click="handleChangeAvatar"
              >
                <span class="text-white text-2xl font-light">+</span>
              </div>
            </div>
            <h2 class="mt-5 text-xl font-normal">{{
              profileData?.user?.nickName || profileData?.user?.userName
            }}</h2>
            <p class="mt-5 text-sm">{{
              profileData?.user?.signature || profileData?.user?.deptName || '暂无信息'
            }}</p>
          </div>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:mail-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ profileData?.user?.email || '-' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ profileData?.roleGroup || '-' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:map-pin-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ profileData?.user?.address || '-' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:dribbble-fill" class="text-g-700" />
              <span class="ml-2 text-sm"
                >{{ profileData?.user.deptName }} - {{ profileData?.postGroup || '-' }}</span
              >
            </div>
          </div>

          <div class="mt-10">
            <h3 class="text-sm font-medium">标签</h3>
            <div class="flex flex-wrap justify-center mt-3.5">
              <div
                v-for="item in userTags"
                :key="item"
                class="py-1 px-1.5 mr-2.5 mb-2.5 text-xs border border-g-300 rounded"
              >
                {{ item }}
              </div>
              <div v-if="userTags.length === 0" class="text-xs text-g-400"> 暂无标签 </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <div class="art-card-sm">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">基本设置</h1>

          <ElForm
            :model="form"
            class="box-border p-5 [&>.el-row_.el-form-item]:w-[calc(50%-10px)] [&>.el-row_.el-input]:w-full [&>.el-row_.el-select]:w-full"
            ref="ruleFormRef"
            :rules="rules"
            label-width="86px"
            label-position="top"
          >
            <ElRow>
              <ElFormItem label="账号" prop="userName">
                <ElInput v-model="form.userName" disabled />
              </ElFormItem>
              <ElFormItem label="昵称" prop="nickName" class="ml-5">
                <ElInput v-model="form.nickName" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="邮箱" prop="email">
                <ElInput v-model="form.email" :disabled="!isEdit" />
              </ElFormItem>
              <ElFormItem label="手机" prop="phoneNumber" class="ml-5">
                <ElInput v-model="form.phoneNumber" :disabled="!isEdit" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="性别" prop="gender">
                <ElSelect v-model="form.gender" placeholder="请选择" :disabled="!isEdit">
                  <ElOption
                    v-for="item in genderOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
              <ElFormItem label="部门" prop="deptName" class="ml-5">
                <ElInput v-model="form.deptName" disabled />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="个性签名" prop="signature">
                <ElInput
                  v-model="form.signature"
                  :disabled="!isEdit"
                  placeholder="请输入个性签名"
                />
              </ElFormItem>
              <ElFormItem label="地址" prop="address" class="ml-5">
                <ElInput v-model="form.address" :disabled="!isEdit" placeholder="请输入地址" />
              </ElFormItem>
            </ElRow>

            <ElRow>
              <ElFormItem label="标签" prop="tags">
                <ElInput
                  v-model="form.tags"
                  :disabled="!isEdit"
                  placeholder="请输入标签，多个标签用逗号分隔"
                />
              </ElFormItem>
            </ElRow>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="handleSave">
                {{ isEdit ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>

        <div class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">更改密码</h1>

          <ElForm :model="pwdForm" class="box-border p-5" label-width="86px" label-position="top">
            <ElFormItem label="当前密码" prop="oldPassword">
              <ElInput
                v-model="pwdForm.oldPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <ElFormItem label="新密码" prop="newPassword">
              <ElInput
                v-model="pwdForm.newPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <ElFormItem label="确认新密码" prop="confirmPassword">
              <ElInput
                v-model="pwdForm.confirmPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
              />
            </ElFormItem>

            <div class="flex-c justify-end [&_.el-button]:!w-27.5">
              <ElButton type="primary" class="w-22.5" v-ripple @click="handleSavePwd">
                {{ isEditPwd ? '保存' : '修改密码' }}
              </ElButton>
            </div>
          </ElForm>
        </div>
      </div>
    </div>

    <ElDialog
      v-model="avatarDialogVisible"
      title="更换头像"
      width="50%"
      :close-on-click-modal="false"
      @close="handleAvatarDialogClose"
    >
      <ArtCutterImg
        v-model:imgUrl="avatarImageUrl"
        :boxWidth="600"
        :boxHeight="300"
        :cutWidth="200"
        :cutHeight="200"
        :quality="1.0"
        :tool="true"
        :showPreview="true"
        :originalGraph="true"
        :title="'图片裁剪'"
        :previewTitle="'预览效果'"
        @cutDown="handleAvatarCutDown"
      />
      <template #footer>
        <ElButton @click="avatarDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSaveAvatar">确定</ElButton>
      </template>
    </ElDialog>

    <ElDialog
      v-model="bgDialogVisible"
      title="更换背景"
      width="50%"
      :close-on-click-modal="false"
      @close="handleBgDialogClose"
    >
      <ArtCutterImg
        v-model:imgUrl="bgImageTempUrl"
        :boxWidth="500"
        :boxHeight="300"
        :cutWidth="300"
        :cutHeight="200"
        :quality="1.0"
        :tool="true"
        :showPreview="true"
        :originalGraph="true"
        :title="'图片裁剪'"
        :previewTitle="'预览效果'"
        @cutDown="handleBgCutDown"
      />
      <template #footer>
        <ElButton @click="bgDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSaveBg">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, toRefs } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElDialog, ElMessage } from 'element-plus'
  import type { ProfileVo, UpdateProfileReq, UpdatePwdReq } from '@/api/system/profile'
  import { profileApi } from '@/api/system/profile'
  import { ossApi } from '@/api/system/oss'
  import defaultAvatar from '@imgs/user/avatar.webp'
  import defaultBg from '@imgs/user/bg.webp'
  import { useDict } from '@/utils/dict'
  import ArtCutterImg from '@/components/core/media/art-cutter-img/index.vue'
  import { useUserStore } from '@/store/modules/user'
  import { buildAuthUrl } from '@/utils/auth-url'

  defineOptions({ name: 'UserCenter' })

  const userStore = useUserStore()

  const profileData = ref<ProfileVo | null>(null)
  const isEdit = ref(false)
  const isEditBg = ref(false)
  const isEditPwd = ref(false)
  const ruleFormRef = ref<FormInstance>()

  const avatarDialogVisible = ref(false)
  const bgDialogVisible = ref(false)
  const avatarImageUrl = ref('')
  const bgImageTempUrl = ref('')
  const isAvatarChanged = ref(false)
  const isBgChanged = ref(false)
  // 背景图 OSS 的 ossId（响应式，避免直接读 localStorage 导致上传后不刷新）
  const bgOssId = ref('')
  // 兼容历史数据：曾直接存储 http(s) 链接
  const bgLegacyUrl = ref('')

  const form = reactive({
    userName: '',
    nickName: '',
    email: '',
    phoneNumber: '',
    gender: '',
    deptName: '',
    address: '',
    signature: '',
    tags: ''
  })

  const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const rules = reactive<FormRules>({
    nickName: [
      { required: true, message: '请输入昵称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
    phoneNumber: [{ required: true, message: '请输入手机号码', trigger: 'blur' }],
    gender: [{ required: true, message: '请选择性别', trigger: 'blur' }]
  })

  const { sys_user_gender: genderOptions } = toRefs(useDict('sys_user_gender'))

  const userTags = computed(() => {
    const tags = profileData.value?.user?.tags
    if (!tags) return []
    return tags.split(',').filter((tag) => tag.trim())
  })

  const bgImageUrl = computed(() => {
    if (bgOssId.value) {
      return buildAuthUrl('/resource/oss/preview/' + bgOssId.value)
    }
    if (bgLegacyUrl.value) {
      return bgLegacyUrl.value
    }
    return defaultBg
  })

  const getAvatarUrl = (avatar?: string | number): string => {
    if (!avatar || avatar === '' || avatar === 'null' || avatar === 'undefined') {
      return defaultAvatar
    }
    const avatarStr = String(avatar)
    if (avatarStr.startsWith('http://') || avatarStr.startsWith('https://')) {
      return avatarStr
    }
    if (avatarStr.startsWith('/')) {
      return `${import.meta.env.VITE_APP_BASE_URL || ''}${avatarStr}`
    }
    // 头像字段为 OSS 的 ossId（纯数字），需经过后端预览接口并携带鉴权参数
    if (/^\d+$/.test(avatarStr)) {
      return buildAuthUrl('/resource/oss/preview/' + avatarStr)
    }
    return `${import.meta.env.VITE_APP_BASE_URL || ''}/${avatarStr}`
  }

  onMounted(() => {
    initBg()
    fetchProfile()
  })

  // 从 localStorage 初始化背景（兼容历史 http 链接与新的 ossId 两种存储格式）
  const initBg = () => {
    const storedBg = localStorage.getItem('user-center-bg')
    if (!storedBg) return
    if (/^\d+$/.test(storedBg)) {
      bgOssId.value = storedBg
    } else if (storedBg.startsWith('http://') || storedBg.startsWith('https://')) {
      bgLegacyUrl.value = storedBg
    }
  }

  const fetchProfile = async () => {
    try {
      const res = await profileApi.getProfile()
      if (res) {
        profileData.value = res
        form.userName = res.user.userName
        form.nickName = res.user.nickName
        form.email = res.user.email
        form.phoneNumber = res.user.phoneNumber
        form.gender = res.user.gender
        form.deptName = res.user.deptName || ''
        form.address = res.user.address || ''
        form.signature = res.user.signature || ''
        form.tags = res.user.tags || ''
      }
    } catch (error) {
      console.error('获取个人信息失败:', error)
    }
  }

  const handleSave = async () => {
    if (!isEdit.value) {
      isEdit.value = true
      isEditBg.value = true
      return
    }

    if (!ruleFormRef.value) return
    await ruleFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const data: UpdateProfileReq = {
            nickName: form.nickName,
            email: form.email,
            phoneNumber: form.phoneNumber,
            gender: form.gender,
            address: form.address,
            signature: form.signature,
            tags: form.tags
          }
          await profileApi.updateProfile(data)
          ElMessage.success('修改成功')
          isEdit.value = false
          isEditBg.value = false
          await fetchProfile()
        } catch (error) {
          console.error('修改个人信息失败:', error)
        }
      }
    })
  }

  const handleSavePwd = async () => {
    if (!isEditPwd.value) {
      isEditPwd.value = true
      return
    }

    if (!pwdForm.oldPassword) {
      ElMessage.warning('请输入旧密码')
      return
    }
    if (!pwdForm.newPassword) {
      ElMessage.warning('请输入新密码')
      return
    }
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
      ElMessage.warning('两次输入的新密码不一致')
      return
    }

    try {
      const data: UpdatePwdReq = {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      }
      await profileApi.updatePwd(data)
      ElMessage.success('密码修改成功，请重新登录')
      setTimeout(() => {
        userStore.logOut()
      }, 1500)
    } catch (error) {
      console.error('修改密码失败:', error)
    }
  }

  const handleChangeAvatar = () => {
    avatarImageUrl.value = getAvatarUrl(profileData.value?.user?.avatar)
    avatarDialogVisible.value = true
  }

  const handleAvatarDialogClose = () => {
    avatarImageUrl.value = ''
    isAvatarChanged.value = false
  }

  const handleAvatarCutDown = (result: { dataURL: string }) => {
    avatarImageUrl.value = result.dataURL
    isAvatarChanged.value = true
    console.log('头像', result)
  }

  const handleSaveAvatar = async () => {
    const currentAvatar = getAvatarUrl(profileData.value?.user?.avatar)
    if (!isAvatarChanged.value) {
      ElMessage.warning('请先点击裁剪按钮')
      return
    }
    if (
      !avatarImageUrl.value ||
      avatarImageUrl.value === defaultAvatar ||
      avatarImageUrl.value === currentAvatar
    ) {
      ElMessage.warning('请选择图片')
      return
    }

    try {
      const response = await fetch(avatarImageUrl.value)
      const blob = await response.blob()
      const formData = new FormData()
      formData.append('file', blob, 'avatar.png')

      const res = await ossApi.uploadOss(formData)
      if (res) {
        await profileApi.updateAvatar({ avatar: res.ossId })
        // 同步更新全局用户状态，使框架左上角头像一并刷新
        userStore.setUserAvatar(res.ossId)
        ElMessage.success('头像修改成功')
        avatarDialogVisible.value = false
        avatarImageUrl.value = ''
        await fetchProfile()
      }
    } catch (error) {
      console.error('上传头像失败:', error)
      ElMessage.error('上传头像失败')
    }
  }

  const handleChangeBg = () => {
    bgImageTempUrl.value = bgImageUrl.value
    bgDialogVisible.value = true
  }

  const handleBgDialogClose = () => {
    bgImageTempUrl.value = ''
    isBgChanged.value = false
  }

  const handleBgCutDown = (result: { dataURL: string }) => {
    bgImageTempUrl.value = result.dataURL
    isBgChanged.value = true
    console.log('背景图片', result)
  }

  const handleSaveBg = async () => {
    if (!isBgChanged.value) {
      ElMessage.warning('请先点击裁剪按钮')
      return
    }
    if (
      !bgImageTempUrl.value ||
      bgImageTempUrl.value === defaultBg ||
      bgImageTempUrl.value === bgImageUrl.value
    ) {
      ElMessage.warning('请选择图片')
      return
    }

    try {
      const response = await fetch(bgImageTempUrl.value)
      const blob = await response.blob()
      const formData = new FormData()
      formData.append('file', blob, 'bg.png')

      const res = await ossApi.uploadOss(formData)
      if (res) {
        bgOssId.value = res.ossId
        localStorage.setItem('user-center-bg', res.ossId)
        ElMessage.success('背景图片修改成功')
        bgDialogVisible.value = false
        bgImageTempUrl.value = ''
      }
    } catch (error) {
      console.error('上传背景图片失败:', error)
      ElMessage.error('上传背景图片失败')
    }
  }
</script>
