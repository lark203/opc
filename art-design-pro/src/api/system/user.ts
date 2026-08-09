import request from '@/utils/http'
import type { RoleVO } from './role'

export interface PageResult<T> {
  rows: T[]
  pageNum: number
  pageSize: number
  total: number
}

export interface UserVO {
  userId: string | number
  tenantId: string
  deptId: number
  userName: string
  nickName: string
  userType: string
  email: string
  phoneNumber: string
  gender: string
  avatar?: string | number
  avatarUrl?: string
  status: string
  delFlag: string
  loginIp: string
  loginDate: string
  remark: string
  deptName: string
  dept?: { deptName?: string }
  roles: RoleVO[]
  roleIds: string[]
  postIds: string[]
  roleId: string | number
  admin: boolean
  address?: string
  signature?: string
  tags?: string
}

export interface UserForm {
  id?: string
  userId?: string
  deptId?: number
  userName: string
  nickName?: string
  password: string
  phoneNumber?: string
  email?: string
  gender?: string
  status: string
  remark?: string
  avatar?: string | number
  postIds: string[]
  roleIds: string[]
  address?: string
  signature?: string
  tags?: string
}

export interface UserQuery {
  userName?: string
  nickName?: string
  phoneNumber?: string
  status?: string
  deptId?: string | number
  roleId?: string | number
  pageNum?: number
  pageSize?: number
}

export interface UserInfoVO {
  user: UserVO
  roles: RoleVO[]
  roleIds: string[]
  posts: any[]
  postIds: string[]
  roleGroup: string
  postGroup: string
}

export interface UserRoleAuthVO {
  user: UserVO
  roles: RoleVO[]
}

export function listUser(query: UserQuery) {
  return request.get<PageResult<UserVO>>({
    url: '/system/user/list',
    params: query
  })
}

export function getUser(userId?: string | number) {
  return request.get<UserInfoVO>({
    url: '/system/user/' + (userId || '')
  })
}

export function addUser(data: UserForm) {
  return request.post({
    url: '/system/user',
    params: data
  })
}

export function updateUser(data: UserForm) {
  return request.put({
    url: '/system/user',
    params: data
  })
}

export function delUser(userId: Array<string | number> | string | number) {
  return request.del({
    url: '/system/user/' + userId
  })
}

export function resetUserPwd(userId: string | number, password: string) {
  const data = { userId, password }
  return request.put({
    url: '/system/user/resetPwd',
    params: data,
    isEncrypt: true,
    repeatSubmit: false
  })
}

export function changeUserStatus(userId: number | string, status: string) {
  const data = { userId, status }
  return request.put({
    url: '/system/user/changeStatus',
    params: data
  })
}

export function unlockUser(userId: number | string) {
  return request.get({
    url: '/system/user/unlock/' + userId
  })
}

export function deptTreeSelect() {
  return request.get<any[]>({
    url: '/system/user/deptTree'
  })
}

export function listUserByDeptId(deptId: string | number) {
  return request.get<UserVO[]>({
    url: '/system/user/list/dept/' + deptId
  })
}

export function getUserAuthRole(userId: string | number) {
  return request.get<UserRoleAuthVO>({
    url: '/system/user/authRole/' + userId
  })
}

export function updateUserAuthRole(userId: string | number, roleIds: Array<string | number>) {
  return request.put({
    url: '/system/user/authRole',
    params: { userId, roleIds }
  })
}

export function importUser(file: File, updateSupport: boolean) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('updateSupport', updateSupport.toString())
  return request.post({
    url: '/system/user/importData',
    data: formData
  })
}

export function getImportTemplate(fileName?: string) {
  return request.download(
    '/system/user/importTemplate',
    {},
    fileName || `user_template_${new Date().getTime()}.xlsx`
  )
}

export function exportUser(query: UserQuery, fileName?: string) {
  return request.download(
    '/system/user/export',
    query,
    fileName || `user_${new Date().getTime()}.xlsx`
  )
}
