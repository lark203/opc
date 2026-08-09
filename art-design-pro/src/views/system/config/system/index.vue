<template>
  <div class="config-page art-full-height">
    <div class="config-container">
      <div class="group-sidebar">
        <div class="group-header">
          <ArtSvgIcon icon="ri:settings-3-line" class="header-icon" />
          <span>系统配置</span>
        </div>
        <div class="group-list">
          <div
            v-for="item in menuList"
            :key="item.key"
            class="group-item"
            :class="{ active: activeKey === item.key }"
            @click="handleTabChange(item.key)"
          >
            <div class="group-content">
              <div class="group-icon">
                <ArtSvgIcon :icon="item.icon" />
              </div>
              <div class="group-info">
                <div class="group-title">{{ item.name }}</div>
                <div class="group-desc">{{ item.description }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="config-main">
        <div class="config-header">
          <div>
            <h3>{{ currentItem?.name }}</h3>
            <p class="config-desc">{{ currentItem?.description }}</p>
          </div>
        </div>
        <div class="config-content">
          <transition name="fade" mode="out-in">
            <component :is="currentItem?.component" :key="activeKey" />
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useAuth } from '@/hooks/core/useAuth'
  import SiteConfig from '../site/index.vue'
  import SecurityConfig from '../security/index.vue'
  import LoginConfig from '../login/index.vue'
  import MailConfig from '../mail/index.vue'
  import SmsConfig from '../sms/index.vue'
  import OssConfig from '../oss/index.vue'
  import LicenseConfig from '../license/index.vue'

  defineOptions({ name: 'SystemConfig' })

  interface ConfigTab {
    key: string
    name: string
    description: string
    icon: string
    permissions: string[]
    component: any
  }

  const { hasPermiOr } = useAuth()
  const route = useRoute()
  const router = useRouter()

  const data: ConfigTab[] = [
    {
      key: 'site',
      name: '网站配置',
      description: '配置网站基本信息、LOGO、SEO等',
      icon: 'ri:apps-2-line',
      permissions: ['system:option:list'],
      component: SiteConfig
    },
    {
      key: 'security',
      name: '安全配置',
      description: '配置密码策略、登录安全等',
      icon: 'ri:shield-check-line',
      permissions: ['system:option:list'],
      component: SecurityConfig
    },
    {
      key: 'login',
      name: '登录配置',
      description: '配置登录方式、验证码等',
      icon: 'ri:login-box-line',
      permissions: ['system:option:list'],
      component: LoginConfig
    },
    {
      key: 'mail',
      name: '邮件配置',
      description: '配置邮件服务器、发送参数等',
      icon: 'ri:mail-send-line',
      permissions: ['system:option:list'],
      component: MailConfig
    },
    {
      key: 'sms',
      name: '短信配置',
      description: '配置短信服务商、模板等',
      icon: 'ri:chat-smile-2-line',
      permissions: ['system:sms:list'],
      component: SmsConfig
    },
    {
      key: 'oss',
      name: '存储配置',
      description: '配置文件存储方式、OSS对象存储等',
      icon: 'ri:cloud-line',
      permissions: ['system:ossConfig:list'],
      component: OssConfig
    },
    {
      key: 'license',
      name: '授权管理',
      description: '查看授权状态、上传授权文件、获取机器指纹',
      icon: 'ri:shield-keyhole-line',
      permissions: ['system:license:list'],
      component: LicenseConfig
    }
  ]

  const menuList = computed(() => {
    return data.filter((item) => hasPermiOr(item.permissions))
  })

  const activeKey = ref('')

  const currentItem = computed(() => {
    return menuList.value.find((item) => item.key === activeKey.value)
  })

  watch(
    () => route.query,
    () => {
      const tab = route.query.tab as string
      if (tab && menuList.value.some((item) => item.key === tab)) {
        activeKey.value = tab
      } else if (menuList.value.length > 0) {
        activeKey.value = menuList.value[0].key
      }
    },
    { immediate: true }
  )

  watch(menuList, (list) => {
    if (list.length > 0 && (!activeKey.value || !list.some((i) => i.key === activeKey.value))) {
      activeKey.value = list[0].key
    }
  })

  function handleTabChange(key: string) {
    activeKey.value = key
    router.replace({ path: route.path, query: { ...route.query, tab: key } })
  }
</script>

<style scoped lang="scss">
  .config-page {
    padding: 0;
  }

  .config-container {
    display: flex;
    gap: 16px;
    height: 100%;
  }

  .group-sidebar {
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    width: 300px;
    height: 100%;
    overflow: hidden;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }

  .group-header {
    display: flex;
    gap: 8px;
    align-items: center;
    padding: 16px 20px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    border-bottom: 1px solid var(--el-border-color-lighter);

    .header-icon {
      font-size: 18px;
      color: var(--el-color-primary);
    }
  }

  .group-list {
    flex: 1;
    padding: 8px;
    overflow-y: auto;
  }

  .group-item {
    display: flex;
    align-items: center;
    padding: 12px;
    margin-bottom: 4px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s;

    &:hover {
      background: var(--el-fill-color-light);

      .group-icon {
        color: var(--el-color-primary);
        background: var(--el-color-primary-light-9);
      }
    }

    &.active {
      padding-left: 9px;
      background: var(--el-color-primary-light-9);
      border-left: 3px solid var(--el-color-primary);

      .group-title {
        font-weight: 600;
        color: var(--el-color-primary);
      }

      .group-icon {
        color: #fff;
        background: var(--el-color-primary);
      }
    }
  }

  .group-content {
    display: flex;
    flex: 1;
    align-items: center;
    min-width: 0;
  }

  .group-icon {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    margin-right: 12px;
    font-size: 18px;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    border-radius: 8px;
    transition: all 0.2s;
  }

  .group-info {
    flex: 1;
    min-width: 0;
  }

  .group-title {
    margin-bottom: 2px;
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    transition: all 0.2s;
  }

  .group-desc {
    overflow: hidden;
    font-size: 12px;
    line-height: 1.4;
    color: var(--el-text-color-secondary);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .config-main {
    display: flex;
    flex: 1;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
  }

  .config-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    h3 {
      margin: 0 0 4px;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .config-desc {
    margin: 0;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .config-content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    :deep(.art-full-height) {
      height: 100%;
    }
  }

  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.2s ease;
  }

  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }

  @media (width <= 768px) {
    .config-container {
      flex-direction: column;
    }

    .group-sidebar {
      width: 100%;
      height: auto;
      max-height: 200px;
    }

    .config-main {
      flex: 1;
    }
  }
</style>
