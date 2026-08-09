<template>
  <div class="h-full w-full">
    <iframe
      :src="snailJobUrl"
      frameborder="0"
      class="h-full w-full min-h-[calc(100vh-120px)] border-none"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'SnailJob' })

  const snailJobUrl = import.meta.env.VITE_APP_SNAILJOB_ADMIN || ''
</script>
