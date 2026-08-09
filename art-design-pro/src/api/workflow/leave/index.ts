import request from '@/utils/http'
import type { LeaveForm, LeaveQuery, LeaveVO } from './types'

/** 查询请假列表（分页） */
export const listLeave = (query?: LeaveQuery) =>
  request.get<{ rows: LeaveVO[]; total: number }>({
    url: '/workflow/leave/list',
    params: query
  })

/** 查询请假详情 */
export const getLeave = (id: string | number) =>
  request.get<LeaveVO>({ url: `/workflow/leave/${id}` })

/** 新增请假 */
export const addLeave = (data: LeaveForm) => request.post<LeaveVO>({ url: '/workflow/leave', data })

/** 修改请假 */
export const updateLeave = (data: LeaveForm) =>
  request.put<LeaveVO>({ url: '/workflow/leave', data })

/** 提交请假并发起流程（后端发起模式） */
export const submitAndFlowStart = (data: LeaveForm) =>
  request.post<LeaveVO>({ url: '/workflow/leave/submitAndFlowStart', data })

/** 删除请假 */
export const delLeave = (id: string | number | Array<string | number>) =>
  request.del({ url: `/workflow/leave/${id}` })
