<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="40%"
    align-center
    @closed="handleClosed"
  >
    <!-- ElForm: 表单组件 -->
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px">
      <ElRow :gutter="20">
        <!-- 上级部门选择（仅新增子部门时显示） -->
        <ElCol v-if="form.parentId !== 0" :span="24">
          <ElFormItem label="上级部门">
            <ElTreeSelect
              v-model="form.parentId"
              :data="deptOptions"
              :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
              value-key="deptId"
              placeholder="选择上级部门"
              check-strictly
              :default-expanded-keys="expandedKeys"
            />
          </ElFormItem>
        </ElCol>
        <!-- 部门名称 -->
        <ElCol :span="12">
          <ElFormItem label="部门名称" prop="deptName">
            <ElInput v-model="form.deptName" placeholder="请输入部门名称" />
          </ElFormItem>
        </ElCol>
        <!-- 类别编码 -->
        <ElCol :span="12">
          <ElFormItem label="类别编码" prop="deptCategory">
            <ElInput v-model="form.deptCategory" placeholder="请输入类别编码" />
          </ElFormItem>
        </ElCol>
        <!-- 显示排序 -->
        <ElCol :span="12">
          <ElFormItem label="显示排序" prop="orderNum">
            <ElInputNumber v-model="form.orderNum" controls-position="right" :min="0" />
          </ElFormItem>
        </ElCol>
        <!-- 负责人 -->
        <ElCol :span="12">
          <ElFormItem label="负责人" prop="leader">
            <ElSelect v-model="form.leader" placeholder="请选择负责人">
              <ElOption
                v-for="item in deptUserList"
                :key="item.userId"
                :label="item.userName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <!-- 联系电话 -->
        <ElCol :span="12">
          <ElFormItem label="联系电话" prop="phone">
            <ElInput v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
          </ElFormItem>
        </ElCol>
        <!-- 邮箱 -->
        <ElCol :span="12">
          <ElFormItem label="邮箱" prop="email">
            <ElInput v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
          </ElFormItem>
        </ElCol>
        <!-- 部门状态（使用字典值） -->
        <ElCol :span="12">
          <ElFormItem label="部门状态">
            <ElRadioGroup v-model="form.status">
              <ElRadio
                v-for="status in sys_normal_disable"
                :key="status.value"
                :label="status.value"
              >
                {{ status.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
      </ElRow>
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
  // 导入部门 API 和类型定义
  import {
    addDept,
    type DeptForm,
    type DeptTreeOption,
    type DeptVO,
    getDept,
    listDept,
    listDeptExcludeChild,
    updateDept
  } from '@/api/system/dept'
  // 导入用户 API 和类型定义
  import { listUserByDeptId, type UserVO } from '@/api/system/user'

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // 定义组件属性
  interface Props {
    visible: boolean
    editData?: DeptVO
    parentDept?: DeptVO
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
  const deptOptions = ref<DeptTreeOption[]>([]) // 部门树选项
  const deptUserList = ref<UserVO[]>([]) // 部门用户列表

  // reactive: 创建响应式表单对象
  const form = reactive<DeptForm>({
    deptId: undefined,
    parentId: undefined,
    deptName: '',
    deptCategory: '',
    orderNum: 0,
    leader: '',
    phone: '',
    email: '',
    status: '0'
  })

  // reactive: 创建表单校验规则
  const rules = reactive<FormRules>({
    deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
    email: [
      {
        type: 'email',
        message: '请输入正确的邮箱地址',
        trigger: ['blur', 'change']
      }
    ],
    phone: [
      {
        pattern: /^1[3456789][0-9]\d{8}$/,
        message: '请输入正确的手机号码',
        trigger: 'blur'
      }
    ]
  })

  // computed: 根据编辑模式动态生成弹窗标题
  const dialogTitle = computed(() => {
    return isEdit.value ? '修改部门' : '新增部门'
  })

  // computed: 计算需要展开的节点路径，从根节点到当前选中的上级部门
  const expandedKeys = computed(() => {
    if (!form.parentId || form.parentId === 0) {
      return []
    }
    const path: (string | number)[] = []
    const findPath = (depts: DeptTreeOption[], targetId: string | number): boolean => {
      for (const dept of depts) {
        if (String(dept.deptId) === String(targetId)) {
          return true
        }
        if (dept.children && findPath(dept.children, targetId)) {
          path.unshift(dept.deptId)
          return true
        }
      }
      return false
    }
    findPath(deptOptions.value, form.parentId)
    return path
  })

  // 构建部门树形结构
  const buildDeptTree = (depts: DeptTreeOption[], parentId: string | number): DeptTreeOption[] => {
    return depts
      .filter((dept) => String(dept.parentId) === String(parentId))
      .map((dept) => ({
        ...dept,
        children: buildDeptTree(depts, dept.deptId)
      }))
  }

  // 加载部门树数据
  const loadDeptTree = async () => {
    const data = await listDept()
    deptOptions.value = buildDeptTree(data, 0)
  }

  // 加载排除子部门的部门树数据（编辑时使用）
  const loadDeptTreeExcludeChild = async (deptId: string | number) => {
    const data = await listDeptExcludeChild(deptId)
    deptOptions.value = buildDeptTree(data, 0)
  }

  // 获取部门所有用户（用于负责人选择）
  const getDeptAllUser = async (deptId: any) => {
    if (deptId !== null && deptId !== '' && deptId !== undefined) {
      const data = await listUserByDeptId(deptId)
      deptUserList.value = data
    }
  }

  // 重置表单数据
  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(form, {
      deptId: undefined,
      parentId: undefined,
      deptName: '',
      deptCategory: '',
      orderNum: 0,
      leader: '',
      phone: '',
      email: '',
      status: '0'
    })
    deptUserList.value = []
  }

  // 加载编辑表单数据
  const loadFormData = async () => {
    if (!props.editData?.deptId) return
    isEdit.value = true
    getDeptAllUser(props.editData.deptId)
    const data = await getDept(props.editData.deptId)
    form.deptId = data.deptId
    form.parentId = data.parentId
    form.deptName = data.deptName
    form.deptCategory = data.deptCategory
    form.orderNum = data.orderNum
    form.leader = data.leader
    form.phone = data.phone
    form.email = data.email
    form.status = data.status
  }

  // 提交表单
  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (form.deptId) {
          // 修改部门
          updateDept(form).then(() => {
            ElMessage.success('修改成功')
            emit('success')
            handleCancel()
          })
        } else {
          // 新增部门
          addDept(form).then(() => {
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
        if (props.editData?.deptId) {
          // 编辑模式：加载排除子部门的树数据和表单数据
          await loadDeptTreeExcludeChild(props.editData.deptId)
          await loadFormData()
          // 加载当前部门的用户列表（用于负责人选择）
          await getDeptAllUser(props.editData.deptId)
        } else {
          // 新增模式：加载部门树数据
          await loadDeptTree()
          if (props.parentDept) {
            // 设置上级部门（新增子部门）
            form.parentId = props.parentDept.deptId
            // 加载上级部门的用户列表（用于负责人选择）
            await getDeptAllUser(props.parentDept.deptId)
          }
        }
      }
    }
  )

  // watch: 监听上级部门变化，自动加载对应部门的用户列表
  watch(
    () => form.parentId,
    async (newDeptId) => {
      if (newDeptId && newDeptId !== 0) {
        await getDeptAllUser(newDeptId)
      } else {
        deptUserList.value = []
      }
    }
  )
</script>
