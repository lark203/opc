import request from '@/utils/http'

export interface TreeVO {
  /** 主键 */
  id: string | number
  /** 父id */
  parentId: string | number
  /** 部门id */
  deptId: string | number
  /** 用户id */
  userId: string | number
  /** 树节点名 */
  treeName: string
  /** 是否包含子节点（前端构建树时补充） */
  hasChildren?: boolean
  /** 子对象 */
  children?: TreeVO[]
}

export interface TreeQuery {
  parentId?: string | number
  deptId?: string | number
  userId?: string | number
  treeName?: string
}

export interface TreeForm {
  id?: string | number
  parentId?: string | number
  deptId?: string | number
  userId?: string | number
  treeName?: string
}

export const treeApi = {
  listTree: (query?: TreeQuery) => {
    return request.get<TreeVO[]>({ url: '/demo/tree/list', params: query })
  },

  getTree: (id: string | number) => {
    return request.get<TreeVO>({ url: '/demo/tree/' + id })
  },

  addTree: (data: TreeForm) => {
    return request.post({ url: '/demo/tree', data })
  },

  updateTree: (data: TreeForm) => {
    return request.put({ url: '/demo/tree', data })
  },

  delTree: (id: string | number | Array<string | number>) => {
    return request.del({ url: '/demo/tree/' + id })
  }
}
