<template>
  <ElDialog v-model="isVisible" title="操作日志详情" width="40%" align-center>
    <ElTabs v-model="activeTab">
      <ElTabPane label="基本信息" name="base">
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="日志编号">{{ form.operId || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="系统模块">{{ form.title || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="操作类型">
            <DictTag :options="sys_oper_type" :value="form.businessType" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="操作人员">{{ form.operName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="所属部门">{{ form.deptName || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="客户端">{{ form.clientKey || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="设备类型">
            <DictTag :options="sys_device_type" :value="form.deviceType" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="浏览器">{{ form.browser || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="操作系统">{{ form.os || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="操作地址">{{ form.operIp || '-' }}</ElDescriptionsItem>
          <ElDescriptionsItem label="操作状态">
            <DictTag :options="sys_common_status" :value="form.status" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="消耗时间">{{
            form.costTime ? form.costTime + '毫秒' : '-'
          }}</ElDescriptionsItem>
          <ElDescriptionsItem label="操作时间" :span="2">{{
            form.operTime || '-'
          }}</ElDescriptionsItem>
        </ElDescriptions>
      </ElTabPane>
      <ElTabPane label="请求参数" name="param">
        <pre class="code-block" v-html="operParamHtml || '无'"></pre>
      </ElTabPane>
      <ElTabPane label="响应结果" name="result">
        <pre class="code-block" v-html="jsonResultHtml || '无'"></pre>
      </ElTabPane>
      <ElTabPane label="异常信息" name="error">
        <pre class="code-block error" v-html="errorMsgHtml || '无'"></pre>
      </ElTabPane>
    </ElTabs>
    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleClose">关 闭</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, toRefs } from 'vue'
  import type { OperLogVO } from '@/api/monitor/operlog'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useDict } from '@/utils/dict'
  import hljs from 'highlight.js'
  import 'highlight.js/styles/github.min.css'

  const isVisible = ref(false)
  const activeTab = ref('base')
  const form = reactive<OperLogVO>({} as OperLogVO)

  const { sys_oper_type, sys_common_status, sys_device_type } = toRefs(
    useDict('sys_oper_type', 'sys_common_status', 'sys_device_type')
  )

  const formatJson = (jsonString: string): string => {
    if (!jsonString) return ''
    try {
      const obj = JSON.parse(jsonString)
      return JSON.stringify(obj, null, 2)
    } catch {
      return jsonString
    }
  }

  const highlightJson = (jsonString: string): string => {
    const formatted = formatJson(jsonString)
    if (!formatted) return ''
    return hljs.highlight(formatted, { language: 'json' }).value
  }

  const operParamHtml = computed(() => highlightJson(form.operParam))
  const jsonResultHtml = computed(() => highlightJson(form.jsonResult))
  const errorMsgHtml = computed(() => {
    if (!form.errorMsg) return ''
    if (form.errorMsg.startsWith('{') || form.errorMsg.startsWith('[')) {
      return highlightJson(form.errorMsg)
    }
    return form.errorMsg
  })

  const openDialog = (data: OperLogVO) => {
    Object.assign(form, data)
    activeTab.value = 'base'
    isVisible.value = true
  }

  const handleClose = () => {
    isVisible.value = false
  }

  defineExpose({ openDialog })
</script>

<style lang="scss" scoped>
  .code-block {
    max-height: 400px;
    padding: 12px;
    overflow: auto;
    font-size: 12px;
    color: var(--el-text-color-regular);
    background: var(--el-bg-color-page);
    border-radius: 6px;

    &.error {
      color: var(--el-color-danger);
    }
  }
</style>
