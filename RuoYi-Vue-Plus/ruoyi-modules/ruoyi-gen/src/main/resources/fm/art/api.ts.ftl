import request from '@/utils/http'
import type { ${BusinessName}Form, ${BusinessName}Query, ${BusinessName}VO } from './types'

export const ${businessName}Api = {
/** 查询${functionName}列表 */
<#if table.tree>
    list${BusinessName}: (query?: ${BusinessName}Query) => {
    return request.get
    <${BusinessName}VO[]>({ url: '/${moduleName}/${businessName}/list', params: query })
    },
<#else>
    list${BusinessName}: (query?: ${BusinessName}Query) => {
    return request.get<{ rows: ${BusinessName}VO[]; total: number }>({
    url: '/${moduleName}/${businessName}/list',
    params: query
    })
    },
</#if>

/** 查询${functionName}详细 */
get${BusinessName}: (${pkColumn.javaField}: string | number) => {
return request.get
<${BusinessName}VO>({ url: '/${moduleName}/${businessName}/' + ${pkColumn.javaField} })
    },

    /** 新增${functionName} */
    add${BusinessName}: (data: ${BusinessName}Form) => {
    return request.post({ url: '/${moduleName}/${businessName}', data })
    },

    /** 修改${functionName} */
    update${BusinessName}: (data: ${BusinessName}Form) => {
    return request.put({ url: '/${moduleName}/${businessName}', data })
    },
    <#if enableStatus>

        /** 修改${functionName}状态 */
        change${BusinessName}Status: (${pkColumn.javaField}: string | number, ${statusField}: <#if statusColumn.javaType == 'Boolean'>boolean<#elseif statusColumn.javaType == 'String'>string<#else>number</#if>) => {
        return request.put({ url: '/${moduleName}/${businessName}/changeStatus', data: { ${pkColumn.javaField}, ${statusField} } })
        },
    </#if>
    <#if enableSort>

        /** 调整${functionName}排序 */
        update${BusinessName}Sort: (${pkColumn.javaField}: string | number, ${sortField}: <#if sortColumn.javaType == 'String' || sortColumn.javaType == 'LocalDateTime'>string<#else>number</#if>) => {
        return request.put({ url: '/${moduleName}/${businessName}/updateSort', data: { ${pkColumn.javaField}, ${sortField} } })
        },
    </#if>

    /** 删除${functionName} */
    del${BusinessName}: (${pkColumn.javaField}: string | number | Array
    <string | number>) => {
        return request.del({ url: '/${moduleName}/${businessName}/' + ${pkColumn.javaField} })
        }<#if enableExport>,

        /** 导出${functionName} */
        export${BusinessName}: (query?: ${BusinessName}Query) => {
        return request.download('/${moduleName}/${businessName}/export', query, `${businessName}_${r'${new Date().getTime()}'}.xlsx`)
        }</#if>
        }
