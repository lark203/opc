<template>
  <div class="relative" :style="{ width: width }">
    <ElInput v-model="iconValue" readonly placeholder="点击选择图标" @click="visible = !visible">
      <template #prepend>
        <ArtSvgIcon v-if="iconValue" :icon="iconValue" />
      </template>
    </ElInput>

    <ElPopover :visible="visible" placement="bottom-end" trigger="click" :width="450">
      <template #reference>
        <div
          class="cursor-pointer text-[#999] absolute right-[10px] top-0 h-[32px] leading-[32px]"
          @click="visible = !visible"
        >
          <ElIcon><CaretTop v-show="visible" /></ElIcon>
          <ElIcon><CaretBottom v-show="!visible" /></ElIcon>
        </div>
      </template>

      <ElInput
        v-model="filterValue"
        class="mb-2"
        placeholder="搜索图标"
        clearable
        @input="filterIcons"
        @clear="filterIcons"
      />

      <div class="iconify-panel">
        <div class="iconify-heading">也可以直接输入 Iconify 图标名</div>
        <div class="iconify-form">
          <ElInput
            v-model="customIcon"
            placeholder="例如：ri:user-3-line"
            clearable
            @keyup.enter="applyCustomIcon"
          />
          <ElButton type="primary" plain @click="applyCustomIcon">使用</ElButton>
        </div>
      </div>

      <div ref="listContainerRef" class="icon-scroll" @scroll="onScroll">
        <ul class="icon-list">
          <ElTooltip
            v-for="iconName in iconNames"
            :key="iconName"
            :content="iconName"
            placement="bottom"
            effect="light"
            :teleported="false"
            :enterable="false"
          >
            <li
              :class="['icon-item', { active: iconValue == iconName }]"
              @click="selectedIcon(iconName)"
            >
              <ArtSvgIcon color="var(--el-text-color-regular)" :icon="iconName" />
            </li>
          </ElTooltip>
        </ul>
        <div class="icon-footer">
          <template v-if="loadingMore">加载中…</template>
          <template v-else-if="hasMore">
            滚动到底部加载更多（{{ iconNames.length }}/{{ filteredIcons.length }}）
          </template>
          <template v-else>已加载全部 {{ filteredIcons.length }} 个图标</template>
        </div>
      </div>
    </ElPopover>
  </div>
</template>

<script setup lang="ts">
  import { computed, nextTick, onMounted, ref, watch } from 'vue'
  import { ElButton, ElIcon, ElInput, ElPopover, ElTooltip } from 'element-plus'
  import { CaretBottom, CaretTop } from '@element-plus/icons-vue'
  import { addCollection, listIcons } from '@iconify/vue'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
  import ri from '@iconify/json/json/ri.json'

  addCollection(ri)

  const props = defineProps<{
    modelValue?: string
    width?: string
  }>()

  const emit = defineEmits(['update:modelValue'])
  const visible = ref(false)
  const width = computed(() => props.width || '400px')
  const iconValue = computed({
    get: () => props.modelValue || '',
    set: (val: string) => emit('update:modelValue', val)
  })

  // 一次性加载全部 ri（线型）图标到内存，但仅分批渲染，避免一次性创建上千个 SVG 节点造成卡顿
  const allIcons = ref<string[]>([])
  // 每次渲染的批量大小
  const PAGE_SIZE = 120
  // 当前已渲染的数量
  const displayCount = ref(PAGE_SIZE)
  const listContainerRef = ref<HTMLElement | null>(null)
  const loadingMore = ref(false)

  const filterValue = ref('')
  const customIcon = ref('')

  // 全局搜索：始终基于完整的 allIcons 过滤（不局限于已渲染部分）
  const filteredIcons = computed(() => {
    const kw = filterValue.value.trim().toLowerCase()
    if (!kw) return allIcons.value
    return allIcons.value.filter((name) => name.toLowerCase().includes(kw))
  })

  // 仅取前 displayCount 个用于渲染（分批加载）
  const iconNames = computed(() => filteredIcons.value.slice(0, displayCount.value))

  const hasMore = computed(() => displayCount.value < filteredIcons.value.length)

  onMounted(() => {
    allIcons.value = listIcons('', 'ri').filter((iconName) => iconName.endsWith('-line'))
    displayCount.value = PAGE_SIZE
  })

  const loadMore = () => {
    displayCount.value = Math.min(displayCount.value + PAGE_SIZE, filteredIcons.value.length)
  }

  const resetList = () => {
    displayCount.value = PAGE_SIZE
    if (listContainerRef.value) listContainerRef.value.scrollTop = 0
  }

  const filterIcons = () => {
    resetList()
  }

  // 滚动到底部时继续加载下一批
  const onScroll = () => {
    const el = listContainerRef.value
    if (!el || loadingMore.value || !hasMore.value) return
    if (el.scrollTop + el.clientHeight >= el.scrollHeight - 40) {
      loadingMore.value = true
      // 让出一帧再追加，避免高频滚动事件堆积导致的卡顿
      requestAnimationFrame(() => {
        loadMore()
        nextTick(() => {
          loadingMore.value = false
        })
      })
    }
  }

  const selectedIcon = (iconName: string) => {
    emit('update:modelValue', iconName)
    visible.value = false
  }

  const applyCustomIcon = () => {
    const value = customIcon.value.trim()
    if (!value) return
    emit('update:modelValue', value)
    visible.value = false
  }

  // 打开选择器时重置到第一批，避免残留滚动位置/分页状态
  watch(visible, (val) => {
    if (val) resetList()
  })

  watch(
    iconValue,
    (value) => {
      customIcon.value = value.includes(':') ? value : ''
    },
    { immediate: true }
  )
</script>

<style lang="scss" scoped>
  .icon-scroll {
    max-height: calc(50vh - 100px);
    overflow-y: auto;
    scroll-behavior: smooth;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  .icon-footer {
    padding: 8px 4px 2px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
    text-align: center;
    user-select: none;
  }

  .iconify-panel {
    padding: 2px 4px 12px;
  }

  .iconify-heading {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
  }

  .iconify-form {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px;
  }

  .icon-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(48px, 1fr));
    gap: 8px;
    padding: 4px;
    margin-top: 10px;

    .icon-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 44px;
      padding: 8px 6px;
      cursor: pointer;
      background: var(--el-bg-color);
      border: 1px solid var(--el-border-color-light);
      border-radius: 12px;
      transition:
        border-color 0.2s ease,
        background-color 0.2s ease,
        color 0.2s ease,
        transform 0.2s ease;

      &:hover {
        color: var(--el-color-primary);
        background: rgb(64 158 255 / 8%);
        border-color: var(--el-color-primary);
        transform: translateY(-1px);
      }

      &.active {
        color: var(--el-color-primary);
        background: rgb(64 158 255 / 12%);
        border-color: var(--el-color-primary);
      }
    }
  }
</style>
