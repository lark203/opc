<template>
  <div class="art-full-height">
    <!-- flex: 左右布局，gap-4: 间距16px -->
    <div class="box-border flex gap-4 h-full max-md:block max-md:gap-0 max-md:h-auto">
      <!-- 左侧部门树面板 -->
      <div class="flex-shrink-0 w-58 h-full max-md:w-full max-md:h-auto max-md:mb-5">
        <ElCard class="tree-card art-card-xs flex flex-col h-full mt-0">
          <template #header>
            <b>部门结构</b>
          </template>
          <ElScrollbar>
            <ElTree
              ref="deptTreeRef"
              :data="deptTreeData"
              :props="treeProps"
              :expand-on-click-node="false"
              node-key="id"
              default-expand-all
              highlight-current
              @node-click="handleDeptNodeClick"
            />
          </ElScrollbar>
        </ElCard>
      </div>

      <!-- 右侧内容区域 -->
      <div class="flex flex-col flex-grow min-w-0">
        <!-- ArtSearchBar: 搜索栏组件 -->
        <ArtSearchBar
          v-model="searchForm"
          :items="formItems"
          @reset="handleReset"
          @search="handleSearch"
        />

        <!-- ElCard: 卡片容器，包裹表格 -->
        <ElCard class="flex flex-col flex-1 min-h-0 art-table-card">
          <!-- ArtTableHeader: 表格头部组件 -->
          <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
            <template #left>
              <!-- ElSpace: 按钮间距容器 -->
              <ElSpace wrap>
                <ElButton type="primary" v-auth="'system:post:add'" @click="() => showDialog('add')"
                  >新增岗位</ElButton
                >
                <ElButton
                  type="success"
                  v-auth="'system:post:edit'"
                  :disabled="selectedRows.length !== 1"
                  @click="() => showDialog('edit')"
                  >修改</ElButton
                >
                <ElButton
                  type="danger"
                  v-auth="'system:post:remove'"
                  :disabled="selectedRows.length === 0"
                  @click="() => handleDelete()"
                  >删除</ElButton
                >
                <ElButton type="info" v-auth="'system:post:export'" @click="handleExport"
                  >导出</ElButton
                >
              </ElSpace>
            </template>
          </ArtTableHeader>
          <!-- ArtTable: 表格组件 -->
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
      </div>
    </div>
    <!-- PostDialog: 岗位新增/编辑弹窗 -->
    <PostDialog
      v-model:visible="dialogVisible"
      :edit-data="currentPostData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  // 导入 Vue 组合式 API
  import { computed, h, onMounted, reactive, ref, toRefs } from 'vue'
  // 导入 Element Plus 组件和消息提示
  import { ElMessage, ElMessageBox } from 'element-plus'
  // 导入表格按钮组件
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  // 导入字典标签组件
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  // 导入表格 hook
  import { useTable } from '@/hooks/core/useTable'
  // 导入岗位对话框组件
  import PostDialog from './modules/post-dialog.vue'
  // 导入字典工具函数
  import { useDict } from '@/utils/dict'
  // 导入岗位 API 和类型定义
  import {
    delPost,
    deptTreeSelect,
    exportPost,
    listPost,
    type PostQuery,
    type PostVO
  } from '@/api/system/post'
  import type { DeptTreeVO } from '@/api/system/dept'

  // 使用字典工具函数获取 sys_normal_disable 字典（正常/禁用状态）
  const { sys_normal_disable } = toRefs(useDict('sys_normal_disable'))

  // ref: 创建响应式变量
  const deptTreeRef = ref() // 部门树引用
  const deptTreeData = ref<DeptTreeVO[]>([]) // 部门树数据
  const dialogVisible = ref(false) // 弹窗显示状态
  const currentPostData = ref<PostVO>() // 当前编辑的岗位数据

  // ref: 创建选中行数组
  const selectedRows = ref<PostVO[]>([])

  // 部门树配置
  const treeProps = {
    children: 'children',
    label: 'label'
  }

  // reactive: 创建响应式搜索表单对象
  let searchForm = reactive<PostQuery>({
    postCode: '',
    postName: '',
    postCategory: '',
    status: '',
    deptId: undefined,
    belongDeptId: undefined
  })

  // computed: 动态生成搜索栏配置项
  const formItems = computed(() => [
    {
      label: '岗位编码',
      key: 'postCode',
      type: 'input',
      props: { clearable: true, placeholder: '请输入岗位编码' }
    },
    {
      label: '类别编码',
      key: 'postCategory',
      type: 'input',
      props: { clearable: true, placeholder: '请输入类别编码' }
    },
    {
      label: '岗位名称',
      key: 'postName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入岗位名称' }
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

  // useTable: 创建表格配置
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
      apiFn: listPost,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm
      },
      paginationKey: {
        current: 'pageNum',
        size: 'pageSize'
      },
      columnsFactory: () => [
        { type: 'selection' },
        { type: 'index', width: 60, label: '序号' },
        { prop: 'postCode', label: '岗位编码' },
        { prop: 'postCategory', label: '类别编码' },
        { prop: 'postName', label: '岗位名称' },
        { prop: 'deptName', label: '部门' },
        { prop: 'postSort', label: '排序', align: 'center' },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          align: 'center',
          formatter: (row: PostVO) =>
            h(DictTag, { options: sys_normal_disable.value, value: row.status })
        },
        { prop: 'createTime', label: '创建时间' },
        {
          prop: 'operation',
          label: '操作',
          width: 140,
          fixed: 'right',
          align: 'center',
          formatter: (row: PostVO) => {
            return h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:post:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:post:remove',
                onClick: () => handleDelete(row)
              })
            ])
          }
        }
      ]
    }
  })

  // 加载部门树数据
  const loadDeptTree = async () => {
    deptTreeData.value = await deptTreeSelect()
  }

  // 搜索按钮点击事件
  const handleSearch = () => {
    searchForm.belongDeptId = undefined
    replaceSearchParams(searchForm)
    getData()
  }

  // 重置按钮点击事件
  const handleReset = () => {
    searchForm.postCode = ''
    searchForm.postName = ''
    searchForm.postCategory = ''
    searchForm.status = ''
    searchForm.deptId = undefined
    searchForm.belongDeptId = undefined
    deptTreeRef.value?.setCurrentKey(null)
    resetSearchParams()
    getData()
  }

  // 部门树节点点击事件
  const handleDeptNodeClick = (data: DeptTreeVO) => {
    searchForm.belongDeptId = data.id
    searchForm.deptId = undefined
    deptTreeRef.value?.setCurrentKey(data.id)
    replaceSearchParams(searchForm)
    getData()
  }

  // 表格选中状态变化事件
  const handleSelectionChange = (selection: PostVO[]) => {
    selectedRows.value = selection
  }

  // 显示弹窗（新增/编辑）
  const showDialog = (type: 'add' | 'edit', row?: PostVO) => {
    currentPostData.value = row
    dialogVisible.value = true
  }

  // 删除岗位
  const handleDelete = async (row?: PostVO) => {
    const postIds = row?.postId || selectedRows.value.map((r) => r.postId).join(',')
    if (!postIds) return
    try {
      await ElMessageBox.confirm(`确定要删除岗位编号为"${postIds}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delPost(postIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  // 页面加载时获取数据
  onMounted(() => {
    loadDeptTree()
    getData()
  })

  const handleExport = () => {
    exportPost(searchForm)
  }
</script>
