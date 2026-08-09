export interface FlowCategoryVO {
  categoryId: string | number
  parentId: string | number
  parentName?: string
  ancestors?: string
  categoryName: string
  orderNum: number
  createTime?: string
  /** 子级分类列表（树形结构时使用） */
  children?: FlowCategoryVO[]
  /** 是否存在子节点（用于树形表格懒加载标识） */
  hasChildren?: boolean
}

export interface FlowCategoryForm {
  categoryId?: string | number
  parentId: string | number
  categoryName: string
  orderNum: number
}

export interface FlowCategoryQuery {
  categoryName?: string
  parentId?: string | number
  pageNum?: number
  pageSize?: number
}

export interface CategoryTreeVO {
  id: string | number
  parentId: string | number
  label: string
  weight?: number
  children?: CategoryTreeVO[]
}
