<template>
  <ElDialog v-model="visible" title="分配权限" width="30%" align-center @close="handleClose">
    <ElForm ref="formRef" :model="formData" label-width="90px">
      <ElTabs v-model="activeTab">
        <ElTabPane label="菜单权限" name="menu">
          <div class="permission-toolbar">
            <ElCheckbox v-model="menuExpand" @change="handleExpand('menu')">展开/折叠</ElCheckbox>
            <ElCheckbox v-model="menuSelectAll" @change="handleSelectAll('menu')"
              >全选/全不选</ElCheckbox
            >
            <ElCheckbox v-model="formData.menuCheckStrictly" @change="handleCheckStrictly('menu')"
              >父子联动</ElCheckbox
            >
          </div>
          <div class="tree-container">
            <ElTree
              ref="menuTreeRef"
              :data="menuOptions"
              show-checkbox
              node-key="id"
              :check-strictly="!formData.menuCheckStrictly"
              :props="{ label: 'label', children: 'children', disabled: 'disabled' }"
              @check="handleMenuCheck"
            >
              <template #default="{ data, node }">
                <div class="tree-node-row">
                  <span
                    class="tree-node-label"
                    :class="{
                      'is-hidden': isMenuHidden(data),
                      'is-disabled': isMenuDisabled(data)
                    }"
                    :style="getNodeStyle(node.level)"
                  >
                    {{ data.label }}
                    <ElTooltip v-if="isMenuHidden(data)" content="隐藏" placement="top">
                      <ElIcon class="menu-icon"><Hide /></ElIcon>
                    </ElTooltip>
                    <ElTooltip v-if="isMenuDisabled(data)" content="停用" placement="top">
                      <ElIcon class="menu-icon menu-disabled-icon"><CircleCloseFilled /></ElIcon>
                    </ElTooltip>
                  </span>
                  <div v-if="data.buttonPermissions?.length" class="tree-node-buttons">
                    <ElCheckbox
                      v-for="button in data.buttonPermissions"
                      :key="button.menuId"
                      :model-value="isButtonChecked(button.menuId)"
                      :disabled="button.disabled"
                      @change="(val) => handleButtonChange(data.id, button.menuId, val as boolean)"
                      @click.stop
                    >
                      <span class="button-label" :class="{ 'is-disabled': button.disabled }">
                        {{ button.menuName }}
                        <ElTooltip v-if="button.disabled" content="停用" placement="top">
                          <ElIcon class="button-icon"><CircleCloseFilled /></ElIcon>
                        </ElTooltip>
                      </span>
                    </ElCheckbox>
                  </div>
                </div>
              </template>
            </ElTree>
          </div>
        </ElTabPane>
        <ElTabPane label="数据权限" name="data">
          <ElFormItem label="权限范围">
            <ElSelect v-model="formData.dataScope" @change="handleDataScopeChange">
              <ElOption
                v-for="item in dataScopeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
          <ElFormItem v-if="formData.dataScope === '2'" label="数据权限">
            <div class="permission-toolbar">
              <ElCheckbox v-model="deptExpand" @change="handleExpand('dept')">展开/折叠</ElCheckbox>
              <ElCheckbox v-model="deptSelectAll" @change="handleSelectAll('dept')"
                >全选/全不选</ElCheckbox
              >
              <ElCheckbox v-model="formData.deptCheckStrictly" @change="handleCheckStrictly('dept')"
                >父子联动</ElCheckbox
              >
            </div>
          </ElFormItem>
          <ElFormItem v-if="formData.dataScope === '2'">
            <div class="tree-container">
              <ElTree
                ref="deptTreeRef"
                :data="deptOptions"
                show-checkbox
                default-expand-all
                node-key="id"
                :check-strictly="!formData.deptCheckStrictly"
                :props="{ label: 'label', children: 'children' }"
              />
            </div>
          </ElFormItem>
        </ElTabPane>
      </ElTabs>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleClose">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import type { FormInstance, TreeInstance } from 'element-plus'
  import { ElIcon, ElMessage, ElTooltip } from 'element-plus'
  import { CircleCloseFilled, Hide } from '@element-plus/icons-vue'
  import type { DeptTreeOption, RoleForm, RoleVO } from '@/api/system/role'
  import { deptTreeSelect, getRole, updateRolePermission } from '@/api/system/role'
  import { roleMenuTreeselect } from '@/api/system/menu'

  interface Props {
    modelValue: boolean
    roleData?: RoleVO
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const formRef = ref<FormInstance>()
  const menuTreeRef = ref<TreeInstance>()
  const deptTreeRef = ref<TreeInstance>()

  const activeTab = ref<'menu' | 'data'>('menu')

  const menuOptions = ref<any[]>([])
  const deptOptions = ref<DeptTreeOption[]>([])

  const menuExpand = ref(false)
  const menuSelectAll = ref(false)
  const deptExpand = ref(true)
  const deptSelectAll = ref(false)

  const selectedMenuIds = ref<Array<string | number>>([])
  const selectedButtonIds = ref<Array<string | number>>([])

  const dataScopeOptions = [
    { value: '1', label: '全部数据权限' },
    { value: '2', label: '自定数据权限' },
    { value: '3', label: '本部门数据权限' },
    { value: '4', label: '本部门及以下数据权限' },
    { value: '5', label: '仅本人数据权限' },
    { value: '6', label: '部门及以下或本人数据权限' }
  ]

  const formData = reactive<RoleForm>({
    roleName: '',
    roleKey: '',
    roleSort: 1,
    status: '0',
    menuCheckStrictly: true,
    deptCheckStrictly: true,
    remark: '',
    dataScope: '1',
    roleId: undefined,
    menuIds: [],
    deptIds: []
  })

  const getNodeStyle = (level: number) => ({
    paddingLeft: `${Math.max(0, level - 1) * 18}px`
  })

  const isMenuHidden = (menu: any) => menu.visible === '1'

  const isMenuDisabled = (menu: any) => menu.status === '1'

  const isButtonChecked = (menuId: string | number) => {
    return selectedButtonIds.value.some((id) => String(id) === String(menuId))
  }

  const handleButtonChange = (
    parentId: string | number,
    buttonId: string | number,
    checked: boolean
  ) => {
    if (checked) {
      selectedButtonIds.value.push(buttonId)
    } else {
      selectedButtonIds.value = selectedButtonIds.value.filter(
        (id) => String(id) !== String(buttonId)
      )
    }
    updateMenuTreeChecked()
  }

  const handleMenuCheck = () => {
    if (!menuTreeRef.value) return
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    const allChecked = [...checkedKeys, ...halfCheckedKeys]
    selectedMenuIds.value = allChecked.filter((id) => {
      const isButton = selectedButtonIds.value.some((bId) => String(bId) === String(id))
      return !isButton
    })
    updateSelectAllStatus()
  }

  const updateMenuTreeChecked = () => {
    if (!menuTreeRef.value) return
    const allChecked = [...selectedMenuIds.value, ...selectedButtonIds.value]
    menuTreeRef.value.setCheckedKeys(allChecked)
    updateSelectAllStatus()
  }

  const updateSelectAllStatus = () => {
    if (!menuTreeRef.value) return
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    const allMenuIds: Array<string | number> = []
    const collectIds = (nodes: any[]) => {
      nodes.forEach((node) => {
        allMenuIds.push(node.id)
        if (node.children) collectIds(node.children)
      })
    }
    collectIds(menuOptions.value)
    menuSelectAll.value = checkedKeys.length === allMenuIds.length && allMenuIds.length > 0
  }

  const handleExpand = (type: 'menu' | 'dept') => {
    const expanded = type === 'menu' ? menuExpand.value : deptExpand.value
    const treeRef = type === 'menu' ? menuTreeRef.value : deptTreeRef.value
    if (!treeRef) return
    const nodes = treeRef.store.nodesMap as Record<string, { expanded: boolean }>
    Object.keys(nodes).forEach((nodeId) => {
      nodes[nodeId].expanded = expanded
    })
  }

  const handleSelectAll = (type: 'menu' | 'dept') => {
    const selectAll = type === 'menu' ? menuSelectAll.value : deptSelectAll.value
    const treeRef = type === 'menu' ? menuTreeRef.value : deptTreeRef.value
    if (!treeRef) return

    if (selectAll) {
      const allIds: Array<string | number> = []
      const collectIds = (nodes: any[]) => {
        nodes.forEach((node) => {
          allIds.push(node.id)
          if (node.buttonPermissions) {
            node.buttonPermissions.forEach((btn: any) => allIds.push(btn.menuId))
          }
          if (node.children) collectIds(node.children)
        })
      }
      collectIds(type === 'menu' ? menuOptions.value : deptOptions.value)
      if (type === 'menu') {
        selectedMenuIds.value = allIds.filter((id) => !isNaN(Number(id)))
        selectedButtonIds.value = allIds.filter((id) => isNaN(Number(id)))
        updateMenuTreeChecked()
      } else {
        const deptIds: Array<string | number> = []
        const collectDeptIds = (nodes: DeptTreeOption[]) => {
          nodes.forEach((node) => {
            deptIds.push(node.id)
            if (node.children) collectDeptIds(node.children)
          })
        }
        collectDeptIds(deptOptions.value)
        treeRef.setCheckedKeys(deptIds)
      }
    } else {
      if (type === 'menu') {
        selectedMenuIds.value = []
        selectedButtonIds.value = []
        updateMenuTreeChecked()
      } else {
        treeRef.setCheckedKeys([])
      }
    }
  }

  const handleCheckStrictly = (type: 'menu' | 'dept') => {
    nextTick(() => {
      if (type === 'menu') {
        updateMenuTreeChecked()
      }
    })
  }

  const handleDataScopeChange = () => {
    if (formData.dataScope !== '2') {
      deptTreeRef.value?.setCheckedKeys([])
    }
  }

  const getDeptCheckedKeys = () => {
    if (!deptTreeRef.value) return []
    const checkedKeys = deptTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = deptTreeRef.value.getHalfCheckedKeys()
    return [...halfCheckedKeys, ...checkedKeys]
  }

  const loadRoleData = async (roleId?: string | number) => {
    if (!roleId) return

    const [roleRes, menuRes, deptRes] = await Promise.all([
      getRole(roleId),
      roleMenuTreeselect(roleId),
      deptTreeSelect(roleId)
    ])

    Object.assign(formData, roleRes)
    formData.roleSort = Number(formData.roleSort)

    const menus = (menuRes as any).data?.menus || (menuRes as any).menus || []
    menuOptions.value = menus

    const checkedKeys = (menuRes as any).data?.checkedKeys || (menuRes as any).checkedKeys || []
    selectedMenuIds.value = checkedKeys.filter((id: string | number) => !isNaN(Number(id)))
    selectedButtonIds.value = checkedKeys.filter((id: string | number) => isNaN(Number(id)))

    deptOptions.value = deptRes.depts || []

    nextTick(() => {
      updateMenuTreeChecked()
      updateSelectAllStatus()
      deptTreeRef.value?.setCheckedKeys(deptRes.checkedKeys || [])
    })
  }

  const resetForm = () => {
    activeTab.value = 'menu'
    formData.roleName = ''
    formData.roleKey = ''
    formData.roleSort = 1
    formData.status = '0'
    formData.menuCheckStrictly = true
    formData.deptCheckStrictly = true
    formData.remark = ''
    formData.dataScope = '1'
    formData.roleId = undefined
    formData.menuIds = []
    formData.deptIds = []
    selectedMenuIds.value = []
    selectedButtonIds.value = []
    menuOptions.value = []
    deptOptions.value = []
    menuExpand.value = false
    menuSelectAll.value = false
    deptExpand.value = true
    deptSelectAll.value = false
    menuTreeRef.value?.setCheckedKeys([])
    deptTreeRef.value?.setCheckedKeys([])
    formRef.value?.resetFields()
    formRef.value?.clearValidate()
  }

  watch(
    () => [props.modelValue, props.roleData],
    async ([visible, roleData]) => {
      if (visible && roleData && typeof roleData === 'object' && 'roleId' in roleData) {
        resetForm()
        await loadRoleData(roleData.roleId)
      }
    },
    { immediate: true }
  )

  const handleClose = () => {
    visible.value = false
    resetForm()
  }

  const handleSubmit = async () => {
    if (!formData.roleId) return

    formData.menuIds = [...selectedMenuIds.value, ...selectedButtonIds.value]
    formData.deptIds = getDeptCheckedKeys()

    await updateRolePermission(formData)
    ElMessage.success('权限修改成功')
    emit('success')
    handleClose()
  }
</script>

<style lang="scss" scoped>
  .permission-toolbar {
    display: flex;
    gap: 16px;
    width: 100%;
    padding-bottom: 12px;
    margin-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .tree-container {
    width: 100%;
    max-height: 220px;
    padding: 8px;
    overflow-y: auto;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
  }

  .tree-node-row {
    display: flex;
    gap: 12px;
    align-items: flex-start;
    width: 100%;
  }

  .tree-node-label {
    display: inline-flex;
    flex-shrink: 0;
    gap: 6px;
    align-items: center;
    line-height: 24px;
    color: var(--el-text-color-primary);
  }

  .tree-node-label.is-hidden:not(.is-disabled) {
    color: var(--el-text-color-secondary);
  }

  .tree-node-label.is-disabled {
    color: var(--el-color-danger);
  }

  .menu-icon {
    flex-shrink: 0;
    margin-top: 1px;
    font-size: 14px;
    color: var(--el-text-color-secondary);
  }

  .menu-icon.menu-disabled-icon {
    color: var(--el-color-danger);
  }

  .tree-node-buttons {
    display: flex;
    flex: 1;
    flex-wrap: wrap;
    gap: 8px;
  }

  .button-label {
    display: inline-flex;
    gap: 4px;
    align-items: center;
  }

  .button-label.is-disabled {
    color: var(--el-color-danger);
  }

  .button-icon {
    flex-shrink: 0;
    margin-top: 1px;
    font-size: 14px;
    color: var(--el-color-danger);
  }
</style>
