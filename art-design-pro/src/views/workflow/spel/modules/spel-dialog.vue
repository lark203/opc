<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="20%"
    align-center
    @closed="handleClosed"
  >
    <!-- ElForm: 表单组件，label-width 控制标签宽度 -->
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
      <!-- 组件名称：注册到 Spring 容器中的组件名 -->
      <ElFormItem label="组件名称" prop="componentName">
        <ElInput
          v-model="form.componentName"
          placeholder="请输入组件名称"
          @input="updateViewSpel"
        />
        <!-- 自定义标签：添加问号图标提示 -->
        <template #label>
          <span class="inline-flex items-center gap-1">
            <ElTooltip content="注册到Spring容器中的组件名，如：spelRuleComponent" placement="top">
              <ElIcon class="cursor-help"><QuestionFilled /></ElIcon>
            </ElTooltip>
            组件名称
          </span>
        </template>
      </ElFormItem>
      <!-- 方法名称：组件中的方法名 -->
      <ElFormItem label="方法名称" prop="methodName">
        <ElInput v-model="form.methodName" placeholder="请输入方法名称" @input="updateViewSpel" />
        <template #label>
          <span class="inline-flex items-center gap-1">
            <ElTooltip content="组件中的方法名称，如：selectDeptLeaderById" placement="top">
              <ElIcon class="cursor-help"><QuestionFilled /></ElIcon>
            </ElTooltip>
            方法名称
          </span>
        </template>
      </ElFormItem>
      <!-- 方法参数：多个参数使用逗号分隔 -->
      <ElFormItem label="方法参数" prop="methodParams">
        <ElInput v-model="form.methodParams" placeholder="请输入方法参数" @input="updateViewSpel" />
        <template #label>
          <span class="inline-flex items-center gap-1">
            <ElTooltip
              content="方法参数，如：deptId，多个使用英文逗号分隔，单参数变量仅支持单个方法参数"
              placement="top"
            >
              <ElIcon class="cursor-help"><QuestionFilled /></ElIcon>
            </ElTooltip>
            方法参数
          </span>
        </template>
      </ElFormItem>
      <!-- SPEL表达式预览：只读展示，根据上方三个字段自动生成 -->
      <ElFormItem label="SPEL表达式">
        <span class="preview-box">
          {{ form.viewSpel || '例如：#{@组件名.方法名(#方法参数)} 或 ${方法参数}' }}
        </span>
      </ElFormItem>
      <!-- 状态：使用字典值 sys_normal_disable 渲染单选框 -->
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="form.status">
          <ElRadio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">
            {{ dict.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <!-- 备注 -->
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>
    <!-- 弹窗底部按钮 -->
    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  // 导入 Vue 组合式 API：computed 计算属性，reactive 响应式对象，ref 响应式引用，toRefs 解构字典，watch 监听
  import { computed, reactive, ref, toRefs, watch } from 'vue'
  // 导入表单校验规则类型
  import type { FormRules } from 'element-plus'
  // 导入 Element Plus 组件和消息提示
  import { ElIcon, ElMessage, ElRadio, ElRadioGroup, ElTooltip } from 'element-plus'
  // 导入问号图标
  import { QuestionFilled } from '@element-plus/icons-vue'
  // 导入字典工具函数，获取 sys_normal_disable 字典（正常/停用状态）
  import { useDict } from '@/utils/dict'
  // 导入流程表达式 API 和类型定义
  import {
    addSpel,
    type FlowSpelForm,
    type FlowSpelVO,
    getSpel,
    updateSpel
  } from '@/api/workflow/spel'

  // 使用字典工具函数获取 sys_normal_disable 字典（0=正常，1=停用）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // ========================= 组件属性与事件 =========================

  // 定义组件属性
  interface Props {
    visible: boolean
    editData?: FlowSpelVO
  }

  // 定义组件事件
  interface Emits {
    (e: 'update:visible', value: boolean): void

    (e: 'success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // ========================= 响应式状态 =========================

  const formRef = ref() // 表单引用
  const submitting = ref(false) // 提交加载状态
  const isEdit = ref(false) // 是否为编辑模式

  // 表单数据，初始化默认值
  const form = reactive<FlowSpelForm>({
    id: undefined,
    componentName: '',
    methodName: '',
    methodParams: '',
    viewSpel: '',
    status: '0',
    remark: ''
  })

  // 表单校验规则
  const rules = reactive<FormRules<FlowSpelForm>>({
    status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
  })

  // computed: 根据编辑模式动态生成弹窗标题
  const dialogTitle = computed(() => (isEdit.value ? '修改流程表达式' : '新增流程表达式'))

  // ========================= SPEL 表达式自动生成 =========================

  // 根据组件名称、方法名称、方法参数自动生成 SPEL 表达式预览
  // 规则：
  // 1. 仅参数存在（无组件名和方法名）：${参数名}（仅支持单个参数）
  // 2. 组件名和方法名都存在：#{@组件名.方法名(#参数1,#参数2)}
  // 3. 缺少组件名或方法名：提示"请填写组件名称和方法名"
  const updateViewSpel = () => {
    const comp = (form.componentName || '').trim()
    const method = (form.methodName || '').trim()
    const paramStr = (form.methodParams || '').trim()

    // 三个字段都为空时，清空预览
    if (!comp && !method && !paramStr) {
      form.viewSpel = ''
      return
    }

    // 仅参数存在（无组件和方法）：生成变量引用 ${参数}
    if (!comp && !method && paramStr) {
      const paramList = paramStr
        .split(',')
        .map((p) => p.trim())
        .filter((p) => p.length > 0)
      // 单参数变量仅支持单个方法参数
      if (paramList.length === 1) {
        form.viewSpel = `\${${paramList[0]}}`
        return
      }
    }

    // 缺少组件名或方法名时，提示填写
    if (!comp || !method) {
      form.viewSpel = '请填写组件名称和方法名'
      return
    }

    // 解析参数列表
    let paramList: string[] = []
    if (paramStr) {
      paramList = paramStr
        .split(',')
        .map((p) => p.trim())
        .filter((p) => p.length > 0)
    }

    // 拼接参数部分：(#param1,#param2) 或 ()
    const paramPart =
      paramList.length > 0 ? '(' + paramList.map((p) => `#${p}`).join(',') + ')' : '()'

    // 生成完整 SPEL 表达式：#{@component.method(#param)}
    form.viewSpel = `#{@${comp}.${method}${paramPart}}`
  }

  // watch: 监听三个字段变化，自动更新 SPEL 预览
  watch(() => [form.componentName, form.methodName, form.methodParams], updateViewSpel)

  // ========================= 表单操作 =========================

  // 重置表单到初始状态
  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(form, {
      id: undefined,
      componentName: '',
      methodName: '',
      methodParams: '',
      viewSpel: '',
      status: '0',
      remark: ''
    })
  }

  // 加载编辑数据：调用接口获取详情并回填表单
  const loadFormData = async () => {
    if (!props.editData?.id) return
    isEdit.value = true
    const data = await getSpel(props.editData.id)
    Object.assign(form, data)
  }

  // 提交表单：校验通过后调用新增/修改接口
  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) return
      submitting.value = true
      try {
        if (form.id) {
          // 编辑模式：调用修改接口
          await updateSpel(form)
          ElMessage.success('修改成功')
        } else {
          // 新增模式：调用新增接口
          await addSpel(form)
          ElMessage.success('新增成功')
        }
        emit('success')
        handleCancel()
      } finally {
        submitting.value = false
      }
    })
  }

  // 取消按钮：关闭弹窗
  const handleCancel = () => {
    emit('update:visible', false)
  }

  // 弹窗关闭动画结束：清理表单数据
  const handleClosed = () => {
    resetForm()
    isEdit.value = false
  }

  // watch: 监听弹窗显示状态，打开时加载数据
  watch(
    () => props.visible,
    async (newVal) => {
      if (newVal) {
        if (props.editData) {
          await loadFormData()
        } else {
          // 新增模式：重置为默认值
          isEdit.value = false
        }
      }
    }
  )
</script>

<style lang="scss" scoped>
  // SPEL 表达式预览框样式
  .preview-box {
    width: 100%;
    // 与 ElInput 高度对齐
    min-height: 36px;
    padding: 10px 12px;
    overflow-x: auto;
    // 等宽字体使表达式更清晰
    font-family: monospace;
    line-height: 1.5;
    color: var(--el-text-color-primary);
    // 禁止换行，超出时水平滚动
    white-space: nowrap;
    background-color: var(--el-fill-color-light);
    border-radius: 4px;
  }
</style>
