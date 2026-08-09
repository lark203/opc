<template>
  <div class="art-full-height">
    <RoleSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />
    <ElCard class="art-table-card">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" v-auth="'system:role:add'" @click="showDialog('add')"
              >新增角色</ElButton
            >
            <ElButton
              type="success"
              v-auth="'system:role:edit'"
              :disabled="selectedRows.length !== 1"
              @click="showDialog('edit', selectedRows[0])"
              >修改</ElButton
            >
            <ElButton
              type="danger"
              v-auth="'system:role:remove'"
              :disabled="selectedRows.length === 0"
              @click="() => handleDelete()"
              >删除</ElButton
            >
            <ElButton type="info" v-auth="'system:role:export'" @click="handleExport"
              >导出</ElButton
            >
          </ElSpace>
        </template>
      </ArtTableHeader>
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
    <RoleEditDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :role-data="currentRoleData"
      @success="refreshData"
    />
    <RolePermissionDialog
      v-model="permissionDialog"
      :role-data="currentRoleData"
      @success="refreshData"
    />
    <RoleUserDialog
      v-model:visible="userDialog"
      :role-data="currentRoleData"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue'
  import { ElMessage, ElMessageBox, ElSwitch } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import RoleSearch from './modules/role-search.vue'
  import RoleEditDialog from './modules/role-edit-dialog.vue'
  import RolePermissionDialog from './modules/role-permission-dialog.vue'
  import RoleUserDialog from './modules/role-user-dialog.vue'
  import {
    changeRoleStatus,
    delRole,
    exportRole,
    listRole,
    type RoleQuery,
    type RoleVO
  } from '@/api/system/role'

  let searchForm = reactive<RoleQuery>({
    roleName: '',
    roleKey: '',
    status: ''
  })

  const dialogVisible = ref(false)
  const permissionDialog = ref(false)
  const userDialog = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentRoleData = ref<RoleVO>()

  const selectedRows = ref<RoleVO[]>([])

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
      apiFn: listRole,
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
        { prop: 'roleName', label: '角色名称' },
        { prop: 'roleKey', label: '权限字符' },
        { prop: 'roleSort', label: '显示顺序' },
        {
          prop: 'status',
          label: '状态',
          formatter: (row: RoleVO) =>
            h(ElSwitch, {
              modelValue: row.status === '0',
              'onUpdate:modelValue': (val: boolean) => handleStatusChange(row, val ? '0' : '1')
            })
        },
        { prop: 'createTime', label: '创建时间' },
        {
          prop: 'operation',
          label: '操作',
          fixed: 'right',
          width: 200,
          formatter: (row: RoleVO) =>
            h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:role:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:role:remove',
                onClick: () => handleDelete(row)
              }),
              h(ArtButtonTable, {
                type: 'menu',
                auth: 'system:role:auth',
                onClick: () => showPermissionDialog(row)
              }),
              h(ArtButtonTable, {
                type: 'user',
                auth: 'system:role:auth',
                onClick: () => showUserDialog(row)
              })
            ])
        }
      ]
    }
  })

  const showDialog = (type: 'add' | 'edit', row?: RoleVO) => {
    dialogType.value = type
    currentRoleData.value = row
    dialogVisible.value = true
  }

  const showPermissionDialog = (row?: RoleVO) => {
    currentRoleData.value = row
    permissionDialog.value = true
  }

  const showUserDialog = (row?: RoleVO) => {
    currentRoleData.value = row
    userDialog.value = true
  }

  const handleSearch = (params: RoleQuery) => {
    Object.assign(searchForm, params)
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.roleName = ''
    searchForm.roleKey = ''
    searchForm.status = ''
    resetSearchParams()
    getData()
  }

  const handleSelectionChange = (selection: RoleVO[]) => {
    selectedRows.value = selection
  }

  const handleDelete = async (row?: RoleVO) => {
    const roleIds = row?.roleId || selectedRows.value.map((r) => r.roleId).join(',')
    if (!roleIds) return
    try {
      let confirmText = ''
      if (row) {
        confirmText = `确定要删除角色"${row.roleName}"吗？`
      } else {
        const roleNames = selectedRows.value.map((r) => r.roleName).join('、')
        confirmText = `确定要删除选中的 ${selectedRows.value.length} 个角色吗？\n${roleNames}`
      }
      await ElMessageBox.confirm(confirmText, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delRole(roleIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleStatusChange = async (row: RoleVO, status: string) => {
    const text = status === '0' ? '启用' : '停用'
    try {
      await ElMessageBox.confirm(`确认要${text}"${row.roleName}"角色吗？`, '状态变更', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await changeRoleStatus(row.roleId, status)
      ElMessage.success(`${text}成功`)
      refreshData()
    } catch (error) {
      row.status = status === '0' ? '1' : '0'
      if (error !== 'cancel') {
        ElMessage.error(`${text}失败`)
      }
    }
  }

  const handleExport = () => {
    exportRole(searchForm)
  }
</script>
