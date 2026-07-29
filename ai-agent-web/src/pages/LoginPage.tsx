import { Form, Input, Button, Message } from '@arco-design/web-react'
import { IconUser, IconLock } from '@arco-design/web-react/icon'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import { setToken } from '../utils/auth'
import useAsync from '../hooks/useAsync'
import AuthLayout from '../components/AuthLayout'

const FormItem = Form.Item

/** 登录页 */
function LoginPage() {
  const [form] = Form.useForm()
  const navigate = useNavigate()

  const { loading, run: handleSubmit } = useAsync(async () => {
    const values = await form.validate()
    const res = await login({
      username: values.username,
      password: values.password,
    })
    setToken(res.data.accessToken, res.data.refreshToken)
    Message.success('登录成功')
    setTimeout(() => navigate('/'), 1000)
  })

  return (
    <AuthLayout
      title="欢迎回来"
      subtitle="登录你的账号继续使用"
      footer={<>还没有账号？<Link to="/register">去注册</Link></>}
    >
      <Form form={form} layout="vertical" autoComplete="off">
        <FormItem field="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
          <Input prefix={<IconUser />} placeholder="请输入用户名" size="large" />
        </FormItem>

        <FormItem field="password" label="密码" rules={[{ required: true, message: '请输入密码' }]}>
          <Input.Password prefix={<IconLock />} placeholder="请输入密码" size="large" />
        </FormItem>

        <FormItem>
          <Button className="auth-form__submit-btn" long size="large" loading={loading} onClick={handleSubmit}>
            登录
          </Button>
        </FormItem>
      </Form>
    </AuthLayout>
  )
}

export default LoginPage
