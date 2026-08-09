<template>
  <div class="menu-page art-full-height">
    <ArtSearchBar
      v-model="searchForm"
      :items="formItems"
      @reset="handleReset"
      @search="handleSearch"
    />
    <ElCard class="art-table-card">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" v-auth="'system:menu:add'" @click="() => handleAdd()"
              >新增</ElButton
            >
            <ElButton type="danger" v-auth="'system:menu:remove'" @click="handleCascadeDelete"
              >级联删除</ElButton
            >
            <ElButton @click="toggleExpand">{{ isExpanded ? '收起' : '展开' }}</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>
      <ArtTable
        ref="tableRef"
        row-key="menuId"
        :loading="loading"
        :columns="columns"
        :data="menuList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />
    </ElCard>
    <MenuDialog
      v-model:visible="dialogVisible"
      :edit-data="currentMenu"
      :parent-menu="parentMenu"
      @success="handleRefresh"
    />
    <ElDialog v-model="deleteDialogVisible" title="级联删除菜单" width="30%" align-center>
      <ElTree
        ref="menuTreeRef"
        :data="menuOptions"
        show-checkbox
        node-key="menuId"
        :check-strictly="false"
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <span class="dialog-footer">
          <ElButton @click="deleteDialogVisible = false">取 消</ElButton>
          <ElButton type="danger" @click="handleSubmitCascadeDelete" :loading="deleteLoading"
            >确 定</ElButton
          >
        </span>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, nextTick, reactive, ref, toRefs } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import MenuDialog from './modules/menu-dialog.vue'
  import { useDict } from '@/utils/dict'
  import {
    cascadeDelMenu,
    delMenu,
    listMenu,
    type MenuQuery,
    type MenuVO,
    treeselect
  } from '@/api/system/menu'

  const { sys_normal_disable, sys_show_hide } = toRefs(
    useDict('sys_normal_disable', 'sys_show_hide')
  )

  const loading = ref(false)
  const isExpanded = ref(false)
  const tableRef = ref()
  const menuTreeRef = ref()
  const deleteLoading = ref(false)

  const dialogVisible = ref(false)
  const deleteDialogVisible = ref(false)
  const currentMenu = ref<MenuVO>()
  const parentMenu = ref<MenuVO>()

  let searchForm = reactive<MenuQuery>({
    menuName: '',
    status: ''
  })

  const menuList = ref<MenuVO[]>([])
  const menuOptions = ref<any[]>([])

  const formItems = computed(() => [
    {
      label: '菜单名称',
      key: 'menuName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入菜单名称' }
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

  const isMenuIconVisible = (icon?: string): boolean => {
    const normalizedIcon = icon?.trim()
    return !!normalizedIcon && normalizedIcon !== '#'
  }

  const getMenuTypeTag = (row: MenuVO): 'primary' | 'success' | 'warning' | 'info' | 'danger' => {
    if (row.menuType === 'F') return 'warning'
    if (row.isFrame === 'Y') return 'danger'
    if (row.menuType === 'M') return 'primary'
    return 'success'
  }

  const getMenuTypeText = (row: MenuVO): string => {
    if (row.menuType === 'F') return '按钮'
    if (row.isFrame === 'Y') return '外链'
    if (row.menuType === 'M') return '目录'
    return '菜单'
  }

  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'menuName',
      label: '菜单名称',
      minWidth: 220,
      formatter: (row: MenuVO) => {
        return h('div', { class: 'menu-name-cell' }, [
          isMenuIconVisible(row.icon) ? h(ArtSvgIcon, { icon: row.icon }) : '',
          h('span', { class: 'menu-name-text' }, row.menuName)
        ])
      }
    },
    {
      prop: 'menuType',
      label: '类型',
      width: 100,
      align: 'center',
      formatter: (row: MenuVO) => {
        return h(ElTag, { type: getMenuTypeTag(row), size: 'small' }, () => getMenuTypeText(row))
      }
    },
    { prop: 'orderNum', label: '排序', width: 60, align: 'center' },
    { prop: 'path', label: '路由地址', showOverflowTooltip: true },
    { prop: 'perms', label: '权限标识', showOverflowTooltip: true },
    { prop: 'component', label: '组件路径', showOverflowTooltip: true },
    {
      prop: 'status',
      label: '状态',
      width: 80,
      align: 'center',
      formatter: (row: MenuVO) =>
        h(DictTag, { options: sys_normal_disable.value, value: row.status })
    },
    {
      prop: 'visible',
      label: '显示',
      width: 90,
      align: 'center',
      formatter: (row: MenuVO) => h(DictTag, { options: sys_show_hide.value, value: row.visible })
    },
    {
      prop: 'operation',
      label: '操作',
      width: 180,
      fixed: 'right',
      align: 'center',
      formatter: (row: MenuVO) => {
        return h('div', [
          h(ArtButtonTable, {
            type: 'edit',
            auth: 'system:menu:edit',
            onClick: () => handleEdit(row)
          }),
          h(ArtButtonTable, {
            type: 'add',
            title: '新增子菜单',
            auth: 'system:menu:add',
            onClick: () => handleAdd(row)
          }),
          h(ArtButtonTable, {
            type: 'delete',
            auth: 'system:menu:remove',
            onClick: () => handleDelete(row)
          })
        ])
      }
    }
  ])

  const buildMenuTree = (menus: MenuVO[], parentId: string | number): MenuVO[] => {
    return menus
      .filter((menu) => String(menu.parentId) === String(parentId))
      .map((menu) => ({
        ...menu,
        hasChildren: menus.some((m) => String(m.parentId) === String(menu.menuId)),
        children: buildMenuTree(menus, menu.menuId)
      }))
  }

  const getList = async () => {
    loading.value = true
    try {
      const data = await listMenu(searchForm)
      menuList.value = buildMenuTree(data, 0)
    } catch (error) {
      console.error('获取菜单失败:', error)
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    getList()
  }

  const handleReset = () => {
    searchForm.menuName = ''
    searchForm.status = ''
    getList()
  }

  const handleRefresh = () => {
    getList()
  }

  const handleAdd = (row?: MenuVO) => {
    currentMenu.value = undefined
    parentMenu.value = row
    dialogVisible.value = true
  }

  const handleEdit = (row: MenuVO) => {
    currentMenu.value = row
    parentMenu.value = undefined
    dialogVisible.value = true
  }

  const handleDelete = async (row: MenuVO) => {
    try {
      await ElMessageBox.confirm(`确定要删除名称为"${row.menuName}"的数据项？`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await delMenu(row.menuId)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }

  const handleCascadeDelete = async () => {
    await loadTreeselect()
    menuTreeRef.value?.setCheckedKeys([])
    deleteDialogVisible.value = true
  }

  const loadTreeselect = async () => {
    const response = await treeselect()
    const menu: any = { menuId: 0, menuName: '主类目', children: [] }
    menu.children = response
    menuOptions.value = [menu]
  }

  const handleSubmitCascadeDelete = async () => {
    const menuIds = menuTreeRef.value?.getCheckedKeys() || []
    if (menuIds.length === 0) {
      ElMessage.warning('请选择要删除的菜单')
      return
    }
    deleteLoading.value = true
    try {
      await cascadeDelMenu(menuIds)
      ElMessage.success('删除成功')
      deleteDialogVisible.value = false
      getList()
    } catch {
      ElMessage.error('删除失败')
    } finally {
      deleteLoading.value = false
    }
  }

  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && menuList.value) {
        const processRows = (rows: MenuVO[]) => {
          rows.forEach((row) => {
            if (row.children?.length || row.hasChildren) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              if (row.children?.length) {
                processRows(row.children)
              }
            }
          })
        }
        processRows(menuList.value)
      }
    })
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="scss" scoped>
  :deep(.menu-name-cell) {
    display: inline-flex;
    gap: 8px;
    align-items: center;
    min-width: 0;
    vertical-align: middle;
  }

  :deep(.menu-name-text) {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

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

        > .cell > .menu-name-cell {
          min-width: 0 !important;
        }
      }
    }

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

  :deep(.el-table__body-wrapper) {
    .el-table__row {
      transition: background-color 0.15s ease;

      &:hover > td {
        background-color: rgb(64 158 255 / 6%) !important;
      }
    }
  }
</style>
