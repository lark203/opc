<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增用户' : '修改用户'"
    width="40%"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="90px">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="用户名称" prop="userName">
            <ElInput
              v-model="formData.userName"
              placeholder="请输入用户名称"
              :disabled="dialogType === 'edit'"
              maxlength="30"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="用户昵称" prop="nickName">
            <ElInput v-model="formData.nickName" placeholder="请输入用户昵称" maxlength="30" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="手机号码" prop="phoneNumber">
            <ElInput v-model="formData.phoneNumber" placeholder="请输入手机号码" maxlength="11" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="邮箱" prop="email">
            <ElInput v-model="formData.email" placeholder="请输入邮箱" maxlength="50" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem v-if="dialogType === 'add'" label="用户密码" prop="password">
            <ElInput
              v-model="formData.password"
              type="password"
              placeholder="请输入用户密码"
              maxlength="20"
              show-password
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="性别">
            <ElSelect v-model="formData.gender" placeholder="请选择性别">
              <ElOption
                v-for="gender in sys_user_gender"
                :key="gender.value"
                :label="gender.label"
                :value="gender.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="状态">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="status in statusOptions" :key="status.value" :label="status.value">
                {{ status.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="部门" prop="deptId">
            <ElTreeSelect
              v-model="formData.deptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="请选择部门"
              check-strictly
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="角色" prop="roleIds">
            <ElSelect v-model="formData.roleIds" filterable multiple placeholder="请选择角色">
              <ElOption
                v-for="role in roleOptions"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleId"
                :disabled="role.status === '1'"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="岗位">
            <ElSelect v-model="formData.postIds" multiple placeholder="请选择岗位">
              <ElOption
                v-for="post in postOptions"
                :key="post.postId"
                :label="post.postName"
                :value="post.postId"
                :disabled="post.status === '1'"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="地址">
            <ElInput v-model="formData.address" placeholder="请输入地址" maxlength="200" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="标签">
            <ElInput
              v-model="formData.tags"
              placeholder="请输入标签，多个标签用逗号分隔"
              maxlength="100"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow>
        <ElCol :span="24">
          <ElFormItem label="个性签名">
            <ElInput
              v-model="formData.signature"
              type="textarea"
              placeholder="请输入个性签名"
              maxlength="500"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow>
        <ElCol :span="24">
          <ElFormItem label="备注">
            <ElInput v-model="formData.remark" type="textarea" placeholder="请输入备注" />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, toRefs, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { UserForm, UserInfoVO, UserVO } from '@/api/system/user'
  import { deptTreeSelect, getUser } from '@/api/system/user'
  import { listPost } from '@/api/system/post'
  import { useDict } from '@/utils/dict'

  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    userData?: Partial<UserVO>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: UserForm): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  const formRef = ref<FormInstance>()

  const deptOptions = ref<any[]>([])
  const roleOptions = ref<any[]>([])
  const postOptions = ref<any[]>([])
  const { sys_user_gender, sys_normal_disable: statusOptions } = toRefs(
    useDict('sys_user_gender', 'sys_normal_disable')
  )

  const formData = reactive<UserForm>({
    userId: undefined,
    deptId: undefined,
    userName: '',
    nickName: '',
    password: '',
    phoneNumber: '',
    email: '',
    gender: '',
    status: '0',
    remark: '',
    postIds: [],
    roleIds: [],
    address: '',
    signature: '',
    tags: ''
  })

  const rules: FormRules = {
    userName: [
      { required: true, message: '用户名称不能为空', trigger: 'blur' },
      { min: 2, max: 20, message: '用户名称长度必须介于 2 和 20 之间', trigger: 'blur' }
    ],
    nickName: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
    password: [
      { required: true, message: '用户密码不能为空', trigger: 'blur' },
      { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
    ],
    email: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] },
      { min: 5, max: 50, message: '邮箱长度必须介于 5 和 50 之间', trigger: 'blur' }
    ],
    phoneNumber: [
      { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
    ],
    roleIds: [{ required: true, message: '用户角色不能为空', trigger: 'blur' }]
  }

  const resetForm = () => {
    formData.userId = undefined
    formData.deptId = undefined
    formData.userName = ''
    formData.nickName = ''
    formData.password = ''
    formData.phoneNumber = ''
    formData.email = ''
    formData.gender = ''
    formData.status = '0'
    formData.remark = ''
    formData.postIds = []
    formData.roleIds = []
    formData.address = ''
    formData.signature = ''
    formData.tags = ''
    formRef.value?.resetFields()
    formRef.value?.clearValidate()
  }

  const loadUserData = async (userId?: string | number) => {
    if (!userId) return
    const res = await getUser(userId)
    const data: UserInfoVO = res
    Object.assign(formData, data.user)
    formData.postIds = data.postIds || []
    formData.roleIds = data.roleIds || []
    roleOptions.value = Array.from(
      new Map([...data.roles, ...data.user.roles].map((role) => [role.roleId, role])).values()
    )
    postOptions.value = data.posts || []
    formData.password = ''
  }

  const loadRoleAndPostOptions = async () => {
    const [userRes, deptRes] = await Promise.all([getUser(), deptTreeSelect()])
    const userData: UserInfoVO = userRes
    roleOptions.value = userData.roles || []
    postOptions.value = userData.posts || []
    deptOptions.value = deptRes || []
  }

  watch(
    () => [props.visible, props.type, props.userData],
    async ([visible, type, userData]) => {
      if (visible) {
        resetForm()
        await loadRoleAndPostOptions()
        if (
          type === 'edit' &&
          userData &&
          typeof userData === 'object' &&
          'userId' in userData &&
          userData.userId
        ) {
          await loadUserData(userData.userId)
        }
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  watch(
    () => formData.deptId,
    async (deptId) => {
      if (!deptId) {
        const userRes = await getUser()
        const data: UserInfoVO = userRes
        postOptions.value = data.posts || []
      } else {
        const postRes = await listPost({ deptId })
        postOptions.value = postRes.rows || []
      }
      formData.postIds = []
    }
  )

  const handleClose = () => {
    dialogVisible.value = false
    resetForm()
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        emit('submit', { ...formData })
        dialogVisible.value = false
      }
    })
  }

  defineExpose({
    setDeptOptions: (options: any[]) => {
      deptOptions.value = options
    },
    setRoleOptions: (options: any[]) => {
      roleOptions.value = options
    },
    setPostOptions: (options: any[]) => {
      postOptions.value = options
    }
  })
</script>
