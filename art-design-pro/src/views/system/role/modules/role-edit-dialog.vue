<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增角色' : '修改角色'"
    width="30%"
    align-center
    @close="handleClose"
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="角色名称" prop="roleName">
            <ElInput v-model="formData.roleName" placeholder="请输入角色名称" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem prop="roleKey">
            <template #label>
              <span>
                <ElTooltip
                  content="控制器中定义的权限字符，如：@SaCheckRole('admin')"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                权限字符
              </span>
            </template>
            <ElInput v-model="formData.roleKey" placeholder="请输入权限字符" />
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow :gutter="20">
        <ElCol :span="12">
          <ElFormItem label="角色顺序" prop="roleSort">
            <ElInputNumber
              v-model="formData.roleSort"
              controls-position="right"
              :min="0"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="状态">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.value">
                {{ dict.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>
      <ElRow>
        <ElCol :span="24">
          <ElFormItem label="备注">
            <ElInput v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
  import { ElIcon, ElMessage, ElTooltip } from 'element-plus'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import type { RoleForm, RoleVO } from '@/api/system/role'
  import { addRole, getRole, updateRole } from '@/api/system/role'
  import { useDict } from '@/utils/dict'

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: RoleVO
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const formRef = ref<FormInstance>()

  const formData = reactive<RoleForm>({
    roleName: '',
    roleKey: '',
    roleSort: 1,
    status: '0',
    menuCheckStrictly: true,
    deptCheckStrictly: true,
    remark: '',
    dataScope: '1',
    roleId: undefined,
    menuIds: [],
    deptIds: []
  })

  const rules: FormRules = {
    roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
    roleKey: [{ required: true, message: '权限字符不能为空', trigger: 'blur' }],
    roleSort: [{ required: true, message: '角色顺序不能为空', trigger: 'blur' }]
  }

  const resetForm = () => {
    formData.roleName = ''
    formData.roleKey = ''
    formData.roleSort = 1
    formData.status = '0'
    formData.menuCheckStrictly = true
    formData.deptCheckStrictly = true
    formData.remark = ''
    formData.dataScope = '1'
    formData.roleId = undefined
    formData.menuIds = []
    formData.deptIds = []
    formRef.value?.resetFields()
    formRef.value?.clearValidate()
  }

  const loadRoleData = async (roleId?: string | number) => {
    if (!roleId) return
    const res = await getRole(roleId)
    Object.assign(formData, res)
    formData.roleSort = Number(formData.roleSort)
  }

  watch(
    () => [props.modelValue, props.dialogType, props.roleData],
    async ([visible, type, roleData]) => {
      if (visible) {
        resetForm()
        if (type === 'edit' && roleData && (roleData as RoleVO).roleId) {
          await loadRoleData((roleData as RoleVO).roleId)
        }
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleClose = () => {
    visible.value = false
    resetForm()
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (formData.roleId) {
          updateRole(formData).then(() => {
            ElMessage.success('修改成功')
            emit('success')
            handleClose()
          })
        } else {
          addRole(formData).then(() => {
            ElMessage.success('新增成功')
            emit('success')
            handleClose()
          })
        }
      }
    })
  }
</script>
