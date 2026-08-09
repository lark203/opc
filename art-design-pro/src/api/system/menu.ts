import request from '@/utils/http'
import type { AppRouteRecord } from '@/types/router'

export interface MenuVO {
  menuId: string | number
  menuName: string
  icon?: string
  parentId: string | number
  orderNum: number
  menuType: string
  path?: string
  component?: string
  queryParam?: string
  isFrame: string
  isCache: string
  visible: string
  status: string
  perms?: string
  activeMenu?: string
  remark?: string
  children?: MenuVO[]
  hasChildren?: boolean
}

export interface MenuForm {
  menuId?: string | number
  menuName: string
  icon?: string
  parentId: string | number
  orderNum: number
  menuType: string
  path?: string
  component?: string
  queryParam?: string
  isFrame: string
  isCache: string
  visible: string
  status: string
  perms?: string
  activeMenu?: string
  remark?: string
}

export interface MenuQuery {
  menuName?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export function getRouters(): Promise<AppRouteRecord[]> {
  return request.get({
    url: '/system/menu/getRouters'
  })
}

export function listMenu(query?: MenuQuery) {
  return request.get<MenuVO[]>({
    url: '/system/menu/list',
    params: query
  })
}

export function getMenu(menuId: string | number) {
  return request.get<MenuForm>({
    url: `/system/menu/${menuId}`
  })
}

export function treeselect() {
  return request.get<any[]>({
    url: '/system/menu/treeselect'
  })
}

export function roleMenuTreeselect(roleId: string | number) {
  return request.get({
    url: `/system/menu/roleMenuTreeselect/${roleId}`
  })
}

export function addMenu(data: MenuForm) {
  return request.post({
    url: '/system/menu',
    params: data
  })
}

export function updateMenu(data: MenuForm) {
  return request.put({
    url: '/system/menu',
    params: data
  })
}

export function delMenu(menuId: string | number) {
  return request.del({
    url: `/system/menu/${menuId}`
  })
}

export function cascadeDelMenu(menuIds: Array<string | number>) {
  return request.del({
    url: `/system/menu/cascade/${menuIds.join(',')}`
  })
}
