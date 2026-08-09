import request from '@/utils/http'

export interface DeptVO {
  deptId: string | number
  parentId: string | number
  deptName: string
  deptCategory?: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: string
  children?: DeptVO[]
  hasChildren?: boolean
}

export interface DeptForm {
  deptId?: string | number
  parentId?: string | number
  deptName: string
  deptCategory?: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: string
}

export interface DeptQuery {
  deptName?: string
  deptCategory?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export interface DeptTreeOption {
  deptId: string | number
  parentId: string | number
  deptName: string
  children?: DeptTreeOption[]
}

export interface DeptTreeVO {
  id: number | string
  label: string
  parentId: number | string
  weight: number
  children: DeptTreeVO[]
  disabled: boolean
}

export function listDept(query?: DeptQuery) {
  return request.get<DeptVO[]>({
    url: '/system/dept/list',
    params: query
  })
}

export function getDept(deptId: string | number) {
  return request.get<DeptForm>({
    url: `/system/dept/${deptId}`
  })
}

export function delDept(deptId: string | number) {
  return request.del({
    url: `/system/dept/${deptId}`
  })
}

export function addDept(data: DeptForm) {
  return request.post({
    url: '/system/dept',
    params: data
  })
}

export function updateDept(data: DeptForm) {
  return request.put({
    url: '/system/dept',
    params: data
  })
}

export function listDeptExcludeChild(deptId: string | number) {
  return request.get<DeptVO[]>({
    url: `/system/dept/list/exclude/${deptId}`
  })
}
