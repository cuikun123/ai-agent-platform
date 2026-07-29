import { Form, Input, Button, Message } from '@arco-design/web-react'
import { IconUser, IconLock, IconEmail, IconIdcard } from '@arco-design/web-react/icon'
import { Link, useNavigate } from 'react-router-dom'
import { ShieldCheck } from 'lucide-react'
import { register } from '../../api/auth'
import useAsync from '../../hooks/useAsync'
import AuthLayout from '../../components/AuthLayout'
import PasswordStrengthBar from './components/PasswordStrengthBar'

const FormItem = Form.Item

/** 注册页 */
function RegisterPage() {
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const password = Form.useWatch('password', form) || ''
  const confirmPassword = Form.useWatch('confirmPassword', form) || ''
  const confirmMatch = confirmPassword ? confirmPassword === password : null

  const { loading, run: handleSubmit } = useAsync(async () => {
    const values = await form.validate()
    await register({
      username: values.username,
      nickname: values.nickname,
      email: values.email,
      password: values.password,
    })
    Message.success('注册成功，即将跳转登录')
    setTimeout(() => navigate('/login'), 1500)
  })

  return (
    <AuthLayout
      title="创建账号"
      subtitle="注册后即可开始使用 AI Agent 平台"
      footer={<>已有账号？<Link to="/login">去登录</Link></>}
    >
      <Form form={form} layout="vertical" autoComplete="off">
        <FormItem field="username" label="用户名" rules={[
          { required: true, message: '请输入用户名' },
          { minLength: 3, message: '用户名至少 3 个字符' },
        ]}>
          <Input prefix={<IconUser />} placeholder="3-20 个字符，支持字母和数字" size="large" />
        </FormItem>

        <FormItem field="nickname" label="真实姓名" rules={[{ required: true, message: '请输入真实姓名' }]}>
          <Input prefix={<IconIdcard />} placeholder="用于团队内显示" size="large" />
        </FormItem>

        <FormItem field="email" label="邮箱" rules={[
          { required: true, message: '请输入邮箱' },
          { type: 'email', message: '邮箱格式不正确' },
        ]}>
          <Input prefix={<IconEmail />} placeholder="请输入企业邮箱（如 name@company.com）" size="large" />
        </FormItem>

        <FormItem field="password" label="密码" rules={[
          { required: true, message: '请输入密码' },
          { minLength: 8, message: '密码至少 8 个字符' },
        ]}>
          <Input.Password prefix={<IconLock />} placeholder="至少 8 位，含大小写字母及数字" size="large" />
        </FormItem>

        <PasswordStrengthBar password={password} />

        <FormItem field="confirmPassword" label="确认密码" rules={[
          { required: true, message: '请确认密码' },
          {
            validator: (value, callback) => {
              if (value !== form.getFieldValue('password')) {
                callback('两次密码不一致')
              } else {
                callback()
              }
            },
          },
        ]}>
          <Input.Password prefix={<IconLock />} placeholder="再次输入密码" size="large" />
        </FormItem>

        {confirmMatch !== null && (
          <div style={{ marginTop: -16, marginBottom: 16 }}>
            {confirmMatch ? (
              <div className="field-valid">✓ 密码一致</div>
            ) : (
              <div className="field-invalid">✗ 密码不一致</div>
            )}
          </div>
        )}

        <FormItem>
          <Button className="auth-form__submit-btn" long size="large" loading={loading} onClick={handleSubmit}>
            注册
          </Button>
        </FormItem>
      </Form>

      <div className="auth-form__trust">
        <ShieldCheck size={14} />
        <span>采用企业级加密，保障您的数据安全</span>
      </div>
    </AuthLayout>
  )
}

export default RegisterPage
