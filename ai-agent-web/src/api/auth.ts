import request from './request'
import type { RegisterRequest, LoginRequest, LoginData, ApiResponse } from '../types/api'

/** 注册 */
export function register(data: RegisterRequest): Promise<ApiResponse<null>> {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/** 登录 */
export function login(data: LoginRequest): Promise<ApiResponse<LoginData>> {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}
