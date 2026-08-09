<!-- 表格按钮 -->
<!-- 支持：新增、编辑、删除、查看、更多、密码、权限、解锁、详情、导出、作废等操作按钮 -->
<!-- 扩展功能：鼠标悬停提示，支持自定义提示文字和默认提示文字 -->
<template>
  <ElTooltip
    v-if="visible"
    :content="tooltipContent"
    :disabled="!tooltipContent"
    placement="top"
    :show-after="300"
    :hide-after="0"
  >
    <div
      :class="[
        'inline-flex items-center justify-center min-w-8 h-8 px-2.5 mr-2.5 text-sm c-p rounded-md align-middle',
        buttonClass,
        { 'pointer-events-none opacity-50': props.disabled }
      ]"
      :style="{ backgroundColor: buttonBgColor, color: iconColor }"
      @click="handleClick"
    >
      <ArtSvgIcon :icon="iconContent" />
    </div>
  </ElTooltip>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElTooltip } from 'element-plus'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'ArtButtonTable' })

  interface Props {
    /** 按钮类型 */
    type?:
      | 'add'
      | 'edit'
      | 'delete'
      | 'more'
      | 'view'
      | 'password'
      | 'permission'
      | 'unlock'
      | 'detail'
      | 'export'
      | 'user'
      | 'lock'
      | 'menu'
      | 'search'
      | 'download'
      | 'invalid'
    /** 按钮图标 */
    icon?: string
    /** 按钮样式类 */
    iconClass?: string
    /** icon 颜色 */
    iconColor?: string
    /** 按钮背景色 */
    buttonBgColor?: string
    /** 鼠标悬停提示文字（不传则使用按钮类型对应的默认提示） */
    title?: string
    /** 禁用 */
    disabled?: boolean
    /** 权限码，传入后无该权限则不渲染（格式如 system:user:edit） */
    auth?: string
  }

  const props = withDefaults(defineProps<Props>(), {})

  const emit = defineEmits<{
    (e: 'click'): void
  }>()

  const { hasAuth } = useAuth()

  // 无权限时不渲染按钮
  const visible = computed(() => !props.auth || hasAuth(props.auth))

  // 默认按钮配置：图标、样式、提示文字
  const defaultButtons = {
    add: { icon: 'ri:add-fill', class: 'bg-theme/12 text-theme', title: '新增' },
    edit: { icon: 'ri:pencil-line', class: 'bg-secondary/12 text-secondary', title: '编辑' },
    delete: { icon: 'ri:delete-bin-5-line', class: 'bg-error/12 text-error', title: '删除' },
    view: { icon: 'ri:eye-line', class: 'bg-info/12 text-info', title: '查看' },
    more: { icon: 'ri:more-2-fill', class: '', title: '更多' },
    password: { icon: 'ri:key-2-line', class: 'bg-warning/12 text-warning', title: '重置密码' },
    permission: {
      icon: 'ri:shield-keyhole-line',
      class: 'bg-success/12 text-success',
      title: '分配权限'
    },
    unlock: { icon: 'ri:lock-unlock-line', class: 'bg-success/12 text-success', title: '解锁' },
    detail: { icon: 'ri:file-info-line', class: 'bg-info/12 text-info', title: '详情' },
    export: { icon: 'ri:download-line', class: 'bg-secondary/12 text-secondary', title: '导出' },
    user: { icon: 'ri:user-3-line', class: 'bg-primary/12 text-primary', title: '分配用户' },
    lock: { icon: 'ri:lock-line', class: 'bg-error/12 text-error', title: '锁定' },
    menu: { icon: 'ri:menu-line', class: 'bg-danger/12 text-danger', title: '分配菜单' },
    search: { icon: 'ri:search-line', class: 'bg-info/12 text-info', title: '搜索' },
    download: { icon: 'ri:file-download-line', class: 'bg-success/12 text-success', title: '下载' },
    invalid: { icon: 'ri:close-circle-line', class: 'bg-error/12 text-error', title: '作废' }
  } as const

  // 获取图标内容
  const iconContent = computed(() => {
    return props.icon || (props.type ? defaultButtons[props.type]?.icon : '') || ''
  })

  // 获取按钮样式类
  const buttonClass = computed(() => {
    return props.iconClass || (props.type ? defaultButtons[props.type]?.class : '') || ''
  })

  // 获取提示文字：优先使用自定义 title，否则使用按钮类型对应的默认提示
  const tooltipContent = computed(() => {
    if (props.title !== undefined) return props.title
    return props.type ? defaultButtons[props.type]?.title || '' : ''
  })

  const handleClick = () => {
    emit('click')
  }
</script>
