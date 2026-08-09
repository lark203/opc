import request from '@/utils/http'

/** 系统基本信息 */
export interface SystemBasicInfoVO {
  name: string
  version: string
  frameworkVersion: string
  environment: string
  startTime: string
  uptime: string
}

/** 服务器信息 */
export interface ServerInfoVO {
  name: string
  os: string
  arch: string
  cpuCores: number
  ip: string
  time: string
}

/** Java 运行环境信息 */
export interface JavaInfoVO {
  version: string
  vendor: string
  home: string
  jvmName: string
  jvmVersion: string
  args: string
}

/** 内存信息 */
export interface MemoryInfoVO {
  total: string
  used: string
  free: string
  usedPercent: number
  jvmTotal: string
  jvmUsed: string
  jvmFree: string
  jvmUsedPercent: number
}

/** 磁盘信息 */
export interface DiskInfoVO {
  path: string
  type: string
  total: string
  used: string
  free: string
  usedPercent: number
}

/** 系统信息监控数据 */
export interface SystemMonitorVO {
  systemInfo: SystemBasicInfoVO
  serverInfo: ServerInfoVO
  javaInfo: JavaInfoVO
  memoryInfo: MemoryInfoVO
  diskInfo: DiskInfoVO[]
}

/** 获取系统信息 */
export function getSysInfo() {
  return request.get<SystemMonitorVO>({
    url: '/monitor/sysinfo'
  })
}
