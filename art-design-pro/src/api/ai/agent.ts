import request from '@/utils/http'

export interface SnailOpenApiUser {
  openId: string
  nickname?: string
  externalId?: string
  created?: boolean
}

export function registerCurrentSnailUser() {
  return request.post<SnailOpenApiUser>({
    url: '/snail-ai/user/register'
  })
}
