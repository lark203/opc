<template>
  <ElDialog
    :model-value="visible"
    title="催办提醒"
    width="480px"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
  >
    <ElForm label-width="80px">
      <ElFormItem label="通知方式">
        <ElCheckboxGroup v-model="messageType">
          <ElCheckbox v-for="item in messageOptions" :key="item.value" :value="item.value">
            {{ item.label }}
          </ElCheckbox>
        </ElCheckboxGroup>
      </ElFormItem>
      <ElFormItem label="催办内容">
        <ElInput v-model="message" type="textarea" :rows="4" placeholder="请输入催办内容" />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="emit('update:visible', false)">取消</ElButton>
      <ElButton type="primary" :disabled="!message.trim()" @click="handleConfirm">发送</ElButton>
    </template>
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
    ElInput
  } from 'element-plus'

  defineProps<{ visible: boolean }>()

  const emit = defineEmits<{
    'update:visible': [val: boolean]
    confirm: [payload: { messageType: string[]; message: string }]
  }>()

  const messageOptions = [
    { label: '站内信', value: '1' },
    { label: '邮件', value: '2' },
    { label: '短信', value: '3' }
  ]

  const messageType = ref<string[]>(['1'])
  const message = ref('')

  const handleConfirm = () => {
    emit('confirm', { messageType: messageType.value, message: message.value.trim() })
    emit('update:visible', false)
  }
</script>
