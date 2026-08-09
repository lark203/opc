export interface RouterJumpVo {
  businessId?: string | number
  taskId?: string | number
  type?: string
  formCustom?: string
  formPath?: string
}

export interface WarmFlowQuery {
  id?: string | number
  type?: string
  onlyDesignShow?: boolean
}
