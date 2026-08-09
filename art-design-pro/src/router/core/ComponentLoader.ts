/**
 * 组件加载器
 *
 * 负责动态加载 Vue 组件
 *
 * @module router/core/ComponentLoader
 * @author 量子科技 Team
 */

import { h } from 'vue'

export class ComponentLoader {
  private modules: Record<string, () => Promise<any>>

  constructor() {
    // 动态导入 views 和 components 目录下所有 .vue 组件
    const viewsModules = import.meta.glob('../../views/**/*.vue')
    const componentsModules = import.meta.glob('../../components/**/*.vue')
    this.modules = { ...viewsModules, ...componentsModules }
  }

  /**
   * 加载组件
   */
  load(componentPath: string): () => Promise<any> {
    if (!componentPath) {
      return this.createEmptyComponent()
    }

    // 构建可能的路径 - 先从 views 目录查找，再从 components 目录查找
    const viewsPath = `../../views${componentPath}.vue`
    const viewsPathWithIndex = `../../views${componentPath}/index.vue`
    const componentsPath = `../../components${componentPath}.vue`
    const componentsPathWithIndex = `../../components${componentPath}/index.vue`

    // 按优先级查找模块
    const module =
      this.modules[viewsPath] ||
      this.modules[viewsPathWithIndex] ||
      this.modules[componentsPath] ||
      this.modules[componentsPathWithIndex]

    if (!module) {
      console.error(
        `[ComponentLoader] 未找到组件: ${componentPath}，尝试过的路径: ${viewsPath}, ${viewsPathWithIndex}, ${componentsPath}, ${componentsPathWithIndex}`
      )
      return this.createErrorComponent(componentPath)
    }

    return module
  }

  /**
   * 加载布局组件
   */
  loadLayout(): () => Promise<any> {
    return () => import('@/views/index/index.vue')
  }

  /**
   * 加载 iframe 组件
   */
  loadIframe(): () => Promise<any> {
    return () => import('@/views/outside/Iframe.vue')
  }

  /**
   * 创建空组件
   */
  private createEmptyComponent(): () => Promise<any> {
    return () =>
      Promise.resolve({
        render() {
          return h('div', {})
        }
      })
  }

  /**
   * 创建错误提示组件
   */
  private createErrorComponent(componentPath: string): () => Promise<any> {
    return () =>
      Promise.resolve({
        render() {
          return h('div', { class: 'route-error' }, `组件未找到: ${componentPath}`)
        }
      })
  }
}
