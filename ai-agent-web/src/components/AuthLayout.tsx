import { Bot } from 'lucide-react'

interface AuthLayoutProps {
  title: string
  subtitle: string
  children: React.ReactNode
  footer?: React.ReactNode
}

/**
 * 认证页面布局（登录/注册共用）
 * 左侧品牌区 + 右侧表单区
 */
function AuthLayout({ title, subtitle, children, footer }: AuthLayoutProps) {
  return (
    <div className="auth-page">
      {/* 左侧：品牌区 */}
      <div className="auth-page__left">
        <div className="auth-brand__orb" />
        <div className="auth-brand__grid" />
        <div className="auth-brand__content">
          <Bot size={64} strokeWidth={1.5} />
          <h1>AI Agent Platform</h1>
          <p>企业级 AI Agent 构建与管理平台</p>
        </div>
      </div>

      {/* 右侧：表单区 */}
      <div className="auth-page__right">
        <div className="auth-form">
          <div className="auth-form__title">{title}</div>
          <div className="auth-form__subtitle">{subtitle}</div>
          {children}
          {footer && <div className="auth-form__footer">{footer}</div>}
        </div>
      </div>
    </div>
  )
}

export default AuthLayout
