<!-- 字典类型搜索组件 -->
<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    :span="8"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  // 导入依赖
  import { computed, ref } from 'vue'
  import type { DictTypeQuery } from '@/api/system/dict'

  // 定义Props
  interface Props {
    modelValue: DictTypeQuery
  }

  // 定义Emits
  interface Emits {
    (e: 'update:modelValue', value: DictTypeQuery): void
    (e: 'search', params: DictTypeQuery): void
    (e: 'reset'): void
  }

  // 获取Props和Emits
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 搜索栏引用
  const searchBarRef = ref()

  // 表单数据双向绑定
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 表单验证规则
  const rules = {}

  // 表单配置项
  const formItems = computed(() => [
    {
      label: '字典名称',
      key: 'dictName',
      type: 'input',
      placeholder: '请输入字典名称',
      clearable: true
    },
    {
      label: '字典类型',
      key: 'dictType',
      type: 'input',
      placeholder: '请输入字典类型',
      clearable: true
    }
  ])

  /**
   * 处理重置事件
   */
  const handleReset = () => {
    emit('reset')
  }

  /**
   * 处理搜索事件
   */
  const handleSearch = async (params: DictTypeQuery) => {
    // 可选：验证表单
    // await searchBarRef.value?.validate()
    emit('search', params)
  }
</script>
