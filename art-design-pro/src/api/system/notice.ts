import request from '@/utils/http'

export interface NoticeVO {
  noticeId: string | number
  noticeTitle: string
  noticeType: string
  noticeContent: string
  status: string
  remark?: string
  createByName?: string
  createTime?: string
}

export interface NoticeForm {
  noticeId?: string | number
  noticeTitle: string
  noticeType: string
  noticeContent: string
  status: string
  remark?: string
}

export interface NoticeQuery {
  noticeTitle?: string
  createByName?: string
  noticeType?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export function listNotice(query: NoticeQuery) {
  return request.get<{ rows: NoticeVO[]; total: number }>({
    url: '/system/notice/list',
    params: query
  })
}

export function getNotice(noticeId: string | number) {
  return request.get<NoticeVO>({
    url: `/system/notice/${noticeId}`
  })
}

export function addNotice(data: NoticeForm) {
  return request.post({
    url: '/system/notice',
    params: data
  })
}

export function updateNotice(data: NoticeForm) {
  return request.put({
    url: '/system/notice',
    params: data
  })
}

export function delNotice(noticeId: Array<string | number> | string | number) {
  return request.del({
    url: `/system/notice/${noticeId}`
  })
}
