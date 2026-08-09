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
  import type { UserQuery } from '@/api/system/user'
  import { useDict } from '@utils/dict'

  interface Props {
    modelValue: UserQuery
  }

  interface Emits {
    (e: 'update:modelValue', value: UserQuery): void
    (e: 'search', params: UserQuery): void
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
      label: '用户名称',
      key: 'userName',
      type: 'input',
      placeholder: '请输入用户名称',
      clearable: true
    },
    {
      label: '用户昵称',
      key: 'nickName',
      type: 'input',
      placeholder: '请输入用户昵称',
      clearable: true
    },
    {
      label: '手机号码',
      key: 'phoneNumber',
      type: 'input',
      placeholder: '请输入手机号码',
      clearable: true,
      props: { maxlength: '11' }
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

  const handleSearch = (params: UserQuery) => {
    emit('search', params)
  }
</script>
