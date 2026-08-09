import request from '@/utils/http'

export interface OssUploadVO {
  ossId: string
  url: string
  fileName: string
}

export interface OssVO {
  ossId: string | number
  url: string
  fileName: string
  originalName: string
  fileSuffix: string
  createByName: string
  service: string
  createTime: string
}

export interface OssQuery {
  fileName?: string
  originalName?: string
  fileSuffix?: string
  createTime?: string
  service?: string
  pageNum?: number
  pageSize?: number
  orderByColumn?: string
  isAsc?: string
}

export interface OssForm {
  file?: undefined | string
}

export const delOss = (ossId: string | number | Array<string | number>) => {
  return request.del({ url: '/resource/oss/' + ossId })
}

export const listByIds = (ossIds: string | number) => {
  return request.get<OssVO[]>({
    url: `/resource/oss/listByIds/${ossIds}`
  })
}

export const ossApi = {
  uploadOss: (data: FormData) => {
    return request.post<OssUploadVO>({ url: '/resource/oss/upload', data })
  },

  listOss: (query?: OssQuery) => {
    return request.get<{ rows: OssVO[]; total: number }>({
      url: '/resource/oss/list',
      params: query
    })
  },

  delOss,

  downloadOss: (ossId: string | number) => {
    return request.download(`/resource/oss/download/${ossId}`)
  },

  listByIds
}
