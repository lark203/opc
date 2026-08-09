import request from '@/utils/http'

export interface RoleVO {
  roleId: string | number
  roleName: string
  roleKey: string
  roleSort: number
  dataScope: string
  menuCheckStrictly: boolean
  deptCheckStrictly: boolean
  status: string
  delFlag: string
  remark?: string
  flag: boolean
  menuIds?: Array<string | number>
  deptIds?: Array<string | number>
  admin: boolean
}

export interface RoleForm {
  roleName: string
  roleKey: string
  roleSort: number
  status: string
  menuCheckStrictly: boolean
  deptCheckStrictly: boolean
  remark: string
  dataScope?: string
  roleId?: string | number
  menuIds: Array<string | number>
  deptIds: Array<string | number>
}

export interface RoleQuery {
  roleName?: string
  roleKey?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export interface DeptTreeOption {
  id: string
  label: string
  parentId: string
  weight: number
  children?: DeptTreeOption[]
}

export interface RoleDeptTree {
  checkedKeys: string[]
  depts: DeptTreeOption[]
}

export interface RoleMenuTree {
  checkedKeys: Array<string | number>
  menus: any[]
}

export interface RoleUserQuery {
  roleId?: string | number
  userName?: string
  nickName?: string
  pageNum?: number
  pageSize?: number
}

export function listRole(query: RoleQuery) {
  return request.get<{ rows: RoleVO[]; total: number }>({
    url: '/system/role/list',
    params: query
  })
}

export function getRole(roleId: string | number) {
  return request.get<RoleVO>({
    url: '/system/role/' + roleId
  })
}

export function addRole(data: RoleForm) {
  return request.post({
    url: '/system/role',
    params: data
  })
}

export function updateRole(data: RoleForm) {
  return request.put({
    url: '/system/role',
    params: data
  })
}

export function updateRolePermission(data: RoleForm) {
  return request.put({
    url: '/system/role/permission',
    params: data
  })
}

export function changeRoleStatus(roleId: string | number, status: string) {
  const data = { roleId, status }
  return request.put({
    url: '/system/role/changeStatus',
    params: data
  })
}

export function delRole(roleId: Array<string | number> | string | number) {
  return request.del({
    url: '/system/role/' + roleId
  })
}

export function deptTreeSelect(roleId: string | number) {
  return request.get<RoleDeptTree>({
    url: '/system/role/deptTree/' + roleId
  })
}

export function getAllocatedList(query: RoleUserQuery) {
  return request.get<{ rows: any[]; total: number }>({
    url: '/system/role/authUser/allocatedList',
    params: query
  })
}

export function getUnallocatedList(query: RoleUserQuery) {
  return request.get<{ rows: any[]; total: number }>({
    url: '/system/role/authUser/unallocatedList',
    params: query
  })
}

export function assignUsersToRole(roleId: string | number, userIds: Array<string | number>) {
  return request.put({
    url: '/system/role/authUser/selectAll',
    params: { roleId, userIds: userIds.join(',') },
    data: { roleId, userIds: userIds.join(',') }
  })
}

export function cancelUsersFromRole(roleId: string | number, userIds: Array<string | number>) {
  return request.put({
    url: '/system/role/authUser/cancelAll',
    params: { roleId, userIds: userIds.join(',') },
    data: { roleId, userIds: userIds.join(',') }
  })
}

export function exportRole(query: RoleQuery, fileName?: string) {
  return request.download(
    '/system/role/export',
    query,
    fileName || `role_${new Date().getTime()}.xlsx`
  )
}
