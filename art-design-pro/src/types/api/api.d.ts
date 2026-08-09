declare namespace Api {
  namespace Common {
    interface PaginationParams {
      pageNum?: number
      pageSize?: number
      total: number
      current: number
      size: number
    }

    type CommonSearchParams = Pick<PaginationParams, 'pageNum' | 'pageSize'>

    interface PaginatedResponse<T = any> {
      rows: T[]
      pageNum: number
      pageSize: number
      total: number
      current?: number
      size?: number
    }

    type EnableStatus = '0' | '1'
  }

  namespace Auth {
    interface LoginParams {
      username: string
      password: string
      code?: string
      uuid?: string
    }

    interface LoginResponse {
      accessToken: string
      refreshToken: string
      expireIn: number
    }

    interface UserVO {
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

    interface UserInfo {
      user: UserVO
      roles: string[]
      permissions: string[]
      buttons?: string[]
    }
  }

  namespace Dict {
    interface DictDataOption {
      label: string
      value: string
      elTagType?: string
      elTagClass?: string
    }

    interface DictTypeVO {
      dictId: number | string
      dictName: string
      dictType: string
      remark: string
      createBy?: string
      createTime?: string
      updateBy?: string
      updateTime?: string
    }

    interface DictTypeForm {
      dictId?: number | string
      dictName: string
      dictType: string
      remark?: string
    }

    interface DictTypeQuery {
      dictName?: string
      dictType?: string
      pageNum?: number
      pageSize?: number
    }

    interface DictDataVO {
      dictCode: string | number
      dictType: string
      dictLabel: string
      dictValue: string
      cssClass: string
      listClass: string
      dictSort: number
      remark: string
      createBy?: string
      createTime?: string
      updateBy?: string
      updateTime?: string
    }

    interface DictDataForm {
      dictCode?: string | number
      dictType?: string
      dictLabel: string
      dictValue: string
      cssClass?: string
      listClass?: string
      dictSort?: number
      remark?: string
    }

    interface DictDataQuery {
      dictName?: string
      dictType?: string
      dictLabel?: string
      pageNum?: number
      pageSize?: number
    }
  }

  namespace SystemManage {
    type UserList = Api.Common.PaginatedResponse<UserListItem>

    interface UserListItem {
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
      delFlag: string
      loginIp: string
      loginDate: string
      remark: string
      deptName: string
      admin: boolean
      userEmail?: string
      userGender?: string
      userPhone?: string
      userRoles?: string[]
    }

    type UserSearchParams = Partial<
      Pick<UserListItem, 'userName' | 'nickName' | 'phoneNumber' | 'status' | 'deptId' | 'email'> &
        Api.Common.CommonSearchParams
    > & { current?: number; userGender?: string; pageNum?: number; pageSize?: number }

    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    interface RoleListItem {
      roleId: number
      roleName: string
      roleKey: string
      roleSort: number
      status: string
      delFlag: string
      dataScope: string
      menuCheckStrictly: string
      deptCheckStrictly: string
      createBy: string
      createTime: string
      updateBy: string
      updateTime: string
      remark: string
      roleCode?: string
      description?: string
      enabled?: string
    }

    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleName' | 'roleKey' | 'status' | 'description'> &
        Api.Common.CommonSearchParams
    > & {
      roleCode?: string
      startTime?: string
      endTime?: string
      current?: number
      pageNum?: number
      pageSize?: number
    }

    interface MenuListItem {
      menuId: number
      menuName: string
      parentId: number
      orderNum: number
      path: string
      component: string
      query: string
      isFrame: string
      isCache: string
      menuType: string
      visible: string
      status: string
      perms: string
      icon: string
      createBy: string
      createTime: string
      updateBy: string
      updateTime: string
      remark: string
    }
  }
}
