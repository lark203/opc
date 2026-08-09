<template>
  <ElForm ref="formRef" :model="infoForm" :rules="rules" label-width="150px">
    <ElRow>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="tplCategory" label="生成模板">
          <ElSelect v-model="infoForm.tplCategory" placeholder="请选择生成模板">
            <ElOption label="单表（增删改查）" value="crud" />
            <ElOption label="树表（增删改查）" value="tree" />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="packageName">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="生成在哪个 java 包下，例如 com.ruoyi.system" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              生成包路径
            </span>
          </template>
          <ElInput v-model="infoForm.packageName" placeholder="请输入生成包路径" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="moduleName">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="可理解为子系统名，例如 system" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              生成模块名
            </span>
          </template>
          <ElInput v-model="infoForm.moduleName" placeholder="请输入生成模块名" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="businessName">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="可理解为功能英文名，例如 user" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              生成业务名
            </span>
          </template>
          <ElInput v-model="infoForm.businessName" placeholder="请输入生成业务名" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="functionName">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="用作类描述，例如 用户" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              生成功能名
            </span>
          </template>
          <ElInput v-model="infoForm.functionName" placeholder="请输入生成功能名" />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="parentMenuId">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="分配到指定菜单下，例如 系统管理" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              上级菜单
            </span>
          </template>
          <ElTreeSelect
            v-model="infoForm.parentMenuId"
            :data="menuOptions"
            :props="{ value: 'id', label: 'label', children: 'children' }"
            value-key="id"
            node-key="id"
            placeholder="选择上级菜单"
            check-strictly
            filterable
            clearable
            highlight-current
          />
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="12">
        <ElFormItem prop="frontendType">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip
                content="对应后端 resources/vm 下的模板目录，例如 vue、react"
                placement="top"
              >
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              前端模板
            </span>
          </template>
          <ElRadioGroup v-model="infoForm.frontendType">
            <ElRadio v-for="item in frontendTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElCol>
    </ElRow>

    <h4 class="gen-form-header">增强选项</h4>
    <ElRow :gutter="20">
      <ElCol :xs="24" :sm="8">
        <ElFormItem prop="enableExport">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="关闭后将不生成 export 接口与前端导出按钮" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              导出能力
            </span>
          </template>
          <ElSwitch v-model="infoForm.enableExport" />
        </ElFormItem>
      </ElCol>
      <ElCol v-if="infoForm.enableStatus" :xs="24" :sm="16">
        <ElFormItem prop="statusField" label="状态字段">
          <ElSelect v-model="infoForm.statusField" placeholder="请选择状态字段" clearable>
            <ElOption
              v-for="column in availableColumns"
              :key="column.columnName"
              :label="`${column.columnName}：${column.columnComment || ''}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="8">
        <ElFormItem prop="enableStatus">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="开启后生成 changeStatus 接口与列表状态开关列" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              状态切换
            </span>
          </template>
          <ElSwitch v-model="infoForm.enableStatus" />
        </ElFormItem>
      </ElCol>
      <ElCol v-if="infoForm.enableUnique" :xs="24" :sm="16">
        <ElFormItem prop="uniqueFields" label="唯一字段">
          <ElSelect
            v-model="infoForm.uniqueFields"
            multiple
            clearable
            filterable
            placeholder="请选择唯一字段"
          >
            <ElOption
              v-for="column in availableColumns"
              :key="column.columnName"
              :label="`${column.columnName}：${column.columnComment || ''}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="8">
        <ElFormItem prop="enableUnique">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip content="开启后按选中的字段生成组合唯一校验" placement="top">
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              组合唯一校验
            </span>
          </template>
          <ElSwitch v-model="infoForm.enableUnique" />
        </ElFormItem>
      </ElCol>
      <ElCol v-if="infoForm.enableSort" :xs="24" :sm="16">
        <ElFormItem prop="sortField" label="排序字段">
          <ElSelect v-model="infoForm.sortField" placeholder="请选择排序字段" clearable>
            <ElOption
              v-for="column in sortableColumns"
              :key="column.columnName"
              :label="`${column.columnName}：${column.columnComment || ''}`"
              :value="column.columnName"
            />
          </ElSelect>
        </ElFormItem>
      </ElCol>
      <ElCol :xs="24" :sm="8">
        <ElFormItem prop="enableSort">
          <template #label>
            <span class="gen-label-help">
              <ElTooltip
                content="开启后生成 updateSort 接口，并在列表中以输入框形式快速调整排序"
                placement="top"
              >
                <ElIcon><QuestionFilled /></ElIcon>
              </ElTooltip>
              排序调整
            </span>
          </template>
          <ElSwitch v-model="infoForm.enableSort" />
        </ElFormItem>
      </ElCol>
    </ElRow>

    <template v-if="infoForm.tplCategory === 'tree'">
      <h4 class="gen-form-header">其他信息</h4>
      <ElRow :gutter="20">
        <ElCol :xs="24" :sm="12">
          <ElFormItem prop="treeCode">
            <template #label>
              <span class="gen-label-help">
                <ElTooltip content="树显示的编码字段名，如：dept_id" placement="top">
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                树编码字段
              </span>
            </template>
            <ElSelect v-model="infoForm.treeCode" placeholder="请选择" clearable>
              <ElOption
                v-for="column in availableColumns"
                :key="column.columnName"
                :label="`${column.columnName}：${column.columnComment || ''}`"
                :value="column.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem prop="treeParentCode">
            <template #label>
              <span class="gen-label-help">
                <ElTooltip content="树显示的父编码字段名，如：parent_id" placement="top">
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                树父编码字段
              </span>
            </template>
            <ElSelect v-model="infoForm.treeParentCode" placeholder="请选择" clearable>
              <ElOption
                v-for="column in availableColumns"
                :key="column.columnName"
                :label="`${column.columnName}：${column.columnComment || ''}`"
                :value="column.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem prop="treeName">
            <template #label>
              <span class="gen-label-help">
                <ElTooltip content="树节点的显示名称字段名，如：dept_name" placement="top">
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                树名称字段
              </span>
            </template>
            <ElSelect v-model="infoForm.treeName" placeholder="请选择" clearable>
              <ElOption
                v-for="column in availableColumns"
                :key="column.columnName"
                :label="`${column.columnName}：${column.columnComment || ''}`"
                :value="column.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem prop="treeRootValue">
            <template #label>
              <span class="gen-label-help">
                <ElTooltip content="默认是 0，用于根节点 parentId 的默认值" placement="top">
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                根节点值
              </span>
            </template>
            <ElInput v-model="infoForm.treeRootValue" placeholder="请输入根节点值" />
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem prop="treeAncestorsField">
            <template #label>
              <span class="gen-label-help">
                <ElTooltip
                  content="选择 ancestors 一类字段后，生成器会自动维护祖级链"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                祖级字段
              </span>
            </template>
            <ElSelect v-model="infoForm.treeAncestorsField" placeholder="请选择祖级字段" clearable>
              <ElOption
                v-for="column in availableColumns"
                :key="column.columnName"
                :label="`${column.columnName}：${column.columnComment || ''}`"
                :value="column.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :xs="24" :sm="12">
          <ElFormItem prop="treeOrderField">
            <template #label>
              <span class="gen-label-help">
                <ElTooltip
                  content="树列表默认按祖级、父节点、树排序字段、主键升序排列"
                  placement="top"
                >
                  <ElIcon><QuestionFilled /></ElIcon>
                </ElTooltip>
                树排序字段
              </span>
            </template>
            <ElSelect v-model="infoForm.treeOrderField" placeholder="请选择树排序字段" clearable>
              <ElOption
                v-for="column in sortableColumns"
                :key="column.columnName"
                :label="`${column.columnName}：${column.columnComment || ''}`"
                :value="column.columnName"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>
    </template>
  </ElForm>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { QuestionFilled } from '@element-plus/icons-vue'
  import { treeselect } from '@/api/system/menu'
  import type { DbColumnVO, DbTableVO } from '@/api/tool/gen'

  interface MenuOption {
    id: string | number
    label: string
    children?: MenuOption[]
  }

  const props = defineProps<{ info: DbTableVO; columns: DbColumnVO[] }>()

  const infoForm = computed(() => props.info)
  const availableColumns = computed(() => props.columns ?? [])
  const sortableColumns = computed(() =>
    availableColumns.value.filter((column) =>
      ['Integer', 'Long', 'Double', 'BigDecimal', 'LocalDateTime'].includes(column.javaType ?? '')
    )
  )
  const frontendTypeOptions = [
    { label: 'Vue', value: 'vue' },
    { label: 'React', value: 'react' },
    { label: 'Art', value: 'art' }
  ]

  const menuOptions = ref<MenuOption[]>([])
  const formRef = ref<FormInstance>()

  const rules: FormRules<DbTableVO> = {
    tplCategory: [{ required: true, message: '请选择生成模板', trigger: 'blur' }],
    frontendType: [
      { required: true, message: '请选择前端模板', trigger: 'change' },
      {
        pattern: /^[A-Za-z0-9_-]+$/,
        message: '仅支持字母、数字、下划线和中划线',
        trigger: 'change'
      }
    ],
    packageName: [{ required: true, message: '请输入生成包路径', trigger: 'blur' }],
    moduleName: [{ required: true, message: '请输入生成模块名', trigger: 'blur' }],
    businessName: [{ required: true, message: '请输入生成业务名', trigger: 'blur' }],
    functionName: [{ required: true, message: '请输入生成功能名', trigger: 'blur' }],
    treeRootValue: [
      {
        validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
          if (infoForm.value.tplCategory === 'tree' && !value) {
            callback(new Error('请输入根节点值'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  const getMenuTreeselect = async () => {
    const res = await treeselect()
    menuOptions.value = (res as MenuOption[]) ?? []
  }

  watch(
    () => infoForm.value.enableStatus,
    (val) => {
      if (!val) infoForm.value.statusField = ''
    }
  )

  watch(
    () => infoForm.value.enableUnique,
    (val) => {
      if (!val) infoForm.value.uniqueFields = []
    }
  )

  watch(
    () => infoForm.value.enableSort,
    (val) => {
      if (!val) infoForm.value.sortField = ''
    }
  )

  const validate = async (): Promise<boolean> => {
    if (!formRef.value) return false
    try {
      await formRef.value.validate()
      return true
    } catch {
      return false
    }
  }

  defineExpose({ validate })

  getMenuTreeselect()
</script>

<style lang="scss" scoped>
  .gen-form-header {
    margin: 4px 0 12px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .gen-label-help {
    display: inline-flex;
    align-items: center;

    .el-icon {
      margin-right: 4px;
      color: var(--el-text-color-secondary);
      cursor: help;
    }
  }

  :deep(.el-form-item__content) {
    justify-content: flex-start;
  }
</style>
