import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath } from 'url'
import vueDevTools from 'vite-plugin-vue-devtools'
import { compression } from 'vite-plugin-compression2'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import ElementPlus from 'unplugin-element-plus/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import tailwindcss from '@tailwindcss/vite'
// import { visualizer } from 'rollup-plugin-visualizer'

export default ({ mode }: { mode: string }) => {
  const root = process.cwd()
  const env = loadEnv(mode, root)
  const { VITE_VERSION, VITE_PORT, VITE_BASE_URL, VITE_API_URL, VITE_API_PROXY_URL } = env

  console.log(`🚀 API_URL = ${VITE_API_URL}`)
  console.log(`🚀 VERSION = ${VITE_VERSION}`)

  return defineConfig({
    /**
     * 全局变量定义
     * 将环境变量注入到代码中，在应用内可通过 __APP_VERSION__ 访问版本号
     */
    define: {
      __APP_VERSION__: JSON.stringify(VITE_VERSION)
    },
    /**
     * 应用部署基础路径
     * 如部署在子目录 /admin 下，则设置为 /admin/
     */
    base: VITE_BASE_URL,
    /**
     * 开发服务器配置
     */
    server: {
      /** 开发服务器端口，默认5173 */
      port: Number(VITE_PORT) || 5173,
      /**
       * 代理配置
       * 将 /dev-api 前缀的请求代理到后端服务地址，解决开发环境跨域问题
       */
      proxy: {
        '/dev-api': {
          target: VITE_API_PROXY_URL,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/dev-api/, '')
        }
      },
      /** 允许外部访问，配合 port 使用可通过 IP 访问 */
      host: true
    },
    /**
     * 路径别名配置
     * 配置后可使用 @、@views、@imgs 等别名代替相对路径
     */
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
        '@views': resolvePath('src/views'),
        '@imgs': resolvePath('src/assets/images'),
        '@icons': resolvePath('src/assets/icons'),
        '@utils': resolvePath('src/utils'),
        '@stores': resolvePath('src/store'),
        '@styles': resolvePath('src/assets/styles')
      }
    },
    /**
     * 构建配置
     */
    build: {
      /** 目标浏览器支持的 ECMAScript 版本 */
      target: 'es2015',
      /** 构建输出目录 */
      outDir: 'dist',
      /** 代码块大小警告阈值（KB），超过此值会警告 */
      chunkSizeWarningLimit: 1500,
      /** 压缩工具，terser 比 esbuild 压缩率更高 */
      minify: 'terser',
      /** terser 压缩选项 */
      terserOptions: {
        compress: {
          drop_console: true,
          drop_debugger: true
        }
      },
      /** 动态导入变量配置 */
      dynamicImportVarsOptions: {
        exclude: [],
        include: ['src/views/**/*.vue']
      },
      /**
       * 手动分包（manualChunks）
       * 将体积大且相互独立的第三方依赖拆分为独立 chunk：
       *  - 提升浏览器并行加载能力，缩短首屏时间
       *  - 依赖内容稳定，配合内容哈希可实现长效缓存（版本升级时仅变更对应 chunk）
       * 原则：仅对 node_modules 中的依赖分组，业务代码保持按路由动态分割。
       * 注：Vite 8 使用 Rolldown 替代 Rollup，build.rollupOptions 已重命名为
       * build.rolldownOptions；函数式 manualChunks 当前为 deprecated（仍可工作），
       * 后续可迁移至 Rolldown 的 codeSplitting 选项。
       */
      rolldownOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) {
              return
            }
            // 图表库（体积大，仅部分页面使用）
            if (id.includes('echarts') || id.includes('zrender')) {
              return 'echarts'
            }
            // 表格导出（体积大，仅导入/导出时使用）
            if (id.includes('xlsx') || id.includes('exceljs')) {
              return 'xlsx'
            }
            // 视频播放
            if (id.includes('xgplayer') || id.includes('xgplayer-flv')) {
              return 'xgplayer'
            }
            // 加解密
            if (id.includes('crypto-js')) {
              return 'crypto'
            }
            // Element Plus 单独成包（基础 UI 库，变更频率低）
            if (id.includes('element-plus')) {
              return 'element-plus'
            }
            // Vue 生态核心运行时
            if (
              id.includes('vue') ||
              id.includes('vue-router') ||
              id.includes('pinia') ||
              id.includes('@vue') ||
              id.includes('vue-demi')
            ) {
              return 'vue'
            }
            // 其余第三方依赖统一归入 vendor
            return 'vendor'
          }
        }
      }
    },
    /**
     * Vite 插件配置
     */
    plugins: [
      /** Vue 3 插件，处理 .vue 文件 */
      vue(),
      /** Tailwind CSS 插件 */
      tailwindcss(),
      /**
       * 自动导入插件
       * 自动导入 Vue、Vue Router、Pinia 等 API，无需手动 import
       */
      AutoImport({
        imports: ['vue', 'vue-router', 'pinia', '@vueuse/core'],
        dts: 'src/types/import/auto-imports.d.ts',
        resolvers: [ElementPlusResolver()],
        eslintrc: {
          enabled: true,
          filepath: './.auto-import.json',
          globalsPropValue: true
        }
      }),
      /**
       * 组件自动导入插件
       * 自动扫描并注册 Vue 组件，无需手动 import 和注册
       */
      Components({
        dts: 'src/types/import/components.d.ts',
        resolvers: [ElementPlusResolver()]
      }),
      /**
       * Element Plus 按需定制主题配置
       * useSource: true 使用源码样式，便于自定义主题变量
       */
      ElementPlus({
        useSource: true
      }),
      /**
       * Gzip 压缩插件
       * 对构建产物进行 gzip 压缩，减少文件体积，提升加载速度
       * 使用 vite-plugin-compression2（Vite 8 / Rolldown 兼容的维护版本）
       */
      compression({
        algorithms: ['gzip'],
        threshold: 10240,
        deleteOriginalAssets: false
      }),
      /** Vue DevTools 开发者工具插件 */
      vueDevTools()
      // visualizer({
      //   open: true,
      //   gzipSize: true,
      //   brotliSize: true,
      //   filename: 'dist/stats.html'
      // }),
    ],
    /**
     * 依赖预构建配置
     * 对指定依赖进行预构建，避免运行时重复请求与转换，提升首次加载速度
     */
    optimizeDeps: {
      include: [
        'echarts/core',
        'echarts/charts',
        'echarts/components',
        'echarts/renderers',
        'xlsx',
        'xgplayer',
        'crypto-js',
        'file-saver',
        'vue-img-cutter',
        'element-plus/es',
        'element-plus/es/components/*/style/css',
        'element-plus/es/components/*/style/index'
      ]
    },
    /**
     * CSS 配置
     */
    css: {
      /** CSS 预处理器配置 */
      preprocessorOptions: {
        scss: {
          additionalData: `
            @use "@styles/core/mixin.scss" as *;
          `
        }
      },
      /** PostCSS 配置 */
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  })
}

function resolvePath(paths: string) {
  return path.resolve(__dirname, paths)
}
