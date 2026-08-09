import request from '@/utils/http'
import type { CategoryTreeVO, FlowCategoryForm, FlowCategoryQuery, FlowCategoryVO } from './types'

export const listCategory = (query: FlowCategoryQuery) =>
  request.get<FlowCategoryVO[]>({ url: '/workflow/category/list', params: query })

export const getCategory = (id: string | number) =>
  request.get<FlowCategoryVO>({ url: `/workflow/category/${id}` })

export const addCategory = (data: FlowCategoryForm) =>
  request.post({ url: '/workflow/category', params: data })

export const updateCategory = (data: FlowCategoryForm) =>
  request.put({ url: '/workflow/category', params: data })

export const delCategory = (id: string | number) => request.del({ url: `/workflow/category/${id}` })

export const categoryTree = () =>
  request.get<CategoryTreeVO[]>({ url: '/workflow/category/categoryTree' })
