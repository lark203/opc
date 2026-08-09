import App from './App.vue'
import { createApp } from 'vue'
import { ElDialog, ElDrawer } from 'element-plus'
import { initStore, store } from './store' // Store
import { initRouter } from './router' // Router
import language from './locales' // 国际化
import '@styles/core/tailwind.css' // tailwind
import '@styles/index.scss' // 样式
import '@utils/sys/console.ts' // 控制台输出内容
import { setupGlobDirectives } from './directives'
import { setupErrorHandle } from './utils/sys/error-handle'
import { useSiteConfigStore } from './store/modules/siteConfig'

// 开发环境加载消息测试工具
if (import.meta.env.DEV) {
  import('./utils/notify-test')
}

(ElDialog as any).props.closeOnClickModal.default = false
;(ElDrawer as any).props.closeOnClickModal.default = false

document.addEventListener(
  'touchstart',
  function () {},
  { passive: false }
)

const app = createApp(App)
initStore(app)
initRouter(app)
setupGlobDirectives(app)
setupErrorHandle(app)

app.use(language)

// 预加载网站配置（公开接口）：在挂载前发起请求，确保登录页 logo/标题/备案号等
// 在首屏渲染时数据已就绪，避免“先显示默认图标、再刷新为配置图标”的闪烁。
// 不阻塞挂载——若接口异常，load() 内部已捕获，页面仍可用。
useSiteConfigStore(store).load()

app.mount('#app')