import request from '@/utils/http'

export interface ProfileUserVo {
  userId: number
  deptId: number
  userName: string
  nickName: string
  userType: string
  email: string
  phoneNumber: string
  gender: string
  avatar: string
  loginIp: string
  loginDate: string
  deptName: string
  address: string
  signature: string
  tags: string
}

export interface ProfileVo {
  user: ProfileUserVo
  roleGroup: string
  postGroup: string
}

export interface UpdateProfileReq {
  nickName: string
  email: string
  phoneNumber: string
  gender: string
  address: string
  signature: string
  tags: string
}

export interface UpdateAvatarReq {
  avatar: string
}

export interface UpdatePwdReq {
  oldPassword: string
  newPassword: string
}

export const profileApi = {
  getProfile: () => {
    return request.get<ProfileVo>({ url: '/system/user/profile' })
  },

  updateProfile: (data: UpdateProfileReq) => {
    return request.put({ url: '/system/user/profile', data })
  },

  updateAvatar: (data: UpdateAvatarReq) => {
    return request.put({ url: '/system/user/profile/avatar', data })
  },

  updatePwd: (data: UpdatePwdReq) => {
    return request.put({ url: '/system/user/profile/updatePwd', data, isEncrypt: true })
  }
}
