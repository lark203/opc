import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DictDataOption, DictDataVO } from '@/api/system/dict'
import { getDicts } from '@/api/system/dict'

export const useDictStore = defineStore('dict', () => {
  const dict = ref<Map<string, DictDataOption[]>>(new Map())

  const getDict = (_key: string): DictDataOption[] | null => {
    if (!_key) {
      return null
    }
    return dict.value.get(_key) || null
  }

  const setDict = (_key: string, _value: DictDataOption[]): boolean => {
    if (!_key) {
      return false
    }
    try {
      dict.value.set(_key, _value)
      return true
    } catch (e) {
      console.error('Error in setDict:', e)
      return false
    }
  }

  const removeDict = (_key: string): boolean => {
    if (!_key) {
      return false
    }
    try {
      return dict.value.delete(_key)
    } catch (e) {
      console.error('Error in removeDict:', e)
      return false
    }
  }

  const cleanDict = (): void => {
    dict.value.clear()
  }

  const toDictDataOption = (data: DictDataVO[]): DictDataOption[] => {
    return data.map((p) => ({
      label: p.dictLabel,
      value: p.dictValue,
      elTagType: p.listClass,
      elTagClass: p.cssClass
    }))
  }

  const loadDict = async (dictType: string): Promise<DictDataOption[] | null> => {
    const cached = getDict(dictType)
    if (cached) {
      return cached
    }
    try {
      const data = await getDicts(dictType)
      const options = toDictDataOption(data)
      setDict(dictType, options)
      return options
    } catch (error) {
      console.error('Error loading dict:', error)
      return null
    }
  }

  return {
    dict,
    getDict,
    setDict,
    removeDict,
    cleanDict,
    loadDict
  }
})
