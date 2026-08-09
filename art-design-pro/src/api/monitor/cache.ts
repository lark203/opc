import request from '@/utils/http'

export interface CacheVO {
  commandStats: Array<{ name: string; value: string }>
  dbSize: number
  info: { [key: string]: string }
}

export function getCache() {
  return request.get<CacheVO>({
    url: '/monitor/cache'
  })
}
