import request from '@/utils/http'

/** Redis 服务器概览信息 */
export interface RedisInfoVO {
  version: string
  mode: string
  uptimeInSeconds: number
  connectedClients: number
  usedMemory: string
  usedMemoryHuman: string
  usedMemoryPeak: string
  usedMemoryPeakHuman: string
  dbSize: number
  keyspaceHits: number
  keyspaceMisses: number
  hitRate: string
  instantaneousOpsPerSec: number
  totalNetInputBytes: string
  totalNetOutputBytes: string
}

/** 缓存键 */
export interface CacheKeyVO {
  key: string
  type: string
  ttl: number
  memoryUsage?: number
  size?: number
}

/** 缓存键详情 */
export interface CacheKeyDetailVO extends CacheKeyVO {
  value: unknown
}

/** 缓存键查询参数 */
export interface CacheKeyQuery {
  pattern?: string
  type?: string
  pageNum?: number
  pageSize?: number
}

/** 获取 Redis 服务器概览信息 */
export function getRedisInfo() {
  return request.get<RedisInfoVO>({
    url: '/monitor/cachekey/info'
  })
}

/** 分页查询缓存键列表 */
export function listCacheKey(query: CacheKeyQuery) {
  return request.get<{ rows: CacheKeyVO[]; total: number }>({
    url: '/monitor/cachekey/keys',
    params: query
  })
}

/** 查询缓存键详情 */
export function getCacheKeyDetail(key: string) {
  return request.get<CacheKeyDetailVO>({
    url: '/monitor/cachekey/key',
    params: { key }
  })
}

/** 删除指定缓存键 */
export function delCacheKey(key: string) {
  return request.del({
    url: '/monitor/cachekey/key',
    params: { key }
  })
}

/** 批量删除缓存键 */
export function delCacheKeyBatch(keys: string[]) {
  return request.del({
    url: '/monitor/cachekey/batch',
    data: keys
  })
}

/** 清空当前库的所有缓存 */
export function clearCacheKey() {
  return request.del({
    url: '/monitor/cachekey/clear'
  })
}
