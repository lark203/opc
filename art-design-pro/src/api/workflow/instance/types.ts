/** 流程实例查询参数 */
export interface FlowInstanceQuery {
  /** 流程分类ID */
  category?: string | number
  /** 任务名称 */
  nodeName?: string
  /** 流程定义编码 */
  flowCode?: string
  /** 流程定义名称 */
  flowName?: string
  /** 申请人ID列表（逗号分隔字符串） */
  createByIds?: string
  /** 当前页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

/** 流程实例视图对象（对应后端 FlowInstanceVo） */
export interface FlowInstanceVO {
  /** 流程实例ID */
  id: string | number
  /** 流程定义ID */
  definitionId: string
  /** 流程定义名称 */
  flowName: string
  /** 流程定义编码 */
  flowCode: string
  /** 流程定义版本 */
  version: string
  /** 业务ID */
  businessId: string
  /** 业务编码 */
  businessCode: string
  /** 业务标题 */
  businessTitle: string
  /** 流程节点编码 */
  nodeCode: string
  /** 任务名称（当前节点） */
  nodeName: string
  /** 流程状态（字典 wf_business_status 的 value） */
  flowStatus: string
  /** 流程状态名称 */
  flowStatusName: string
  /** 流程激活状态（0挂起 1激活） */
  activityStatus: number
  /** 审批表单是否自定义（Y是 N否） */
  formCustom: string
  /** 审批表单路径 */
  formPath: string
  /** 创建时间 */
  createTime: string
  /** 创建者ID */
  createBy: string
  /** 申请人名称 */
  createByName: string
  /** 流程分类ID */
  category: string
  /** 流程分类名称 */
  categoryName: string
  /** 更新时间（结束时间） */
  updateTime?: string
}

/** 流程变量表单（对应后端 FlowVariableBo） */
export interface FlowVariableForm {
  /** 流程实例ID */
  instanceId: string | number
  /** 变量KEY */
  key: string
  /** 变量值 */
  value: string
}

/** 作废流程表单（对应后端 FlowInvalidBo） */
export interface FlowInvalidForm {
  /** 流程实例ID */
  id: string | number
  /** 作废原因 */
  comment: string
}

/** 撤销流程表单（对应后端 FlowCancelBo） */
export interface FlowCancelForm {
  /** 业务ID */
  businessId: string
  /** 撤销说明 */
  message?: string
}

/** 历史流程任务视图对象（对应后端 FlowHisTaskVo） */
export interface FlowHisTaskVO {
  /** 节点名称 */
  nodeName: string
  /** 办理人名称 */
  approverName?: string
  /** 流程状态（字典 wf_task_status 的 value） */
  flowStatus?: string
  /** 审批意见 */
  message?: string
  /** 开始时间 */
  createTime?: string
  /** 结束时间 */
  updateTime?: string
  /** 运行时长 */
  runDuration?: string
}
