<template>
  <div class="h-full w-full">
    <iframe
      :src="snailAiUrl"
      frameborder="0"
      class="h-full w-full min-h-[calc(100vh-120px)] border-none"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'SnailAi' })

  const snailAiUrl = import.meta.env.VITE_APP_SNAILAI_ADMIN || ''
</script>
