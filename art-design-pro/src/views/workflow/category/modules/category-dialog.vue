<template>
  <ElDialog
    :model-value="visible"
    :title="editData?.categoryId ? '修改分类' : '新增分类'"
    width="20%"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @open="handleOpen"
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="88px">
      <ElFormItem label="上级分类">
        <ElTreeSelect
          v-model="form.parentId"
          :data="parentOptions"
          :props="{ label: 'label', children: 'children' }"
          value-key="id"
          placeholder="请选择上级分类"
          check-strictly
          clearable
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="分类名称" prop="categoryName">
        <ElInput v-model="form.categoryName" placeholder="请输入分类名称" />
      </ElFormItem>
      <ElFormItem label="显示排序" prop="orderNum">
        <ElInputNumber
          v-model="form.orderNum"
          :min="0"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="emit('update:visible', false)">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue'
  import {
    ElButton,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElInputNumber,
    ElMessage,
    ElTreeSelect,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import {
    addCategory,
    categoryTree,
    type CategoryTreeVO,
    type FlowCategoryForm,
    type FlowCategoryVO,
    updateCategory
  } from '@/api/workflow/category'

  // editData 使用 Partial 类型，新增子分类时仅传入 parentId（部分字段）
  const props = defineProps<{
    visible: boolean
    editData?: Partial<FlowCategoryVO>
  }>()

  const emit = defineEmits<{
    'update:visible': [val: boolean]
    success: []
  }>()

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const parentOptions = ref<CategoryTreeVO[]>([])

  const form = reactive<FlowCategoryForm>({
    parentId: 0,
    categoryName: '',
    orderNum: 1
  })

  const rules: FormRules<FlowCategoryForm> = {
    categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
    orderNum: [{ required: true, message: '请输入排序', trigger: 'blur' }]
  }

  const excludeSubtree = (list: CategoryTreeVO[], excludeId: string | number): CategoryTreeVO[] => {
    return list
      .filter((node) => node.id !== excludeId)
      .map((node) => ({
        ...node,
        children: node.children ? excludeSubtree(node.children, excludeId) : undefined
      }))
  }

  const handleOpen = async () => {
    const tree = await categoryTree()
    // 提取编辑 ID 到局部变量，便于 TypeScript 类型收窄（Partial 下 categoryId 可能为 undefined）
    const editId = props.editData?.categoryId
    const base = editId ? excludeSubtree(tree, editId) : tree
    parentOptions.value = [{ id: 0, label: '顶级分类', children: base }]

    formRef.value?.resetFields()
    if (editId && props.editData) {
      // 编辑模式：回填表单数据
      Object.assign(form, {
        categoryId: props.editData.categoryId,
        parentId: props.editData.parentId,
        categoryName: props.editData.categoryName,
        orderNum: props.editData.orderNum
      })
    } else {
      // 新增模式：设置默认值，parentId 取自传入数据（新增子分类时为父级 ID）
      form.categoryId = undefined
      form.parentId = props.editData?.parentId ?? 0
      form.categoryName = ''
      form.orderNum = 1
    }
  }

  const handleClosed = () => {
    formRef.value?.resetFields()
  }

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (form.categoryId) {
        await updateCategory(form)
      } else {
        await addCategory(form)
      }
      ElMessage.success('保存成功')
      emit('success')
      emit('update:visible', false)
    } finally {
      submitting.value = false
    }
  }
</script>
