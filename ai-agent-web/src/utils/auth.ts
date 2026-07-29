const TOKEN_KEY = 'ai_agent_token'

/** 存储 token */
export function setToken(accessToken: string, refreshToken: string) {
  localStorage.setItem(TOKEN_KEY, JSON.stringify({ accessToken, refreshToken }))
}

/** 获取 accessToken */
export function getAccessToken(): string | null {
  const raw = localStorage.getItem(TOKEN_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw).accessToken
  } catch {
    return null
  }
}

/** 获取 refreshToken */
export function getRefreshToken(): string | null {
  const raw = localStorage.getItem(TOKEN_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw).refreshToken
  } catch {
    return null
  }
}

/** 清除 token（登出时调用） */
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}
