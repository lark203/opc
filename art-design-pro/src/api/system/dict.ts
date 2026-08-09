import request from '@/utils/http'

export interface PageResult<T> {
  rows: T[]
  pageNum: number
  pageSize: number
  total: number
}

export interface DictTypeVO {
  dictId: number | string
  dictName: string
  dictType: string
  remark: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface DictTypeForm {
  dictId?: number | string
  dictName: string
  dictType: string
  remark?: string
}

export interface DictTypeQuery {
  dictName?: string
  dictType?: string
  pageNum?: number
  pageSize?: number
}

export interface DictDataVO {
  dictCode: string | number
  dictType: string
  dictLabel: string
  dictValue: string
  cssClass: string
  listClass: string
  dictSort: number
  remark: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface DictDataForm {
  dictCode?: string | number
  dictType?: string
  dictLabel: string
  dictValue: string
  cssClass?: string
  listClass?: string
  dictSort?: number
  remark?: string
}

export interface DictDataQuery {
  dictName?: string
  dictType?: string
  dictLabel?: string
  pageNum?: number
  pageSize?: number
}

export interface DictDataOption {
  label: string
  value: string
  elTagType?: string
  elTagClass?: string
}

export function getDicts(dictType: string) {
  return request.get<DictDataVO[]>({
    url: `/system/dict/data/type/${dictType}`
  })
}

export function listDictType(query: DictTypeQuery) {
  return request.get<PageResult<DictTypeVO>>({
    url: '/system/dict/type/list',
    params: query
  })
}

export function getDictType(dictId: number | string) {
  return request.get<DictTypeVO>({
    url: `/system/dict/type/${dictId}`
  })
}

export function addDictType(data: DictTypeForm) {
  return request.post({
    url: '/system/dict/type',
    params: data
  })
}

export function updateDictType(data: DictTypeForm) {
  return request.put({
    url: '/system/dict/type',
    params: data
  })
}

export function delDictType(dictId: string | number | Array<string | number>) {
  return request.del({
    url: `/system/dict/type/${dictId}`
  })
}

export function refreshDictCache() {
  return request.del({
    url: '/system/dict/type/refreshCache'
  })
}

export function getDictTypeOptions() {
  return request.get<DictTypeVO[]>({
    url: '/system/dict/type/optionselect'
  })
}

export function listDictData(query: DictDataQuery) {
  return request.get<PageResult<DictDataVO>>({
    url: '/system/dict/data/list',
    params: query
  })
}

export function getDictData(dictCode: string | number) {
  return request.get<DictDataVO>({
    url: `/system/dict/data/${dictCode}`
  })
}

export function addDictData(data: DictDataForm) {
  return request.post({
    url: '/system/dict/data',
    params: data
  })
}

export function updateDictData(data: DictDataForm) {
  return request.put({
    url: '/system/dict/data',
    params: data
  })
}

export function delDictData(dictCode: string | number | Array<string | number>) {
  return request.del({
    url: `/system/dict/data/${dictCode}`
  })
}

export function exportDictType(query: DictTypeQuery, fileName?: string) {
  return request.download(
    '/system/dict/type/export',
    query,
    fileName || `dict_type_${new Date().getTime()}.xlsx`
  )
}

export function exportDictData(query: DictDataQuery, fileName?: string) {
  return request.download(
    '/system/dict/data/export',
    query,
    fileName || `dict_data_${new Date().getTime()}.xlsx`
  )
}
