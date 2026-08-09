import axios, { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { ApiStatus } from './status'
import { handleError, HttpError, showError, showSuccess } from './error'
import { $t } from '@/locales'
import { BaseResponse } from '@/types'
import {
  decryptBase64,
  decryptWithAes,
  encryptBase64,
  encryptWithAes,
  generateAesKey
} from '@/utils/crypto'
import { decrypt, encrypt } from '@/utils/jsencrypt'
import { blobValidate, tansParams } from '@/utils/ruoyi'
import { saveBlob } from '@/utils/download'

const REQUEST_TIMEOUT = 15000
const LOGOUT_DELAY = 500
const MAX_RETRIES = 0
const RETRY_DELAY = 1000
const UNAUTHORIZED_DEBOUNCE_TIME = 3000

let isUnauthorizedErrorShown = false
let unauthorizedTimer: NodeJS.Timeout | null = null

interface ExtendedAxiosRequestConfig extends AxiosRequestConfig {
  showErrorMessage?: boolean
  showSuccessMessage?: boolean
  isToken?: boolean
  isEncrypt?: boolean
  repeatSubmit?: boolean
}

const { VITE_API_URL, VITE_WITH_CREDENTIALS, VITE_APP_CLIENT_ID } = import.meta.env

const axiosInstance = axios.create({
  timeout: REQUEST_TIMEOUT,
  baseURL: VITE_API_URL,
  withCredentials: VITE_WITH_CREDENTIALS === 'true',
  validateStatus: (status) => status >= 200 && status < 300,
  transformResponse: [
    (data, headers) => {
      const contentType = headers['content-type']
      if (typeof contentType === 'string' && contentType.includes('application/json')) {
        try {
          return JSON.parse(data)
        } catch {
          return data
        }
      }
      return data
    }
  ]
})

const encryptHeader = 'encrypt-key'

axiosInstance.interceptors.request.use(
  (request: InternalAxiosRequestConfig) => {
    const { accessToken } = useUserStore()
    const isToken = (request as ExtendedAxiosRequestConfig).isToken !== false
    const isEncrypt = (request as ExtendedAxiosRequestConfig).isEncrypt === true
    const isRepeatSubmit = (request as ExtendedAxiosRequestConfig).repeatSubmit === false

    if (isToken && accessToken) {
      request.headers.set('Authorization', 'Bearer ' + accessToken)
    }

    request.headers.set('clientid', VITE_APP_CLIENT_ID || '')

    if (!isRepeatSubmit && (request.method === 'post' || request.method === 'put')) {
      const requestObj = {
        url: request.url,
        data: typeof request.data === 'object' ? JSON.stringify(request.data) : request.data,
        time: new Date().getTime()
      }
      const sessionObj = sessionStorage.getItem('sessionObj')
      if (!sessionObj) {
        sessionStorage.setItem('sessionObj', JSON.stringify(requestObj))
      } else {
        const { url: s_url, data: s_data, time: s_time } = JSON.parse(sessionObj)
        const interval = 500
        if (
          s_data === requestObj.data &&
          requestObj.time - s_time < interval &&
          s_url === requestObj.url
        ) {
          const message = '数据正在处理，请勿重复提交'
          console.warn(`[${s_url}]: ` + message)
          return Promise.reject(new Error(message))
        } else {
          sessionStorage.setItem('sessionObj', JSON.stringify(requestObj))
        }
      }
    }

    if (request.data && !(request.data instanceof FormData) && !request.headers['Content-Type']) {
      request.headers.set('Content-Type', 'application/json')
      request.data = JSON.stringify(request.data)
    }

    if (import.meta.env.VITE_APP_ENCRYPT === 'true') {
      if (isEncrypt && (request.method === 'post' || request.method === 'put')) {
        console.log('[Crypto] Encrypting request:', request.url)
        const aesKey = generateAesKey()
        const encryptedKey = encrypt(encryptBase64(aesKey))
        if (encryptedKey) {
          request.headers.set(encryptHeader, encryptedKey)
          console.log('[Crypto] encrypt-key header set')
        }
        if (typeof request.data === 'string') {
          request.data = encryptWithAes(request.data, aesKey)
          console.log('[Crypto] Request data encrypted')
        }
      }
    }

    return request
  },
  (error) => {
    showError(createHttpError($t('httpMsg.requestConfigError'), ApiStatus.error))
    return Promise.reject(error)
  }
)

axiosInstance.interceptors.response.use(
  (response: AxiosResponse<BaseResponse>) => {
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    if (import.meta.env.VITE_APP_ENCRYPT === 'true') {
      const keyStr = response.headers[encryptHeader]
      if (keyStr != null && keyStr != '') {
        console.log('[Crypto] Decrypting response:', response.config.url)
        const data = response.data
        const decryptKey = decrypt(keyStr)
        if (typeof decryptKey === 'string') {
          const aesKey = decryptBase64(decryptKey)
          const decryptData = decryptWithAes(data as unknown as string, aesKey)
          response.data = JSON.parse(decryptData)
          console.log('[Crypto] Response data decrypted')
        }
      }
    }
    const { code, msg } = response.data
    if (code === 0 || code === 200) return response
    if (code === ApiStatus.unauthorized) handleUnauthorizedError(msg)
    throw createHttpError(msg || $t('httpMsg.requestFailed'), code)
  },
  (error) => {
    if (error.response?.status === ApiStatus.unauthorized) handleUnauthorizedError()
    return Promise.reject(handleError(error))
  }
)

function createHttpError(message: string, code: number) {
  return new HttpError(message, code)
}

function handleUnauthorizedError(message?: string): never {
  const error = createHttpError(message || $t('httpMsg.unauthorized'), ApiStatus.unauthorized)

  if (!isUnauthorizedErrorShown) {
    isUnauthorizedErrorShown = true
    logOut()

    unauthorizedTimer = setTimeout(resetUnauthorizedError, UNAUTHORIZED_DEBOUNCE_TIME)

    showError(error, true)
    throw error
  }

  throw error
}

function resetUnauthorizedError() {
  isUnauthorizedErrorShown = false
  if (unauthorizedTimer) clearTimeout(unauthorizedTimer)
  unauthorizedTimer = null
}

function logOut() {
  setTimeout(() => {
    useUserStore().logOut()
  }, LOGOUT_DELAY)
}

function shouldRetry(statusCode: number) {
  return [
    ApiStatus.requestTimeout,
    ApiStatus.internalServerError,
    ApiStatus.badGateway,
    ApiStatus.serviceUnavailable,
    ApiStatus.gatewayTimeout
  ].includes(statusCode)
}

async function retryRequest<T>(
  config: ExtendedAxiosRequestConfig,
  retries: number = MAX_RETRIES
): Promise<T> {
  try {
    return await request<T>(config)
  } catch (error) {
    if (retries > 0 && error instanceof HttpError && shouldRetry(error.code)) {
      await delay(RETRY_DELAY)
      return retryRequest<T>(config, retries - 1)
    }
    throw error
  }
}

function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

async function request<T = any>(config: ExtendedAxiosRequestConfig): Promise<T> {
  if (
    ['POST', 'PUT'].includes(config.method?.toUpperCase() || '') &&
    config.params &&
    !config.data
  ) {
    config.data = config.params
    config.params = undefined
  }

  try {
    const res = await axiosInstance.request<BaseResponse<T>>(config)

    if (config.showSuccessMessage && res.data.msg) {
      showSuccess(res.data.msg)
    }

    return res.data.data as T
  } catch (error) {
    if (error instanceof HttpError && error.code !== ApiStatus.unauthorized) {
      const showMsg = config.showErrorMessage !== false
      showError(error, showMsg)
    }
    return Promise.reject(error)
  }
}

const api = {
  globalHeaders() {
    const { accessToken } = useUserStore()
    return {
      Authorization: 'Bearer ' + accessToken,
      clientid: import.meta.env.VITE_APP_CLIENT_ID || ''
    }
  },
  get<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'GET' })
  },
  post<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'POST' })
  },
  put<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'PUT' })
  },
  del<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>({ ...config, method: 'DELETE' })
  },
  request<T>(config: ExtendedAxiosRequestConfig) {
    return retryRequest<T>(config)
  },
  async download(url: string, params?: any, fileName?: string) {
    const downloadLoadingInstance = ElLoading.service({
      text: '正在下载数据，请稍候',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    try {
      let resp
      if (params && Object.keys(params).length > 0) {
        resp = await axiosInstance.post(url, params, {
          transformRequest: [(params_1: any) => tansParams(params_1)],
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          responseType: 'blob'
        })
      } else {
        resp = await axiosInstance.get(url, {
          responseType: 'blob'
        })
      }
      const isLogin = blobValidate(resp.data)
      if (isLogin) {
        const blob = new Blob([resp.data], { type: 'application/octet-stream' })
        const downloadFileName =
          fileName || decodeURIComponent(resp.headers['download-filename'] as string) || 'download'
        saveBlob(blob, downloadFileName)
      } else {
        const blob_2 = new Blob([resp.data])
        const resText = await blob_2.text()
        const rspObj = JSON.parse(resText)
        const errMsg = rspObj.msg || '下载失败'
        ElMessage.error(errMsg)
      }
      downloadLoadingInstance.close()
    } catch (r) {
      console.error(r)
      ElMessage.error('下载文件出现错误，请联系管理员！')
      downloadLoadingInstance.close()
    }
  }
}

export default api
