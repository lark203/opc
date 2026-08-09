import request from '@/utils/http'

export interface DemoVO {
  /** 主键 */
  id: string | number
  /** 部门id */
  deptId: string | number
  /** 用户id */
  userId: string | number
  /** 排序号 */
  orderNum: number
  /** key键 */
  testKey: string
  /** 值 */
  value: string
}

export interface DemoQuery {
  testKey?: string
  value?: string
  pageNum?: number
  pageSize?: number
}

export interface DemoForm {
  id?: string | number
  deptId?: string | number
  userId?: string | number
  orderNum?: number
  testKey?: string
  value?: string
}

export const demoApi = {
  listDemo: (query?: DemoQuery) => {
    return request.get<{ rows: DemoVO[]; total: number }>({
      url: '/demo/demo/list',
      params: query
    })
  },

  getDemo: (id: string | number) => {
    return request.get<DemoVO>({ url: '/demo/demo/' + id })
  },

  addDemo: (data: DemoForm) => {
    return request.post({ url: '/demo/demo', data })
  },

  updateDemo: (data: DemoForm) => {
    return request.put({ url: '/demo/demo', data })
  },

  delDemo: (id: string | number | Array<string | number>) => {
    return request.del({ url: '/demo/demo/' + id })
  },

  exportDemo: (query?: DemoQuery) => {
    return request.download('/demo/demo/export', query, `demo_${new Date().getTime()}.xlsx`)
  }
}
