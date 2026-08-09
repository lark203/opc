<template>
  <ElForm ref="formRef" :model="infoForm" :rules="rules" label-width="150px">
    <ElRow>
      <ElCol :xs="24" :sm="12">
        <ElFormItem label="表名称" prop="tableName">
          <ElInput v-model="infoForm.tableName" placeholder="请输入表名称" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem label="表描述" prop="tableComment">
          <ElInput v-model="infoForm.tableComment" placeholder="请输入表描述" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem label="实体类名称" prop="className">
          <ElInput v-model="infoForm.className" placeholder="请输入实体类名称" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem label="作者" prop="functionAuthor">
          <ElInput v-model="infoForm.functionAuthor" placeholder="请输入作者" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24">
        <ElFormItem label="备注" prop="remark">
          <ElInput v-model="infoForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </ElFormItem>
      </ElCol>
    </ElRow>
  </ElForm>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { DbTableVO } from '@/api/tool/gen'

  const props = defineProps<{ info: DbTableVO }>()

  const infoForm = computed(() => props.info)
  const formRef = ref<FormInstance>()

  const rules: FormRules<DbTableVO> = {
    tableName: [{ required: true, message: '请输入表名称', trigger: 'blur' }],
    tableComment: [{ required: true, message: '请输入表描述', trigger: 'blur' }],
    className: [{ required: true, message: '请输入实体类名称', trigger: 'blur' }],
    functionAuthor: [{ required: true, message: '请输入作者', trigger: 'blur' }]
  }

  const validate = async (): Promise<boolean> => {
    if (!formRef.value) return false
    try {
      await formRef.value.validate()
      return true
    } catch {
      return false
    }
  }

  defineExpose({ validate })
</script>
