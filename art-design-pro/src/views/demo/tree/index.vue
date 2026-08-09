<template>
  <div class="demo-tree-page art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      :isExpand="true"
      @reset="handleReset"
      @search="handleSearch"
    />
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="handleRefresh">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" v-auth="'demo:tree:add'" @click="() => handleAdd()"
              >新增</ElButton
            >
            <ElButton @click="toggleExpand">{{ isExpanded ? '收起' : '展开' }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        ref="tableRef"
        row-key="id"
        :loading="loading"
        :columns="columns"
        :data="treeList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      >
        <template #action="{ row }">
          <ArtButtonTable type="edit" auth="demo:tree:edit" @click="() => handleUpdate(row)" />
          <ArtButtonTable
            type="add"
            title="新增子节点"
            auth="demo:tree:add"
            @click="() => handleAdd(row)"
          />
          <ArtButtonTable type="delete" auth="demo:tree:remove" @click="() => handleDelete(row)" />
        </template>
      </ArtTable>
    </ElCard>
    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="500px" align-center>
      <ElForm :model="form" :rules="rules" ref="formRef" label-width="80px">
        <ElFormItem label="父id" prop="parentId">
          <ElTreeSelect
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ value: 'id', label: 'treeName', children: 'children' }"
            value-key="id"
            check-strictly
            placeholder="请选择父id"
          />
        </ElFormItem>
        <ElFormItem label="部门id" prop="deptId">
          <ElInput v-model="form.deptId" placeholder="请输入部门id" />
        </ElFormItem>
        <ElFormItem label="用户id" prop="userId">
          <ElInput v-model="form.userId" placeholder="请输入用户id" />
        </ElFormItem>
        <ElFormItem label="值" prop="treeName">
          <ElInput v-model="form.treeName" placeholder="请输入值" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="buttonLoading" @click="submitForm">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import type { TreeForm, TreeQuery, TreeVO } from '@/api/demo/tree'
  import { treeApi } from '@/api/demo/tree'

  defineOptions({ name: 'Tree' })

  type TreeOption = {
    id: string | number
    treeName: string
    children?: TreeOption[]
  }

  const loading = ref(false)
  const isExpanded = ref(false)
  const tableRef = ref()

  const treeList = ref<TreeVO[]>([])
  const treeOptions = ref<TreeOption[]>([])

  const dialogVisible = ref(false)
  const dialogTitle = ref('')
  const buttonLoading = ref(false)
  const formRef = ref<FormInstance>()

  let searchForm = reactive<TreeQuery>({
    treeName: ''
  })

  const form = reactive<TreeForm>({
    id: undefined,
    parentId: undefined,
    deptId: undefined,
    userId: undefined,
    treeName: ''
  })

  const formItems = computed(() => [
    {
      label: '树节点名',
      key: 'treeName',
      type: 'input',
      props: { placeholder: '请输入树节点名', clearable: true }
    }
  ])

  const { columnChecks, columns } = useTableColumns<TreeVO>(() => [
    { prop: 'treeName', label: '树节点名' },
    { prop: 'parentId', label: '父id', align: 'center' },
    { prop: 'deptId', label: '部门id', align: 'center' },
    { prop: 'userId', label: '用户id', align: 'center' },
    {
      prop: 'operation',
      label: '操作',
      width: 200,
      fixed: 'right',
      align: 'center',
      useSlot: true,
      slotName: 'action'
    }
  ])

  const rules: FormRules<TreeForm> = {
    parentId: [{ required: true, message: '父id不能为空', trigger: 'blur' }],
    deptId: [{ required: true, message: '部门id不能为空', trigger: 'blur' }],
    userId: [{ required: true, message: '用户id不能为空', trigger: 'blur' }],
    treeName: [{ required: true, message: '值不能为空', trigger: 'blur' }]
  }

  // 构建树形结构（客户端将扁平列表转换为树）
  const buildTree = (list: TreeVO[], parentId: string | number): TreeVO[] => {
    return list
      .filter((item) => String(item.parentId) === String(parentId))
      .map((item) => ({
        ...item,
        hasChildren: list.some((d) => String(d.parentId) === String(item.id)),
        children: buildTree(list, item.id)
      }))
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = await treeApi.listTree(searchForm)
      treeList.value = buildTree(res, 0)
    } catch (error) {
      console.error('获取测试树失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 查询测试树下拉树结构
  const getTreeselect = async () => {
    const res = await treeApi.listTree()
    const top: TreeOption = { id: 0, treeName: '顶级节点', children: [] }
    top.children = buildTree(res, 0)
    treeOptions.value = [top]
  }

  const handleSearch = () => {
    getList()
  }

  const handleReset = () => {
    searchForm.treeName = ''
    getList()
  }

  const handleRefresh = () => {
    getList()
  }

  const resetForm = () => {
    form.id = undefined
    form.parentId = undefined
    form.deptId = undefined
    form.userId = undefined
    form.treeName = ''
    formRef.value?.resetFields()
  }

  const handleAdd = async (row?: TreeVO) => {
    resetForm()
    await getTreeselect()
    form.parentId = row ? row.id : 0
    dialogTitle.value = '添加测试树'
    dialogVisible.value = true
  }

  const handleUpdate = async (row: TreeVO) => {
    resetForm()
    await getTreeselect()
    if (row) {
      form.parentId = row.id
    }
    const res = await treeApi.getTree(row.id)
    Object.assign(form, res)
    dialogTitle.value = '修改测试树'
    dialogVisible.value = true
  }

  const submitForm = () => {
    formRef.value?.validate(async (valid: boolean) => {
      if (valid) {
        buttonLoading.value = true
        try {
          if (form.id) {
            await treeApi.updateTree(form)
          } else {
            await treeApi.addTree(form)
          }
          dialogVisible.value = false
          ElMessage.success('操作成功')
          getList()
        } finally {
          buttonLoading.value = false
        }
      }
    })
  }

  const handleDelete = async (row: TreeVO) => {
    try {
      await ElMessageBox.confirm(`确定要删除测试树编号为"${row.id}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await treeApi.delTree(row.id)
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
      if (tableRef.value?.elTableRef && treeList.value) {
        const processRows = (rows: TreeVO[]) => {
          rows.forEach((row) => {
            if (row.children?.length || row.hasChildren) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              if (row.children?.length) {
                processRows(row.children)
              }
            }
          })
        }
        processRows(treeList.value)
      }
    })
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
  // 树形表格层级缩进样式（art-design-pro 默认主题会清除 el-table 自带的缩进，需手动补偿）
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
