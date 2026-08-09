import request from '@/utils/http'

export interface LicenseState {
  /** 授权是否有效 */
  valid: boolean
  /** 当前机器指纹 */
  fingerprint?: string
  /** 授权签发时间（yyyy-MM-dd HH:mm:ss） */
  issuedAt?: string
  /** 授权过期时间（yyyy-MM-dd HH:mm:ss） */
  expireAt?: string
  /** 授权版本 */
  version?: string
  /** 授权类型 */
  type?: string
  /** 状态说明信息 */
  message?: string
  /** 最近一次校验时间（ISO-8601 字符串或时间戳） */
  lastChecked?: string | number
}

/** 获取当前授权状态 */
export function getLicenseInfo() {
  return request.get<LicenseState>({ url: '/license/info' })
}

/** 获取当前机器指纹（用于生成授权码） */
export function getLicenseFingerprint() {
  return request.get<string>({ url: '/license/fingerprint' })
}

/** 上传授权文件（.lic），成功后返回最新授权状态 */
export function uploadLicense(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<LicenseState>({ url: '/license/upload', data: formData })
}
