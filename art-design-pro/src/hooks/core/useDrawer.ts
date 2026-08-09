import { ref } from 'vue'

export function useDrawer<T = any>() {
  const visible = ref(false)
  const data = ref<T | null>(null)

  const open = (item?: T) => {
    data.value = item || null
    visible.value = true
  }

  const close = () => {
    visible.value = false
    data.value = null
  }

  return {
    visible,
    data,
    open,
    close
  }
}
