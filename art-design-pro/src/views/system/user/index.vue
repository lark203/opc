<template>
  <div class="art-full-height">
    <div class="box-border flex gap-4 h-full max-md:block max-md:gap-0 max-md:h-auto">
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

      <div class="flex flex-col flex-grow min-w-0">
        <UserSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

        <ElCard class="flex flex-col flex-1 min-h-0 art-table-card">
          <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton type="primary" v-auth="'system:user:add'" @click="() => showDialog('add')"
                  >新增用户</ElButton
                >
                <ElButton
                  type="success"
                  v-auth="'system:user:edit'"
                  :disabled="
                    selectedRows.length !== 1 || selectedRows[0]?.userId === '1761100000000000001'
                  "
                  @click="() => showDialog('edit', selectedRows[0])"
                  >修改</ElButton
                >
                <ElButton
                  type="danger"
                  v-auth="'system:user:remove'"
                  :disabled="
                    selectedRows.length === 0 ||
                    selectedRows.some((r) => r.userId === '1761100000000000001')
                  "
                  @click="() => handleDelete()"
                  >删除</ElButton
                >
                <ElButton
                  type="warning"
                  v-auth="'system:user:edit'"
                  :disabled="
                    selectedRows.length !== 1 || selectedRows[0]?.userId === '1761100000000000001'
                  "
                  @click="handleUnlock"
                  >解锁</ElButton
                >
                <ElDropdown v-auth="['system:user:import', 'system:user:export']">
                  <ElButton type="info">
                    更多
                    <ElIcon class="el-icon--right"><ArrowDown /></ElIcon>
                  </ElButton>
                  <template #dropdown>
                    <ElDropdownMenu>
                      <ElDropdownItem v-if="hasPermi('system:user:import')" @click="importTemplate"
                        >下载模板</ElDropdownItem
                      >
                      <ElDropdownItem v-if="hasPermi('system:user:import')" @click="handleImport"
                        >导入数据</ElDropdownItem
                      >
                      <ElDropdownItem v-if="hasPermi('system:user:export')" @click="handleExport"
                        >导出数据</ElDropdownItem
                      >
                    </ElDropdownMenu>
                  </template>
                </ElDropdown>
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
      </div>
    </div>
    <UserDialog
      ref="userDialogRef"
      v-model:visible="dialogVisible"
      :type="dialogType"
      :user-data="currentUserData"
      @submit="handleDialogSubmit"
    />
    <ElDialog v-model="upload.open" :title="upload.title" width="25%" append-to-body>
      <ElUpload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <ElIcon class="el-icon--upload">
          <UploadFilled />
        </ElIcon>
        <div class="el-upload__text">
          将文件拖到此处，或
          <em>点击上传</em>
        </div>
        <template #tip>
          <div class="text-center el-upload__tip">
            <div class="el-upload__tip">
              <ElCheckbox v-model="upload.updateSupport" />
              是否更新已经存在的用户数据
            </div>
            <span>仅允许导入xls、xlsx格式文件。</span>
            <ElLink
              type="primary"
              underline="never"
              style="font-size: 12px; vertical-align: baseline"
              @click="importTemplate"
            >
              下载模板
            </ElLink>
          </div>
        </template>
      </ElUpload>
      <template #footer>
        <div class="dialog-footer">
          <ElButton type="primary" v-auth="'system:user:import'" @click="submitFileForm"
            >确 定</ElButton
          >
          <ElButton @click="upload.open = false">取 消</ElButton>
        </div>
      </template>
    </ElDialog>
    <UserRoleDialog
      ref="userRoleDialogRef"
      v-model:visible="roleDialogVisible"
      :user-id="currentRoleUserId"
      :user-name="currentRoleUserName"
      @submit="handleRoleSubmit"
    />
    <UserDetailDrawer ref="userDetailDrawerRef" />
  </div>
</template>

<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElCheckbox,
    ElDropdown,
    ElDropdownItem,
    ElDropdownMenu,
    ElIcon,
    ElLink,
    ElMessage,
    ElMessageBox,
    ElSwitch,
    ElUpload,
    type UploadInstance
  } from 'element-plus'
  import { ArrowDown, UploadFilled } from '@element-plus/icons-vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import UserRoleDialog from './modules/user-role-dialog.vue'
  import UserDetailDrawer from './modules/user-detail-drawer.vue'
  import {
    addUser,
    changeUserStatus,
    delUser,
    deptTreeSelect,
    exportUser,
    getImportTemplate,
    listUser,
    resetUserPwd,
    unlockUser,
    updateUser,
    updateUserAuthRole,
    type UserForm,
    type UserQuery,
    type UserVO
  } from '@/api/system/user'
  import request from '@utils/http'

  const { hasPermi } = useAuth()

  const deptTreeRef = ref()
  const userDialogRef = ref()
  const userRoleDialogRef = ref()
  const userDetailDrawerRef = ref()
  const deptTreeData = ref<any[]>([])

  const treeProps = {
    children: 'children',
    label: 'label'
  }

  const dialogType = ref<'add' | 'edit'>('add')
  const dialogVisible = ref(false)
  const currentUserData = ref<Partial<UserVO>>({})
  const roleDialogVisible = ref(false)
  const currentRoleUserId = ref<string | number>('')
  const currentRoleUserName = ref('')

  const selectedRows = ref<UserVO[]>([])

  let searchForm = reactive<UserQuery>({
    userName: '',
    nickName: '',
    phoneNumber: '',
    status: '',
    deptId: '',
    roleId: ''
  })

  const uploadRef = ref<UploadInstance>()
  const upload = reactive({
    // 是否显示弹出层（用户导入）
    open: false,
    // 弹出层标题（用户导入）
    title: '',
    // 是否禁用上传
    isUploading: false,
    // 是否更新已经存在的用户数据
    updateSupport: 0,
    // 设置上传的请求头部
    headers: request.globalHeaders(),
    // 上传的地址
    url: import.meta.env.VITE_API_URL + '/system/user/importData'
  })

  const loadDeptTree = async () => {
    const res = await deptTreeSelect()
    deptTreeData.value = res
  }

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
      apiFn: listUser,
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
        { type: 'index', label: '序号', width: 60 },
        {
          prop: 'userName',
          label: '用户名称',
          formatter: (row: UserVO) =>
            h('span', { class: 'link-type', onClick: () => showDetail(row) }, row.userName)
        },
        { prop: 'nickName', label: '用户昵称' },
        { prop: 'deptName', label: '部门' },
        { prop: 'phoneNumber', label: '手机号码' },
        {
          prop: 'status',
          label: '状态',
          formatter: (row: UserVO) =>
            h(ElSwitch, {
              modelValue: row.status === '0',
              disabled: row.userId === '1761100000000000001',
              'onUpdate:modelValue': (val: boolean) => handleStatusChange(row, val ? '0' : '1')
            })
        },
        { prop: 'createTime', label: '创建时间' },
        {
          prop: 'operation',
          label: '操作',
          fixed: 'right',
          width: 200,
          formatter: (row: UserVO) => {
            if (row.userId === '1761100000000000001') {
              return h('span', { class: 'text-gray-400' }, '系统内置')
            }
            return h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                auth: 'system:user:edit',
                onClick: () => showDialog('edit', row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                auth: 'system:user:remove',
                onClick: () => handleDelete(row)
              }),
              h(ArtButtonTable, {
                type: 'password',
                auth: 'system:user:resetPwd',
                onClick: () => handleResetPwd(row)
              }),
              h(ArtButtonTable, {
                type: 'permission',
                auth: 'system:user:auth',
                onClick: () => showRoleDialog(row)
              })
            ])
          }
        }
      ]
    }
  })

  const handleSearch = (params: UserQuery) => {
    Object.assign(searchForm, params)
    replaceSearchParams(searchForm)
    getData()
  }

  const handleReset = () => {
    searchForm.userName = ''
    searchForm.nickName = ''
    searchForm.phoneNumber = ''
    searchForm.status = ''
    searchForm.deptId = ''
    deptTreeRef.value?.setCurrentKey(null)
    resetSearchParams()
    getData()
  }

  const handleDeptNodeClick = (data: any) => {
    searchForm.deptId = data.id
    deptTreeRef.value?.setCurrentKey(data.id)
    replaceSearchParams(searchForm)
    getData()
  }

  const handleSelectionChange = (selection: UserVO[]) => {
    selectedRows.value = selection
  }

  const showDialog = (type: 'add' | 'edit', row?: UserVO) => {
    dialogType.value = type
    currentUserData.value = row || {}
    dialogVisible.value = true
  }

  const showDetail = (row: UserVO) => {
    userDetailDrawerRef.value?.open(row)
  }

  const handleDialogSubmit = async (formData: UserForm) => {
    try {
      if (formData.userId) {
        await updateUser(formData)
        ElMessage.success('修改成功')
      } else {
        await addUser(formData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      currentUserData.value = {}
      refreshData()
    } catch (error) {
      console.error('操作失败:', error)
    }
  }

  const handleDelete = async (row?: UserVO) => {
    const userIds = row?.userId || selectedRows.value.map((r) => r.userId).join(',')
    if (!userIds) return
    try {
      let confirmText = ''
      if (row) {
        confirmText = `确定要删除用户"${row.nickName || row.userName}"吗？`
      } else {
        const userNames = selectedRows.value.map((r) => r.nickName || r.userName).join('、')
        confirmText = `确定要删除选中的 ${selectedRows.value.length} 个用户吗？\n${userNames}`
      }
      await ElMessageBox.confirm(confirmText, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delUser(userIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleUnlock = async () => {
    if (selectedRows.value.length !== 1) return
    const row = selectedRows.value[0]
    const userId = row.userId
    const userName = row.nickName || row.userName
    try {
      await ElMessageBox.confirm(`确定要解锁用户"${userName}"吗？`, '解锁确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await unlockUser(userId)
      ElMessage.success('解锁成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('解锁失败')
      }
    }
  }

  const handleStatusChange = async (row: UserVO, status: string) => {
    const text = status === '0' ? '启用' : '停用'
    try {
      await ElMessageBox.confirm(`确认要${text}"${row.userName}"用户吗？`, '状态变更', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await changeUserStatus(row.userId, status)
      ElMessage.success(`${text}成功`)
      refreshData()
    } catch (error) {
      row.status = status === '0' ? '1' : '0'
      if (error !== 'cancel') {
        ElMessage.error(`${text}失败`)
      }
    }
  }

  const handleResetPwd = async (row: UserVO) => {
    try {
      const result = await ElMessageBox.prompt(`请输入"${row.userName}"的新密码`, '重置密码', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: '用户密码长度必须介于 5 和 20 之间'
      })
      await resetUserPwd(row.userId, result.value)
      ElMessage.success('修改成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('重置密码失败')
      }
    }
  }

  const showRoleDialog = (row: UserVO) => {
    currentRoleUserId.value = row.userId
    currentRoleUserName.value = row.userName
    roleDialogVisible.value = true
  }

  const handleRoleSubmit = async (userId: string | number, roleIds: Array<string | number>) => {
    try {
      await updateUserAuthRole(userId, roleIds)
      ElMessage.success('分配角色成功')
      refreshData()
    } catch (error) {
      ElMessage.error('分配角色失败', error)
    }
  }

  const handleImport = () => {
    upload.title = '用户导入'
    upload.open = true
  }

  const handleExport = () => {
    exportUser(searchForm)
  }

  const importTemplate = () => {
    getImportTemplate()
  }

  const handleFileUploadProgress = () => {
    upload.isUploading = true
  }

  const formatImportResultMessage = (message: unknown) => {
    return String(message ?? '')
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/&nbsp;/gi, ' ')
      .replace(/<[^>]+>/g, '')
  }

  const handleFileSuccess = (response: any, file: any) => {
    upload.open = false
    upload.isUploading = false
    uploadRef.value?.handleRemove(file)
    ElMessageBox.alert(formatImportResultMessage(response.msg), '导入结果', {
      customClass: 'import-result-box'
    })
    refreshData()
  }

  const submitFileForm = () => {
    uploadRef.value?.submit()
  }

  onMounted(() => {
    loadDeptTree()
    getData()
  })
</script>

<style lang="scss" scoped>
  :deep(.link-type) {
    color: var(--el-color-primary);
    cursor: pointer;
  }

  :deep(.link-type:hover) {
    color: var(--el-color-primary-light-3);
  }
</style>
