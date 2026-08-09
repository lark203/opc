<template>
  <ElDialog v-model="visible" title="导入表" width="90%" top="5vh" append-to-body>
    <ElForm :model="queryParams" :inline="true">
      <ElFormItem label="数据源" prop="dataName">
        <ElSelect v-model="queryParams.dataName" filterable placeholder="请选择/输入数据源名称">
          <ElOption v-for="item in dataNameList" :key="item" :label="item" :value="item" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="表名称" prop="tableName">
        <ElInput
          v-model="queryParams.tableName"
          placeholder="请输入表名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </ElFormItem>
      <ElFormItem label="表描述" prop="tableComment">
        <ElInput
          v-model="queryParams.tableComment"
          placeholder="请输入表描述"
          clearable
          @keyup.enter="handleQuery"
        />
      </ElFormItem>
      <ElFormItem>
        <ElButton type="primary" @click="handleQuery">搜索</ElButton>
        <ElButton @click="resetQuery">重置</ElButton>
      </ElFormItem>
    </ElForm>
    <ElRow>
      <ElTable
        ref="tableRef"
        border
        :data="dbTableList"
        max-height="40vh"
        @selection-change="handleSelectionChange"
      >
        <ElTableColumn type="selection" width="55" />
        <ElTableColumn prop="tableName" label="表名称" showOverflowTooltip />
        <ElTableColumn prop="tableComment" label="表描述" showOverflowTooltip />
        <ElTableColumn prop="createTime" label="创建时间" />
        <ElTableColumn prop="updateTime" label="更新时间" />
      </ElTable>
      <ElPagination
        v-show="total > 0"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        @current-change="getList"
        @size-change="getList"
        layout="total, prev, pager, next, jumper"
      />
    </ElRow>
    <template #footer>
      <div class="dialog-footer">
        <ElButton type="primary" @click="handleImportTable">确 定</ElButton>
        <ElButton @click="visible = false">取 消</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue'
  import { ElMessage, ElTable } from 'element-plus'
  import {
    type DbTableQuery,
    type DbTableVO,
    getDataNames,
    importTable,
    listDbTable
  } from '@/api/tool/gen'

  const total = ref(0)
  const visible = ref(false)
  const tables = ref<string[]>([])
  const dbTableList = ref<DbTableVO[]>([])

  const tableRef = ref<InstanceType<typeof ElTable>>()

  const queryParams = reactive<DbTableQuery>({
    pageNum: 1,
    pageSize: 10,
    dataName: '',
    tableName: '',
    tableComment: ''
  })

  const dataNameList = ref<string[]>([])

  const emit = defineEmits(['ok'])

  const show = async (dataName: string) => {
    const res = await getDataNames()
    dataNameList.value = res
    if (dataName) {
      queryParams.dataName = dataName
    } else if (dataNameList.value.length > 0) {
      queryParams.dataName = dataNameList.value[0]
    }
    await getList()
    visible.value = true
  }

  const handleSelectionChange = (selection: DbTableVO[]) => {
    tables.value = selection.map((item) => item.tableName)
  }

  const getList = async () => {
    const res = await listDbTable(queryParams)
    dbTableList.value = res.rows
    total.value = res.total
  }

  const handleQuery = () => {
    queryParams.pageNum = 1
    getList()
  }

  const resetQuery = () => {
    queryParams.tableName = ''
    queryParams.tableComment = ''
    handleQuery()
  }

  const handleImportTable = async () => {
    const tableNames = tables.value.join(',')
    if (!tableNames) {
      ElMessage.error('请选择要导入的表')
      return
    }
    await importTable({
      tables: tableNames,
      dataName: queryParams.dataName || ''
    })
    ElMessage.success('导入成功')
    visible.value = false
    emit('ok')
  }

  defineExpose({
    show
  })
</script>
