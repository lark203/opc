import request from '@/utils/http'
import type { FlowTaskVO, TaskOperationBo, TaskQuery } from './types'

/** 查询当前用户待办任务（分页） */
export const pageByTaskWait = (query: TaskQuery) =>
  request.get<{ rows: FlowTaskVO[]; total: number }>({
    url: '/workflow/task/pageByTaskWait',
    params: query
  })

/** 查询当前用户已办任务（分页） */
export const pageByTaskFinish = (query: TaskQuery) =>
  request.get<{ rows: FlowTaskVO[]; total: number }>({
    url: '/workflow/task/pageByTaskFinish',
    params: query
  })

/** 查询当前用户抄送任务（分页） */
export const pageByTaskCopy = (query: TaskQuery) =>
  request.get<{ rows: FlowTaskVO[]; total: number }>({
    url: '/workflow/task/pageByTaskCopy',
    params: query
  })

/** 查询全部待办任务（分页，需 workflow:task:list 权限） */
export const pageByAllTaskWait = (query: TaskQuery) =>
  request.get<{ rows: FlowTaskVO[]; total: number }>({
    url: '/workflow/task/pageByAllTaskWait',
    params: query
  })

/** 查询全部已办任务（分页，需 workflow:task:list 权限） */
export const pageByAllTaskFinish = (query: TaskQuery) =>
  request.get<{ rows: FlowTaskVO[]; total: number }>({
    url: '/workflow/task/pageByAllTaskFinish',
    params: query
  })

/** 启动流程 */
export const startWorkFlow = (data: object) =>
  request.post<{ taskId: string | number }>({ url: '/workflow/task/startWorkFlow', data })

/** 办理流程 */
export const completeTask = (data: object) =>
  request.post({ url: '/workflow/task/completeTask', data })

/** 任务驳回 */
export const backProcess = (data: object) =>
  request.post({ url: '/workflow/task/backProcess', data })

/** 获取当前任务 */
export const getTask = (taskId: string | number) =>
  request.get<FlowTaskVO>({ url: `/workflow/task/getTask/${taskId}` })

/**
 * 修改任务办理人
 * @param taskIdList 任务ID列表
 * @param userId 办理人用户ID
 */
export const updateAssignee = (taskIdList: Array<string | number>, userId: string | number) =>
  request.put({
    url: `/workflow/task/updateAssignee/${userId}`,
    data: taskIdList
  })

/** 终止任务 */
export const terminationTask = (data: { taskId: string | number; comment?: string }) =>
  request.post({ url: '/workflow/task/terminationTask', data })

/** 获取可驳回的任务节点 */
export const getBackTaskNode = (taskId: string | number, nodeCode: string) =>
  request.get({ url: `/workflow/task/getBackTaskNode/${taskId}/${nodeCode}` })

/**
 * 任务操作
 * @param operation 操作类型：delegateTask 委派、transferTask 转办、addSignature 加签、reductionSignature 减签
 */
export const taskOperation = (operation: string, data: TaskOperationBo) =>
  request.post({ url: `/workflow/task/taskOperation/${operation}`, data })

/** 获取当前任务办理人 */
export const currentTaskAllUser = (taskId: string | number) =>
  request.get({ url: `/workflow/task/currentTaskAllUser/${taskId}` })

/** 获取下一节点 */
export const getNextNodeList = (data: object) =>
  request.post({ url: '/workflow/task/getNextNodeList', data })

/** 催办任务 */
export const urgeTask = (data: {
  taskIdList: Array<string | number>
  messageType: string[]
  message: string
}) => request.post({ url: '/workflow/task/urgeTask', data })
