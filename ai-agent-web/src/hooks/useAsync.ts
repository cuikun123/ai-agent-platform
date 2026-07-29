import { useState, useCallback } from 'react'

/**
 * 通用异步请求 hook
 * 封装 loading 状态 + try/catch/finally 模式
 *
 * 用法：
 *   const { loading, run } = useAsync(async (data) => { await api(data) })
 *   <Button loading={loading} onClick={() => run(formData)}>提交</Button>
 */
function useAsync<T extends (...args: unknown[]) => Promise<unknown>>(asyncFn: T) {
  const [loading, setLoading] = useState(false)

  const run = useCallback(
    async (...args: Parameters<T>) => {
      try {
        setLoading(true)
        return await asyncFn(...args)
      } finally {
        setLoading(false)
      }
    },
    [asyncFn],
  )

  return { loading, run }
}

export default useAsync
