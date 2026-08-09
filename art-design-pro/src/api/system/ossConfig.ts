import request from '@/utils/http'

export interface OssConfigVO {
  ossConfigId: string | number
  configKey: string
  endpoint: string
  domainUrl: string
  bucketName: string
  prefix: string
  region: string
  accessPolicy: string
  status: string
}

export interface OssConfigQuery {
  configKey?: string
  bucketName?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export interface OssConfigForm {
  ossConfigId?: string | number
  configKey: string
  accessKey: string
  secretKey: string
  bucketName: string
  prefix: string
  endpoint: string
  domainUrl: string
  isHttps: string
  accessPolicy: string
  region: string
  status: string
  remark: string
}

export const ossConfigApi = {
  listOssConfig: (query?: OssConfigQuery) => {
    return request.get<{ rows: OssConfigVO[]; total: number }>({
      url: '/resource/oss/config/list',
      params: query
    })
  },

  getOssConfig: (ossConfigId: string | number) => {
    return request.get<OssConfigVO>({
      url: '/resource/oss/config/' + ossConfigId
    })
  },

  addOssConfig: (data: OssConfigForm) => {
    return request.post({
      url: '/resource/oss/config',
      data
    })
  },

  updateOssConfig: (data: OssConfigForm) => {
    return request.put({
      url: '/resource/oss/config',
      data
    })
  },

  delOssConfig: (ossConfigId: string | number | Array<string | number>) => {
    return request.del({ url: '/resource/oss/config/' + ossConfigId })
  },

  changeOssConfigStatus: (ossConfigId: string | number, status: string, configKey: string) => {
    return request.put({
      url: '/resource/oss/config/changeStatus',
      data: { ossConfigId, status, configKey }
    })
  }
}
