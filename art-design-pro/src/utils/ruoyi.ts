/**
 * 时间戳/日期字符串格式化（RuoYi 标准 parseTime）
 * @param time 时间戳（毫秒）或日期字符串或 Date 对象
 * @param pattern 格式模板，默认 '{y}-{m}-{d} {h}:{i}:{s}'
 * @returns 格式化后的时间字符串
 */
export const parseTime = (
  time: string | number | Date | null | undefined,
  pattern = '{y}-{m}-{d} {h}:{i}:{s}'
): string => {
  if (time === null || time === undefined || time === '') return ''
  let date: Date
  if (typeof time === 'object') {
    date = time as Date
  } else {
    if (typeof time === 'string') {
      time = parseInt(time, 10)
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj: Record<string, number> = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  return pattern.replace(/\{([ymdhisa])+\}/g, (result, key) => {
    const value = formatObj[key]
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    return value.toString().padStart(2, '0')
  })
}

/**
 * 参数处理
 * @param {*} params  参数
 */
export const tansParams = (params: any) => {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    const part = encodeURIComponent(propName) + '='
    if (value !== null && value !== '' && typeof value !== 'undefined') {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== '' && typeof value[key] !== 'undefined') {
            const paramKey = propName + '[' + key + ']'
            const subPart = encodeURIComponent(paramKey) + '='
            result += subPart + encodeURIComponent(value[key]) + '&'
          }
        }
      } else {
        result += part + encodeURIComponent(value) + '&'
      }
    }
  }
  return result
}

export const blobValidate = (data: any) => {
  return data.type !== 'application/json'
}

export const parseStrEmpty = (str: any) => {
  if (!str || str === 'undefined' || str === 'null') {
    return ''
  }
  return str
}
