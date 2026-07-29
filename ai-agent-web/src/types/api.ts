/** API 统一响应格式 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

/** 注册请求参数 */
export interface RegisterRequest {
  username: string
  realName: string
  email: string
  password: string
}

/** 登录请求参数 */
export interface LoginRequest {
  username: string
  password: string
}

/** 登录响应数据 */
export interface LoginData {
  accessToken: string
  refreshToken: string
}
