<template>
  <ElDialog
    :model-value="visible"
    title="审批记录"
    width="80%"
    :close-on-click-modal="false"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
  >
    <ElTabs v-model="activeTab">
      <ElTabPane v-loading="loading" label="流程图" name="chart">
        <FlowChart v-if="instanceId" :instance-id="instanceId" />
      </ElTabPane>
      <ElTabPane v-loading="loading" label="审批信息" name="info">
        <ElTable :data="recordList" border style="width: 100%">
          <ElTableColumn type="index" label="序号" align="center" width="60" />
          <ElTableColumn prop="nodeName" label="任务名称" align="center" />
          <ElTableColumn label="办理人" align="center" min-width="120">
            <template #default="{ row }">
              <UserNameDisplay :names="row.approverName" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="状态" width="100" align="center">
            <template #default="{ row }">
              <DictTag :options="wf_task_status" :value="row.flowStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="message" label="审批意见" align="center" show-overflow-tooltip />
          <ElTableColumn
            prop="createTime"
            label="开始时间"
            width="160"
            align="center"
            show-overflow-tooltip
          />
          <ElTableColumn
            prop="updateTime"
            label="结束时间"
            width="160"
            align="center"
            show-overflow-tooltip
          />
          <ElTableColumn
            prop="runDuration"
            label="运行时长"
            width="140"
            align="center"
            show-overflow-tooltip
          />
        </ElTable>
      </ElTabPane>
    </ElTabs>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import { ElDialog, ElTable, ElTableColumn, ElTabPane, ElTabs } from 'element-plus'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useDict } from '@/utils/dict'
  import type { FlowHisTaskVO } from '@/api/workflow/instance'
  import { flowHisTaskList } from '@/api/workflow/instance'
  import FlowChart from './flowChart.vue'
  import UserNameDisplay from './UserNameDisplay.vue'

  defineProps<{ visible: boolean }>()

  const emit = defineEmits<{
    'update:visible': [val: boolean]
  }>()

  const { wf_task_status } = useDict('wf_task_status')

  const activeTab = ref('chart')
  const loading = ref(false)
  const recordList = ref<FlowHisTaskVO[]>([])
  const instanceId = ref<string | number>('')

  /** 初始化审批记录（对外暴露，通过业务ID加载） */
  const init = async (businessId: string | number) => {
    emit('update:visible', true)
    loading.value = true
    activeTab.value = 'chart'
    recordList.value = []
    instanceId.value = ''
    try {
      const res = await flowHisTaskList(businessId)
      recordList.value = res?.list || []
      instanceId.value = res?.instanceId || businessId
    } finally {
      loading.value = false
    }
  }

  defineExpose({ init })
</script>
