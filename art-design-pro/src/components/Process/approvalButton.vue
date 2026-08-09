<template>
  <div class="approval-button">
    <!-- 暂存按钮：新增或编辑（草稿/取消/驳回状态）时显示 -->
    <ElButton v-if="showSubmit" type="info" @click="emit('submitForm', 'draft')">暂存</ElButton>
    <!-- 提交按钮：新增或编辑（草稿/取消/驳回状态）时显示 -->
    <ElButton v-if="showSubmit" type="primary" @click="emit('submitForm', 'submit')">提交</ElButton>
    <!-- 审批按钮：审批模式且流程状态为 waiting 时显示 -->
    <ElButton v-if="showApproval" type="primary" @click="emit('approvalVerifyOpen')">审批</ElButton>
    <!-- 流程进度按钮：有ID且非草稿状态时显示 -->
    <ElButton v-if="showProgress" @click="emit('handleApprovalRecord')">流程进度</ElButton>
    <ElButton @click="emit('back')">返回</ElButton>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElButton } from 'element-plus'

  const props = withDefaults(
    defineProps<{
      /** add: 新增 / update: 编辑 / approval: 审批 / view: 查看 */
      pageType?: 'add' | 'update' | 'approval' | 'view'
      /** 流程状态（draft/cancel/back/waiting/finish 等） */
      flowStatus?: string
      /** 业务ID（有值时表示已存在的单据） */
      id?: string | number
    }>(),
    { pageType: 'add' }
  )

  const emit = defineEmits<{
    /** 提交表单：status 为 draft（暂存）或 submit（提交） */
    submitForm: [status: 'draft' | 'submit']
    approvalVerifyOpen: []
    handleApprovalRecord: []
    back: []
  }>()

  /** 是否显示暂存/提交按钮：新增或编辑（草稿/取消/驳回状态） */
  const showSubmit = computed(
    () =>
      props.pageType === 'add' ||
      (props.pageType === 'update' &&
        !!props.flowStatus &&
        (props.flowStatus === 'draft' ||
          props.flowStatus === 'cancel' ||
          props.flowStatus === 'back'))
  )

  /** 是否显示审批按钮：审批模式且流程状态为 waiting */
  const showApproval = computed(
    () => props.pageType === 'approval' && props.flowStatus === 'waiting'
  )

  /** 是否显示流程进度按钮：有ID且非草稿状态 */
  const showProgress = computed(
    () => !!props.id && props.flowStatus !== 'draft' && props.flowStatus !== undefined
  )
</script>

<style lang="scss" scoped>
  .approval-button {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
    margin-top: 16px;
  }
</style>
