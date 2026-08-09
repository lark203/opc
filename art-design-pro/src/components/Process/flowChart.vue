<template>
  <div class="flow-chart">
    <ElEmpty v-if="!src" description="暂无流程图" />
    <iframe v-else :src="src" frameborder="0" class="flow-chart-iframe" />
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { buildWarmFlowUrl } from '@/api/workflow/workflowCommon'

  const props = withDefaults(
    defineProps<{
      /** 流程实例 id */
      instanceId?: string | number
      /** 流程定义 id（设计/流程图查看二选一） */
      definitionId?: string | number
      /** 仅设计模式 */
      onlyDesignShow?: boolean
    }>(),
    { onlyDesignShow: true }
  )

  const src = computed(() => {
    if (props.instanceId) {
      return buildWarmFlowUrl('index.html', {
        type: 'FlowChart',
        id: String(props.instanceId)
      })
    }
    if (props.definitionId) {
      return buildWarmFlowUrl('index.html', {
        id: String(props.definitionId),
        onlyDesignShow: props.onlyDesignShow ? 'true' : 'false'
      })
    }
    return ''
  })
</script>

<style lang="scss" scoped>
  .flow-chart {
    width: 100%;

    .flow-chart-iframe {
      width: 100%;
      height: 60vh;
      border: 0;
      border-radius: 8px;
      background: var(--el-bg-color-page);
    }
  }
</style>
