<template>
  <ElDrawer
    v-model="dialogVisible"
    title="用户信息详情"
    size="40%"
    append-to-body
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <div class="drawer-content">
      <ElCard shadow="never" class="detail-card">
        <template #header>
          <div class="card-header">
            <div class="avatar-wrapper">
              <ElAvatar
                :size="64"
                :src="avatarUrl || undefined"
                :style="{ backgroundColor: avatarBgColor }"
              >
                {{ (userInfo.nickName || userInfo.userName || '用户')[0] }}
              </ElAvatar>
            </div>
            <div class="user-basic">
              <h3>{{ userInfo.nickName || userInfo.userName }}</h3>
              <p class="user-account">账号：{{ userInfo.userName }}</p>
            </div>
          </div>
        </template>
        <ElDescriptions :column="2" border class="detail-descriptions">
          <ElDescriptionsItem label="归属部门">
            {{ userInfo.deptName || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="手机号码">
            {{ userInfo.phoneNumber || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="邮箱">
            {{ userInfo.email || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="用户状态">
            <DictTag :options="sys_normal_disable" :value="userInfo.status" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="用户性别">
            <DictTag :options="sys_user_gender" :value="userInfo.gender" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="地址">
            {{ userInfo.address || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="标签">
            <ElSpace v-if="tagList.length" wrap>
              <ElTag
                v-for="(tag, index) in tagList"
                :key="index"
                :type="tagTypes[index % tagTypes.length]"
                size="small"
              >
                {{ tag }}
              </ElTag>
            </ElSpace>
            <span v-else>-</span>
          </ElDescriptionsItem>
          <ElDescriptionsItem label="岗位">
            {{ postNames || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="角色" :span="2">
            {{ roleNames || '-' }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="detail-card mt-4">
        <template #header>
          <span class="card-title">登录信息</span>
        </template>
        <ElDescriptions :column="2" border class="detail-descriptions">
          <ElDescriptionsItem label="最后登录IP">
            {{ userInfo.loginIp || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="最后登录时间">
            {{ userInfo.loginDate || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="个性签名" :span="2">
            {{ userInfo.signature || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">
            {{ userInfo.remark || '-' }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>
    </div>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, toRefs, watch } from 'vue'
  import { ElAvatar, ElSpace } from 'element-plus'
  import DictTag from '@/components/core/forms/dict-tag/index.vue'
  import { useDrawer } from '@/hooks/core/useDrawer'
  import { getUser, type UserInfoVO, type UserVO } from '@/api/system/user'
  import { useDict } from '@/utils/dict'
  import { buildAuthUrl } from '@/utils/auth-url'

  interface Props {
    userId?: string | number
  }

  const props = defineProps<Props>()

  const { visible: dialogVisible, data, open, close } = useDrawer<UserVO>()

  const loading = ref(false)
  const userInfo = reactive<Partial<UserVO>>({})
  const postOptions = ref<any[]>([])
  const roleOptions = ref<any[]>([])
  const postIds = ref<string[]>([])
  const roleIds = ref<string[]>([])

  const { sys_user_gender, sys_normal_disable } = toRefs(
    useDict('sys_user_gender', 'sys_normal_disable')
  )

  const tagTypes = ['primary', 'success', 'warning', 'danger', 'info'] as const

  // 头像地址：存在头像（OSS ossId 或 URL）时解析为可访问的预览地址
  const avatarUrl = computed(() => {
    const avatar = userInfo.avatar
    if (!avatar || avatar === '' || avatar === 'null' || avatar === 'undefined') {
      return ''
    }
    const avatarStr = String(avatar)
    if (avatarStr.startsWith('http://') || avatarStr.startsWith('https://')) {
      return avatarStr
    }
    if (/^\d+$/.test(avatarStr)) {
      return buildAuthUrl('/resource/oss/preview/' + avatarStr)
    }
    return ''
  })

  // 无头像时的占位背景色：基于用户名生成稳定且各异的颜色（每次渲染对同一用户保持一致）
  const avatarBgColor = computed(() => {
    const name = userInfo.nickName || userInfo.userName || '用户'
    let hash = 0
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash)
    }
    const hue = Math.abs(hash) % 360
    return `hsl(${hue}, 60%, 55%)`
  })

  const tagList = computed(() => {
    if (!userInfo.tags) return []
    return userInfo.tags
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag)
  })

  const postNames = computed(() => {
    if (!postOptions.value.length || !postIds.value.length) return ''
    return postOptions.value
      .filter((p) => postIds.value.includes(String(p.postId)))
      .map((p) => p.postName)
      .join('、')
  })

  const roleNames = computed(() => {
    if (!roleOptions.value.length || !roleIds.value.length) return ''
    return roleOptions.value
      .filter((r) => roleIds.value.includes(String(r.roleId)))
      .map((r) => r.roleName)
      .join('、')
  })

  const loadUserData = async (userId?: string | number) => {
    if (!userId) return
    loading.value = true
    try {
      const res: UserInfoVO = await getUser(userId)
      Object.assign(userInfo, res.user)
      postOptions.value = res.posts || []
      roleOptions.value = res.roles || []
      postIds.value = (res.postIds || []).map(String)
      roleIds.value = (res.roleIds || []).map(String)
    } catch (error) {
      console.error('获取用户信息失败:', error)
    } finally {
      loading.value = false
    }
  }

  const handleClosed = () => {
    close()
    Object.keys(userInfo).forEach((key) => {
      userInfo[key as keyof UserVO] = undefined as any
    })
    postOptions.value = []
    roleOptions.value = []
    postIds.value = []
    roleIds.value = []
  }

  watch(
    () => [dialogVisible.value, data.value],
    async ([visible, item]) => {
      if (visible && item && typeof item === 'object' && 'userId' in item) {
        await loadUserData(item.userId)
      }
    }
  )

  watch(
    () => props.userId,
    async (userId) => {
      if (userId) {
        await loadUserData(userId)
        open()
      }
    }
  )

  defineExpose({
    open,
    close
  })
</script>

<style lang="scss" scoped>
  .drawer-content {
    padding: 0;
  }

  .detail-card {
    border-radius: 8px;
  }

  .card-header {
    display: flex;
    gap: 16px;
    align-items: center;
    padding-bottom: 12px;
    margin-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color-light);

    .avatar-wrapper {
      flex-shrink: 0;
    }

    .user-basic {
      flex: 1;

      h3 {
        margin: 0 0 4px;
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .user-account {
        margin: 0;
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .card-title {
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
  }

  .detail-descriptions {
    :deep(.el-descriptions__label) {
      min-width: 100px;
      font-weight: 500;
      color: var(--el-text-color-secondary);
    }

    :deep(.el-descriptions__content) {
      color: var(--el-text-color-primary);
    }
  }
</style>
