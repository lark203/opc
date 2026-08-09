export interface FlowSpelVO {
  id: string | number
  componentName: string
  methodName: string
  methodParams?: string
  viewSpel: string
  status: string
  remark?: string
  createTime?: string
}

export interface FlowSpelForm {
  id?: string | number
  componentName: string
  methodName: string
  methodParams?: string
  viewSpel: string
  status: string
  remark?: string
}

export interface FlowSpelQuery {
  componentName?: string
  methodName?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
