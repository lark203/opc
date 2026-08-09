<!-- 布局内容 -->
<template>
  <div class="layout-content" :class="{ 'overflow-auto': isFullPage }" :style="containerStyle">
    <div id="app-content-header">
      <!-- 节日滚动 -->
      <ArtFestivalTextScroll v-if="!isFullPage" />

      <!-- 路由信息调试 -->
      <div
        v-if="isOpenRouteInfo === 'true'"
        class="px-2 py-1.5 mb-3 text-sm text-g-500 bg-g-200 border-full-d rounded-md"
      >
        router meta：{{ route.meta }}
      </div>
    </div>

    <RouterView v-if="isRefresh" v-slot="{ Component, route }" :style="contentStyle">
      <!-- 缓存路由动画 -->
      <Transition :name="showTransitionMask ? '' : actualTransition" mode="out-in" appear>
        <KeepAlive :max="10" :exclude="keepAliveExclude">
          <component
            class="art-page-view"
            :is="Component"
            :key="getViewKey(route)"
            v-if="route.meta.keepAlive"
          />
        </KeepAlive>
      </Transition>

      <!-- 非缓存路由动画 -->
      <Transition :name="showTransitionMask ? '' : actualTransition" mode="out-in" appear>
        <component
          class="art-page-view"
          :is="Component"
          :key="getViewKey(route)"
          v-if="!route.meta.keepAlive"
        />
      </Transition>
    </RouterView>

    <!-- 全屏页面切换过渡遮罩（用于提升页面切换视觉体验） -->
    <Teleport to="body">
      <div
        v-show="showTransitionMask"
        class="fixed top-0 left-0 z-[2000] w-screen h-screen pointer-events-none bg-box"
      />
    </Teleport>
  </div>
</template>
<script setup lang="ts">
  import type { CSSProperties } from 'vue'
  import type { RouteLocationNormalizedLoaded } from 'vue-router'
  import { useRoute } from 'vue-router'
  import { useAutoLayoutHeight } from '@/hooks/core/useLayoutHeight'
  import { useSettingStore } from '@/store/modules/setting'
  import { useWorktabStore } from '@/store/modules/worktab'

  defineOptions({ name: 'ArtPageContent' })

  const route = useRoute()
  const { containerMinHeight } = useAutoLayoutHeight()
  const { pageTransition, containerWidth, refresh } = storeToRefs(useSettingStore())
  const { keepAliveExclude } = storeToRefs(useWorktabStore())

  const isRefresh = shallowRef(true)
  const isOpenRouteInfo = import.meta.env.VITE_OPEN_ROUTE_INFO
  const showTransitionMask = ref(false)

  // 标记是否是首次加载（浏览器刷新）
  const isFirstLoad = ref(true)

  // 检查当前路由是否需要使用无基础布局模式
  const isFullPage = computed(() => route.matched.some((r) => r.meta?.isFullPage))
  const prevIsFullPage = ref(isFullPage.value)

  // 切换动画名称：首次加载、从全屏返回时不使用动画
  const actualTransition = computed(() => {
    if (isFirstLoad.value) return ''
    if (prevIsFullPage.value && !isFullPage.value) return ''
    return pageTransition.value
  })

  // 监听全屏状态变化，显示过渡遮罩
  watch(isFullPage, (val, oldVal) => {
    if (val !== oldVal) {
      showTransitionMask.value = true
      // 延迟隐藏遮罩，给足时间让页面完成切换
      setTimeout(() => {
        showTransitionMask.value = false
      }, 50)
    }

    nextTick(() => {
      prevIsFullPage.value = val
    })
  })

  const containerStyle = computed((): CSSProperties =>
    isFullPage.value
      ? {
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100vh',
          zIndex: 2500,
          background: 'var(--default-bg-color)'
        }
      : {
          maxWidth: containerWidth.value
        }
  )

  /**
   * 计算当前 RouterView 实际渲染的路由层级 key。
   *
   * 后端模式下的三级（及以上）菜单会生成「目录容器层」：例如
   *   /system(Layout) > /system/monitor(ParentView) > /system/monitor/operlog(叶子)
   * 此时本 RouterView 渲染的 Component 是 ParentView，而非叶子页面。
   * 若继续用最深的叶子 path 作为 key（即原来的 route.path），
   * 那么同一目录下的兄弟菜单（操作日志 / 登录日志）切换时，key 会不断变化，
   * 导致 ParentView 在 KeepAlive + Transition(mode="out-in") 中被反复卸载/重建，
   * 引发渲染异常（表现就是「三级切三级报错，二级切三级正常」）。
   *
   * 这里取当前 RouterView 深度（Layout 之后的第一级，即 matched[1]）对应的 path 作为 key，
   * 使目录容器在兄弟菜单间保持稳定：切换兄弟叶子时只替换容器内部的 router-view，不再重建容器本身。
   * 对二级（叶子直接渲染）菜单，matched[1] 就是叶子自身，行为与原来一致。
   */
  const getViewKey = (r: RouteLocationNormalizedLoaded): string => {
    return r.matched[1]?.path || r.path
  }

  const contentStyle = computed((): CSSProperties => ({
    minHeight: containerMinHeight.value
  }))

  const reload = () => {
    isRefresh.value = false
    nextTick(() => {
      isRefresh.value = true
    })
  }

  watch(refresh, reload, { flush: 'post' })

  // 组件挂载后标记首次加载完成
  onMounted(() => {
    // 延迟一帧，确保首次渲染完成
    nextTick(() => {
      isFirstLoad.value = false
    })
  })
</script>
