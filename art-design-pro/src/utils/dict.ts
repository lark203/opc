import { reactive } from 'vue'
import type { DictDataOption } from '@/api/system/dict'
import { getDicts } from '@/api/system/dict'
import { useDictStore } from '@/store/modules/dict'

const pendingRequests = new Map<string, Promise<DictDataOption[]>>()

export const useDict = (...args: string[]): { [key: string]: DictDataOption[] } => {
  const res = reactive<{ [key: string]: DictDataOption[] }>({})

  args.forEach(async (dictType) => {
    res[dictType] = []
    const dicts = useDictStore().getDict(dictType)
    if (dicts) {
      res[dictType] = dicts
    } else {
      if (!pendingRequests.has(dictType)) {
        const request = getDicts(dictType)
          .then((data) => {
            const dictOptions = data.map((p) => ({
              label: p.dictLabel,
              value: p.dictValue,
              elTagType: p.listClass,
              elTagClass: p.cssClass
            }))
            useDictStore().setDict(dictType, dictOptions)
            return dictOptions
          })
          .finally(() => pendingRequests.delete(dictType))
        pendingRequests.set(dictType, request)
      }
      res[dictType] = await pendingRequests.get(dictType)!
    }
  })

  return res
}

export const getDictLabel = (
  options: DictDataOption[],
  value: string | number | undefined
): string => {
  if (!value && value !== 0) return '-'
  const option = options.find((item) => String(item.value) === String(value))
  return option?.label || String(value)
}

export const getDictOption = (
  options: DictDataOption[],
  value: string | number | undefined
): DictDataOption | undefined => {
  if (!value && value !== 0) return undefined
  return options.find((item) => String(item.value) === String(value))
}
