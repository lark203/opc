<template>
  <div class="art-full-height">
    <!-- ArtSearchBar: 搜索栏组件，通过 v-model 双向绑定搜索表单 -->
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />

    <!-- ElCard: 卡片容器，包裹表格，art-table-card 实现 flex 自适应高度 -->
    <ElCard class="flex flex-col flex-1 min-h-0 art-table-card">
      <!-- ArtTableHeader: 表格头部组件，包含操作按钮区、列配置和刷新功能 -->
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
        <template #left>
          <!-- ElSpace: 按钮间距容器，wrap 属性支持自动换行适配窄屏 -->
          <ElSpace wrap>
            <!-- 新增按钮，v-auth 控制权限（workflow:spel:add） -->
            <ElButton type="primary" v-auth="'workflow:spel:add'" @click="showDialog('add')">
              新增
            </ElButton>
            <!-- 修改按钮，需选中单行才能操作 -->
            <ElButton
              type="success"
              v-auth="'workflow:spel:edit'"
              :disabled="selectedRows.length !== 1"
              @click="showDialog('edit')"
            >
              修改
            </ElButton>
            <!-- 删除按钮，需选中至少一行才能操作 -->
            <ElButton
              type="danger"
              v-auth="'workflow:spel:remove'"
              :disabled="selectedRows.length === 0"
              @click="handleDelete()"
            >
              删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <!-- ArtTable: 表格组件，支持分页、选择、自定义列渲染 -->
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <!-- SpelDialog: 新增/编辑流程表达式弹窗组件 -->
    <SpelDialog
      v-model:visible="dialogVisible"
      :edit-data="currentSpelData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  // 导入 Vue 组合式 API：computed 用于计算属性，h 用于渲染函数，reactive/ref 用于响应式数据，toRefs 用于解构字典
  import { computed, h, reactive, ref, toRefs } from 'vue'
  // 导入 Element Plus 消息提示和确认框组件
  import { ElMessage, ElMessageBox } from 'element-plus'
  // 导入表格操作按钮组件（内置权限控制和悬停提示）
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  // 导入字典标签组件，用于渲染状态列
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  // 导入表格数据管理 hook，自动处理分页、请求、加载状态等
  import { useTable } from '@/hooks/core/useTable'
  // 导入流程表达式弹窗组件
  import SpelDialog from './modules/spel-dialog.vue'
  // 导入字典工具函数，获取 sys_normal_disable 字典（正常/停用状态）
  import { useDict } from '@/utils/dict'
  // 导入流程表达式 API 和类型定义
  import { delSpel, type FlowSpelQuery, type FlowSpelVO, listSpel } from '@/api/workflow/spel'

  // 使用字典工具函数获取 sys_normal_disable 字典（0=正常，1=停用）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // ========================= 响应式状态定义 =========================

  const dialogVisible = ref(false) // 弹窗显示状态
  const currentSpelData = ref<FlowSpelVO>() // 当前编辑的表达式数据
  const selectedRows = ref<FlowSpelVO[]>([]) // 表格选中的行数据

  // 搜索表单，reactive 创建响应式对象
  let searchForm = reactive<FlowSpelQuery>({
    componentName: '',
    methodName: '',
    status: ''
  })

  // ========================= 搜索栏配置 =========================

  // computed: 动态生成搜索栏配置项，使用 computed 以响应字典数据异步加载
  const formItems = computed(() => [
    {
      label: '组件名称',
      key: 'componentName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入组件名称' }
    },
    {
      label: '方法名',
      key: 'methodName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入方法名' }
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

  // ========================= 表格配置 =========================

  // useTable: 表格数据管理 hook，封装分页、请求、列配置等功能
  // 仅选择列和操作列设置 width，其他列不设置宽度（遵循功能实现准则第10条）
  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      // API 请求函数
      apiFn: listSpel,
      // 请求参数：分页参数 + 搜索条件
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      // 分页字段映射：后端使用 pageNum/pageSize
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      // 列配置工厂函数
      columnsFactory: () => [
        // 选择列，固定宽度 50px
        { type: 'selection', width: 50 },
        // 全局序号列，自动根据分页计算序号
        { type: 'globalIndex', label: '序号' },
        // 组件名称列，不设置宽度自动分配
        { prop: 'componentName', label: '组件名称' },
        // 方法名称列
        { prop: 'methodName', label: '方法名称' },
        // 参数名称列
        { prop: 'methodParams', label: '参数名称' },
        // SPEL表达式列
        { prop: 'viewSpel', label: 'SPEL表达式' },
        // 状态列，使用 DictTag 组件渲染字典标签
        {
          prop: 'status',
          label: '状态',
          formatter: (row: FlowSpelVO) =>
            h(DictTag, { options: sys_normal_disable.value, value: row.status })
        },
        // 备注列
        { prop: 'remark', label: '备注' },
        // 操作列，固定宽度 140px，固定在右侧
        {
          prop: 'operation',
          label: '操作',
          width: 140,
          fixed: 'right',
          align: 'center',
          // formatter: 使用 ArtButtonTable 渲染操作按钮，内置权限控制
          formatter: (row: FlowSpelVO) => {
            return h('div', [
              // 编辑按钮，权限码 workflow:spel:edit
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'workflow:spel:edit',
                onClick: () => showDialog('edit', row)
              }),
              // 删除按钮，权限码 workflow:spel:remove
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'workflow:spel:remove',
                onClick: () => handleDelete(row)
              })
            ])
          }
        }
      ]
    }
  })

  // ========================= 搜索与重置 =========================

  // 搜索按钮回调：替换搜索参数并重新请求数据
  const handleSearch = () => {
    replaceSearchParams(searchForm)
    getData()
  }

  // 重置按钮回调：清空搜索条件并重新加载
  const handleReset = () => {
    searchForm.componentName = ''
    searchForm.methodName = ''
    searchForm.status = ''
    resetSearchParams()
    getData()
  }

  // ========================= 表格选择 =========================

  // 表格选中状态变化回调
  const handleSelectionChange = (selection: FlowSpelVO[]) => {
    selectedRows.value = selection
  }

  // ========================= 弹窗操作 =========================

  // 打开弹窗（新增/编辑）
  // type: 'add' 新增 | 'edit' 编辑
  // row: 编辑时传入当前行数据
  const showDialog = (type: 'add' | 'edit', row?: FlowSpelVO) => {
    currentSpelData.value = type === 'edit' ? row : undefined
    dialogVisible.value = true
  }

  // 删除表达式
  // row: 单行删除时传入当前行；不传则批量删除选中行
  const handleDelete = async (row?: FlowSpelVO) => {
    // 获取要删除的 ID：单行取 row.id，批量取选中行 ID 拼接
    const ids = row?.id || selectedRows.value.map((r) => r.id).join(',')
    if (!ids) return
    try {
      await ElMessageBox.confirm(`确定要删除编号为"${ids}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delSpel(ids)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      // 用户点击取消时 error 为 'cancel'，不显示错误提示
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // 页面加载时获取数据
  getData()
</script>
