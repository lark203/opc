import request from '@/utils/http'
import type { FlowDefinitionForm, FlowDefinitionQuery, FlowDefinitionVO } from './types'

export const listDefinition = (query: FlowDefinitionQuery) =>
  request.get<{ rows: FlowDefinitionVO[]; total: number }>({
    url: '/workflow/definition/list',
    params: query
  })

export const unPublishList = (query: FlowDefinitionQuery) =>
  request.get<{ rows: FlowDefinitionVO[]; total: number }>({
    url: '/workflow/definition/unPublishList',
    params: query
  })

export const getDefinition = (id: string | number) =>
  request.get<FlowDefinitionVO>({ url: `/workflow/definition/${id}` })

export const addDefinition = (data: FlowDefinitionForm) =>
  request.post({ url: '/workflow/definition', params: data })

export const editDefinition = (data: FlowDefinitionForm) =>
  request.put({ url: '/workflow/definition', params: data })

export const deleteDefinition = (ids: string | number | Array<string | number>) =>
  request.del({ url: `/workflow/definition/${ids}` })

export const publish = (id: string | number) =>
  request.put({ url: `/workflow/definition/publish/${id}` })

export const unPublish = (id: string | number) =>
  request.put({ url: `/workflow/definition/unPublish/${id}` })

// active 为 @RequestParam，需拼在 URL 上（put 会把 params 转成 body）
export const active = (id: string | number, activityStatus: boolean) =>
  request.put({ url: `/workflow/definition/active/${id}?active=${activityStatus}` })

export const copyDefinition = (id: string | number) =>
  request.post({ url: `/workflow/definition/copy/${id}` })

export const xmlString = (id: string | number) =>
  request.get<string>({ url: `/workflow/definition/xmlString/${id}` })

export const importDef = (data: FormData) =>
  request.post({
    url: '/workflow/definition/importDef',
    data,
    repeatSubmit: false
  })

// 后端 exportDef 为 POST 接口，download 仅在 params 非空时才走 POST，故显式带上 id
export const exportDef = (id: string | number, fileName?: string) =>
  request.download(`/workflow/definition/exportDef/${id}`, { id }, fileName)
