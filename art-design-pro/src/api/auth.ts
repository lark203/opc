import request from '@/utils/http'

const clientId = import.meta.env.VITE_APP_CLIENT_ID

export interface LoginData {
  username: string
  password: string
  code?: string
  uuid?: string
  clientId?: string
  grantType?: string
}

export interface LoginResult {
  access_token: string
  refresh_token: string
  expire_in: number
  /** 密码是否已过期（需强制改密） */
  password_expired?: boolean
  /** 密码是否即将过期（提醒） */
  password_expiring_soon?: boolean
  /** 密码剩余有效天数 */
  password_expire_in_days?: number | null
}

export interface UserInfo {
  user: UserVO
  roles: string[]
  permissions: string[]
}

export interface UserVO {
  userId: string | number
  tenantId: string
  deptId: number
  userName: string
  nickName: string
  userType: string
  email: string
  phoneNumber: string
  gender: string
  avatar: string
  status: string
  loginIp: string
  loginDate: string
  remark: string
  deptName: string
}

export function login(data: LoginData) {
  const params = {
    ...data,
    username: data.username,
    password: data.password,
    clientId: data.clientId || clientId,
    grantType: data.grantType || 'password'
  }
  return request.post<LoginResult>({
    url: '/auth/login',
    params,
    isToken: false,
    isEncrypt: true
  })
}

export function logout() {
  return request.post({
    url: '/auth/logout'
  })
}

export function getCodeImg() {
  return request.get({
    url: '/auth/code',
    isToken: false
  })
}

export function getInfo(): Promise<UserInfo> {
  return request.get({
    url: '/system/user/getInfo'
  })
}

export interface RegisterData {
  username: string
  password: string
  confirmPassword?: string
  code?: string
  uuid?: string
  clientId?: string
  grantType?: string
  userType?: string
}

export function register(data: RegisterData) {
  const params = {
    ...data,
    clientId: data.clientId || clientId,
    grantType: data.grantType || 'password'
  }
  return request.post({
    url: '/auth/register',
    params,
    isToken: false,
    isEncrypt: true
  })
}

/** 发送重置密码验证码（可指定通道，返回发送通道与脱敏目的地） */
export function sendResetCode(data: { username: string; channel?: 'email' | 'sms' }) {
  return request.post({
    url: '/auth/reset-password/send-code',
    params: data,
    isToken: false
  })
}

/** 查询用户可用的重置密码验证通道（邮箱 / 短信） */
export function getResetChannels(data: { username: string }) {
  return request.post<Array<{ channel: 'email' | 'sms'; destination: string }>>({
    url: '/auth/reset-password/channels',
    params: data,
    isToken: false
  })
}

/** 校验验证码并重置密码 */
export function resetPassword(data: { username: string; code: string; newPassword: string }) {
  return request.post({
    url: '/auth/reset-password',
    params: data,
    isToken: false,
    isEncrypt: true
  })
}

export const fetchLogin = login
export const fetchGetUserInfo = getInfo
