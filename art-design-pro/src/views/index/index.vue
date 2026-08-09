<!-- 布局容器 -->
<template>
  <div class="app-layout">
    <aside id="app-sidebar">
      <ArtSidebarMenu />
    </aside>

    <main id="app-main">
      <div id="app-header">
        <ArtHeaderBar />
      </div>
      <div id="app-content">
        <ArtPageContent />
      </div>
      <footer id="app-footer" v-if="site.showFooter && (site.copyright || site.beian)">
        <span v-if="site.copyright">{{ site.copyright }}</span>
        <template v-if="site.beian">
          <span class="sep">·</span>
          <a :href="BEIAN_URL" target="_blank" rel="noopener noreferrer">{{ site.beian }}</a>
        </template>
      </footer>
    </main>

    <div id="app-global">
      <ArtGlobalComponent />
    </div>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'AppLayout' })
  import { onMounted } from 'vue'
  import { useSiteConfigStore } from '@/store/modules/siteConfig'

  const site = useSiteConfigStore()
  const BEIAN_URL = 'https://beian.miit.gov.cn/'

  onMounted(() => {
    site.load()
  })
</script>

<style lang="scss" scoped>
  @use './style';
</style>
