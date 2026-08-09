<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="20%"
    align-center
    @closed="handleClosed"
  >
    <!-- ElForm: 表单组件 -->
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px">
      <ElFormItem label="岗位名称" prop="postName">
        <ElInput v-model="form.postName" placeholder="请输入岗位名称" />
      </ElFormItem>
      <ElFormItem label="部门" prop="deptId">
        <ElTreeSelect
          v-model="form.deptId"
          :data="deptOptions"
          :props="{ value: 'id', label: 'label', children: 'children' }"
          value-key="id"
          placeholder="请选择部门"
          check-strictly
          :default-expanded-keys="expandedKeys"
        />
      </ElFormItem>
      <ElFormItem label="岗位编码" prop="postCode">
        <ElInput v-model="form.postCode" placeholder="请输入岗位编码" />
      </ElFormItem>
      <ElFormItem label="类别编码" prop="postCategory">
        <ElInput v-model="form.postCategory" placeholder="请输入类别编码" />
      </ElFormItem>
      <ElFormItem label="岗位顺序" prop="postSort">
        <ElInputNumber v-model="form.postSort" controls-position="right" :min="0" />
      </ElFormItem>
      <!-- 使用字典值渲染状态选项 -->
      <ElFormItem label="岗位状态" prop="status">
        <ElRadioGroup v-model="form.status">
          <ElRadio v-for="status in sys_normal_disable" :key="status.value" :label="status.value">
            {{ status.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="form.remark" type="textarea" placeholder="请输入备注" />
      </ElFormItem>
    </ElForm>
    <!-- 弹窗底部按钮 -->
    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  // 导入 Vue 组合式 API
  import { computed, reactive, ref, toRefs, watch } from 'vue'
  // 导入表单校验规则类型
  import type { FormRules } from 'element-plus'
  // 导入 Element Plus 组件和消息提示
  import { ElMessage, ElTreeSelect } from 'element-plus'
  // 导入字典工具函数
  import { useDict } from '@/utils/dict'
  // 导入岗位 API 和类型定义
  import {
    addPost,
    deptTreeSelect,
    getPost,
    type PostForm,
    type PostVO,
    updatePost
  } from '@/api/system/post'

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // 定义组件属性
  interface Props {
    visible: boolean
    editData?: PostVO
  }

  // 定义组件事件
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  // withDefaults: 为属性设置默认值
  const props = withDefaults(defineProps<Props>(), {
    visible: false
  })

  // defineEmits: 声明组件事件
  const emit = defineEmits<Emits>()

  // ref: 创建响应式变量
  const formRef = ref() // 表单引用
  const isEdit = ref(false) // 是否为编辑模式
  const deptOptions = ref<any[]>([]) // 部门树选项

  // reactive: 创建响应式表单对象
  const form = reactive<PostForm>({
    postId: undefined,
    postCode: '',
    postName: '',
    postCategory: '',
    postSort: 0,
    status: '0',
    remark: '',
    deptId: undefined
  })

  // reactive: 创建表单校验规则
  const rules = reactive<FormRules>({
    postName: [{ required: true, message: '岗位名称不能为空', trigger: 'blur' }],
    postCode: [{ required: true, message: '岗位编码不能为空', trigger: 'blur' }],
    deptId: [{ required: true, message: '部门不能为空', trigger: 'blur' }],
    postSort: [{ required: true, message: '岗位顺序不能为空', trigger: 'blur' }]
  })

  // computed: 根据编辑模式动态生成弹窗标题
  const dialogTitle = computed(() => {
    return isEdit.value ? '修改岗位' : '新增岗位'
  })

  // computed: 计算需要展开的节点路径，从根节点到当前选中的部门
  const expandedKeys = computed(() => {
    if (!form.deptId || form.deptId === 0) {
      return []
    }
    const path: (string | number)[] = []
    const findPath = (depts: any[], targetId: string | number): boolean => {
      for (const dept of depts) {
        if (String(dept.id) === String(targetId)) {
          return true
        }
        if (dept.children && findPath(dept.children, targetId)) {
          path.unshift(dept.id)
          return true
        }
      }
      return false
    }
    findPath(deptOptions.value, form.deptId)
    return path
  })

  // 加载部门树数据
  const loadTreeSelect = async () => {
    const res = await deptTreeSelect()
    deptOptions.value = res
  }

  // 重置表单数据
  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(form, {
      postId: undefined,
      postCode: '',
      postName: '',
      postCategory: '',
      postSort: 0,
      status: '0',
      remark: '',
      deptId: undefined
    })
  }

  // 加载编辑表单数据
  const loadFormData = async () => {
    if (!props.editData?.postId) return
    isEdit.value = true
    const data = await getPost(props.editData.postId)
    Object.assign(form, data)
  }

  // 提交表单
  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (form.postId) {
          // 修改岗位
          updatePost(form).then(() => {
            ElMessage.success('修改成功')
            emit('success')
            handleCancel()
          })
        } else {
          // 新增岗位
          addPost(form).then(() => {
            ElMessage.success('新增成功')
            emit('success')
            handleCancel()
          })
        }
      }
    })
  }

  // 取消按钮点击事件
  const handleCancel = () => {
    emit('update:visible', false)
  }

  // 弹窗关闭事件（清理数据）
  const handleClosed = () => {
    resetForm()
    isEdit.value = false
  }

  // watch: 监听弹窗显示状态变化
  watch(
    () => props.visible,
    async (newVal) => {
      if (newVal) {
        await loadTreeSelect()
        if (props.editData) {
          await loadFormData()
        }
      }
    }
  )
</script>
