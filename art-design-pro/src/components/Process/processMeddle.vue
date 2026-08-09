<template>
  <ElDialog
    :model-value="visible"
    title="流程干预"
    width="60%"
    top="8vh"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
  >
    <ElForm label-width="92px">
      <ElFormItem label="干预说明">
        <ElInput
          v-model="message"
          type="textarea"
          :rows="3"
          placeholder="请输入干预说明/审批意见"
        />
      </ElFormItem>
      <ElFormItem label="通知方式">
        <ElCheckboxGroup v-model="messageType">
          <ElCheckbox v-for="m in messageOptions" :key="m.value" :value="m.value">{{
            m.label
          }}</ElCheckbox>
        </ElCheckboxGroup>
      </ElFormItem>
    </ElForm>

    <div class="action-bar">
      <ElButton :loading="loading" @click="openUserPick('transferTask', '转办', false)"
        >转办</ElButton
      >
      <ElButton :loading="loading" @click="openUserPick('delegateTask', '委托', false)"
        >委托</ElButton
      >
      <ElButton :loading="loading" @click="openUserPick('addSignature', '加签', true)"
        >加签</ElButton
      >
      <ElButton :loading="loading" @click="openUserPick('reductionSignature', '减签', true)"
        >减签</ElButton
      >
      <ElButton type="danger" :loading="loading" @click="handleTermination">终止</ElButton>
    </div>

    <ElDialog
      :model-value="pickVisible"
      :title="pickTitle"
      width="60%"
      top="8vh"
      append-to-body
      @update:model-value="(val: boolean) => (pickVisible = val)"
    >
      <UserSelect
        :visible="pickVisible"
        :multiple="pickMultiple"
        @confirm-call-back="onPickConfirm"
      />
    </ElDialog>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import {
    ElButton,
    ElCheckbox,
    ElCheckboxGroup,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage
  } from 'element-plus'
  import UserSelect from '@/components/UserSelect/index.vue'
  import { taskOperation, terminationTask } from '@/api/workflow/task'
  import type { UserVO } from '@/api/system/user'

  defineProps<{ visible: boolean }>()

  const emit = defineEmits<{
    'update:visible': [val: boolean]
    success: []
  }>()

  const messageOptions = [
    { label: '站内信', value: '1' },
    { label: '邮件', value: '2' },
    { label: '短信', value: '3' }
  ]

  const taskId = ref<string | number>('')
  const message = ref('')
  const messageType = ref<string[]>(['1'])
  const loading = ref(false)
  const pickVisible = ref(false)
  const pickTitle = ref('')
  const pickMultiple = ref(false)
  const pickAction = ref('')

  /** 打开干预弹窗并设置任务ID */
  const openDialog = (id: string | number) => {
    taskId.value = id
    emit('update:visible', true)
  }
  defineExpose({ openDialog })

  const openUserPick = (action: string, title: string, multiple: boolean) => {
    pickAction.value = action
    pickTitle.value = title
    pickMultiple.value = multiple
    pickVisible.value = true
  }

  const onPickConfirm = (users: UserVO[]) => {
    pickVisible.value = false
    if (!users.length) return
    const payload = {
      taskId: taskId.value,
      message: message.value,
      messageType: messageType.value
    }
    const call =
      pickAction.value === 'transferTask' || pickAction.value === 'delegateTask'
        ? taskOperation(pickAction.value, { ...payload, userId: users[0].userId })
        : taskOperation(pickAction.value, {
            ...payload,
            userIds: users.map((u) => String(u.userId))
          })
    call.then(finish)
  }

  const finish = () => {
    ElMessage.success('操作成功')
    emit('success')
    emit('update:visible', false)
  }

  const handleTermination = async () => {
    loading.value = true
    try {
      await terminationTask({ taskId: taskId.value, comment: message.value || '终止流程' })
      finish()
    } finally {
      loading.value = false
    }
  }
</script>

<style lang="scss" scoped>
  .action-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: flex-end;
  }
</style>
