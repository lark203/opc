<!-- 按钮权限示例页面 -->
<template>
  <div class="w-full py-2">
    <!-- 页面头部 -->
    <div class="mb-6">
      <h2 class="m-0 mb-2 text-xl font-medium">{{ $t('menus.examples.permission.buttonAuth') }}</h2>
      <p class="m-0 text-sm leading-[1.6] text-g-700">
        此页面演示按钮级别的权限控制。权限模型与 RuoYi-Vue-Plus / plus-ui 一致：按钮权限基于
        <code class="px-1.5 py-0.5 font-mono text-xs text-theme bg-theme/10 rounded">getInfo</code>
        返回的权限码集合（如 <code class="font-mono">system:user:add</code>），超级管理员拥有通配符
        <code class="font-mono">*:*:*</code> 可绕过所有按钮权限校验。
      </p>
    </div>

    <div class="mb-6">
      <ElCard class="art-card-xs">
        <template #header>
          <div class="flex-cb font-semibold">
            <span class="flex-1">当前用户权限信息</span>
          </div>
        </template>
        <div>
          <div class="flex items-start mb-4 last:mb-0">
            <span class="min-w-20 font-semibold">用户角色：</span>
            <ElTag :type="getRoleTagType(currentUserRole)">
              {{ getRoleDisplayName(currentUserRole) }}
            </ElTag>
          </div>
          <div class="flex items-start mb-4 last:mb-0">
            <span class="min-w-20 font-semibold">权限码：</span>
            <div class="flex flex-wrap gap-2">
              <ElTag
                v-for="permission in currentUserPermissions"
                :key="permission"
                size="small"
                type="info"
                class="m-0"
              >
                {{ permission }}
              </ElTag>
              <span v-if="!currentUserPermissions.length" class="italic text-red-500"
                >无权限码</span
              >
            </div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- 基于角色的权限控制演示 -->
    <div class="mb-6 last:mb-0">
      <ElCard class="art-card-xs">
        <template #header>
          <div class="flex-cb font-semibold">
            <span class="flex-1">基于角色的权限控制（v-roles 指令）</span>
          </div>
        </template>
        <div>
          <p class="p-3 m-0 mb-5 text-sm leading-[1.6] text-g-700 bg-g-200 rounded">
            使用
            <code class="px-1.5 py-0.5 font-mono text-xs text-theme bg-theme/10 rounded"
              >v-roles</code
            >
            指令控制按钮显示，只要用户拥有指定角色中的任意一个（或超级角色
            <code class="font-mono">admin</code>/<code class="font-mono">superadmin</code
            >），元素就会显示。
          </p>

          <div class="grid grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-5">
            <div class="flex flex-col gap-2">
              <ElButton type="primary" plain v-roles="'R_SUPER'"> 超级管理员可见 </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-roles="'R_SUPER'"</code
                >
                <span class="text-g-700">只有超级管理员可见</span>
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton type="warning" plain v-roles="['R_SUPER', 'R_ADMIN']">
                管理员可见
              </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-roles="['R_SUPER', 'R_ADMIN']"</code
                >
                <span class="text-g-700">超级管理员和管理员可见</span>
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton type="success" plain v-roles="['R_SUPER', 'R_ADMIN', 'R_USER']">
                所有用户可见
              </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-roles="['R_SUPER', 'R_ADMIN', 'R_USER']"</code
                >
                <span class="text-g-700">所有已登录用户可见</span>
              </div>
            </div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- 基于权限码的按钮控制演示 -->
    <div class="mb-6 last:mb-0">
      <ElCard class="art-card-xs">
        <template #header>
          <div class="flex-cb font-semibold">
            <span class="flex-1">基于权限码的按钮控制（v-auth 指令）</span>
          </div>
        </template>
        <div>
          <p class="p-3 m-0 mb-5 text-sm leading-[1.6] text-g-700 bg-g-200 rounded">
            <code class="px-1.5 py-0.5 font-mono text-xs text-theme bg-theme/10 rounded"
              >v-auth</code
            >
            指令根据当前用户的权限码集合控制按钮显示。无权限时元素会从 DOM
            中移除。支持字符串或数组写法。
          </p>

          <div class="grid grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-5">
            <div class="flex flex-col gap-2">
              <ElButton type="primary" plain v-auth="'system:user:add'"> 新增 </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-auth="'system:user:add'"</code
                >
                <span class="text-g-700">拥有 system:user:add 权限时显示</span>
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton type="warning" plain v-auth="'system:user:edit'"> 编辑 </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-auth="'system:user:edit'"</code
                >
                <span class="text-g-700">拥有 system:user:edit 权限时显示</span>
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton type="danger" plain v-auth="'system:user:remove'"> 删除 </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-auth="'system:user:remove'"</code
                >
                <span class="text-g-700">拥有 system:user:remove 权限时显示</span>
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton type="info" plain v-auth="['system:user:export', 'system:user:import']">
                导出 / 导入
              </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-auth="['system:user:export', 'system:user:import']"</code
                >
                <span class="text-g-700">满足任意一个权限码即显示</span>
              </div>
            </div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- 编程式权限控制演示 -->
    <div class="mb-6 last:mb-0">
      <ElCard class="art-card-xs">
        <template #header>
          <div class="flex-cb font-semibold">
            <span class="flex-1">编程式权限控制（useAuth）</span>
          </div>
        </template>
        <div>
          <p class="p-3 m-0 mb-5 text-sm leading-[1.6] text-g-700 bg-g-200 rounded">
            <code class="px-1.5 py-0.5 font-mono text-xs text-theme bg-theme/10 rounded"
              >useAuth()</code
            >
            提供 <code class="font-mono">hasPermi</code>、<code class="font-mono">hasPermiOr</code
            >、<code class="font-mono">hasPermiAnd</code>、<code class="font-mono">hasRole</code>
            等方法，用于模板 <code class="font-mono">v-if</code> 或逻辑判断。
          </p>

          <div class="grid grid-cols-[repeat(auto-fit,minmax(280px,1fr))] gap-5">
            <div class="flex flex-col gap-2">
              <ElButton v-if="hasPermi('system:user:add')" type="primary"> 新增用户 </ElButton>
              <ElButton v-else type="info" disabled> 无新增权限 </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >v-if="hasPermi('system:user:add')"</code
                >
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton
                :disabled="!hasPermiOr(['system:user:edit', 'system:user:remove'])"
                :type="hasPermiOr(['system:user:edit', 'system:user:remove']) ? 'warning' : 'info'"
              >
                {{
                  hasPermiOr(['system:user:edit', 'system:user:remove'])
                    ? '编辑/删除'
                    : '无操作权限'
                }}
              </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >hasPermiOr([...])</code
                >
                <span class="text-g-700">满足任意一个权限码</span>
              </div>
            </div>

            <div class="flex flex-col gap-2">
              <ElButton
                :disabled="!hasPermiAnd(['system:user:add', 'system:user:edit'])"
                :type="hasPermiAnd(['system:user:add', 'system:user:edit']) ? 'success' : 'info'"
              >
                {{
                  hasPermiAnd(['system:user:add', 'system:user:edit'])
                    ? '新增并编辑'
                    : '需同时拥有两个权限'
                }}
              </ElButton>
              <div class="text-xs">
                <code class="block p-1 px-2 mb-1 font-mono bg-g-200 border-full-d rounded"
                  >hasPermiAnd([...])</code
                >
                <span class="text-g-700">需同时满足所有权限码</span>
              </div>
            </div>
          </div>
        </div>
      </ElCard>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'PermissionButtonAuth' })

  const { hasPermi, hasPermiOr, hasPermiAnd } = useAuth()
  const userStore = useUserStore()

  // 当前用户角色
  const currentUserRole = computed(() => {
    return userStore.info?.roles?.[0] || ''
  })

  // 当前用户权限码
  const currentUserPermissions = computed(() => {
    return userStore.info?.permissions || []
  })

  // 获取角色标签类型
  const getRoleTagType = (role: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' => {
    const roleMap: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
      R_SUPER: 'warning',
      R_ADMIN: 'primary',
      R_USER: 'success',
      admin: 'danger',
      superadmin: 'danger'
    }
    return roleMap[role] || 'info'
  }

  // 获取角色显示名称
  const getRoleDisplayName = (role: string) => {
    const roleMap: Record<string, string> = {
      R_SUPER: '超级管理员',
      R_ADMIN: '管理员',
      R_USER: '普通用户',
      admin: '管理员',
      superadmin: '超级管理员'
    }
    return roleMap[role] || '未知角色'
  }
</script>
