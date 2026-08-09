<!-- 字典类型弹窗组件 -->
<template>
  <ElDialog v-model="dialogVisible" :title="dialogTitle" width="30%" align-center destroy-on-close>
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <!-- 字典名称 -->
      <ElFormItem label="字典名称" prop="dictName">
        <ElInput v-model="formData.dictName" placeholder="请输入字典名称" />
      </ElFormItem>

      <!-- 字典类型 -->
      <ElFormItem prop="dictType">
        <template #label>
          <span>
            <ElTooltip content="数据存储中的Key值，如：sys_user_gender" placement="top">
              <i class="el-icon-question"></i>
            </ElTooltip>
            字典类型
          </span>
        </template>
        <ElInput v-model="formData.dictType" placeholder="请输入字典类型" maxlength="100" />
      </ElFormItem>

      <!-- 备注 -->
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="formData.remark" type="textarea" placeholder="请输入备注内容" />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  // 导入依赖
  import { computed, reactive, ref, watch, nextTick } from 'vue'
  import { ElTooltip } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { DictTypeVO, DictTypeForm } from '@/api/system/dict'

  // 定义Props
  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    data?: Partial<DictTypeVO>
  }

  // 定义Emits
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', formData: DictTypeForm): void
  }

  // 获取Props和Emits
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框标题
  const dialogTitle = computed(() => (props.type === 'add' ? '新增字典类型' : '修改字典类型'))

  // 对话框显示控制（双向绑定）
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据初始值
  const initFormData: DictTypeForm = {
    dictId: undefined,
    dictName: '',
    dictType: '',
    remark: ''
  }

  // 表单数据
  const formData = reactive<DictTypeForm>({ ...initFormData })

  // 表单验证规则
  const rules: FormRules = {
    dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }],
    dictType: [{ required: true, message: '字典类型不能为空', trigger: 'blur' }]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initForm = () => {
    Object.assign(formData, { ...initFormData })
    if (props.type === 'edit' && props.data) {
      formData.dictId = props.data.dictId
      formData.dictName = props.data.dictName || ''
      formData.dictType = props.data.dictType || ''
      formData.remark = props.data.remark || ''
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.data],
    ([visible]) => {
      if (visible) {
        initForm()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   * 验证通过后触发提交事件
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate((valid) => {
      if (valid) {
        emit('submit', { ...formData })
      }
    })
  }
</script>
