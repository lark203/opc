/// <reference types="vite/client" />

declare module 'nprogress'

declare module 'crypto-js'

declare module 'vue-img-cutter'

declare module 'file-saver'

declare module 'qrcode.vue' {
  export type Level = 'L' | 'M' | 'Q' | 'H'
  export type RenderAs = 'canvas' | 'svg'
  export type GradientType = 'linear' | 'radial'
  export interface ImageSettings {
    src: string
    height: number
    width: number
    excavate: boolean
  }
  export interface QRCodeProps {
    value: string
    size?: number
    level?: Level
    background?: string
    foreground?: string
    renderAs?: RenderAs
  }
  const QrcodeVue: any
  export default QrcodeVue
}

// 全局变量声明
declare const __APP_VERSION__: string // 版本号

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_LOGO_TITLE: string
  readonly VITE_APP_ENV: string
  readonly VITE_APP_BASE_API: string
  readonly VITE_APP_CONTEXT_PATH: string
  readonly VITE_APP_MONITOR_ADMIN: string
  readonly VITE_APP_SNAILJOB_ADMIN: string
  readonly VITE_APP_SNAILAI_ADMIN: string
  readonly VITE_APP_PORT: string
  readonly VITE_APP_ENCRYPT: string
  readonly VITE_APP_RSA_PUBLIC_KEY: string
  readonly VITE_APP_RSA_PRIVATE_KEY: string
  readonly VITE_APP_CLIENT_ID: string
  readonly VITE_APP_MESSAGE_ENABLED: string
  readonly VITE_APP_MESSAGE_TRANSPORT: string
  readonly VITE_APP_MESSAGE_PATH: string
  readonly VITE_ACCESS_MODE: string
  readonly VITE_BUILD_COMPRESS: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
