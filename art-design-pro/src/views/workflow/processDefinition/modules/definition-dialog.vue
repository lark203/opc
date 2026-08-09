<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    width="650px"
    align-center
    :close-on-click-modal="false"
    @update:model-value="handleCancel"
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="120px">
      <ElFormItem label="流程类别" prop="category">
        <ElTreeSelect
          v-model="form.category"
          :data="categoryOptions"
          :props="{ value: 'id', label: 'label', children: 'children' }"
          value-key="id"
          filterable
          check-strictly
          :render-after-expand="false"
          placeholder="请选择流程类别"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="流程编码" prop="flowCode">
        <ElInput
          v-model="form.flowCode"
          placeholder="请输入流程编码"
          maxlength="40"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="流程名称" prop="flowName">
        <ElInput
          v-model="form.flowName"
          placeholder="请输入流程名称"
          maxlength="100"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="设计器模式" prop="modelValue">
        <ElRadioGroup v-model="form.modelValue" :disabled="!!form.id">
          <ElRadio value="CLASSICS" border>经典模式</ElRadio>
          <ElRadio value="MIMIC" border>仿钉钉模式</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="流程配置">
        <ElCheckbox v-model="autoPass" label="下一节点执行人是当前任务处理人自动审批" />
      </ElFormItem>
      <ElFormItem label="是否动态表单" prop="formCustom">
        <ElRadioGroup v-model="form.formCustom">
          <ElRadio value="Y" border disabled>是</ElRadio>
          <ElRadio value="N" border>否</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="表单路径" prop="formPath">
        <ElInput
          v-model="form.formPath"
          placeholder="请输入表单路径"
          maxlength="100"
          show-word-limit
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import {
    addDefinition,
    editDefinition,
    type FlowDefinitionForm,
    type FlowDefinitionVO,
    getDefinition
  } from '@/api/workflow/definition'
  import { categoryTree, type CategoryTreeVO } from '@/api/workflow/category'

  interface Props {
    visible: boolean
    editData?: FlowDefinitionVO
    /** 新增时默认选中的流程类别 */
    defaultCategory?: string | number
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success', isEdit: boolean): void
  }

  const props = withDefaults(defineProps<Props>(), { visible: false })
  const emit = defineEmits<Emits>()

  const formRef = ref()
  const isEdit = ref(false)
  const submitting = ref(false)
  const autoPass = ref(false)
  const categoryOptions = ref<CategoryTreeVO[]>([])

  const defaultForm = (): FlowDefinitionForm => ({
    id: undefined,
    flowName: '',
    flowCode: '',
    category: '',
    ext: '',
    formPath: '',
    formCustom: 'N',
    modelValue: 'CLASSICS'
  })

  const form = reactive<FlowDefinitionForm>(defaultForm())

  const rules = reactive<FormRules>({
    category: [{ required: true, message: '分类名称不能为空', trigger: 'change' }],
    flowName: [{ required: true, message: '流程定义名称不能为空', trigger: 'blur' }],
    flowCode: [{ required: true, message: '流程定义编码不能为空', trigger: 'blur' }],
    formCustom: [{ required: true, message: '请选择是否动态表单', trigger: 'change' }],
    modelValue: [{ required: true, message: '设计器模式不能为空', trigger: 'change' }]
  })

  const dialogTitle = computed(() => (isEdit.value ? '修改流程' : '新增流程'))

  const loadCategoryTree = async () => {
    categoryOptions.value = await categoryTree()
  }

  const loadFormData = async () => {
    if (!props.editData?.id) return
    isEdit.value = true
    const data = await getDefinition(props.editData.id)
    Object.assign(form, data)
    autoPass.value = false
    if (form.ext) {
      try {
        const extJson = JSON.parse(form.ext)
        autoPass.value = !!extJson.autoPass
      } catch {
        autoPass.value = false
      }
    }
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      form.ext = JSON.stringify({ autoPass: autoPass.value })
      if (form.id) {
        await editDefinition(form)
        ElMessage.success('修改成功')
      } else {
        await addDefinition(form)
        ElMessage.success('新增成功')
      }
      emit('success', !!form.id)
      handleCancel()
    } finally {
      submitting.value = false
    }
  }

  const handleCancel = () => {
    emit('update:visible', false)
  }

  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(form, defaultForm())
    autoPass.value = false
    isEdit.value = false
  }

  watch(
    () => props.visible,
    async (val) => {
      if (!val) return
      await loadCategoryTree()
      if (props.editData) {
        await loadFormData()
      } else if (props.defaultCategory) {
        form.category = props.defaultCategory
      }
    }
  )
</script>
