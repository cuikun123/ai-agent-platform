import { Navigate } from 'react-router-dom'
import { getAccessToken } from '../utils/auth'

interface AuthGuardProps {
  children: React.ReactNode
}

/**
 * 路由守卫：未登录时跳转登录页
 */
function AuthGuard({ children }: AuthGuardProps) {
  const token = getAccessToken()

  if (!token) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

export default AuthGuard
