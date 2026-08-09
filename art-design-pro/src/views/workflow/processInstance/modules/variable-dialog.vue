<template>
  <ElDialog
    :model-value="visible"
    title="流程变量"
    width="60%"
    :close-on-click-modal="false"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @open="handleOpen"
  >
    <!-- 当前变量展示区 -->
    <ElCard v-loading="loading" class="mb-4">
      <template #header>
        <span
          >流程定义名称：<ElTag>{{ flowName }}</ElTag></span
        >
      </template>
      <div class="variable-json">
        <pre v-if="formattedVariables">{{ formattedVariables }}</pre>
        <ElEmpty v-else description="暂无流程变量" :image-size="60" />
      </div>
    </ElCard>

    <!-- 新增/修改变量表单 -->
    <ElCard v-loading="loading">
      <ElForm ref="formRef" :model="form" :rules="rules" inline label-width="100px">
        <ElFormItem label="变量KEY" prop="key">
          <ElInput v-model="form.key" placeholder="请输入变量KEY" />
        </ElFormItem>
        <ElFormItem label="变量值" prop="value">
          <ElInput v-model="form.value" placeholder="请输入变量值" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" v-auth="'workflow:instance:variable'" @click="handleSubmit">
            确认
          </ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDialog,
    ElEmpty,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElTag,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import { instanceVariable, updateVariable } from '@/api/workflow/instance'
  import type { FlowVariableForm } from '@/api/workflow/instance/types'

  const props = defineProps<{
    visible: boolean
    instanceId?: string | number
    flowName?: string
  }>()

  const emit = defineEmits<{
    'update:visible': [val: boolean]
  }>()

  const formRef = ref<FormInstance>()
  const loading = ref(false)
  const variables = ref('')

  const form = reactive<FlowVariableForm>({
    instanceId: '',
    key: '',
    value: ''
  })

  const rules: FormRules<FlowVariableForm> = {
    key: [{ required: true, message: '请输入变量KEY', trigger: 'blur' }],
    value: [{ required: true, message: '请输入变量值', trigger: 'blur' }]
  }

  /** 将变量字符串格式化为美观的 JSON */
  const formattedVariables = computed(() => {
    if (!variables.value) return ''
    try {
      return JSON.stringify(JSON.parse(variables.value), null, 2)
    } catch {
      return variables.value
    }
  })

  const loadVariables = async () => {
    if (!props.instanceId) return
    loading.value = true
    try {
      const data = await instanceVariable(props.instanceId)
      variables.value = data.variable || ''
    } finally {
      loading.value = false
    }
  }

  const handleOpen = () => {
    form.key = ''
    form.value = ''
    form.instanceId = props.instanceId || ''
    loadVariables()
  }

  const handleSubmit = async () => {
    await formRef.value?.validate()
    loading.value = true
    try {
      form.instanceId = props.instanceId || ''
      await updateVariable({ ...form })
      ElMessage.success('操作成功')
      await loadVariables()
      form.key = ''
      form.value = ''
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.visible,
    (val) => {
      if (!val) {
        formRef.value?.resetFields()
        variables.value = ''
      }
    }
  )
</script>

<style lang="scss" scoped>
  .variable-json {
    max-height: 300px;
    overflow-y: auto;

    pre {
      padding: 12px;
      margin: 0;
      font-family: Consolas, Monaco, monospace;
      font-size: 13px;
      line-height: 1.6;
      word-break: break-all;
      white-space: pre-wrap;
      background: var(--el-fill-color-lighter);
      border-radius: 6px;
    }
  }
</style>
