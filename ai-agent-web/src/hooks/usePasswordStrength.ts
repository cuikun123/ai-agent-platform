/** 密码强度计算结果 */
export interface PasswordStrength {
  level: number   // 0=空, 1=弱, 2=中, 3=强
  label: string
  colorClass: string
}

const labels = ['', '弱', '中', '强']
const colorClasses = ['', 'weak', 'medium', 'strong']

/**
 * 计算密码强度
 * 评分规则：长度≥8 + 大小写混合 + 数字 + 特殊字符
 */
export function getPasswordStrength(password: string): PasswordStrength {
  if (!password) return { level: 0, label: '', colorClass: '' }

  let score = 0
  if (password.length >= 8) score++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++
  if (/\d/.test(password)) score++
  if (/[^a-zA-Z0-9]/.test(password)) score++

  const level = score <= 1 ? 1 : score <= 2 ? 2 : 3
  return { level, label: labels[level], colorClass: colorClasses[level] }
}
