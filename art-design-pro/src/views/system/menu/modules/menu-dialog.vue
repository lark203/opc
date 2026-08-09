<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="40%"
    align-center
    class="menu-dialog"
    @closed="handleClosed"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
      <ElRow :gutter="20">
        <ElCol :span="24">
          <ElFormItem label="上级菜单">
            <ElTreeSelect
              v-model="form.parentId"
              :data="menuOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="选择上级菜单"
              check-strictly
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="菜单类型" prop="menuType">
            <ElRadioGroup v-model="form.menuType">
              <ElRadio value="M">目录</ElRadio>
              <ElRadio value="C">菜单</ElRadio>
              <ElRadio value="F">按钮</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType !== 'F'" :span="24">
          <ElFormItem label="菜单图标" prop="icon">
            <IconSelect v-model="form.icon" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="菜单名称" prop="menuName">
            <ElInput v-model="form.menuName" placeholder="请输入菜单名称" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="显示排序" prop="orderNum">
            <ElInputNumber v-model="form.orderNum" controls-position="right" :min="0" />
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType !== 'F'" :span="12">
          <ElFormItem>
            <template #label>
              <span>
                <ElTooltip content="选择是外链则路由地址需要以`http(s)://`开头" placement="top">
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                是否外链
              </span>
            </template>
            <ElRadioGroup v-model="form.isFrame">
              <ElRadio v-for="dict in sys_yes_no" :key="dict.value" :value="dict.value">
                {{ dict.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType !== 'F'" :span="12">
          <ElFormItem prop="path">
            <template #label>
              <span>
                <ElTooltip
                  content="访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                路由地址
              </span>
            </template>
            <ElInput v-model="form.path" placeholder="请输入路由地址" />
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType === 'C'" :span="12">
          <ElFormItem prop="component">
            <template #label>
              <span>
                <ElTooltip
                  content="访问的组件路径，如：`system/user/index`，默认在`views`目录下"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                组件路径
              </span>
            </template>
            <ElInput v-model="form.component" placeholder="请输入组件路径" />
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType !== 'M'" :span="12">
          <ElFormItem>
            <template #label>
              <span>
                <ElTooltip
                  content="控制器中定义的权限字符，如：@SaCheckPermission('system:user:list')"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                权限字符
              </span>
            </template>
            <ElInput v-model="form.perms" placeholder="请输入权限标识" maxlength="100" />
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType === 'C'" :span="12">
          <ElFormItem>
            <template #label>
              <span>
                <ElTooltip
                  content='访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`'
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                路由参数
              </span>
            </template>
            <ElInput v-model="form.queryParam" placeholder="请输入路由参数" maxlength="255" />
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType === 'C'" :span="12">
          <ElFormItem>
            <template #label>
              <span>
                <ElTooltip
                  content="选择是则会被`keep-alive`缓存，需要匹配组件的`name`和地址保持一致"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                是否缓存
              </span>
            </template>
            <ElRadioGroup v-model="form.isCache">
              <ElRadio value="Y">缓存</ElRadio>
              <ElRadio value="N">不缓存</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.menuType !== 'F'" :span="12">
          <ElFormItem>
            <template #label>
              <span>
                <ElTooltip
                  content="选择隐藏则路由将不会出现在侧边栏，但仍然可以访问"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                显示状态
              </span>
            </template>
            <ElRadioGroup v-model="form.visible">
              <ElRadio v-for="dict in sys_show_hide" :key="dict.value" :value="dict.value">
                {{ dict.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem>
            <template #label>
              <span>
                <ElTooltip content="选择停用则路由将不会出现在侧边栏，也不能被访问" placement="top">
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                菜单状态
              </span>
            </template>
            <ElRadioGroup v-model="form.status">
              <ElRadio v-for="dict in sys_normal_disable" :key="dict.value" :value="dict.value">
                {{ dict.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol v-if="form.visible !== '0'" :span="12">
          <ElFormItem label="激活路由" prop="activeMenu">
            <template #label>
              <span>
                <ElTooltip
                  content="隐藏菜单填写默认激活路由，比如激活父菜单的路由 /system/user"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                激活路由
              </span>
            </template>
            <ElInput v-model="form.activeMenu" placeholder="请输入激活路径" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="备注" prop="remark">
            <ElInput v-model="form.remark" placeholder="请输入备注" maxlength="500" />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, toRefs, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { ElIcon, ElMessage, ElRadio, ElRadioGroup, ElTooltip, ElTreeSelect } from 'element-plus'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import type { MenuForm, MenuVO } from '@/api/system/menu'
  import { addMenu, getMenu, treeselect, updateMenu } from '@/api/system/menu'
  import { useDict } from '@/utils/dict'
  import IconSelect from '@/components/IconSelect/index.vue'

  const { sys_show_hide, sys_normal_disable, sys_yes_no } = toRefs(
    useDict('sys_show_hide', 'sys_normal_disable', 'sys_yes_no')
  )

  interface Props {
    visible: boolean
    editData?: MenuVO
    parentMenu?: MenuVO
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const isEdit = ref(false)
  const menuOptions = ref<any[]>([])

  const form = reactive<MenuForm>({
    menuId: undefined,
    menuName: '',
    icon: '',
    parentId: 0,
    orderNum: 1,
    menuType: 'M',
    path: '',
    component: '',
    queryParam: '',
    isFrame: 'N',
    isCache: 'Y',
    visible: '0',
    status: '0',
    perms: '',
    activeMenu: '',
    remark: ''
  })

  const rules = reactive<FormRules>({
    menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '菜单顺序不能为空', trigger: 'blur' }],
    path: [{ required: true, message: '路由地址不能为空', trigger: 'blur' }]
  })

  const dialogTitle = computed(() => {
    return isEdit.value ? '修改菜单' : '新增菜单'
  })

  const loadTreeselect = async () => {
    const response = await treeselect()
    const menu: any = { id: 0, label: '主类目', children: [] }
    menu.children = response || []
    menuOptions.value = [menu]
  }

  const resetForm = () => {
    formRef.value?.resetFields()
    Object.assign(form, {
      menuId: undefined,
      menuName: '',
      icon: '',
      parentId: 0,
      orderNum: 1,
      menuType: 'M',
      path: '',
      component: '',
      queryParam: '',
      isFrame: 'N',
      isCache: 'Y',
      visible: '0',
      status: '0',
      perms: '',
      activeMenu: '',
      remark: ''
    })
  }

  const loadFormData = async () => {
    if (!props.editData?.menuId) return
    isEdit.value = true
    const data = await getMenu(props.editData.menuId)
    Object.assign(form, data)
  }

  const handleSubmit = async () => {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        if (form.menuId) {
          updateMenu(form).then(() => {
            ElMessage.success('修改成功')
            emit('success')
            handleCancel()
          })
        } else {
          addMenu(form).then(() => {
            ElMessage.success('新增成功')
            emit('success')
            handleCancel()
          })
        }
      }
    })
  }

  const handleCancel = () => {
    emit('update:visible', false)
  }

  const handleClosed = () => {
    resetForm()
    isEdit.value = false
  }

  watch(
    () => props.visible,
    async (newVal) => {
      if (newVal) {
        await loadTreeselect()
        if (props.parentMenu) {
          form.parentId = props.parentMenu.menuId
        }
        if (props.editData) {
          await loadFormData()
        }
      }
    }
  )
</script>
