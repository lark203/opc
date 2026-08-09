export interface FlowDefinitionVO {
  id: string | number
  flowCode: string
  flowName: string
  category: string | number
  categoryName?: string
  version: number
  /** 0 未发布 1 已发布 9 失效 */
  isPublish: string | number
  /** Y/N 是否自定义表单 */
  formCustom?: string
  formPath?: string
  /** 0 挂起 1 激活 */
  activityStatus: string | number
  listenerType?: string
  listenerPath?: string
  ext?: string
  createTime?: string
}

export interface FlowDefinitionForm {
  id?: string | number
  category: string | number
  ext?: string
  formPath?: string
  /** Y/N */
  formCustom?: string
  /** CLASSICS / MIMIC */
  modelValue?: string
  flowCode?: string
  flowName?: string
}

export interface FlowDefinitionQuery {
  flowCode?: string
  flowName?: string
  category?: string | number
  isPublish?: string | number
  pageNum?: number
  pageSize?: number
}

export interface DefinitionXmlVO {
  xml: string[]
  xmlStr: string
}
