import { useUserStore } from '@/store/modules/user'

/**
 * 构造携带鉴权信息的完整 URL，用于 <img> / <video> 等无法自定义请求头的原生标签。
 * 鉴权参数通过 query 传递（token + clientid），与消息推送 SSE 的鉴权方式保持一致。
 *
 * @param path 接口路径，例如 /resource/oss/preview/123
 * @returns 携带 Authorization 与 clientid 的完整 URL
 */
export function buildAuthUrl(path: string): string {
  const { accessToken } = useUserStore()
  const base = import.meta.env.VITE_API_URL
  const clientId = import.meta.env.VITE_APP_CLIENT_ID ?? ''
  return `${base}${path}?Authorization=Bearer ${accessToken}&clientid=${clientId}`
}
