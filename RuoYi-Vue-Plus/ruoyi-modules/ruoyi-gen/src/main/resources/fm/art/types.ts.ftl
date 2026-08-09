export interface ${BusinessName}VO {
<#list columns as column>
    <#if column.pk>
        /** ${column.columnComment} */
        ${column.javaField}: ${column.tsType}
    <#elseif column.list || (table.tree && (column.javaField == treeCode || column.javaField == treeParentCode || column.javaField == treeName))>
        /** ${column.columnComment} */
        ${column.javaField}: ${column.tsType}
        <#if column.htmlType == "imageUpload">
            /** ${column.columnComment}Url */
            ${column.javaField}Url: string
        </#if>
    </#if>
</#list>
<#if table.tree>
    /** 是否含有子节点 */
    hasChildren?: boolean
    /** 子对象 */
    children?: ${BusinessName}VO[]
</#if>
}

export interface ${BusinessName}Form {
<#list columns as column>
    <#if column.pk>
        /** ${column.columnComment} */
        ${column.javaField}?: ${column.tsType}
    <#elseif column.insert || column.edit>
        /** ${column.columnComment} */
        ${column.javaField}?: <#if column.htmlType == "checkbox">string | string[]<#else>${column.tsType}</#if>
    </#if>
</#list>
}

export interface ${BusinessName}Query {
<#list columns as column>
    <#if column.query>
        /** ${column.columnComment} */
        ${column.javaField}?: ${column.tsType}
    </#if>
</#list>
<#if !table.tree>
    /** 页码 */
    pageNum?: number
    /** 每页条数 */
    pageSize?: number
</#if>
<#if needAddDateRange>
    /** 日期范围参数 */
    params?: Record
    <string, any>
</#if>
}
