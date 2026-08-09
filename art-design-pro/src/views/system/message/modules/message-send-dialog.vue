<template>
  <ElDialog
    :model-value="visible"
    title="发送消息"
    width="40%"
    align-center
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @open="handleOpen"
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="90px">
      <ElFormItem label="接收范围" prop="range">
        <ElRadioGroup v-model="form.range">
          <ElRadio value="user">指定用户</ElRadio>
          <ElRadio value="all">全局广播</ElRadio>
        </ElRadioGroup>
      </ElFormItem>

      <ElFormItem v-if="form.range === 'user'" label="接收用户" prop="userIds">
        <ElButton @click="userSelectVisible = true">
          选择用户<span v-if="selectedUserIds.length"
            >（已选 {{ selectedUserIds.length }} 人）</span
          >
        </ElButton>
      </ElFormItem>

      <ElFormItem label="消息分类" prop="category">
        <ElSelect v-model="form.category" placeholder="请选择消息分类" style="width: 100%">
          <ElOption label="系统消息" value="system" />
          <ElOption label="通知公告" value="notice" />
          <ElOption label="工作流" value="workflow" />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="标题" prop="title">
        <ElInput v-model="form.title" placeholder="请输入消息标题" maxlength="100" />
      </ElFormItem>

      <ElFormItem label="内容" prop="content">
        <ElInput
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入消息内容"
          maxlength="500"
        />
      </ElFormItem>

      <ElFormItem label="跳转路径">
        <ElInput v-model="form.path" placeholder="可选，如 /system/user" />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="emit('update:visible', false)">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">发送</ElButton>
    </template>

    <UserSelect
      v-model:visible="userSelectVisible"
      :multiple="true"
      @confirm-call-back="handleUserSelected"
    />
  </ElDialog>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue'
  import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
  import UserSelect from '@/components/UserSelect/index.vue'
  import type { UserVO } from '@/api/system/user'
  import { sendMessage, type SysMessageSendBo } from '@/api/system/message'

  interface Props {
    visible: boolean
  }

  const props = withDefaults(defineProps<Props>(), { visible: false })

  const emit = defineEmits<{
    'update:visible': [val: boolean]
    success: []
  }>()

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const userSelectVisible = ref(false)
  const selectedUserIds = ref<Array<string | number>>([])

  const form = reactive({
    range: 'user',
    category: 'system',
    title: '',
    content: '',
    path: ''
  })

  const rules = reactive<FormRules>({
    range: [{ required: true, message: '请选择接收范围', trigger: 'change' }],
    category: [{ required: true, message: '请选择消息分类', trigger: 'change' }],
    title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
    content: [{ required: true, message: '内容不能为空', trigger: 'blur' }]
  })

  const handleOpen = () => {
    selectedUserIds.value = []
  }

  const handleClosed = () => {
    formRef.value?.resetFields()
    selectedUserIds.value = []
    form.range = 'user'
    form.category = 'system'
    form.title = ''
    form.content = ''
    form.path = ''
  }

  const handleUserSelected = (users: UserVO[]) => {
    selectedUserIds.value = users.map((u) => u.userId)
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (!valid) return

      if (form.range === 'user' && selectedUserIds.value.length === 0) {
        ElMessage.warning('请选择接收用户')
        return
      }

      const data: SysMessageSendBo = {
        broadcast: form.range === 'all',
        userIds: form.range === 'user' ? selectedUserIds.value : [],
        title: form.title,
        category: form.category,
        message: form.content,
        content: form.content,
        path: form.path || undefined
      }

      submitting.value = true
      try {
        await sendMessage(data)
        ElMessage.success('发送成功')
        emit('update:visible', false)
        emit('success')
      } catch {
        ElMessage.error('发送失败')
      } finally {
        submitting.value = false
      }
    })
  }
</script>
