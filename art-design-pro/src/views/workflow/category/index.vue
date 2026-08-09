<template>
  <div class="category-page art-full-height">
    <!-- ArtSearchBar: 搜索栏组件，通过 v-model 双向绑定搜索表单 -->
    <ArtSearchBar
      v-model="searchForm"
      :items="searchItems"
      @search="handleSearch"
      @reset="handleReset"
    />
    <!-- ElCard: 卡片容器，包裹表格头部和表格主体，art-table-card 实现 flex 自适应高度 -->
    <ElCard class="art-table-card">
      <!-- ArtTableHeader: 表格头部组件，包含操作按钮区、列配置和刷新功能 -->
      <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="loadData">
        <template #left>
          <!-- ElSpace: 按钮间距容器，wrap 属性支持自动换行适配窄屏 -->
          <ElSpace wrap>
            <!-- 新增分类按钮，v-auth 控制权限（workflow:category:add） -->
            <ElButton type="primary" v-auth="'workflow:category:add'" @click="showDialog('add')">
              新增分类
            </ElButton>
            <!-- 展开/收起按钮，切换树形表格所有节点的展开状态 -->
            <ElButton @click="toggleExpand">{{ isExpanded ? '收起' : '展开' }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <!-- ArtTable: 封装表格组件，支持树形结构展示 -->
      <!-- row-key: 指定行数据的唯一标识字段，树形表格必需 -->
      <!-- tree-props: 配置树形结构的子节点字段和懒加载标识字段 -->
      <!-- default-expand-all: 默认是否展开所有节点 -->
      <ArtTable
        ref="tableRef"
        row-key="categoryId"
        :loading="loading"
        :columns="columns"
        :data="treeData"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />
    </ElCard>
    <!-- CategoryDialog: 新增/编辑分类弹窗组件 -->
    <CategoryDialog v-model:visible="dialogVisible" :edit-data="currentData" @success="loadData" />
  </div>
</template>

<script setup lang="ts">
  // 导入 Vue 组合式 API：h 用于渲染函数，nextTick 用于 DOM 更新后执行，reactive/ref 用于响应式数据
  import { h, nextTick, reactive, ref } from 'vue'
  // 导入 Element Plus 消息提示和确认框组件
  import { ElMessage, ElMessageBox } from 'element-plus'
  // 导入表格操作按钮组件（内置权限控制和悬停提示）
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  // 导入表格列配置管理 hook，支持列的显示/隐藏/排序等动态配置
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  // 导入分类弹窗组件
  import CategoryDialog from './modules/category-dialog.vue'
  // 导入流程分类 API 接口和类型定义
  import {
    delCategory,
    type FlowCategoryQuery,
    type FlowCategoryVO,
    listCategory
  } from '@/api/workflow/category'

  // ========================= 响应式状态定义 =========================

  const loading = ref(false) // 表格加载状态
  const isExpanded = ref(false) // 树形节点展开状态（true=已展开，false=已收起）
  const tableRef = ref() // ArtTable 组件引用，用于调用 toggleRowExpansion 方法
  const treeData = ref<FlowCategoryVO[]>([]) // 树形结构的分类列表数据
  const dialogVisible = ref(false) // 弹窗显示状态
  const currentData = ref<Partial<FlowCategoryVO>>() // 当前编辑/新增的分类数据（Partial 允许部分字段）

  // 搜索表单，reactive 创建响应式对象
  let searchForm = reactive<FlowCategoryQuery>({
    categoryName: ''
  })

  // ========================= 搜索栏配置 =========================

  // 搜索栏配置项，定义搜索字段的类型和属性
  const searchItems = [
    {
      label: '分类名称',
      key: 'categoryName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入分类名称' }
    }
  ]

  // ========================= 表格列配置 =========================

  // useTableColumns: 管理表格列配置，返回 columns（当前显示列）和 columnChecks（列显示状态）
  // 仅选择列和操作列设置 width，其他列不设置宽度（遵循功能实现准则第10条）
  const { columnChecks, columns } = useTableColumns(() => [
    // 选择列，固定宽度 50px
    /*{ type: 'selection', width: 50 },*/
    // 分类名称列（树形结构首列，不设置宽度，自动撑满剩余空间）
    { prop: 'categoryName', label: '分类名称' },
    // 排序列，居中显示
    { prop: 'orderNum', label: '排序', align: 'center' },
    // 创建时间列，居中显示
    { prop: 'createTime', label: '创建时间', align: 'center' },
    // 操作列，固定宽度 200px，固定在右侧
    {
      prop: 'operation',
      label: '操作',
      width: 200,
      fixed: 'right',
      align: 'center',
      // formatter: 自定义渲染函数，使用 ArtButtonTable 渲染操作按钮
      formatter: (row: FlowCategoryVO) => {
        return h('div', [
          // 编辑按钮，权限码 workflow:category:edit
          h(ArtButtonTable, {
            type: 'edit',
            auth: 'workflow:category:edit',
            onClick: () => showDialog('edit', row)
          }),
          // 新增子分类按钮，权限码 workflow:category:add，自定义提示文字
          h(ArtButtonTable, {
            type: 'add',
            title: '新增子分类',
            auth: 'workflow:category:add',
            onClick: () => showDialog('add', row)
          }),
          // 删除按钮，权限码 workflow:category:remove
          h(ArtButtonTable, {
            type: 'delete',
            auth: 'workflow:category:remove',
            onClick: () => handleDelete(row)
          })
        ])
      }
    }
  ])

  // ========================= 树形数据构建 =========================

  // 将扁平列表数据构建为树形结构
  // list: 后端返回的扁平分类列表，parentId: 父级 ID（顶级为 0）
  const buildTree = (list: FlowCategoryVO[], parentId: string | number): FlowCategoryVO[] => {
    return (
      list
        // 使用 String 比较，避免 string/number 类型不一致导致匹配失败
        .filter((item) => String(item.parentId) === String(parentId))
        .map((item) => {
          // 递归构建子节点
          const children = buildTree(list, item.categoryId)
          return {
            ...item,
            // hasChildren: 标识是否存在子节点，用于树形表格展开图标显示
            hasChildren: children.length > 0,
            children
          }
        })
    )
  }

  // ========================= 数据加载 =========================

  // 加载分类列表数据，调用后端接口并构建树形结构
  const loadData = async () => {
    loading.value = true
    try {
      const res = await listCategory(searchForm)
      // 从顶级（parentId=0）开始构建树形结构
      treeData.value = buildTree(res, 0)
    } catch (error) {
      console.error('获取流程分类失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索按钮回调，重新加载数据
  const handleSearch = () => {
    loadData()
  }

  // 重置按钮回调，清空搜索条件并重新加载
  const handleReset = () => {
    searchForm.categoryName = ''
    loadData()
  }

  // ========================= 弹窗操作 =========================

  // 打开弹窗（新增/编辑）
  // type: 'add' 新增 | 'edit' 编辑
  // row: 编辑时传入当前行数据；新增子分类时传入父行数据
  const showDialog = (type: 'add' | 'edit', row?: FlowCategoryVO) => {
    if (type === 'edit') {
      // 编辑模式：传入完整行数据
      currentData.value = row
    } else if (row) {
      // 新增子分类模式：仅传入 parentId（当前行作为父级）
      currentData.value = { parentId: row.categoryId }
    } else {
      // 新增顶级分类模式：parentId 为 0
      currentData.value = { parentId: 0 }
    }
    dialogVisible.value = true
  }

  // 删除分类
  const handleDelete = async (row: FlowCategoryVO) => {
    try {
      // 弹出确认框，防止误删除
      await ElMessageBox.confirm(`确定要删除分类"${row.categoryName}"吗？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delCategory(row.categoryId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // 用户点击取消时 error 为 'cancel'，不显示错误提示
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // ========================= 展开/收起操作 =========================

  // 切换树形表格所有节点的展开/收起状态
  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    // nextTick: 等待 DOM 更新后操作表格实例
    nextTick(() => {
      if (tableRef.value?.elTableRef && treeData.value) {
        // 递归遍历所有行，调用 toggleRowExpansion 设置展开状态
        const processRows = (rows: FlowCategoryVO[]) => {
          rows.forEach((row) => {
            if (row.children?.length || row.hasChildren) {
              // toggleRowExpansion: ElTable 实例方法，第二个参数为是否展开
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              if (row.children?.length) {
                processRows(row.children)
              }
            }
          })
        }
        processRows(treeData.value)
      }
    })
  }

  // 页面加载时获取分类列表
  loadData()
</script>

<style lang="scss" scoped>
  // 树形表格缩进样式，确保展开图标和层级缩进正确显示
  :deep(.el-table__body) {
    .el-table__row {
      .el-table__cell:first-child {
        white-space: nowrap !important;
        vertical-align: middle !important;

        // 展开图标样式
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

        // 层级缩进
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
