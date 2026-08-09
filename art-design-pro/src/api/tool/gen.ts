import request from '@/utils/http'

export interface TableVO {
  tableId: string | number
  dataName?: string
  tableName: string
  tableComment: string
  className: string
  tplCategory?: string
  frontendType?: string
  packageName?: string
  moduleName?: string
  businessName?: string
  functionName?: string
  functionAuthor?: string
  options?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface TableQuery {
  tableName?: string
  tableComment?: string
  dataName?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export interface DbTableVO {
  tableId?: string | number
  dataName?: string
  tableName: string
  tableComment: string
  className?: string
  tplCategory?: string
  frontendType?: string
  packageName?: string
  moduleName?: string
  businessName?: string
  functionName?: string
  functionAuthor?: string
  parentMenuId?: string | number
  treeCode?: string
  treeParentCode?: string
  treeName?: string
  treeRootValue?: string
  treeAncestorsField?: string
  treeOrderField?: string
  enableExport?: boolean
  enableStatus?: boolean
  statusField?: string
  enableUnique?: boolean
  uniqueFields?: string[]
  enableSort?: boolean
  sortField?: string
  options?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface DbTableQuery {
  dataName?: string
  tableName?: string
  tableComment?: string
  pageNum?: number
  pageSize?: number
}

export interface DbColumnVO {
  columnId?: string | number
  tableId?: string | number
  columnName: string
  columnComment?: string
  columnType?: string
  javaType?: string
  javaField?: string
  isPk?: string
  isIncrement?: string
  isRequired?: string
  isInsert?: string
  isEdit?: string
  isList?: string
  isQuery?: string
  queryType?: string
  htmlType?: string
  dictType?: string
  sort?: number
}

export interface GenTableDetailPayload {
  info: DbTableVO
  rows: DbColumnVO[]
}

export interface DbTableForm {
  tableId?: string | number
  dataName?: string
  tableName?: string
  tableComment?: string
  className?: string
  functionAuthor?: string
  remark?: string
  tplCategory?: string
  frontendType?: string
  packageName?: string
  moduleName?: string
  businessName?: string
  functionName?: string
  parentMenuId?: string | number
  treeCode?: string
  treeParentCode?: string
  treeName?: string
  treeRootValue?: string
  treeAncestorsField?: string
  treeOrderField?: string
  enableExport?: boolean
  enableStatus?: boolean
  statusField?: string
  enableUnique?: boolean
  uniqueFields?: string[]
  enableSort?: boolean
  sortField?: string
  columns?: DbColumnVO[]
  options?: string
  params?: Record<string, unknown>
}

export function listTable(query: TableQuery) {
  return request.get<{ rows: TableVO[]; total: number }>({
    url: '/tool/gen/list',
    params: query
  })
}

export function listDbTable(query: DbTableQuery) {
  return request.get<{ rows: DbTableVO[]; total: number }>({
    url: '/tool/gen/db/list',
    params: query
  })
}

export function getGenTable(tableId: string | number) {
  return request.get<GenTableDetailPayload>({
    url: `/tool/gen/${tableId}`
  })
}

export function updateGenTable(data: DbTableForm) {
  return request.put({
    url: '/tool/gen',
    params: data
  })
}

export function importTable(data: { tables: string; dataName: string }) {
  return request.post({
    url: `/tool/gen/importTable?tables=${encodeURIComponent(data.tables)}&dataName=${encodeURIComponent(data.dataName)}`
  })
}

export function previewTable(tableId: string | number) {
  return request.get<Record<string, string>>({
    url: `/tool/gen/preview/${tableId}`
  })
}

export function delTable(tableId: string | number | Array<string | number>) {
  return request.del({
    url: `/tool/gen/${tableId}`
  })
}

export function synchDb(tableId: string | number) {
  return request.get({
    url: `/tool/gen/synchDb/${tableId}`
  })
}

export function getDataNames() {
  return request.get<string[]>({
    url: '/tool/gen/getDataNames'
  })
}
