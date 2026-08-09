<template>
  <div class="h-full w-full">
    <iframe
      :src="monitorUrl"
      frameborder="0"
      class="h-full w-full min-h-[calc(100vh-120px)] border-none"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'AdminMonitor' })

  const monitorUrl = import.meta.env.VITE_APP_MONITOR_ADMIN || ''
</script>
