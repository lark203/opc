<!-- 字典数据弹窗组件 -->
<template>
  <ElDialog v-model="dialogVisible" :title="dialogTitle" width="30%" align-center destroy-on-close>
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80px">
      <!-- 字典类型（只读） -->
      <ElFormItem label="字典类型">
        <ElInput v-model="formData.dictType" disabled />
      </ElFormItem>

      <!-- 数据标签 -->
      <ElFormItem label="数据标签" prop="dictLabel">
        <ElInput v-model="formData.dictLabel" placeholder="请输入数据标签" />
      </ElFormItem>

      <!-- 数据键值 -->
      <ElFormItem label="数据键值" prop="dictValue">
        <ElInput v-model="formData.dictValue" placeholder="请输入数据键值" />
      </ElFormItem>

      <!-- 样式属性 -->
      <ElFormItem label="样式属性" prop="cssClass">
        <ElInput v-model="formData.cssClass" placeholder="请输入样式属性" />
      </ElFormItem>

      <!-- 显示排序 -->
      <ElFormItem label="显示排序" prop="dictSort">
        <ElInputNumber v-model="formData.dictSort" controls-position="right" :min="0" />
      </ElFormItem>

      <!-- 回显样式 -->
      <ElFormItem label="回显样式" prop="listClass">
        <ElSelect v-model="formData.listClass" placeholder="请选择回显样式">
          <ElOption
            v-for="item in listClassOptions"
            :key="item.value"
            :label="item.label + '(' + item.value + ')'"
            :value="item.value"
          />
        </ElSelect>
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
  import { ElInputNumber, ElSelect, ElOption } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { DictDataVO, DictDataForm } from '@/api/system/dict'

  // 定义Props
  interface Props {
    visible: boolean
    type: 'add' | 'edit'
    data?: Partial<DictDataVO>
    dictType?: string
  }

  // 定义Emits
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', formData: DictDataForm): void
  }

  // 获取Props和Emits
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 对话框标题
  const dialogTitle = computed(() => (props.type === 'add' ? '新增字典数据' : '修改字典数据'))

  // 对话框显示控制（双向绑定）
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  // 表单实例
  const formRef = ref<FormInstance>()

  // 回显样式选项
  const listClassOptions = [
    { value: 'default', label: '默认' },
    { value: 'primary', label: '主要' },
    { value: 'success', label: '成功' },
    { value: 'info', label: '信息' },
    { value: 'warning', label: '警告' },
    { value: 'danger', label: '危险' }
  ]

  // 表单数据初始值
  const initFormData: DictDataForm = {
    dictCode: undefined,
    dictType: '',
    dictLabel: '',
    dictValue: '',
    cssClass: '',
    listClass: 'primary',
    dictSort: 0,
    remark: ''
  }

  // 表单数据
  const formData = reactive<DictDataForm>({ ...initFormData })

  // 表单验证规则
  const rules: FormRules = {
    dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
    dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }],
    dictSort: [{ required: true, message: '数据顺序不能为空', trigger: 'blur' }]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initForm = () => {
    Object.assign(formData, { ...initFormData })
    // 设置字典类型
    formData.dictType = props.dictType || ''
    // 如果是编辑模式，填充数据
    if (props.type === 'edit' && props.data) {
      formData.dictCode = props.data.dictCode
      formData.dictLabel = props.data.dictLabel || ''
      formData.dictValue = props.data.dictValue || ''
      formData.cssClass = props.data.cssClass || ''
      formData.listClass = props.data.listClass || 'primary'
      formData.dictSort = props.data.dictSort || 0
      formData.remark = props.data.remark || ''
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.data, props.dictType],
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
