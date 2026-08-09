<template>
  <div class="dept-page art-full-height">
    <!-- ArtSearchBar: 搜索栏组件，使用 computed 动态生成搜索项 -->
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />
    <!-- ElCard: 卡片容器，包裹表格 -->
    <ElCard class="art-table-card">
      <!-- ArtTableHeader: 表格头部组件，包含新增、展开/收起按钮和刷新功能 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <!-- ElSpace: 按钮间距容器 -->
          <ElSpace wrap>
            <ElButton type="primary" v-auth="'system:dept:add'" @click="() => handleAdd()"
              >新增</ElButton
            >
            <ElButton @click="toggleExpand">{{ isExpanded ? '收起' : '展开' }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <!-- ArtTable: 表格组件，支持树形结构 -->
      <ArtTable
        ref="tableRef"
        row-key="deptId"
        :loading="loading"
        :columns="columns"
        :data="deptList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />
    </ElCard>
    <!-- DeptDialog: 部门新增/编辑弹窗 -->
    <DeptDialog
      v-model:visible="dialogVisible"
      :edit-data="currentDept"
      :parent-dept="parentDept"
      @success="handleRefresh"
    />
  </div>
</template>

<script setup lang="ts">
  // 导入 Vue 组合式 API
  import { computed, h, nextTick, reactive, ref, toRefs } from 'vue'
  // 导入 Element Plus 组件和消息提示
  import { ElMessage, ElMessageBox } from 'element-plus'
  // 导入表格按钮组件
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  // 导入字典标签组件
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  // 导入表格列管理 hook
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  // 导入部门对话框组件
  import DeptDialog from './modules/dept-dialog.vue'
  // 导入字典工具函数
  import { useDict } from '@/utils/dict'
  // 导入部门 API 和类型定义
  import { delDept, type DeptQuery, type DeptVO, listDept } from '@/api/system/dept'

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // ref: 创建响应式变量
  const loading = ref(false) // 加载状态
  const isExpanded = ref(false) // 树形展开状态
  const tableRef = ref() // 表格引用

  // 弹窗相关变量
  const dialogVisible = ref(false) // 弹窗显示状态
  const currentDept = ref<DeptVO>() // 当前编辑的部门数据
  const parentDept = ref<DeptVO>() // 上级部门数据（用于新增子部门）

  // reactive: 创建响应式搜索表单对象
  let searchForm = reactive<DeptQuery>({
    deptName: '',
    deptCategory: '',
    status: ''
  })

  // ref: 创建部门列表数据
  const deptList = ref<DeptVO[]>([])

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

  // useTableColumns: 创建表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'deptName',
      label: '部门名称',
      minWidth: 260
    },
    { prop: 'deptCategory', label: '类别编码', width: 200, align: 'center' },
    { prop: 'orderNum', label: '排序', width: 100, align: 'center' },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      align: 'center',
      formatter: (row: DeptVO) =>
        h(DictTag, { options: sys_normal_disable.value, value: row.status })
    },
    { prop: 'createTime', label: '创建时间', width: 200, align: 'center' },
    {
      prop: 'operation',
      label: '操作',
      width: 180,
      fixed: 'right',
      align: 'center',
      formatter: (row: DeptVO) => {
        return h('div', [
          h(ArtButtonTable, {
            type: 'edit',
            auth: 'system:dept:edit',
            onClick: () => handleEdit(row)
          }),
          h(ArtButtonTable, {
            type: 'add',
            title: '新增子部门',
            auth: 'system:dept:add',
            onClick: () => handleAdd(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            auth: 'system:dept:remove',
            onClick: () => handleDelete(row)
          })
        ])
      }
    }
  ])

  // 构建部门树形结构
  const buildDeptTree = (depts: DeptVO[], parentId: string | number): DeptVO[] => {
    return depts
      .filter((dept) => String(dept.parentId) === String(parentId))
      .map((dept) => ({
        ...dept,
        hasChildren: depts.some((d) => String(d.parentId) === String(dept.deptId)),
        children: buildDeptTree(depts, dept.deptId)
      }))
  }

  // 获取部门列表
  const getList = async () => {
    loading.value = true
    try {
      const data = await listDept(searchForm)
      deptList.value = buildDeptTree(data, 0)
    } catch (error) {
      console.error('获取部门失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索按钮点击事件
  const handleSearch = () => {
    getList()
  }

  // 重置按钮点击事件
  const handleReset = () => {
    searchForm.deptName = ''
    searchForm.deptCategory = ''
    searchForm.status = ''
    getList()
  }

  // 刷新页面数据
  const handleRefresh = () => {
    getList()
  }

  // 新增部门（支持新增子部门）
  const handleAdd = (row?: DeptVO) => {
    currentDept.value = undefined
    parentDept.value = row
    dialogVisible.value = true
  }

  // 编辑部门
  const handleEdit = (row: DeptVO) => {
    currentDept.value = row
    parentDept.value = undefined
    dialogVisible.value = true
  }

  // 删除部门
  const handleDelete = async (row: DeptVO) => {
    try {
      await ElMessageBox.confirm(`确定要删除名称为"${row.deptName}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delDept(row.deptId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // 展开/收起所有节点
  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && deptList.value) {
        const processRows = (rows: DeptVO[]) => {
          rows.forEach((row) => {
            if (row.children?.length || row.hasChildren) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              if (row.children?.length) {
                processRows(row.children)
              }
            }
          })
        }
        processRows(deptList.value)
      }
    })
  }

  // 页面加载时获取部门列表
  onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
  // 树形表格缩进样式
  :deep(.el-table__body) {
    .el-table__row {
      .el-table__cell:first-child {
        white-space: nowrap !important;
        vertical-align: middle !important;

        > .el-table__expand-icon {
          display: inline-flex !important;
          align-items: center !important;
          margin-right: 8px !important;
          font-size: 14px !important;
          color: var(--el-text-color-secondary) !important;
          vertical-align: middle !important;

          &:hover {
            color: var(--el-color-primary) !important;
          }
        }

        > .el-table__indent {
          display: inline-block !important;
          width: 0 !important;
          padding: 0 !important;
          margin: 0 !important;
          vertical-align: middle !important;
        }

        > .cell {
          display: inline-flex !important;
          align-items: center !important;
          min-width: 0 !important;
          vertical-align: middle !important;
        }
      }
    }

    // 不同层级的缩进
    .el-table__row--level-1 .el-table__cell:first-child {
      padding-left: 32px !important;
    }

    .el-table__row--level-2 .el-table__cell:first-child {
      padding-left: 56px !important;
    }

    .el-table__row--level-3 .el-table__cell:first-child {
      padding-left: 80px !important;
    }

    .el-table__row--level-4 .el-table__cell:first-child {
      padding-left: 104px !important;
    }

    .el-table__row--level-5 .el-table__cell:first-child {
      padding-left: 128px !important;
    }
  }

  // 表格行悬停样式
  :deep(.el-table__body-wrapper) {
    .el-table__row {
      transition: background-color 0.15s ease;

      &:hover > td {
        background-color: rgb(64 158 255 / 6%) !important;
      }
    }
  }
</style>
