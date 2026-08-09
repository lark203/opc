<template>
  <span class="user-name-display" :title="text">
    {{ text }}
  </span>
</template>

<script setup lang="ts">
  import { computed } from 'vue'

  const props = withDefaults(
    defineProps<{
      /** 后端已翻译的名称串，如 "张三,李四" */
      names?: string
      /** 兜底值 */
      fallback?: string
    }>(),
    { names: '', fallback: '-' }
  )

  const text = computed(() => (props.names && props.names.trim() ? props.names : props.fallback))
</script>

<style lang="scss" scoped>
  .user-name-display {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: bottom;
  }
</style>
