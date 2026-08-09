/** 请假视图对象（对应后端 TestLeaveVo） */
export interface LeaveVO {
  /** 主键 */
  id: string | number
  /** 申请编号 */
  applyCode?: string
  /** 请假类型（1事假 2调休 3病假 4婚假） */
  leaveType: string
  /** 开始时间 */
  startDate: string
  /** 结束时间 */
  endDate: string
  /** 请假天数 */
  leaveDays: number
  /** 请假原因 */
  remark: string
  /** 流程状态（字典 wf_business_status 的 value） */
  status?: string
}

/** 请假表单对象（对应后端 TestLeaveBo） */
export interface LeaveForm {
  /** 主键 */
  id?: string | number
  /** 申请编号 */
  applyCode?: string
  /** 请假类型 */
  leaveType?: string
  /** 开始时间 */
  startDate?: string
  /** 结束时间 */
  endDate?: string
  /** 请假天数 */
  leaveDays?: number
  /** 请假原因 */
  remark?: string
  /** 流程状态 */
  status?: string
}

/** 请假查询参数（对应后端 TestLeaveBo + PageQuery） */
export interface LeaveQuery {
  /** 请假天数起始（范围查询） */
  startLeaveDays?: number
  /** 请假天数结束（范围查询） */
  endLeaveDays?: number
  /** 当前页码 */
  pageNum?: number
  /** 每页条数 */
  pageSize?: number
}

/** 启动流程参数（对应后端 StartProcessBo） */
export interface StartProcessBo {
  /** 业务ID */
  businessId: string | number
  /** 流程定义编码 */
  flowCode: string
  /** 流程变量 */
  variables: Record<string, unknown>
  /** 业务扩展字段 */
  bizExt: Record<string, unknown>
}
