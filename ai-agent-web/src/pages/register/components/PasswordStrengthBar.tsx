import { getPasswordStrength } from '../../../hooks/usePasswordStrength'

interface PasswordStrengthBarProps {
  password: string
}

/** 密码强度条组件 */
function PasswordStrengthBar({ password }: PasswordStrengthBarProps) {
  const { level, label, colorClass } = getPasswordStrength(password)

  if (!password) return null

  return (
    <div style={{ marginTop: 6 }}>
      <div className="password-strength">
        {[1, 2, 3].map((l) => (
          <div
            key={l}
            className={`password-strength__bar ${l <= level ? `password-strength__bar--active ${colorClass}` : ''}`}
          />
        ))}
      </div>
      <div className={`password-strength__text password-strength__text--${colorClass}`}>
        密码强度：{label}
      </div>
    </div>
  )
}

export default PasswordStrengthBar
