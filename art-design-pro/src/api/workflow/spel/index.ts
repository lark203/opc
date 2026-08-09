import request from '@/utils/http'
import type { FlowSpelForm, FlowSpelQuery, FlowSpelVO } from './types'

export const listSpel = (query: FlowSpelQuery) =>
  request.get<{ rows: FlowSpelVO[]; total: number }>({ url: '/workflow/spel/list', params: query })

export const getSpel = (id: string | number) =>
  request.get<FlowSpelVO>({ url: `/workflow/spel/${id}` })

export const addSpel = (data: FlowSpelForm) => request.post({ url: '/workflow/spel', params: data })

export const updateSpel = (data: FlowSpelForm) =>
  request.put({ url: '/workflow/spel', params: data })

export const delSpel = (ids: string | number | Array<string | number>) =>
  request.del({ url: `/workflow/spel/${ids}` })
