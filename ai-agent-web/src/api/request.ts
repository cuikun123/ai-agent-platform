import { Message } from '@arco-design/web-react'
import type { ApiResponse } from '../types/api'
import { getAccessToken, clearToken } from '../utils/auth'

/** 需要跳转登录页的错误码（token 失效相关） */
const TOKEN_ERROR_CODES = [1001, 1002, 1003]

/**
 * 全局请求方法
 * 统一处理：请求头、响应解析、错误拦截
 */
async function request<T = unknown>(
  url: string,
  options: RequestInit = {},
): Promise<ApiResponse<T>> {
  const token = getAccessToken()

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const res = await fetch(url, {
    ...options,
    headers,
  })

  // HTTP 错误：尝试读取后端返回的 JSON 错误信息
  if (!res.ok) {
    try {
      const errData: ApiResponse = await res.json()
      handleAuthError(errData.code)
      Message.error(errData.message || `请求失败 (${res.status})`)
      return Promise.reject(errData)
    } catch {
      Message.error(`请求失败 (${res.status})`)
      return Promise.reject(new Error(`HTTP ${res.status}`))
    }
  }

  const data: ApiResponse<T> = await res.json()

  // 业务错误拦截
  if (data.code !== 0) {
    handleAuthError(data.code)
    Message.error(data.message)
    return Promise.reject(data)
  }

  return data
}

/**
 * token 失效时清除 token 并跳转登录页
 */
function handleAuthError(code: number) {
  if (TOKEN_ERROR_CODES.includes(code)) {
    clearToken()
    window.location.href = '/login'
  }
}

export default request
