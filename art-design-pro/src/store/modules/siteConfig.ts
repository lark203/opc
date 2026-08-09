import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSiteOption, type SysOptionVO } from '@/api/system/option'

/**
 * 网站配置（来自 sys_option 表 SITE 分类）
 *
 * 登录页、系统页头、页脚等处均从此处读取品牌信息；
 * value 为空时回退到 default_value。
 */
export const useSiteConfigStore = defineStore('siteConfig', () => {
  const logo = ref('')
  const favicon = ref('')
  const title = ref('')
  const description = ref('')
  const subDescription = ref('')
  const copyright = ref('')
  const beian = ref('')
  const showFooter = ref(true)

  /** 是否已加载，避免重复请求 */
  const loaded = ref(false)

  /** 把 Favicon 和标题应用到浏览器 */
  function applyToDocument() {
    if (favicon.value) {
      const link = document.querySelector("link[rel~='icon']") as HTMLLinkElement | null
      if (link) link.href = favicon.value
    }
    // 更新浏览器标题并缓存，下次加载时 index.html 内联脚本可直接读取
    if (title.value) {
      document.title = title.value
      try {
        localStorage.setItem('site-title', title.value)
      } catch {
        // localStorage 异常时忽略
      }
    }
  }

  /** 把字符串 'true'/'false' 解析为布尔，缺省视为 true（默认展示底部区域） */
  function toBool(v: string | undefined): boolean {
    if (v === undefined || v === null || v === '') return true
    return v === 'true'
  }

  /** 从选项列表填充状态 */
  function fillFromList(list: SysOptionVO[]) {
    const map: Record<string, string> = {}
    list.forEach((o) => {
      map[o.code] = o.value || o.defaultValue || ''
    })
    logo.value = map['SITE_LOGO'] || ''
    favicon.value = map['SITE_FAVICON'] || ''
    title.value = map['SITE_TITLE'] || ''
    description.value = map['SITE_DESCRIPTION'] || ''
    subDescription.value = map['SITE_SUB_DESCRIPTION'] || ''
    copyright.value = map['SITE_COPYRIGHT'] || ''
    beian.value = map['SITE_BEIAN'] || ''
    showFooter.value = toBool(map['SITE_SHOW_FOOTER'])
    applyToDocument()
  }

  /** 拉取网站配置（公开接口，无需登录） */
  async function load(force = false) {
    if (loaded.value && !force) return
    try {
      const list = await getSiteOption()
      fillFromList(list)
      loaded.value = true
    } catch (e) {
      // 接口异常时不阻塞页面，保留已有（或默认）展示
      console.error('[siteConfig] 加载网站配置失败', e)
    }
  }

  /** 保存后从表单数据直接刷新展示，无需重新请求 */
  function updateFromForm(values: Record<string, string>) {
    logo.value = values['SITE_LOGO'] ?? logo.value
    favicon.value = values['SITE_FAVICON'] ?? favicon.value
    title.value = values['SITE_TITLE'] ?? title.value
    description.value = values['SITE_DESCRIPTION'] ?? description.value
    subDescription.value = values['SITE_SUB_DESCRIPTION'] ?? subDescription.value
    copyright.value = values['SITE_COPYRIGHT'] ?? copyright.value
    beian.value = values['SITE_BEIAN'] ?? beian.value
    if ('SITE_SHOW_FOOTER' in values) showFooter.value = toBool(values['SITE_SHOW_FOOTER'])
    applyToDocument()
  }

  return {
    logo,
    favicon,
    title,
    description,
    subDescription,
    copyright,
    beian,
    showFooter,
    loaded,
    load,
    updateFromForm
  }
})
