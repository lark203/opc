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
  import type { RoleQuery } from '@/api/system/role'
  import { useDict } from '@utils/dict'

  interface Props {
    modelValue: RoleQuery
  }

  interface Emits {
    (e: 'update:modelValue', value: RoleQuery): void
    (e: 'search', params: RoleQuery): void
    (e: 'reset'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  const searchBarRef = ref()

  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const rules = {}

  const formItems = computed(() => [
    {
      label: '角色名称',
      key: 'roleName',
      type: 'input',
      placeholder: '请输入角色名称',
      clearable: true
    },
    {
      label: '权限字符',
      key: 'roleKey',
      type: 'input',
      placeholder: '请输入权限字符',
      clearable: true
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        options: sys_normal_disable.value || [],
        clearable: true
      }
    }
  ])

  const handleReset = () => {
    emit('reset')
  }

  const handleSearch = (params: RoleQuery) => {
    emit('search', params)
  }
</script>
