<template>
  <div class="art-full-height flex flex-col gap-3">
    <!-- 顶部操作按钮 -->
    <ElCard shadow="never">
      <ApprovalButton
        :page-type="pageType"
        :flow-status="form.status"
        :id="form.id"
        @submit-form="handleSubmitForm"
        @approval-verify-open="handleApprovalVerifyOpen"
        @handle-approval-record="handleApprovalRecord"
        @back="handleBack"
      />
    </ElCard>

    <!-- 表单区域 -->
    <ElCard shadow="never" class="flex-1 min-h-0">
      <ElForm
        ref="leaveFormRef"
        v-loading="loading"
        :model="form"
        :rules="rules"
        :disabled="pageType === 'view'"
        label-width="100px"
      >
        <!-- 流程定义：仅新增时可选择，其他状态只读展示 -->
        <ElFormItem label="流程定义">
          <ElSelect
            v-model="flowCode"
            placeholder="选择流程定义"
            :disabled="pageType !== 'add'"
            style="width: 100%"
          >
            <ElOption
              v-for="item in flowCodeOptions"
              :key="item.flowCode"
              :label="item.flowName"
              :value="item.flowCode"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="请假类型" prop="leaveType">
          <ElSelect v-model="form.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <ElOption
              v-for="item in leaveTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="请假时间" required>
          <ElDatePicker
            v-model="leaveTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            style="width: 100%"
            @change="changeLeaveTime"
          />
        </ElFormItem>
        <ElFormItem label="请假天数" prop="leaveDays">
          <ElInput v-model="form.leaveDays" disabled type="number" placeholder="请假天数" />
        </ElFormItem>
        <ElFormItem label="请假原因" prop="remark">
          <ElInput v-model="form.remark" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </ElFormItem>
      </ElForm>
    </ElCard>

    <!-- 审批组件 -->
    <SubmitVerify
      v-model:visible="submitVerifyVisible"
      ref="submitVerifyRef"
      :task-variables="taskVariables"
      @success="handleSubmitSuccess"
    />

    <!-- 审批记录 -->
    <ApprovalRecord ref="approvalRecordRef" v-model:visible="approvalRecordVisible" />
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import {
    ElCard,
    ElDatePicker,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElOption,
    ElSelect,
    type FormInstance,
    type FormRules
  } from 'element-plus'
  import ApprovalButton from '@/components/Process/approvalButton.vue'
  import ApprovalRecord from '@/components/Process/approvalRecord.vue'
  import SubmitVerify from '@/components/Process/submitVerify.vue'
  import { startWorkFlow } from '@/api/workflow/task'
  import { getInstanceByBusinessId } from '@/api/workflow/instance'
  import { type FlowDefinitionVO, listDefinition } from '@/api/workflow/definition'
  import {
    addLeave,
    getLeave,
    type LeaveForm,
    type StartProcessBo,
    updateLeave
  } from '@/api/workflow/leave'

  const route = useRoute()
  const router = useRouter()

  /** 请假类型选项 */
  const leaveTypeOptions = [
    { value: '1', label: '事假' },
    { value: '2', label: '调休' },
    { value: '3', label: '病假' },
    { value: '4', label: '婚假' }
  ]

  /** 已发布流程定义列表（从后端获取） */
  const flowCodeOptions = ref<FlowDefinitionVO[]>([])
  /** 流程定义编码 */
  const flowCode = ref<string>('')

  /** 页面类型：add 新增 / update 编辑 / view 查看 / approval 审批 */
  const pageType = ref<string>('add')
  /** 请假时间范围（开始/结束） */
  const leaveTime = ref<string[]>([])
  /** 表单加载状态 */
  const loading = ref(false)
  /** 按钮加载状态 */
  const buttonLoading = ref(false)

  /** 审批弹窗状态 */
  const submitVerifyVisible = ref(false)
  const submitVerifyRef = ref<InstanceType<typeof SubmitVerify>>()
  /** 审批记录弹窗状态 */
  const approvalRecordVisible = ref(false)
  const approvalRecordRef = ref<InstanceType<typeof ApprovalRecord>>()

  /** 表单引用 */
  const leaveFormRef = ref<FormInstance>()

  /** 流程变量（传给 SubmitVerify 组件） */
  const taskVariables = ref<Record<string, unknown>>({})

  /** 启动流程参数 */
  const submitFormData = ref<StartProcessBo>({
    businessId: '',
    flowCode: '',
    variables: {},
    bizExt: {}
  })

  /** 表单数据 */
  const form = ref<LeaveForm>({})

  /** 判断是否可编辑（草稿/取消/驳回状态可重新提交） */
  /*  const isEditable = computed(
    () =>
      form.value.status === 'draft' ||
      form.value.status === 'cancel' ||
      form.value.status === 'back'
  )*/

  /** 表单校验规则 */
  const rules: FormRules = {
    leaveType: [{ required: true, message: '请假类型不能为空', trigger: 'change' }],
    leaveDays: [{ required: true, message: '请假天数不能为空', trigger: 'blur' }],
    remark: [{ required: true, message: '请假原因不能为空', trigger: 'blur' }]
  }

  /** 加载已发布的流程定义列表 */
  const loadFlowCodeOptions = async () => {
    const res = await listDefinition({ isPublish: 1, pageNum: 1, pageSize: 100 })
    flowCodeOptions.value = res?.rows || []
    // 未设置流程定义编码时默认选中第一个
    if (flowCodeOptions.value.length > 0 && !flowCode.value) {
      flowCode.value = flowCodeOptions.value[0].flowCode
    }
  }

  /** 计算请假天数（根据时间范围） */
  const changeLeaveTime = () => {
    if (!leaveTime.value || leaveTime.value.length < 2) return
    const startDate = new Date(leaveTime.value[0]).getTime()
    const endDate = new Date(leaveTime.value[1]).getTime()
    const diffInMilliseconds = endDate - startDate
    form.value.leaveDays = Math.floor(diffInMilliseconds / (1000 * 60 * 60 * 24)) + 1
  }

  /** 获取请假详情（同时通过业务ID查询关联的流程实例，回填流程定义编码） */
  const getInfo = async () => {
    loading.value = true
    try {
      const id = route.query.id as string
      const res = await getLeave(id)
      form.value = res
      leaveTime.value = [form.value.startDate || '', form.value.endDate || '']
      // 通过业务ID获取流程实例，回填流程定义编码
      try {
        const instance = await getInstanceByBusinessId(id)
        if (instance?.flowCode) {
          flowCode.value = instance.flowCode
        }
      } catch {
        // 草稿状态无流程实例，忽略错误
      }
    } finally {
      loading.value = false
    }
  }

  /** 提交表单（暂存或提交并发起流程） */
  const handleSubmitForm = (status: 'draft' | 'submit') => {
    if (!leaveTime.value || leaveTime.value.length === 0) {
      ElMessage.error('请假时间不能为空')
      return
    }
    leaveFormRef.value?.validate(async (valid: boolean) => {
      if (!valid) return
      buttonLoading.value = true
      try {
        form.value.startDate = leaveTime.value[0]
        form.value.endDate = leaveTime.value[1]
        let res: LeaveForm
        if (form.value.id) {
          res = await updateLeave(form.value)
        } else {
          res = await addLeave(form.value)
        }
        form.value = res
        // 暂存：只保存不发起流程
        if (status === 'draft') {
          ElMessage.success('暂存成功')
          handleBack()
        } else {
          // 提交：保存并发起流程
          await handleStartWorkFlow(res)
        }
      } finally {
        buttonLoading.value = false
      }
    })
  }

  /** 发起流程 */
  const handleStartWorkFlow = async (data: LeaveForm) => {
    submitFormData.value.flowCode = flowCode.value
    submitFormData.value.businessId = data.id as string | number
    // 流程变量（leave2/6 使用 leaveDays，leave4/5 使用 userList）
    taskVariables.value = {
      leaveDays: data.leaveDays,
      userList: ['1', '3', '4']
    }
    // 业务扩展字段
    submitFormData.value.bizExt = {
      businessTitle: '请假申请',
      businessCode: data.applyCode
    }
    submitFormData.value.variables = taskVariables.value
    const resp = await startWorkFlow(submitFormData.value)
    if (submitVerifyRef.value && resp) {
      submitVerifyRef.value.openDialog(resp.taskId)
    }
  }

  /** 打开审批弹窗（审批模式） */
  const handleApprovalVerifyOpen = () => {
    const taskId = route.query.taskId as string
    submitVerifyRef.value?.openDialog(taskId)
  }

  /** 查看审批记录 */
  const handleApprovalRecord = () => {
    const id = (route.query.id as string) || (form.value.id as string)
    approvalRecordRef.value?.init(id)
  }

  /** 提交成功回调 */
  const handleSubmitSuccess = () => {
    handleBack()
  }

  /** 返回上一页 */
  const handleBack = () => {
    router.go(-1)
  }

  onMounted(() => {
    nextTick(async () => {
      pageType.value = (route.query.type as string) || 'add'
      form.value = {}
      leaveTime.value = []
      loading.value = false
      // 始终加载已发布流程定义列表（用于展示流程定义名称）
      await loadFlowCodeOptions()
      if (
        pageType.value === 'update' ||
        pageType.value === 'view' ||
        pageType.value === 'approval'
      ) {
        await getInfo()
      }
    })
  })
</script>
