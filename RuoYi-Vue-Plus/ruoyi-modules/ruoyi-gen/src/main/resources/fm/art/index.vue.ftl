<#assign searchColumns = []>
<#assign dateBetweenColumns = []>
<#assign indexDicts = []>
<#assign needDictTag = false>
<#assign needElImage = false>
<#assign needElSwitchH = false>
<#assign needElInputNumberH = false>
<#assign needElDatePickerH = false>
<#list columns as column>
    <#if column.query>
        <#assign searchColumns = searchColumns + [column]>
        <#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
            <#assign dateBetweenColumns = dateBetweenColumns + [column]>
        </#if>
    </#if>
    <#if (column.query || column.list) && column.dictType?has_content && (column.htmlType == "select" || column.htmlType == "radio" || column.htmlType == "checkbox" || column.htmlType == "switch")>
        <#if !indexDicts?seq_contains(column.dictType)>
            <#assign indexDicts = indexDicts + [column.dictType]>
        </#if>
    </#if>
    <#if column.pk && column.list>
    <#elseif enableStatus && statusField == column.javaField>
        <#assign needElSwitchH = true>
    <#elseif enableSort && sortField == column.javaField>
        <#if column.javaType == "LocalDateTime"><#assign needElDatePickerH = true><#else><#assign needElInputNumberH = true></#if>
    <#elseif column.list && column.htmlType == "switch">
        <#assign needElSwitchH = true>
    <#elseif column.list && column.htmlType == "datetime">
    <#elseif column.list && column.htmlType == "imageUpload">
        <#assign needElImage = true>
    <#elseif column.list && column.dictType?has_content>
        <#assign needDictTag = true>
    </#if>
</#list>
<#if enableStatus && !statusColumn.list>
    <#assign needElSwitchH = true>
</#if>
<#if enableSort && !sortColumn.list>
    <#if sortColumn.javaType == "LocalDateTime"><#assign needElDatePickerH = true><#else><#assign needElInputNumberH = true></#if>
</#if>
<#assign needH = needDictTag || needElImage || needElSwitchH || needElInputNumberH || needElDatePickerH>
<#assign epImports = []>
<#if needElSwitchH><#assign epImports = epImports + ["ElSwitch"]></#if>
<#if needElImage><#assign epImports = epImports + ["ElImage"]></#if>
<#if needElInputNumberH><#assign epImports = epImports + ["ElInputNumber"]></#if>
<#if needElDatePickerH><#assign epImports = epImports + ["ElDatePicker"]></#if>
<template>
    <div class="${businessName}-page art-full-height">
        <ArtSearchBar
            v-model="searchForm"
            :items="formItems"
            :isExpand="true"
            @reset="handleReset"
            @search="handleSearch"
        />
        <ElCard class="art-table-card">
            <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
                <template #left>
                    <ElSpace wrap>
                        <ElButton type="primary" v-auth="'${moduleName}:${businessName}:add'"
                                  @click="() => showDialog()">
                            新增
                        </ElButton>
                        <ElButton
                            type="success"
                            v-auth="'${moduleName}:${businessName}:edit'"
                            :disabled="selectedRows.length !== 1"
                            @click="() => showDialog(selectedRows[0])"
                        >
                            修改
                        </ElButton>
                        <ElButton
                            type="danger"
                            v-auth="'${moduleName}:${businessName}:remove'"
                            :disabled="selectedRows.length === 0"
                            @click="() => handleDelete()"
                        >
                            删除
                        </ElButton>
                        <#if enableExport>
                            <ElButton type="info" v-auth="'${moduleName}:${businessName}:export'" @click="handleExport">
                                导出
                            </ElButton>
                        </#if>
                    </ElSpace>
                </template>
            </ArtTableHeader>
            <ArtTable
                row-key="${pkColumn.javaField}"
                :data="data"
                :columns="columns"
                :loading="loading"
                :pagination="pagination"
                @pagination:size-change="handleSizeChange"
                @pagination:current-change="handleCurrentChange"
                @selection-change="handleSelectionChange"
            >
                <template #action="{ row }">
                    <ArtButtonTable
                        type="edit"
                        auth="${moduleName}:${businessName}:edit"
                        @click="() => showDialog(row)"
                    />
                    <ArtButtonTable
                        type="delete"
                        auth="${moduleName}:${businessName}:remove"
                        @click="() => handleDelete(row)"
                    />
                </template>
            </ArtTable>
        </ElCard>
        <!-- ${functionName}新增/编辑弹窗 -->
        <${BusinessName}Dialog v-model:visible="dialogVisible" :edit-data="currentRow" @success="refreshData"/>
    </div>
</template>

<script setup lang="ts">
    import {
        computed,

        <#if needH > h, </#if>
        reactive, ref, toRefs
    }
        from
            'vue'
    import DictTag from '@/components/core/forms/dict-tag/index.vue'
    import {useTable} from '@/hooks/core/useTable'
    import {$

    {
        businessName
    }
    Api
    }
    from
    '@/api/'
    import {useDict} from '@/utils/dict'

    <#if epImports?size gt 0>

    </#if>
    <#if needDictTag>

    </#if>
    import ${BusinessName} Dialog from './modules/${businessName}-dialog.vue'
    import type {${BusinessName}Query, ${BusinessName}VO} from '@/api/${moduleName}/${businessName}/types'

    <#if indexDicts?size gt 0>

    </#if>

    defineOptions({name: '${BusinessName}'})

    <#if indexDicts?size gt 0>
    const {<#list indexDicts as dict>${dict}<#sep>, </#sep></#list>} = toRefs(useDict(<#list indexDicts as dict>'${dict}'<#sep>, </#sep></#list>))

    </#if>
    <#if enableStatus>
    const ${statusField}ActiveValue = <#if statusColumn.javaType == "Boolean">true<#elseif statusColumn.javaType == "Integer" || statusColumn.javaType == "Long">0
    <#else>'0'
    </#if>
    const ${statusField}InactiveValue = <#if statusColumn.javaType == "Boolean">false<#elseif statusColumn.javaType == "Integer" || statusColumn.javaType == "Long">1
    <#else>'1'
    </#if>

    </#if>
    const selectedRows = ref < ${BusinessName}VO[] > ([])
    const dialogVisible = ref(false)
    const currentRow = ref < ${BusinessName}VO > ()

    let searchForm = reactive < ${BusinessName}Query<#if dateBetweenColumns?size gt 0> & {<#list dateBetweenColumns as column>dateRange${column.capJavaField}? : [string, string]<#sep>; </#sep></#list>}</#if> > ({
        <#list searchColumns as column>
        <#if column.htmlType == "datetime" && column.queryType == "BETWEEN">dateRange${column.capJavaField}<#else>${column.javaField}</#if>: undefined,
        </#list>
    })

    const formItems = computed(() => [
        <#list searchColumns as column>
        <#if column.htmlType == "datetime" && column.queryType == "BETWEEN">
        {
            label: '${column.columnLabel}',
            key: 'dateRange${column.capJavaField}',
            type: 'daterange',
            props: {
                valueFormat: 'YYYY-MM-DD HH:mm:ss',
                startPlaceholder: '开始日期',
                endPlaceholder: '结束日期'
            }
        },
        <#elseif column.htmlType == "datetime">
        {
            label: '${column.columnLabel}',
            key: '${column.javaField}',
            type: 'date',
            props: {valueFormat: 'YYYY-MM-DD', placeholder: '请选择${column.columnLabel}', clearable: true}
        },
        <#elseif column.htmlType == "inputNumber">
        {
            label: '${column.columnLabel}',
            key: '${column.javaField}',
            type: 'number',
            props: {controlsPosition: 'right'}
        },
        <#elseif (column.htmlType == "select" || column.htmlType == "radio" || column.htmlType == "switch") && column.dictType?has_content>
        {
            label: '${column.columnLabel}',
            key: '${column.javaField}',
            type: 'select',
            props: {
                placeholder: '请选择${column.columnLabel}',
                options: ${column.dictType}.value || [],
                clearable: true
            }
        },
        <#elseif column.htmlType == "switch">
        {
            label: '${column.columnLabel}',
            key: '${column.javaField}',
            type: 'select',
            props: {
                placeholder: '请选择${column.columnLabel}',
                <#if column.javaType == "Boolean">
                options: [
                    {label: '是', value: true},
                    {label: '否', value: false}
                ],
                <#elseif column.javaType == "Integer" || column.javaType == "Long">
                options: [
                    {label: '开启', value: 0},
                    {label: '关闭', value: 1}
                ],
                <#else>
                options: [
                    {label: '开启', value: '0'},
                    {label: '关闭', value: '1'}
                ],
                </#if>
                clearable: true
            }
        },
        <#elseif column.htmlType == "select" || column.htmlType == "radio">
        {
            label: '${column.columnLabel}',
            key: '${column.javaField}',
            type: 'select',
            props: {placeholder: '请选择字典生成', options: [], clearable: true}
        },
        <#else>
        {
            label: '${column.columnLabel}',
            key: '${column.javaField}',
            type: 'input',
            props: {placeholder: '请输入${column.columnLabel}', clearable: true}
        },
        </#if>
        </#list>
    ])

    const {
        data,
        columns,
        columnChecks,
        pagination,
        loading,
        refreshData,
        getData,
        replaceSearchParams,
        resetSearchParams,
        handleSizeChange,
        handleCurrentChange
    } = useTable({
        core: {
            apiFn: ${businessName}Api.list${BusinessName},
            apiParams: {
                pageNum: 1,
                pageSize: 10
            },
            paginationKey: {current: 'pageNum', size: 'pageSize'},
            columnsFactory: () => [
                {type: 'selection'},
                {type: 'index', width: 60, label: '序号'},
                <#list columns as column>
                <#if column.pk && column.list>
                {prop: '${column.javaField}', label: '${column.columnLabel}'},
                <#elseif enableStatus && statusField == column.javaField>
                {
                    prop: '${column.javaField}',
                    label: '${column.columnLabel}',
                    width: 100,
                    align: 'center',
                    formatter: (row: ${BusinessName}VO) =>
                        h(ElSwitch, {
                            modelValue: row.${statusField},
                            activeValue: ${statusField}ActiveValue,
                            inactiveValue: ${statusField}InactiveValue,
                            'onUpdate:modelValue': (val: any) => (row.${statusField} = val),
                            onChange: () => handleStatusChange(row)
                        })
                },
                <#elseif enableSort && sortField == column.javaField>
                {
                    prop: '${column.javaField}',
                    label: '${column.columnLabel}',
                    width: 160,
                    align: 'center',
                    formatter: (row: ${BusinessName}VO) =>
                        <#if column.javaType == "LocalDateTime">
                        h(ElDatePicker, {
                            modelValue: row.${sortField},
                            type: 'datetime',
                            valueFormat: 'YYYY-MM-DD HH:mm:ss',
                            'onUpdate:modelValue': (val: any) => (row.${sortField} = val),
                            onChange: () => handleSortChange(row)
                        })
                    <#else>
                    h(ElInputNumber, {
                        modelValue: row.${sortField},
                        controlsPosition: 'right',
                        min: 0,
                        'onUpdate:modelValue':(val
    :
    any
    ) =>
    (row.${sortField} = val),
        onChange
    :
    () => handleSortChange(row)
    })
    </#if>
    },
    <#elseif column.list && column.htmlType == "switch">
    {
        prop: '${column.javaField}',
            label
    :
        '${column.columnLabel}',
            width
    :
        120,
            align
    :
        'center',
            formatter
    :
        (row: ${BusinessName}VO) =>
            h(ElSwitch, {
                modelValue: row.${column.javaField},
                <#if column.javaType == "Boolean">
                activeValue: true,
                inactiveValue: false,
                <#elseif column.javaType == "Integer" || column.javaType == "Long">
                activeValue: 0,
                inactiveValue: 1,
                <#else>
                activeValue: '0',
                inactiveValue: '1',
                </#if>
                disabled: true
            })
    }
    ,
    <#elseif column.list && column.htmlType == "datetime">
    {
        prop: '${column.javaField}', label
    :
        '${column.columnLabel}', width
    :
        180, align
    :
        'center'
    }
    ,
    <#elseif column.list && column.htmlType == "imageUpload">
    {
        prop: '${column.javaField}Url',
            label
    :
        '${column.columnLabel}',
            width
    :
        100,
            align
    :
        'center',
            formatter
    :
        (row: ${BusinessName}VO) =>
            h(ElImage, {
                src: row.${column.javaField}Url,
                previewSrcList: [row.${column.javaField}Url],
                previewTeleported: true,
                style: {width: '50px', height: '50px'}
            })
    }
    ,
    <#elseif column.list && column.dictType?has_content>
    <#if column.htmlType == "checkbox">
    {
        prop: '${column.javaField}',
            label
    :
        '${column.columnLabel}',
            align
    :
        'center',
            formatter
    :
        (row: ${BusinessName}VO) =>
            h(
                'div',
                (row.${column.javaField} ? String(row.${column.javaField}).split(',') : []).map((v: string) =>
                    h(DictTag, {key: v, options: ${column.dictType}.value, value: v})
                )
            )
    }
    ,
    <#else>
    {
        prop: '${column.javaField}',
            label
    :
        '${column.columnLabel}',
            align
    :
        'center',
            formatter
    :
        (row: ${BusinessName}VO) =>
            h(DictTag, {options: ${column.dictType}.value, value: row.${column.javaField}})
    }
    ,
    </#if>
    <#elseif column.list && "" != column.javaField>
    {
        prop: '${column.javaField}', label
    :
        '${column.columnLabel}'
    }
    ,
    </#if>
    </#list>
    <#if enableStatus && !statusColumn.list>
    {
        prop: '${statusField}',
            label
    :
        '${statusColumn.columnLabel}',
            width
    :
        100,
            align
    :
        'center',
            formatter
    :
        (row: ${BusinessName}VO) =>
            h(ElSwitch, {
                modelValue: row.${statusField},
                activeValue: ${statusField}ActiveValue,
                inactiveValue: ${statusField}InactiveValue,
                'onUpdate:modelValue': (val: any) => (row.${statusField} = val),
                onChange: () => handleStatusChange(row)
            })
    }
    ,
    </#if>
    <#if enableSort && !sortColumn.list>
    {
        prop: '${sortField}',
            label
    :
        '${sortColumn.columnLabel}',
            width
    :
        160,
            align
    :
        'center',
            formatter
    :
        (row: ${BusinessName}VO) =>
            <#if sortColumn.javaType == "LocalDateTime">
            h(ElDatePicker, {
                modelValue: row.${sortField},
                type: 'datetime',
                valueFormat: 'YYYY-MM-DD HH:mm:ss',
                'onUpdate:modelValue': (val: any) => (row.${sortField} = val),
                onChange: () => handleSortChange(row)
            })
        <#else>
        h(ElInputNumber, {
            modelValue: row.${sortField},
            controlsPosition: 'right',
            min: 0,
            'onUpdate:modelValue': (val: any) => (row.${sortField} = val),
            onChange: () => handleSortChange(row)
        })
        </#if>
    }
    ,
    </#if>
    {
        prop: 'operation',
            label
    :
        '操作',
            width
    :
        120,
            useSlot
    :
        true,
            slotName
    :
        'action',
            fixed
    :
        'right'
    }
    ]
    }
    })

    const handleSelectionChange = (rows: ${BusinessName}VO[]) => {
        selectedRows.value = rows
    }

    const handleSearch = () => {
        <#if dateBetweenColumns?size gt 0>
        const {<#list dateBetweenColumns as column>dateRange${column.capJavaField}, </#list>...rest} = searchForm
        const query: ${BusinessName}Query = {...rest, params: {}}
        <#list dateBetweenColumns as column>
        if (dateRange${column.capJavaField} && dateRange${column.capJavaField}.length === 2) {
            query.params
            !['begin${column.capJavaField}'] = dateRange${column.capJavaField}[0]
            query.params
            !['end${column.capJavaField}'] = dateRange${column.capJavaField}[1]
        }
        </#list>
        replaceSearchParams(query)
        <#else>
        replaceSearchParams(searchForm)
        </#if>
        getData()
    }

    const handleReset = () => {
        <#list searchColumns as column>
        searchForm.<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">dateRange${column.capJavaField}<#else>${column.javaField}</#if> = undefined
        </#list>
        resetSearchParams()
        getData()
    }

    /** 打开新增/编辑弹窗 */
    const showDialog = (row
    ? : ${BusinessName}VO
    ) =>
    {
        currentRow.value = row
        dialogVisible.value = true
    }

    /** 删除${functionName} */
    const handleDelete = async (row
    ? : ${BusinessName}VO
    ) =>
    {
        const ${pkColumn.javaField}s = row ? [row.${pkColumn.javaField}] : selectedRows.value.map((item) => item.${pkColumn.javaField})
        if (${pkColumn.javaField}s.length === 0) return
        try {
            await ElMessageBox.confirm('是否确认删除${functionName}编号为"' + ${pkColumn.javaField}s + '"的数据项？', '删除确认', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            await ${businessName}Api.del${BusinessName}(${pkColumn.javaField}s)
            ElMessage.success('删除成功')
            refreshData()
        } catch (error) {
            if (error !== 'cancel') {
                ElMessage.error('删除失败')
            }
        }
    }
    <#if enableStatus>

    /** 修改${functionName}状态 */
    const handleStatusChange = async (row: ${BusinessName}VO) => {
        const text = row.${statusField} === ${statusField}ActiveValue ? '启用' : '停用'
        try {
            await ElMessageBox.confirm('确认要"' + text + '"吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            await ${businessName}Api.change${BusinessName}Status(row.${pkColumn.javaField}, row.${statusField})
            ElMessage.success(text + '成功')
        } catch (error) {
            row.${statusField} = row.${statusField} === ${statusField}ActiveValue ? ${statusField}InactiveValue : ${statusField}ActiveValue
            if (error !== 'cancel') {
                ElMessage.error(text + '失败')
            }
        }
    }
    </#if>
    <#if enableSort>

    /** 调整${functionName}排序 */
    const handleSortChange = async (row: ${BusinessName}VO) => {
        try {
            await ${businessName}Api.update${BusinessName}Sort(row.${pkColumn.javaField}, row.${sortField})
            ElMessage.success('排序更新成功')
        } catch {
            refreshData()
        }
    }
    </#if>
    <#if enableExport>

    /** 导出${functionName} */
    const handleExport = () => {
        ${businessName}Api.export${BusinessName}(searchForm)
    }
    </#if>
</script>
