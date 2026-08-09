import request from '@/utils/http'
import type { DeptTreeVO } from './dept'

export interface PostVO {
  postId: string | number
  postCode: string
  postName: string
  postCategory: string
  postSort: number
  status: string
  remark?: string
  deptId?: string | number
  deptName?: string
}

export interface PostForm {
  postId?: string | number
  postCode: string
  postName: string
  postCategory: string
  postSort: number
  status: string
  remark?: string
  deptId?: string | number
}

export interface PostQuery {
  postCode?: string
  postName?: string
  postCategory?: string
  status?: string
  deptId?: string | number
  belongDeptId?: string | number
  pageNum?: number
  pageSize?: number
}

export function listPost(query: PostQuery) {
  return request.get<{ rows: PostVO[]; total: number }>({
    url: '/system/post/list',
    params: query
  })
}

export function getPost(postId: string | number) {
  return request.get<PostForm>({
    url: `/system/post/${postId}`
  })
}

export function addPost(data: PostForm) {
  return request.post({
    url: '/system/post',
    params: data
  })
}

export function updatePost(data: PostForm) {
  return request.put({
    url: '/system/post',
    params: data
  })
}

export function delPost(postId: Array<string | number> | string | number) {
  return request.del({
    url: `/system/post/${postId}`
  })
}

export function deptTreeSelect() {
  return request.get<DeptTreeVO[]>({
    url: '/system/post/deptTree'
  })
}

export function exportPost(query: PostQuery, fileName?: string) {
  return request.download(
    '/system/post/export',
    query,
    fileName || `post_${new Date().getTime()}.xlsx`
  )
}
