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
  // 导入 Vue 组合式 API
  import { computed, reactive, ref, toRefs } from 'vue'
  // 导入搜索栏组件
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  // 导入字典工具函数
  import { useDict } from '@/utils/dict'
  // 导入部门类型定义
  import type { DeptQuery } from '@/api/system/dept'

  // 定义组件事件
  const emit = defineEmits<{
    (e: 'search', params: DeptQuery): void
    (e: 'reset'): void
  }>()

  // ref: 创建搜索栏引用
  const searchBarRef = ref()

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // reactive: 创建响应式搜索表单对象
  let formData = reactive<DeptQuery>({
    deptName: '',
    deptCategory: '',
    status: ''
  })

  // reactive: 创建表单校验规则（当前为空）
  const rules = reactive({})

  // computed: 动态生成搜索栏配置项
  const formItems = computed(() => [
    {
      label: '部门名称',
      key: 'deptName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入部门名称' }
    },
    {
      label: '类别编码',
      key: 'deptCategory',
      type: 'input',
      props: { clearable: true, placeholder: '请输入类别编码' }
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

  // 搜索按钮点击事件
  const handleSearch = () => {
    emit('search', { ...formData })
  }

  // 重置按钮点击事件
  const handleReset = () => {
    formData.deptName = ''
    formData.deptCategory = ''
    formData.status = ''
    emit('reset')
  }
</script>
