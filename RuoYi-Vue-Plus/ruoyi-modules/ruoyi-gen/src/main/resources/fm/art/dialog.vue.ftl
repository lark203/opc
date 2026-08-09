<#assign formColumns = []>
<#assign formObjectColumns = []>
<#assign checkboxColumns = []>
<#assign requiredColumns = []>
<#assign dialogDicts = []>
<#assign needImageUpload = false>
<#assign needFileUpload = false>
<#assign needEditor = false>
<#list columns as column>
    <#if column.pk>
        <#assign formObjectColumns = formObjectColumns + [column]>
    <#elseif column.insert || column.edit>
        <#assign formColumns = formColumns + [column]>
        <#assign formObjectColumns = formObjectColumns + [column]>
        <#if column.htmlType == "checkbox"><#assign checkboxColumns = checkboxColumns + [column]></#if>
        <#if column.required><#assign requiredColumns = requiredColumns + [column]></#if>
        <#if column.dictType?has_content && !dialogDicts?seq_contains(column.dictType)>
            <#assign dialogDicts = dialogDicts + [column.dictType]>
        </#if>
        <#if column.htmlType == "imageUpload"><#assign needImageUpload = true></#if>
        <#if column.htmlType == "fileUpload"><#assign needFileUpload = true></#if>
        <#if column.htmlType == "editor"><#assign needEditor = true></#if>
    </#if>
</#list>
<template>
    <ElDialog
        :title="dialogTitle"
        :model-value="visible"
        @update:model-value="handleCancel"
        width="40%"
        align-center
        @closed="handleClosed"
    >
        <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
            <#list formColumns as column>
                <ElFormItem label="${column.columnLabel}" prop="${column.javaField}">
                    <#if column.htmlType == "textarea">
                        <ElInput v-model="form.${column.javaField}" type="textarea"
                                 placeholder="请输入${column.columnLabel}"/>
                    <#elseif column.htmlType == "inputNumber">
                        <ElInputNumber v-model="form.${column.javaField}" controls-position="right" :min="0"
                                       style="width: 100%"/>
                    <#elseif column.htmlType == "imageUpload">
                        <ImageUpload v-model="form.${column.javaField}"/>
                    <#elseif column.htmlType == "fileUpload">
                        <FileUpload v-model="form.${column.javaField}"/>
                    <#elseif column.htmlType == "editor">
                        <ArtWangEditor v-model="form.${column.javaField}" height="200px"/>
                    <#elseif column.htmlType == "datetime">
                        <ElDatePicker v-model="form.${column.javaField}" type="datetime"
                                      value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择${column.columnLabel}"
                                      style="width: 100%"/>
                    <#elseif column.htmlType == "select">
                        <#if column.dictType?has_content>
                            <ElSelect v-model="form.${column.javaField}" placeholder="请选择${column.columnLabel}"
                                      clearable>
                                <ElOption v-for="dict in ${column.dictType}.value || []" :key="dict.value"
                                          :label="dict.label"
                                          :value="<#if (column.javaType == "Integer" || column.javaType == "Long")>parseInt(dict.value)<#else>dict.value</#if>"/>
                            </ElSelect>
                        <#else>
                            <ElSelect v-model="form.${column.javaField}" placeholder="请选择${column.columnLabel}"
                                      clearable/>
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
                                <ElCheckbox v-for="dict in ${column.dictType}.value || []" :key="dict.value"
                                            :label="dict.value">
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
                        <ElInput v-model="form.${column.javaField}" placeholder="请输入${column.columnLabel}"
                                 clearable/>
                    </#if>
                </ElFormItem>
            </#list>
        </ElForm>
        <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" :loading="buttonLoading" @click="handleSubmit">确 定</ElButton>
      </span>
        </template>
    </ElDialog>
</template>

<script setup lang="ts">
    import {computed, reactive, ref, toRefs, watch} from 'vue'
    import {ElMessage} from 'element-plus'
    import {useDict} from '@/utils/dict'
    import {add$

    {
        BusinessName
    }
    ,
    get$
    {
        BusinessName
    }
    ,
    update$
    {
        BusinessName
    }
    }
    from
    '@/api/'
    import type {FormRules} from 'element-plus'

    <#if needImageUpload>

    </#if>
    <#if needFileUpload>

    </#if>
    <#if needEditor>

    </#if>
    import type {${BusinessName}Form, ${BusinessName}VO} from '@/api/${moduleName}/${businessName}/types'

    <#if dialogDicts?size gt 0>

    const {<#list dialogDicts as dict>${dict}<#sep>, </#sep></#list>} = toRefs(useDict(<#list dialogDicts as dict>'${dict}'<#sep>, </#sep></#list>))
    </#if>

    interface
    Props
    {
        visible: boolean
        editData ? : ${BusinessName}VO
    }

    interface
    Emits
    {
        (e: 'update:visible', value
    :
        boolean
    ):
        void
            (e: 'success')
    :
        void
    }

    const props = withDefaults(defineProps < Props > (), {
        visible: false
    })

    const emit = defineEmits < Emits > ()

    const formRef = ref()
    const buttonLoading = ref(false)

    const form = reactive < ${BusinessName}Form > ({
        <#list formObjectColumns as column>
        ${column.javaField}: undefined,
        </#list>
    })

    const rules = reactive < FormRules > ({
        <#list requiredColumns as column>
        ${column.javaField}: [{
            required: true,
            message: '${column.columnLabel}不能为空',
            trigger: '<#if column.htmlType == "input" || column.htmlType == "textarea">blur<#else>change</#if>'
        }],
        </#list>
    })

    const dialogTitle = computed(() => (props.editData?.${pkColumn.javaField} ? '修改${functionName}' : '新增${functionName}'))

    const loadFormData = async () => {
        if (!props.editData?.${pkColumn.javaField}) return
        const data = await get${BusinessName}(props.editData.${pkColumn.javaField})
        Object.assign(form, data)
        <#list checkboxColumns as column>
        if ((data as
        any
    ).
        ${column.javaField}
    )
        {
            form.${column.javaField} = String((data
            as
            any
        ).
            ${column.javaField}
        ).
            split(',')
        }
        </#list>
    }

    const resetForm = () => {
        formRef.value?.resetFields()
        Object.assign(form, {
            <#list formObjectColumns as column>
            ${column.javaField}: undefined,
            </#list>
        })
    }

    const handleSubmit = async () => {
        if (!formRef.value) return
        try {
            await formRef.value.validate()
        } catch {
            return
        }
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
                await update${BusinessName}(form)
            } else {
                await add${BusinessName}(form)
            }
            ElMessage.success('操作成功')
            emit('success')
            handleCancel()
        } finally {
            buttonLoading.value = false
        }
    }

    const handleCancel = () => {
        emit('update:visible', false)
    }

    const handleClosed = () => {
        resetForm()
    }

    watch(
        () => props.visible,
        async (newVal) => {
            if (newVal && props.editData) {
                await loadFormData()
            }
        }
    )
</script>
