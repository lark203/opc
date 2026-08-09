import request from '@/utils/http'
import type {
  FlowCancelForm,
  FlowHisTaskVO,
  FlowInstanceQuery,
  FlowInstanceVO,
  FlowInvalidForm,
  FlowVariableForm
} from './types'

/** 查询运行中实例列表（分页） */
export const pageByRunning = (query: FlowInstanceQuery) =>
  request.get<{ rows: FlowInstanceVO[]; total: number }>({
    url: '/workflow/instance/pageByRunning',
    params: query
  })

/** 查询已完成实例列表（分页） */
export const pageByFinish = (query: FlowInstanceQuery) =>
  request.get<{ rows: FlowInstanceVO[]; total: number }>({
    url: '/workflow/instance/pageByFinish',
    params: query
  })

/** 分页查询当前登录人发起的单据（我发起的） */
export const pageByCurrent = (query: FlowInstanceQuery) =>
  request.get<{ rows: FlowInstanceVO[]; total: number }>({
    url: '/workflow/instance/pageByCurrent',
    params: query
  })

/** 撤销流程申请（仅申请人可撤销） */
export const cancelProcessApply = (data: FlowCancelForm) =>
  request.put({ url: '/workflow/instance/cancelProcessApply', data })

/** 删除运行中流程实例 */
export const deleteByInstanceIds = (ids: string | number | Array<string | number>) =>
  request.del({ url: `/workflow/instance/deleteByInstanceIds/${ids}` })

/** 删除历史流程实例 */
export const deleteHisByInstanceIds = (ids: string | number | Array<string | number>) =>
  request.del({ url: `/workflow/instance/deleteHisByInstanceIds/${ids}` })

/** 获取流程变量 */
export const instanceVariable = (instanceId: string | number) =>
  request.get<{ variable: string }>({ url: `/workflow/instance/instanceVariable/${instanceId}` })

/** 修改流程变量 */
export const updateVariable = (data: FlowVariableForm) =>
  request.put({ url: '/workflow/instance/updateVariable', data })

/** 作废流程 */
export const invalid = (data: FlowInvalidForm) =>
  request.post({ url: '/workflow/instance/invalid', data })

/** 通过业务ID获取历史流程任务列表（含流程实例ID） */
export const flowHisTaskList = (businessId: string | number) =>
  request.get<{ list: FlowHisTaskVO[]; instanceId: string | number }>({
    url: `/workflow/instance/flowHisTaskList/${businessId}`
  })

/** 通过业务ID获取流程实例信息（含流程定义编码 flowCode） */
export const getInstanceByBusinessId = (businessId: string | number) =>
  request.get<FlowInstanceVO>({ url: `/workflow/instance/getInfo/${businessId}` })
