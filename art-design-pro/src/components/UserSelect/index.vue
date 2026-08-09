<template>
  <ElDialog
    :model-value="visible"
    :title="title"
    width="60%"
    top="8vh"
    class="art-full-height-dialog"
    @update:model-value="(val: boolean) => emit('update:visible', val)"
    @open="handleOpen"
    @closed="handleClosed"
  >
    <div class="box-border flex gap-4 user-select-layout">
      <!-- 左侧部门树（参考用户管理页面布局） -->
      <div class="flex-shrink-0 w-58 h-full">
        <ElCard class="tree-card art-card-xs flex flex-col h-full mt-0">
          <template #header>
            <b>部门结构</b>
          </template>
          <ElInput v-model="deptKeyword" placeholder="搜索部门" clearable class="mb-2" />
          <ElScrollbar>
            <ElTree
              ref="treeRef"
              :data="deptOptions"
              :props="{ label: 'label', children: 'children' }"
              node-key="id"
              :expand-on-click-node="false"
              :filter-node-method="filterDept"
              default-expand-all
              highlight-current
              @node-click="handleNodeClick"
            />
          </ElScrollbar>
        </ElCard>
      </div>

      <!-- 右侧用户表格 -->
      <div class="flex flex-col flex-grow min-w-0 h-full">
        <ArtSearchBar
          v-model="searchForm"
          :items="searchItems"
          :hide-label="true"
          @search="handleSearch"
          @reset="handleReset"
        />
        <div class="flex-1 min-h-0">
          <ArtTable
            ref="artTableRef"
            :loading="loading"
            :data="data"
            :columns="columns"
            :pagination="pagination"
            :show-table-header="false"
            row-key="userId"
            @selection-change="handleSelectionChange"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-500">已选 {{ selectedUsers.length }} 人</span>
        <div>
          <ElButton @click="emit('update:visible', false)">取消</ElButton>
          <ElButton type="primary" @click="handleConfirm">确定</ElButton>
        </div>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import { ElButton, ElCard, ElDialog, ElInput, ElScrollbar, ElTree } from 'element-plus'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { deptTreeSelect, listUser, type UserVO } from '@/api/system/user'

  interface DeptNode {
    id: string | number
    label: string
    children?: DeptNode[]
  }

  const props = withDefaults(
    defineProps<{
      visible: boolean
      multiple?: boolean
      userIds?: Array<string | number>
      title?: string
    }>(),
    { multiple: true, userIds: () => [], title: '选择用户' }
  )

  const emit = defineEmits<{
    'update:visible': [val: boolean]
    confirmCallBack: [users: UserVO[]]
  }>()

  const deptOptions = ref<DeptNode[]>([])
  const deptKeyword = ref('')
  const treeRef = ref<InstanceType<typeof ElTree>>()
  const artTableRef = ref()
  const currentDeptId = ref<string | number>('')
  const selectedUsers = ref<UserVO[]>([])

  const searchForm = reactive<{ nickName?: string; userName?: string }>({})

  const searchItems = computed(() => [
    { label: '昵称', key: 'nickName', type: 'input', props: { placeholder: '请输入昵称' } },
    { label: '用户名', key: 'userName', type: 'input', props: { placeholder: '请输入用户名' } }
  ])

  const filterDept = (value: string, data: DeptNode) => {
    if (!value) return true
    return data.label.includes(value)
  }

  watch(deptKeyword, (val) => treeRef.value?.filter(val))

  /** useTable 管理用户列表数据与分页 */
  const {
    columns,
    data,
    loading,
    pagination,
    getData,
    replaceSearchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange
  } = useTable({
    core: {
      apiFn: listUser,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        nickName: searchForm.nickName,
        userName: searchForm.userName,
        deptId: currentDeptId.value
      },
      paginationKey: { current: 'pageNum', size: 'pageSize' },
      immediate: false,
      columnsFactory: () => {
        const cols: Record<string, unknown>[] = []
        if (props.multiple) {
          cols.push({ type: 'selection', width: 50, reserveSelection: true })
        } else {
          cols.push({ type: 'index', width: 50, label: '序号' })
        }
        cols.push({ prop: 'nickName', label: '用户昵称' })
        cols.push({ prop: 'userName', label: '用户名' })
        cols.push({ prop: 'deptName', label: '部门' })
        cols.push({ prop: 'phonenumber', label: '手机号' })
        return cols
      }
    }
  })

  const handleNodeClick = (data: DeptNode) => {
    currentDeptId.value = data.id
    replaceSearchParams({
      ...searchForm,
      deptId: data.id
    })
    getData()
  }

  const handleSearch = () => {
    replaceSearchParams({
      ...searchForm,
      deptId: currentDeptId.value
    })
    getData()
  }

  const handleReset = () => {
    searchForm.nickName = undefined
    searchForm.userName = undefined
    currentDeptId.value = ''
    treeRef.value?.setCurrentKey(null)
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (rows: UserVO[]) => {
    selectedUsers.value = rows
  }

  const loadDeptTree = async () => {
    const res = await deptTreeSelect()
    deptOptions.value = (res as unknown as DeptNode[]) || []
  }

  const handleOpen = async () => {
    await loadDeptTree()
    await getData()
    await nextTick()
    // 预选用户：加载全部用户匹配 ID，利用 reserveSelection + row-key 跨页保留选中
    if (props.userIds.length && props.multiple) {
      const res = await listUser({ pageNum: 1, pageSize: 9999 })
      const pre = res.rows.filter((u) => props.userIds.includes(u.userId))
      selectedUsers.value = pre
      await nextTick()
      pre.forEach((u) => artTableRef.value?.elTableRef?.toggleRowSelection(u, true))
    }
  }

  const handleClosed = () => {
    selectedUsers.value = []
    currentDeptId.value = ''
    searchForm.nickName = undefined
    searchForm.userName = undefined
    deptKeyword.value = ''
    artTableRef.value?.elTableRef?.clearSelection()
  }

  const handleConfirm = () => {
    const result = props.multiple ? selectedUsers.value : selectedUsers.value.slice(0, 1)
    emit('confirmCallBack', result)
    emit('update:visible', false)
  }
</script>

<style lang="scss" scoped>
  .user-select-layout {
    height: 60vh;
  }
</style>
