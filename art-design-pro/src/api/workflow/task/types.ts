/** 待办任务查询参数（对应后端 FlowTaskBo + PageQuery） */
export interface TaskQuery {
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

/** 流程任务视图对象（对应后端 FlowTaskVo） */
export interface FlowTaskVO {
  /** 任务ID */
  id: string | number
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
  /** 流程定义ID */
  definitionId?: string
  /** 流程实例ID */
  instanceId: string
  /** 流程定义名称 */
  flowName: string
  /** 业务ID */
  businessId: string
  /** 节点编码 */
  nodeCode: string
  /** 节点名称（任务名称） */
  nodeName: string
  /** 节点类型（0开始 1中间 2结束 3互斥网关 4并行网关） */
  nodeType?: number
  /** 审批表单是否自定义（Y是 N否） */
  formCustom: string
  /** 审批表单路径 */
  formPath: string
  /** 流程定义编码 */
  flowCode: string
  /** 流程版本号 */
  version?: string
  /** 流程状态（字典 wf_business_status 的 value） */
  flowStatus: string
  /** 流程状态名称 */
  flowStatusName?: string
  /** 流程分类ID */
  category?: string
  /** 流程分类名称 */
  categoryName?: string
  /** 办理人IDs */
  assigneeIds?: string
  /** 办理人名称 */
  assigneeNames?: string
  /** 抄送人ID */
  processedBy?: string
  /** 抄送人名称 */
  processedByName?: string
  /** 申请人ID */
  createBy?: string
  /** 申请人名称 */
  createByName?: string
  /** 是否为申请人节点 */
  applyNode?: boolean
  /** 业务编码 */
  businessCode?: string
  /** 业务标题 */
  businessTitle?: string
  /** 按钮权限列表 */
  buttonList?: ButtonList[]
  /** 抄送对象列表 */
  copyList?: FlowCopyVo[]
  /** 自定义参数 Map */
  varList?: Record<string, string>
  /** 任务状态（已办任务的办理状态，字典 wf_task_status） */
  flowTaskStatus?: string
  /** 审批人名称（已办任务） */
  approverName?: string
}

/** 按钮权限 */
export interface ButtonList {
  /** 按钮编码 */
  code: string
  /** 是否显示 */
  show: boolean
}

/** 抄送对象 */
export interface FlowCopyVo {
  /** 用户ID */
  userId: string | number
  /** 用户昵称 */
  nickName: string
}

/** 任务操作参数（对应后端 TaskOperationBo） */
export interface TaskOperationBo {
  /** 委派/转办人的用户ID（针对委派/转办操作） */
  userId?: string | number
  /** 加签/减签人的用户ID列表（针对加签/减签操作） */
  userIds?: string[]
  /** 任务ID */
  taskId: string | number
  /** 消息类型（1站内信 2邮件 3短信） */
  messageType?: string[]
  /** 意见或备注信息 */
  message?: string
}
