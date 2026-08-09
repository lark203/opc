<template>
  <ElDialog
    :model-value="visible"
    title="审批"
    width="680px"
    top="6vh"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @open="handleOpen"
  >
    <ElDescriptions :column="2" border size="small" class="mb-3">
      <ElDescriptionsItem label="流程">{{ task?.flowName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="当前节点">{{ task?.nodeName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="业务单号">{{ task?.businessCode || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="业务标题">{{ task?.businessTitle || '-' }}</ElDescriptionsItem>
      <ElDescriptionsItem label="申请人">{{ task?.createByName }}</ElDescriptionsItem>
      <ElDescriptionsItem label="办理人">
        <UserNameDisplay :names="task?.assigneeNames" />
      </ElDescriptionsItem>
    </ElDescriptions>

    <ElForm label-width="92px">
      <ElFormItem label="审批意见">
        <ElInput v-model="message" type="textarea" :rows="3" placeholder="请输入审批意见" />
      </ElFormItem>

      <ElFormItem label="通知方式">
        <ElCheckboxGroup v-model="messageType">
          <ElCheckbox v-for="m in messageOptions" :key="m.value" :value="m.value">{{
            m.label
          }}</ElCheckbox>
        </ElCheckboxGroup>
      </ElFormItem>

      <ElFormItem label="抄送人">
        <div class="flex flex-wrap items-center gap-2">
          <ElTag v-for="u in copyUsers" :key="u.userId" closable @close="removeCopy(u)">
            {{ u.nickName }}
          </ElTag>
          <ElButton size="small" @click="copyVisible = true">选择</ElButton>
        </div>
      </ElFormItem>

      <ElFormItem v-if="showButtons.pass" label="下一步审批人">
        <div class="flex flex-wrap items-center gap-2">
          <ElTag v-if="nextApprover" closable @close="nextApprover = undefined">
            {{ nextApproverName }}
          </ElTag>
          <ElButton size="small" @click="nextVisible = true">选择</ElButton>
        </div>
      </ElFormItem>
    </ElForm>

    <div class="action-bar">
      <ElButton v-if="showButtons.pass" type="primary" :loading="loading" @click="handlePass"
        >通过</ElButton
      >
      <ElButton v-if="showButtons.back" :loading="loading" @click="handleBack">退回</ElButton>
      <ElButton
        v-if="showButtons.transfer"
        :loading="loading"
        @click="openUserPick('transferTask', '转办', false)"
      >
        转办
      </ElButton>
      <ElButton
        v-if="showButtons.delegate"
        :loading="loading"
        @click="openUserPick('delegateTask', '委托', false)"
      >
        委托
      </ElButton>
      <ElButton
        v-if="showButtons.addSign"
        :loading="loading"
        @click="openUserPick('addSignature', '加签', true)"
      >
        加签
      </ElButton>
      <ElButton
        v-if="showButtons.subSign"
        :loading="loading"
        @click="openUserPick('reductionSignature', '减签', true)"
      >
        减签
      </ElButton>
      <ElButton
        v-if="showButtons.termination"
        type="danger"
        :loading="loading"
        @click="handleTermination"
      >
        终止
      </ElButton>
    </div>

    <!-- 抄送选择 -->
    <UserSelect v-model:visible="copyVisible" multiple @confirm-call-back="onCopyConfirm" />

    <!-- 下一步审批人选择 -->
    <UserSelect
      v-model:visible="nextVisible"
      :multiple="false"
      @confirm-call-back="onNextConfirm"
    />

    <!-- 转办/委托/加签/减签 选人 -->
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
  import { computed, ref } from 'vue'
  import {
    ElButton,
    ElCheckbox,
    ElCheckboxGroup,
    ElDescriptions,
    ElDescriptionsItem,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElTag
  } from 'element-plus'
  import UserSelect from '@/components/UserSelect/index.vue'
  import UserNameDisplay from './UserNameDisplay.vue'
  import {
    backProcess,
    completeTask,
    type FlowCopyVo,
    type FlowTaskVO,
    getTask,
    taskOperation,
    terminationTask
  } from '@/api/workflow/task'
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
  const task = ref<FlowTaskVO>()
  const message = ref('')
  const messageType = ref<string[]>(['1'])
  const copyUsers = ref<FlowCopyVo[]>([])
  const nextApprover = ref<string | number>()
  const nextApproverName = ref('')
  const loading = ref(false)

  const copyVisible = ref(false)
  const nextVisible = ref(false)
  const pickVisible = ref(false)
  const pickTitle = ref('')
  const pickMultiple = ref(false)
  const pickAction = ref('')

  const showButtons = computed(() => {
    const map = new Map<string, boolean>()
    ;(task.value?.buttonList || []).filter((b) => b.show).forEach((b) => map.set(b.code, true))
    const empty = map.size === 0
    return {
      pass: true,
      back: empty || map.has('back'),
      termination: empty || map.has('termination'),
      transfer: empty || map.has('transfer'),
      delegate: empty || map.has('trust'),
      addSign: empty || map.has('addSign'),
      subSign: empty || map.has('subSign')
    }
  })

  /** 打开审批弹窗并设置任务ID */
  const openDialog = async (id: string | number) => {
    taskId.value = id
    emit('update:visible', true)
  }

  const handleOpen = async () => {
    if (!taskId.value) return
    const res = await getTask(taskId.value)
    task.value = res
    copyUsers.value = (res.copyList || []).map((c) => ({ userId: c.userId, nickName: c.nickName }))
    message.value = ''
    messageType.value = ['1']
    nextApprover.value = undefined
  }

  defineExpose({ openDialog })

  const removeCopy = (u: FlowCopyVo) => {
    copyUsers.value = copyUsers.value.filter((c) => c.userId !== u.userId)
  }

  const onCopyConfirm = (users: UserVO[]) => {
    copyUsers.value = users.map((u) => ({ userId: u.userId, nickName: u.nickName }))
  }

  const onNextConfirm = (users: UserVO[]) => {
    const u = users[0]
    nextApprover.value = u?.userId
    nextApproverName.value = u?.nickName || ''
  }

  const openUserPick = (action: string, title: string, multiple: boolean) => {
    pickAction.value = action
    pickTitle.value = title
    pickMultiple.value = multiple
    pickVisible.value = true
  }

  const onPickConfirm = (users: UserVO[]) => {
    pickVisible.value = false
    if (pickAction.value === 'transferTask' || pickAction.value === 'delegateTask') {
      const u = users[0]
      if (!u) return
      taskOperation(pickAction.value, {
        userId: u.userId,
        taskId: taskId.value,
        message: message.value,
        messageType: messageType.value
      }).then(finish)
    } else {
      taskOperation(pickAction.value, {
        userIds: users.map((u) => String(u.userId)),
        taskId: taskId.value,
        message: message.value,
        messageType: messageType.value
      }).then(finish)
    }
  }

  const finish = () => {
    ElMessage.success('操作成功')
    emit('success')
    emit('update:visible', false)
  }

  const handlePass = async () => {
    loading.value = true
    try {
      await completeTask({
        taskId: taskId.value,
        message: message.value,
        messageType: messageType.value,
        flowCopyList: copyUsers.value,
        handler: nextApprover.value ? String(nextApprover.value) : undefined
      })
      finish()
    } finally {
      loading.value = false
    }
  }

  const handleBack = async () => {
    loading.value = true
    try {
      await backProcess({
        taskId: taskId.value,
        message: message.value,
        messageType: messageType.value
      })
      finish()
    } finally {
      loading.value = false
    }
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
    margin-top: 12px;
    border-top: 1px solid var(--el-border-color-light);
    padding-top: 12px;
  }
</style>
