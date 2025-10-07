/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, delay = 300) {
  let timer = null
  
  return function(...args) {
    if (timer) {
      clearTimeout(timer)
    }
    
    timer = setTimeout(() => {
      func.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 节流函数
 * @param {Function} func 要节流的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, delay = 300) {
  let timer = null
  let lastTime = 0
  
  return function(...args) {
    const now = Date.now()
    
    if (now - lastTime < delay) {
      if (timer) {
        clearTimeout(timer)
      }
      
      timer = setTimeout(() => {
        lastTime = now
        func.apply(this, args)
      }, delay)
    } else {
      lastTime = now
      func.apply(this, args)
    }
  }
}

/**
 * 用于Vue 3 Composition API的防抖Hook
 * @param {Function} fn 要防抖的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounceFn(fn, delay = 300) {
  return debounce(fn, delay)
}

/**
 * 用于Vue 3 Composition API的节流Hook
 * @param {Function} fn 要节流的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export function useThrottleFn(fn, delay = 300) {
  return throttle(fn, delay)
}

