<template>
  <ElDialog v-model="dialogVisible" title="分配用户" width="45%" align-center @close="handleClose">
    <div class="select-user-container">
      <div class="search-section">
        <ElCard shadow="never" class="search-card">
          <ElForm :model="queryParams" inline class="query-form">
            <ElFormItem label="用户名称">
              <ElInput
                v-model="queryParams.userName"
                placeholder="请输入用户名称"
                clearable
                class="search-input"
                @keyup.enter="handleQuery"
              />
            </ElFormItem>
            <ElFormItem label="手机号码">
              <ElInput
                v-model="queryParams.phoneNumber"
                placeholder="请输入手机号码"
                clearable
                class="search-input"
                @keyup.enter="handleQuery"
              />
            </ElFormItem>
            <ElFormItem>
              <ElButton type="primary" @click="handleQuery">搜索</ElButton>
              <ElButton @click="resetQuery">重置</ElButton>
            </ElFormItem>
          </ElForm>
        </ElCard>

        <ElCard shadow="never" class="table-card">
          <ElTable
            ref="tableRef"
            :data="userList"
            :height="300"
            row-key="userId"
            @selection-change="handleSelectionChange"
          >
            <ElTableColumn type="selection" width="55" />
            <ElTableColumn prop="userName" label="用户名" show-overflow-tooltip />
            <ElTableColumn prop="nickName" label="昵称" show-overflow-tooltip />
            <ElTableColumn prop="phoneNumber" label="手机" show-overflow-tooltip />
          </ElTable>
          <ElPagination
            v-if="total > 0"
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            class="pagination"
            @size-change="handleQuery"
            @current-change="handleQuery"
          />
        </ElCard>
      </div>

      <div class="action-section">
        <div
          class="action-btn"
          :class="{ disabled: selectedIds.length === 0 }"
          @click="addSelected"
        >
          <ElIcon><ArrowRight /></ElIcon>
        </div>
        <div
          class="action-btn"
          :class="{ disabled: selectedUsers.length === 0 }"
          style="background-color: var(--el-color-danger)"
          @click="removeAllSelected"
        >
          <ElTooltip content="移除所有" placement="top">
            <ElIcon>
              <ArrowLeft />
            </ElIcon>
          </ElTooltip>
        </div>
      </div>

      <div class="selected-section">
        <ElCard shadow="never" class="selected-card">
          <template #header>
            <div class="selected-header">
              <span>已选择用户</span>
              <span class="count">{{ selectedUsers.length }} 人</span>
            </div>
          </template>
          <div class="selected-list" v-if="selectedUsers.length > 0">
            <div
              v-for="user in selectedUsers"
              :key="user.userId"
              class="selected-item"
              @click="removeSelected(user.userId)"
            >
              <span class="user-info">{{ user.userName }} ({{ user.nickName }})</span>
              <ElIcon class="remove-icon">
                <CircleClose />
              </ElIcon>
            </div>
          </div>
          <div class="empty-tip" v-else>
            <ElIcon size="32" color="#c0c4cc">
              <User />
            </ElIcon>
            <p>暂无选择用户</p>
          </div>
        </ElCard>
      </div>
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import { ArrowLeft, ArrowRight, CircleClose, User } from '@element-plus/icons-vue'
  import type { RoleVO } from '@/api/system/role'

  interface Props {
    visible: boolean
    roleData?: RoleVO
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  interface UserItem {
    userId: string | number
    userName: string
    nickName: string
    email: string
    phoneNumber: string
    status: string
    deptName?: string
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const tableRef = ref()

  const queryParams = reactive({
    roleId: undefined as string | number | undefined,
    userName: undefined as string | undefined,
    phoneNumber: undefined as string | undefined,
    pageNum: 1,
    pageSize: 10
  })

  const userList = ref<UserItem[]>([])
  const total = ref(0)
  const selectedIds = ref<Array<string | number>>([])
  const selectedUsers = ref<UserItem[]>([])

  const resetForm = () => {
    queryParams.roleId = undefined
    queryParams.userName = undefined
    queryParams.phoneNumber = undefined
    queryParams.pageNum = 1
    queryParams.pageSize = 10
    userList.value = []
    total.value = 0
    selectedIds.value = []
    selectedUsers.value = []
  }

  const getList = async () => {
    const { getUnallocatedList } = await import('@/api/system/role')
    const res = await getUnallocatedList({
      roleId: queryParams.roleId,
      userName: queryParams.userName,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    userList.value = res.rows || []
    total.value = res.total || 0
  }

  const loadSelectedUsers = async () => {
    const { getAllocatedList } = await import('@/api/system/role')
    const res = await getAllocatedList({
      roleId: queryParams.roleId,
      pageNum: 1,
      pageSize: 1000
    })
    selectedUsers.value = res.rows || []
  }

  const loadData = async (roleId?: string | number) => {
    if (!roleId) return
    queryParams.roleId = roleId
    await Promise.all([getList(), loadSelectedUsers()])
  }

  watch(
    () => [props.visible, props.roleData],
    async ([visible]) => {
      if (visible) {
        resetForm()
        if (props.roleData) {
          await loadData(props.roleData.roleId)
        }
      }
    },
    { immediate: true }
  )

  const handleSelectionChange = (selection: UserItem[]) => {
    selectedIds.value = selection.map((item) => item.userId)
  }

  const handleQuery = () => {
    queryParams.pageNum = 1
    getList()
  }

  const resetQuery = () => {
    queryParams.userName = undefined
    queryParams.phoneNumber = undefined
    queryParams.pageNum = 1
    getList()
  }

  const addSelected = async () => {
    if (selectedIds.value.length === 0 || !queryParams.roleId) return
    try {
      const { assignUsersToRole } = await import('@/api/system/role')
      await assignUsersToRole(queryParams.roleId, selectedIds.value)
      selectedIds.value = []
      tableRef.value?.clearSelection()
      await Promise.all([getList(), loadSelectedUsers()])
      emit('success')
    } catch (error) {
      console.error('添加用户失败:', error)
    }
  }

  const removeSelected = async (userId: string | number) => {
    if (!queryParams.roleId) return
    try {
      const { cancelUsersFromRole } = await import('@/api/system/role')
      await cancelUsersFromRole(queryParams.roleId, [userId])
      await Promise.all([getList(), loadSelectedUsers()])
      emit('success')
    } catch (error) {
      console.error('取消授权失败:', error)
    }
  }

  const removeAllSelected = async () => {
    if (selectedUsers.value.length === 0 || !queryParams.roleId) return
    try {
      const { cancelUsersFromRole } = await import('@/api/system/role')
      const userIds = selectedUsers.value.map((u) => u.userId)
      await cancelUsersFromRole(queryParams.roleId, userIds)
      await Promise.all([getList(), loadSelectedUsers()])
      emit('success')
    } catch (error) {
      console.error('批量取消授权失败:', error)
    }
  }

  const handleClose = () => {
    dialogVisible.value = false
    resetForm()
  }
</script>

<style lang="scss" scoped>
  .select-user-container {
    display: flex;
    gap: 12px;
    height: 450px;

    @media (width <= 768px) {
      flex-direction: column;
      height: auto;
    }
  }

  .search-section {
    display: flex;
    flex: 2;
    flex-direction: column;
    gap: 12px;

    @media (width <= 768px) {
      flex: none;
    }
  }

  .search-card {
    flex-shrink: 0;

    :deep(.el-card__body) {
      padding: 12px 16px;
    }
  }

  .query-form {
    :deep(.el-form-item) {
      margin-right: 8px;
      margin-bottom: 0;
    }

    .search-input {
      width: 140px;
    }
  }

  .table-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-card__body) {
      display: flex;
      flex: 1;
      flex-direction: column;
      padding: 0;
      overflow: hidden;
    }

    :deep(.el-table) {
      flex: 1;

      :deep(.el-table__header-wrapper) {
        background-color: var(--el-bg-color-page);
      }

      :deep(.el-table__header-row) {
        background-color: var(--el-bg-color-page) !important;
      }

      :deep(.el-table__header-cell) {
        background-color: var(--el-bg-color-page) !important;
      }
    }

    .pagination {
      flex-shrink: 0;
      padding: 12px 16px;
      border-top: 1px solid var(--el-border-color-lighter);
    }
  }

  .action-section {
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    gap: 12px;
    justify-content: center;
    padding: 8px 0;

    @media (width <= 768px) {
      flex-direction: row;
      justify-content: center;
    }
  }

  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    color: #fff;
    cursor: pointer;
    background-color: var(--el-color-primary);
    border-radius: 50%;
    transition: all 0.2s ease;

    &:hover {
      opacity: 0.8;
    }

    &.disabled {
      cursor: not-allowed;
      opacity: 0.4;
    }

    :deep(.el-icon) {
      font-size: 16px;
    }

    @media (width <= 768px) {
      width: 40px;
      height: 40px;
    }
  }

  .selected-section {
    display: flex;
    flex: 1;
    flex-direction: column;

    @media (width <= 768px) {
      flex: none;
    }
  }

  .selected-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;

    :deep(.el-card__body) {
      flex: 1;
      padding: 0;
      overflow: hidden;
    }
  }

  .selected-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .count {
      padding: 2px 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      background-color: var(--el-fill-color);
      border-radius: 10px;
    }
  }

  .selected-list {
    height: 100%;
    padding: 8px;
    overflow-y: auto;
  }

  .selected-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    margin-bottom: 4px;
    cursor: pointer;
    background-color: var(--el-fill-color-light);
    border-radius: 4px;
    transition: all 0.2s ease;

    &:hover {
      background-color: var(--el-color-primary-light-5);
    }

    .user-info {
      flex: 1;
      overflow: hidden;
      font-size: 13px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .remove-icon {
      flex-shrink: 0;
      margin-left: 8px;
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }

  .empty-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: var(--el-text-color-secondary);

    p {
      margin-top: 8px;
      font-size: 13px;
    }
  }

  .status-normal {
    font-size: 12px;
    color: var(--el-color-success);
  }

  .status-disabled {
    font-size: 12px;
    color: var(--el-color-danger);
  }
</style>
