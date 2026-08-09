<#assign searchColumns = []>
<#assign dateBetweenColumns = []>
<#assign indexDicts = []>
<#assign needDictTag = false>
<#assign needElImage = false>
<#assign needElSwitchH = false>
<#assign needElInputNumberH = false>
<#assign needElDatePickerH = false>
<#assign needImageUpload = false>
<#assign needFileUpload = false>
<#assign needEditor = false>
<#assign formColumns = []>
<#assign checkboxColumns = []>
<#assign requiredColumns = []>
<#assign treeNameLabel = ''>
<#list columns as column>
    <#if column.javaField == treeName><#assign treeNameLabel = column.columnLabel></#if>
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
    <#if column.insert || column.edit>
        <#assign formColumns = formColumns + [column]>
        <#if column.htmlType == "checkbox"><#assign checkboxColumns = checkboxColumns + [column]></#if>
        <#if column.required><#assign requiredColumns = requiredColumns + [column]></#if>
        <#if column.dictType?has_content && !indexDicts?seq_contains(column.dictType)>
            <#assign indexDicts = indexDicts + [column.dictType]>
        </#if>
        <#if column.htmlType == "imageUpload"><#assign needImageUpload = true></#if>
        <#if column.htmlType == "fileUpload"><#assign needFileUpload = true></#if>
        <#if column.htmlType == "editor"><#assign needEditor = true></#if>
    </#if>
</#list>
<#assign formObjectColumns = []>
<#list columns as column>
    <#if column.pk>
        <#assign formObjectColumns = formObjectColumns + [column]>
    <#elseif column.insert || column.edit>
        <#assign formObjectColumns = formObjectColumns + [column]>
    </#if>
</#list>
<#assign needH = needDictTag || needElImage || needElSwitchH || needElInputNumberH || needElDatePickerH>
<#assign epImports = []>
<#if needElSwitchH><#assign epImports = epImports + ["ElSwitch"]></#if>
<#if needElImage><#assign epImports = epImports + ["ElImage"]></#if>
<#if needElInputNumberH><#assign epImports = epImports + ["ElInputNumber"]></#if>
<#if needElDatePickerH><#assign epImports = epImports + ["ElDatePicker"]></#if>

<#macro control column>
    <#if column.htmlType == "textarea">
        <ElInput v-model="form.${column.javaField}" type="textarea" placeholder="请输入${column.columnLabel}"/>
    <#elseif column.htmlType == "inputNumber">
        <ElInputNumber v-model="form.${column.javaField}" controls-position="right" :min="0" style="width: 100%"/>
    <#elseif column.htmlType == "imageUpload">
        <ImageUpload v-model="form.${column.javaField}"/>
    <#elseif column.htmlType == "fileUpload">
        <FileUpload v-model="form.${column.javaField}"/>
    <#elseif column.htmlType == "editor">
        <ArtWangEditor v-model="form.${column.javaField}" height="200px"/>
    <#elseif column.htmlType == "datetime">
        <ElDatePicker v-model="form.${column.javaField}" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"
                      placeholder="选择${column.columnLabel}" style="width: 100%"/>
    <#elseif column.htmlType == "select">
        <#if column.dictType?has_content>
            <ElSelect v-model="form.${column.javaField}" placeholder="请选择${column.columnLabel}" clearable>
                <ElOption v-for="dict in ${column.dictType}.value || []" :key="dict.value" :label="dict.label"
                          :value="<#if (column.javaType == "Integer" || column.javaType == "Long")>parseInt(dict.value)<#else>dict.value</#if>"/>
            </ElSelect>
        <#else>
            <ElSelect v-model="form.${column.javaField}" placeholder="请选择${column.columnLabel}" clearable/>
        </#if>
    <#elseif column.htmlType == "radio">
        <#if column.dictType?has_content>
            <ElRadioGroup v-model="form.${column.javaField}">
                <ElRadio v-for="dict in ${column.dictType}.value || []" :key="dict.value"
                         :label="<#if (column.javaType == "Integer" || column.javaType == "Long")>parseInt(dict.value)<#else>dict.value</#if>">
                    {{ dict.label }}
                </ElRadio>
            </ElRadioGroup>
        <#else>
            <ElRadioGroup v-model="form.${column.javaField}"/>
        </#if>
    <#elseif column.htmlType == "checkbox">
        <#if column.dictType?has_content>
            <ElCheckboxGroup v-model="form.${column.javaField}">
                <ElCheckbox v-for="dict in ${column.dictType}.value || []" :key="dict.value" :label="dict.value">
                    {{ dict.label }}
                </ElCheckbox>
            </ElCheckboxGroup>
        <#else>
            <ElCheckboxGroup v-model="form.${column.javaField}"/>
        </#if>
    <#elseif column.htmlType == "switch">
        <ElSwitch
            v-model="form.${column.javaField}"
            <#if column.javaType == "Boolean">
                :active-value="true"
                :inactive-value="false"
            <#elseif column.javaType == "Integer" || column.javaType == "Long">
                :active-value="0"
                :inactive-value="1"
            <#else>
                :active-value="'0'"
                :inactive-value="'1'"
            </#if>
        />
    <#else>
        <ElInput v-model="form.${column.javaField}" placeholder="请输入${column.columnLabel}" clearable/>
    </#if>
</#macro>
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
            <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="handleRefresh">
                <template #left>
                    <ElSpace wrap>
                        <ElButton type="primary" v-auth="'${moduleName}:${businessName}:add'"
                                  @click="() => handleAdd()">
                            新增
                        </ElButton>
                        <ElButton @click="toggleExpand">{{ isExpanded ? '收起' : '展开' }}</ElButton>
                    </ElSpace>
                </template>
            </ArtTableHeader>
            <ArtTable
                ref="tableRef"
                row-key="${treeCode}"
                :loading="loading"
                :columns="columns"
                :data="treeList"
                :stripe="false"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                :default-expand-all="false"
            >
                <template #action="{ row }">
                    <ArtButtonTable type="edit" auth="${moduleName}:${businessName}:edit"
                                    @click="() => handleUpdate(row)"/>
                    <ArtButtonTable
                        type="add"
                        title="新增子节点"
                        auth="${moduleName}:${businessName}:add"
                        @click="() => handleAdd(row)"
                    />
                    <ArtButtonTable type="delete" auth="${moduleName}:${businessName}:remove"
                                    @click="() => handleDelete(row)"/>
                </template>
            </ArtTable>
        </ElCard>
        <ElDialog v-model="dialogVisible" :title="dialogTitle" width="500px" align-center>
            <ElForm :model="form" :rules="rules" ref="formRef" label-width="80px">
                <#list formColumns as column>
                    <#if column.javaField == treeParentCode>
                        <ElFormItem label="${column.columnLabel}" prop="${treeParentCode}">
                            <ElTreeSelect
                                v-model="form.${treeParentCode}"
                                :data="treeOptions"
                                :props="{ value: '${treeCode}', label: '${treeName}', children: 'children' }"
                                value-key="${treeCode}"
                                check-strictly
                                placeholder="请选择${column.columnLabel}"
                            />
                        </ElFormItem>
                    <#else>
                        <ElFormItem label="${column.columnLabel}" prop="${column.javaField}">
                            <@control column=column />
                        </ElFormItem>
                    </#if>
                </#list>
            </ElForm>
            <template #footer>
                <ElButton @click="dialogVisible = false">取消</ElButton>
                <ElButton type="primary" :loading="buttonLoading" @click="submitForm">确定</ElButton>
            </template>
        </ElDialog>
    </div>
</template>

<script setup lang="ts">
    import {computed, nextTick, onMounted, reactive, ref, toRefs} from 'vue'
    import {ElMessage, ElMessageBox,} from 'element-plus'
    import DictTag from '@/components/core/forms/dict-tag/index.vue'
    import {useTableColumns} from '@/hooks/core/useTableColumns'
    import {$

    {
        businessName
    }
    Api
    }
    from
    '@/api/'
    import {useDict} from '@/utils/dict'
    import type {FormInstance, FormRules} from 'element-plus'

    <#if needDictTag>

    </#if>
    <#if needImageUpload>

    </#if>
    <#if needFileUpload>

    </#if>
    <#if needEditor>

    </#if>
    import type {
        ${BusinessName}Form,
        ${BusinessName}Query,
        ${BusinessName}VO
    } from '@/api/${moduleName}/${businessName}/types'

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
    type
    TreeOption = {
        ${treeCode}: string | number
        ${treeName}: string
        children? : TreeOption[]
    }

    const loading = ref(false)
    const isExpanded = ref(false)
    const tableRef = ref()
    const treeList = ref < ${BusinessName}VO[] > ([])
    const treeOptions = ref < TreeOption[] > ([])
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const buttonLoading = ref(false)
    const formRef = ref < FormInstance > ()

    let searchForm = reactive < ${BusinessName}Query<#if dateBetweenColumns?size gt 0> & {<#list dateBetweenColumns as column>dateRange${column.capJavaField}? : [string, string]<#sep>; </#sep></#list>}</#if> > ({
        <#list searchColumns as column>
        <#if column.htmlType == "datetime" && column.queryType == "BETWEEN">dateRange${column.capJavaField}<#else>${column.javaField}</#if>: undefined,
        </#list>
    })

    const form = reactive < ${BusinessName}Form > ({
        <#list formObjectColumns as column>
        ${column.javaField}: undefined<#sep>,
        </#sep>
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

    const {columnChecks, columns} = useTableColumns < ${BusinessName}VO > (() => [
        {prop: '${treeName}', label: '${treeNameLabel}', minWidth: 200},
        <#list columns as column>
        <#if column.pk>
        <#elseif column.javaField == treeName>
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
                'onUpdate:modelValue':(val: any)
    =>
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
        200,
            fixed
    :
        'right',
            align
    :
        'center',
            useSlot
    :
        true,
            slotName
    :
        'action'
    }
    ])

    const rules: FormRules<${BusinessName}Form> = {
        <#list requiredColumns as column>
        ${column.javaField}: [{
            required: true,
            message: '${column.columnLabel}不能为空',
            trigger: '<#if column.htmlType == "input" || column.htmlType == "textarea">blur<#else>change</#if>'
        }]<#sep>,
        </#sep>
        </#list>
    }

    // 构建树形结构（客户端将扁平列表转换为树）
    const buildTree = (list: ${BusinessName}VO[], parentId: string | number
    ):
    ${BusinessName}VO[]
    =>
    {
        return list
            .filter((item) => String(item.${treeParentCode}) === String(parentId))
            .map((item) => ({
                ...item,
                hasChildren: list.some((d) => String(d.${treeParentCode}) === String(item.${treeCode})),
                children: buildTree(list, item.${treeCode})
            }))
    }

    const getList = async (query
    ? : ${BusinessName}Query
    ) =>
    {
        loading.value = true
        try {
            const res = await ${businessName}Api.list${BusinessName}(query || searchForm)
            treeList.value = buildTree(res, ${treeRootValue})
        } catch (error) {
            console.error('获取${functionName}失败:', error)
        } finally {
            loading.value = false
        }
    }

    // 查询${functionName}下拉树结构
    const getTreeselect = async () => {
        const res = await ${businessName}Api.list${BusinessName}()
        const top: TreeOption = {${treeCode}: ${treeRootValueTsLiteral}, ${treeName}: '顶级节点', children: []}
        top.children = buildTree(res, ${treeRootValue})
        treeOptions.value = [top]
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
        getList(query)
        <#else>
        getList()
        </#if>
    }

    const handleReset = () => {
        <#list searchColumns as column>
        searchForm.<#if column.htmlType == "datetime" && column.queryType == "BETWEEN">dateRange${column.capJavaField}<#else>${column.javaField}</#if> = undefined
        </#list>
        getList()
    }

    const handleRefresh = () => {
        getList()
    }

    const resetForm = () => {
        <#list formObjectColumns as column>
        form.${column.javaField} = undefined
        </#list>
        formRef.value?.resetFields()
    }

    const handleAdd = async (row
    ? : ${BusinessName}VO
    ) =>
    {
        resetForm()
        await getTreeselect()
        form.${treeParentCode} = row ? row.${treeCode} : ${treeRootValueTsLiteral}
        dialogTitle.value = '添加${functionName}'
        dialogVisible.value = true
    }

    const handleUpdate = async (row: ${BusinessName}VO) => {
        resetForm()
        await getTreeselect()
        const res = await ${businessName}Api.get${BusinessName}(row.${treeCode})
        Object.assign(form, res)
        <#list checkboxColumns as column>
        if ((res as
        any
    ).
        ${column.javaField}
    )
        {
            form.${column.javaField} = String((res
            as
            any
        ).
            ${column.javaField}
        ).
            split(',')
        }
        </#list>
        dialogTitle.value = '修改${functionName}'
        dialogVisible.value = true
    }

    const submitForm = () => {
        formRef.value?.validate(async (valid: boolean) => {
            if (valid) {
                <#list checkboxColumns as column>
                if (Array.isArray(form.${column.javaField})) {
                    form.${column.javaField} = (form.${column.javaField}
                    as
                    string[]
                ).
                    join(',')
                }
                </#list>
                buttonLoading.value = true
                try {
                    if (form.${pkColumn.javaField}) {
                        await ${businessName}Api.update${BusinessName}(form)
                    } else {
                        await ${businessName}Api.add${BusinessName}(form)
                    }
                    dialogVisible.value = false
                    ElMessage.success('操作成功')
                    getList()
                } finally {
                    buttonLoading.value = false
                }
            }
        })
    }

    const handleDelete = async (row: ${BusinessName}VO) => {
        try {
            await ElMessageBox.confirm('确定要删除${functionName}编号为"' + row.${treeCode} + '"的数据项？', '删除确认', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })
            await ${businessName}Api.del${BusinessName}(row.${treeCode})
            ElMessage.success('删除成功')
            getList()
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
            getList()
        }
    }
    </#if>

    // 展开/收起所有节点
    const toggleExpand = () => {
        isExpanded.value = !isExpanded.value
        nextTick(() => {
            if (tableRef.value?.elTableRef && treeList.value) {
                const processRows = (rows: ${BusinessName}VO[]) => {
                    rows.forEach((row) => {
                        if (row.children?.length || row.hasChildren) {
                            tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
                            if (row.children?.length) {
                                processRows(row.children)
                            }
                        }
                    })
                }
                processRows(treeList.value)
            }
        })
    }

    onMounted(() => {
        getList()
    })
</script>

<style lang="scss" scoped>
    /
    /
    树形表格层级缩进样式（art-design-pro 默认主题会清除 el-table 自带的缩进，需手动补偿）
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
            }
        }

    / / 不同层级的缩进 . el-table__row--level-1 . el-table__cell: first-child {
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

    /
    /
    表格行悬停样式
    :deep(.el-table__body-wrapper) {
        .el-table__row {
            transition: background-color 0.15s ease;

            &:hover > td {
                background-color: rgb(64 158 255 / 6%) !important;
            }
        }
    }
</style>
