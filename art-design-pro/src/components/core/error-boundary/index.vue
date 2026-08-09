<!-- 全局错误边界 -->
<!--
  作用：
  1. 捕获其子孙组件（包括动态路由页面）渲染/setup 阶段抛出的未捕获异常，
     避免异常一路冒泡到根节点导致整棵应用被卸载（整屏白屏，且后续所有菜单都空白）。
  2. 在出错区域就地展示真实错误信息（组件名 + 报错 + 堆栈），
     便于快速定位，而不是留下一个无法排查的空白页。
-->
<template>
  <div v-if="error" class="route-error-boundary">
    <ElResult
      icon="warning"
      title="页面渲染出错"
      sub-title="该页面抛出异常，已被错误边界捕获，不会影响其它功能。"
    >
      <template #extra>
        <div class="route-error-detail">
          <p v-if="errorComponent" class="route-error-component">
            出错组件：{{ errorComponent }}
          </p>
          <pre class="route-error-message">{{ errorMessage }}</pre>
          <pre v-if="errorStack" class="route-error-stack">{{ errorStack }}</pre>
          <ElButton type="primary" @click="reload">重新加载页面</ElButton>
        </div>
      </template>
    </ElResult>
  </div>
  <template v-else>
    <slot />
  </template>
</template>

<script setup lang="ts">
  import { onErrorCaptured, ref } from 'vue'

  interface ErrorInfo {
    message: string
    stack?: string
    component?: string
  }

  const error = ref<ErrorInfo | null>(null)
  const errorComponent = ref('')
  const errorMessage = ref('')
  const errorStack = ref('')

  /**
   * 捕获子孙组件未处理的异常
   * 返回 false 阻止异常继续冒泡到 app.config.errorHandler / 根节点，
   * 从而避免整棵应用被卸载（这是"点一个菜单后全站白屏"的根因）。
   */
  onErrorCaptured((err: unknown, instance: any, info: string) => {
    const raw = err instanceof Error ? err : new Error(String(err))
    error.value = {
      message: raw.message,
      stack: raw.stack,
      component: info
    }
    errorComponent.value = info
    errorMessage.value = raw.message
    errorStack.value = raw.stack || ''
    // false：停止向上传播，防止白屏传染到整个后台
    return false
  })

  const reload = (): void => {
    error.value = null
    errorComponent.value = ''
    errorMessage.value = ''
    errorStack.value = ''
    window.location.reload()
  }
</script>

<style scoped>
  .route-error-boundary {
    padding: 24px;
  }

  .route-error-detail {
    max-width: 100%;
    text-align: left;
  }

  .route-error-component {
    font-weight: 600;
    color: var(--el-color-danger);
  }

  .route-error-message,
  .route-error-stack {
    max-height: 240px;
    padding: 12px;
    margin: 8px 0 16px;
    overflow: auto;
    font-size: 12px;
    line-height: 1.6;
    word-break: break-all;
    white-space: pre-wrap;
    background: var(--el-fill-color-light);
    border-radius: 6px;
  }
</style>
