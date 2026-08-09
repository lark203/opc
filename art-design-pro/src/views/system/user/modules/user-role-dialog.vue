<template>
  <ElDialog v-model="dialogVisible" title="分配角色" width="40%" align-center @close="handleClose">
    <div class="user-info mb-4">
      <span class="label">用户名称：</span>
      <span class="value">{{ userInfo.userName }}</span>
    </div>
    <ElForm ref="formRef" :model="formData" label-width="0px">
      <ElFormItem>
        <ElSelect
          v-model="formData.roleIds"
          multiple
          filterable
          allow-create
          placeholder="请选择角色"
          style="width: 100%"
        >
          <ElOption
            v-for="role in roleOptions"
            :key="role.roleId"
            :label="role.roleName"
            :value="role.roleId"
          />
        </ElSelect>
      </ElFormItem>
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
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import type { FormInstance } from 'element-plus'
  import type { UserVO } from '@/api/system/user'
  import type { RoleVO } from '@/api/system/role'

  interface Props {
    visible: boolean
    userId?: string | number
    userName?: string
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void

    (e: 'submit', userId: string | number, roleIds: Array<string | number>): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const userInfo = reactive<UserVO>({
    userId: '',
    userName: '',
    nickName: '',
    deptId: 0,
    tenantId: '',
    userType: '',
    email: '',
    phoneNumber: '',
    gender: '',
    status: '',
    delFlag: '',
    loginIp: '',
    loginDate: '',
    remark: '',
    deptName: '',
    roles: [],
    roleIds: [],
    postIds: [],
    roleId: '',
    admin: false
  })

  const roleOptions = ref<RoleVO[]>([])
  const formRef = ref<FormInstance>()

  const formData = reactive({
    roleIds: [] as Array<string | number>
  })

  const resetForm = () => {
    userInfo.userId = ''
    userInfo.userName = ''
    roleOptions.value = []
    formData.roleIds = []
    formRef.value?.resetFields()
    formRef.value?.clearValidate()
  }

  const loadData = async (userId?: string | number) => {
    if (!userId) return
    const { getUserAuthRole } = await import('@/api/system/user')
    const res = await getUserAuthRole(userId)
    Object.assign(userInfo, res.user)
    roleOptions.value = res.roles || []
    formData.roleIds = res.roles
      .filter((role: RoleVO) => role.flag)
      .map((role: RoleVO) => role.roleId)
  }

  watch(
    () => [props.visible, props.userId, props.userName],
    async ([visible, userId, userName]) => {
      if (visible) {
        resetForm()
        if (userName) {
          userInfo.userName = userName as string
        }
        if (userId) {
          await loadData(userId as string | number)
        }
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleClose = () => {
    dialogVisible.value = false
    resetForm()
  }

  const handleSubmit = () => {
    if (!userInfo.userId) return
    emit('submit', userInfo.userId, formData.roleIds)
    dialogVisible.value = false
  }
</script>

<style lang="scss" scoped>
  .user-info {
    padding: 12px 16px;
    font-size: 14px;
    background: var(--el-color-info-light-9);
    border-radius: 4px;

    .label {
      margin-right: 8px;
      color: var(--el-text-color-secondary);
    }

    .value {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }
</style>
