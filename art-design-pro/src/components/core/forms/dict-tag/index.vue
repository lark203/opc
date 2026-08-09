<template>
  <ElTag
    v-if="option"
    :type="(option.elTagType as 'success' | 'info' | 'primary' | 'warning' | 'danger') || 'primary'"
    size="small"
    effect="plain"
    :class="option.elTagClass"
  >
    {{ option.label }}
  </ElTag>
  <span v-else>{{ value || '-' }}</span>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElTag } from 'element-plus'
  import type { DictDataOption } from '@/api/system/dict'

  const props = defineProps<{
    options: DictDataOption[]
    value: string | number | undefined
  }>()

  const option = computed(() => {
    if (!props.value && props.value !== 0) return undefined
    return props.options.find((item) => String(item.value) === String(props.value))
  })
</script>
