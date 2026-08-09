<template>
  <ElCard class="gen-edit-page art-full-height">
    <ElTabs v-model="activeName" class="gen-edit-tabs">
      <ElTabPane label="基本信息" name="basic">
        <BasicInfoForm ref="basicInfoRef" :info="info" />
      </ElTabPane>
      <ElTabPane label="字段信息" name="columnInfo">
        <div class="gen-column-table-box" :style="containerHeight">
          <ElTable border :data="columns" row-key="columnId" height="100%">
            <ElTableColumn label="序号" type="index" min-width="5%" />
            <ElTableColumn
              label="字段列名"
              prop="columnName"
              min-width="10%"
              show-overflow-tooltip
            />
            <ElTableColumn label="字段描述" min-width="10%">
              <template #default="scope">
                <ElInput v-model="scope.row.columnComment" />
              </template>
            </ElTableColumn>
            <ElTableColumn
              label="物理类型"
              prop="columnType"
              min-width="10%"
              show-overflow-tooltip
            />
            <ElTableColumn label="Java类型" min-width="11%">
              <template #default="scope">
                <ElSelect v-model="scope.row.javaType">
                  <ElOption label="Long" value="Long" />
                  <ElOption label="String" value="String" />
                  <ElOption label="Integer" value="Integer" />
                  <ElOption label="Double" value="Double" />
                  <ElOption label="BigDecimal" value="BigDecimal" />
                  <ElOption label="LocalDateTime" value="LocalDateTime" />
                  <ElOption label="Boolean" value="Boolean" />
                </ElSelect>
              </template>
            </ElTableColumn>
            <ElTableColumn label="java属性" min-width="10%">
              <template #default="scope">
                <ElInput v-model="scope.row.javaField" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="插入" min-width="5%" align="center">
              <template #default="scope">
                <ElCheckbox v-model="scope.row.isInsert" true-value="1" false-value="0" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="编辑" min-width="5%" align="center">
              <template #default="scope">
                <ElCheckbox v-model="scope.row.isEdit" true-value="1" false-value="0" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="列表" min-width="5%" align="center">
              <template #default="scope">
                <ElCheckbox v-model="scope.row.isList" true-value="1" false-value="0" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="查询" min-width="5%" align="center">
              <template #default="scope">
                <ElCheckbox v-model="scope.row.isQuery" true-value="1" false-value="0" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="必填" min-width="5%" align="center">
              <template #default="scope">
                <ElCheckbox v-model="scope.row.isRequired" true-value="1" false-value="0" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="查询方式" min-width="10%">
              <template #default="scope">
                <ElSelect v-model="scope.row.queryType">
                  <ElOption label="=" value="EQ" />
                  <ElOption label="!=" value="NE" />
                  <ElOption label=">" value="GT" />
                  <ElOption label=">=" value="GE" />
                  <ElOption label="<" value="LT" />
                  <ElOption label="<=" value="LE" />
                  <ElOption label="LIKE" value="LIKE" />
                  <ElOption label="BETWEEN" value="BETWEEN" />
                </ElSelect>
              </template>
            </ElTableColumn>
            <ElTableColumn label="显示类型" min-width="12%">
              <template #default="scope">
                <ElSelect v-model="scope.row.htmlType" @change="handleHtmlTypeChange(scope.row)">
                  <ElOption label="文本框" value="input" />
                  <ElOption label="数字输入" value="inputNumber" />
                  <ElOption label="文本域" value="textarea" />
                  <ElOption label="下拉框" value="select" />
                  <ElOption label="单选框" value="radio" />
                  <ElOption label="复选框" value="checkbox" />
                  <ElOption label="开关" value="switch" />
                  <ElOption label="日期控件" value="datetime" />
                  <ElOption label="图片上传" value="imageUpload" />
                  <ElOption label="文件上传" value="fileUpload" />
                  <ElOption label="富文本控件" value="editor" />
                </ElSelect>
              </template>
            </ElTableColumn>
            <ElTableColumn label="字典类型" min-width="12%">
              <template #default="scope">
                <ElSelect
                  v-model="scope.row.dictType"
                  clearable
                  filterable
                  placeholder="请选择"
                  value-on-clear=""
                  :disabled="!supportsDictHtmlType(scope.row.htmlType)"
                >
                  <ElOption
                    v-for="dict in dictOptions"
                    :key="dict.dictType"
                    :label="dict.dictName"
                    :value="dict.dictType"
                  >
                    <span style="float: left">{{ dict.dictName }}</span>
                    <span style="float: right; font-size: 13px; color: #8492a6">{{
                      dict.dictType
                    }}</span>
                  </ElOption>
                </ElSelect>
              </template>
            </ElTableColumn>
          </ElTable>
        </div>
      </ElTabPane>
      <ElTabPane label="生成信息" name="genInfo">
        <GenInfoForm ref="genInfoRef" :info="info" :columns="columns" />
      </ElTabPane>
    </ElTabs>
    <div class="gen-edit-footer">
      <ElButton type="primary" @click="submitForm">提交</ElButton>
      <ElButton @click="close">返回</ElButton>
    </div>
  </ElCard>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import { useTableHeight } from '@/hooks/core/useTableHeight'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import BasicInfoForm from './modules/basicInfoForm.vue'
  import GenInfoForm from './modules/genInfoForm.vue'
  import { type DictTypeVO, getDictTypeOptions } from '@/api/system/dict'
  import {
    type DbColumnVO,
    type DbTableForm,
    type DbTableVO,
    getGenTable,
    updateGenTable
  } from '@/api/tool/gen'
  import { useWorktabStore } from '@/store/modules/worktab'

  const route = useRoute()
  const router = useRouter()
  const worktabStore = useWorktabStore()

  // 字段信息表格自动撑满剩余高度（本页无表格头部与分页，返回 height: 100%）
  const { containerHeight } = useTableHeight({
    showTableHeader: computed(() => false),
    paginationHeight: computed(() => 0),
    tableHeaderHeight: computed(() => 0),
    paginationSpacing: computed(() => 0)
  })

  const activeName = ref('columnInfo')
  const columns = ref<DbColumnVO[]>([])
  const dictOptions = ref<DictTypeVO[]>([])

  const DICT_HTML_TYPES = ['select', 'radio', 'checkbox', 'switch']

  const info = reactive<DbTableVO>({
    tableId: '',
    tableName: '',
    tableComment: '',
    className: '',
    functionAuthor: '',
    tplCategory: 'crud',
    frontendType: 'vue',
    packageName: '',
    moduleName: '',
    businessName: '',
    functionName: '',
    enableExport: true,
    enableStatus: false,
    statusField: '',
    enableUnique: false,
    uniqueFields: [],
    enableSort: false,
    sortField: '',
    treeRootValue: '0',
    treeAncestorsField: '',
    treeOrderField: ''
  })

  const basicInfoRef = ref<InstanceType<typeof BasicInfoForm>>()
  const genInfoRef = ref<InstanceType<typeof GenInfoForm>>()

  const supportsDictHtmlType = (htmlType?: string): boolean =>
    DICT_HTML_TYPES.includes(htmlType ?? '')

  const normalizeColumnDictType = (column: DbColumnVO) => {
    if (!supportsDictHtmlType(column.htmlType)) {
      column.dictType = ''
    }
  }

  const handleHtmlTypeChange = (column: DbColumnVO) => {
    normalizeColumnDictType(column)
  }

  const submitForm = async () => {
    const basicOk = (await basicInfoRef.value?.validate()) ?? false
    const genOk = (await genInfoRef.value?.validate()) ?? false
    if (!basicOk || !genOk) {
      ElMessage.error('表单校验未通过，请重新检查提交内容')
      return
    }
    columns.value.forEach(normalizeColumnDictType)
    const genTable: DbTableForm = {
      tableId: info.tableId,
      dataName: info.dataName,
      tableName: info.tableName,
      tableComment: info.tableComment,
      className: info.className,
      functionAuthor: info.functionAuthor,
      remark: info.remark,
      tplCategory: info.tplCategory,
      frontendType: info.frontendType,
      packageName: info.packageName,
      moduleName: info.moduleName,
      businessName: info.businessName,
      functionName: info.functionName,
      parentMenuId: info.parentMenuId,
      treeCode: info.treeCode,
      treeParentCode: info.treeParentCode,
      treeName: info.treeName,
      treeRootValue: info.treeRootValue,
      treeAncestorsField: info.treeAncestorsField,
      treeOrderField: info.treeOrderField,
      enableExport: info.enableExport,
      enableStatus: info.enableStatus,
      statusField: info.statusField,
      enableUnique: info.enableUnique,
      uniqueFields: info.uniqueFields,
      enableSort: info.enableSort,
      sortField: info.sortField,
      params: {
        treeCode: info.treeCode,
        treeParentCode: info.treeParentCode,
        treeName: info.treeName,
        treeRootValue: info.treeRootValue,
        treeAncestors: info.treeAncestorsField,
        treeOrderField: info.treeOrderField,
        parentMenuId: info.parentMenuId,
        enableExport: info.enableExport,
        enableStatus: info.enableStatus,
        statusField: info.statusField,
        enableUnique: info.enableUnique,
        uniqueFields: info.uniqueFields,
        enableSort: info.enableSort,
        sortField: info.sortField
      },
      columns: columns.value
    }
    await updateGenTable(genTable)
    ElMessage.success('保存成功')
    close()
  }

  const close = () => {
    worktabStore.removeTab(route.path)
    router.push('/tool/gen')
  }

  onMounted(async () => {
    const tableId = route.params.tableId as string | undefined
    if (!tableId) return
    const detail = await getGenTable(tableId)
    if (detail?.info) {
      Object.assign(info, detail.info)
    }
    columns.value = (detail?.rows ?? []).map((column) => {
      const item: DbColumnVO = { ...column }
      normalizeColumnDictType(item)
      return item
    })
    const dictRes = await getDictTypeOptions()
    dictOptions.value = dictRes ?? []
  })
</script>

<style lang="scss" scoped>
  /* 卡片内容作为弹性容器，让 tabs 撑满剩余高度 */
  .gen-edit-page :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 0 20px 20px;
    overflow: hidden;
  }

  /* tabs 充满卡片内容，内容区弹性伸缩 */
  .gen-edit-tabs {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;

    :deep(.el-tabs__header) {
      flex-shrink: 0;
      margin-bottom: 12px;
    }

    :deep(.el-tabs__content) {
      flex: 1;
      min-height: 0;
    }

    :deep(.el-tab-pane) {
      height: 100%;
    }
  }

  /* 字段信息表格容器，撑满 tab 内容区 */
  .gen-column-table-box {
    height: 100%;
  }

  .gen-edit-footer {
    flex-shrink: 0;
    margin-top: 16px;
    text-align: center;
  }
</style>
