import { Layout, Menu } from '@arco-design/web-react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { IconHome, IconMessage, IconBook, IconTool, IconSettings } from '@arco-design/web-react/icon'

const { Sider, Content, Header } = Layout
const MenuItem = Menu.Item

/** 主布局：侧边栏 + 内容区 */
function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <Layout style={{ height: '100vh' }}>
      <Sider
        width={220}
        style={{ background: '#fff', borderRight: '1px solid #e5e6eb' }}
      >
        <div style={{ height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: 16 }}>
          AI Agent Platform
        </div>
        <Menu
          selectedKeys={[location.pathname]}
          onClickMenuItem={(key) => navigate(key)}
        >
          <MenuItem key="/">
            <IconHome /> 首页
          </MenuItem>
          <MenuItem key="/chat">
            <IconMessage /> 对话
          </MenuItem>
          <MenuItem key="/knowledge">
            <IconBook /> 知识库
          </MenuItem>
          <MenuItem key="/tools">
            <IconTool /> 工具
          </MenuItem>
          <MenuItem key="/settings">
            <IconSettings /> 设置
          </MenuItem>
        </Menu>
      </Sider>
      <Layout>
        <Header style={{ height: 48, background: '#fff', borderBottom: '1px solid #e5e6eb', display: 'flex', alignItems: 'center', padding: '0 24px' }}>
          <span style={{ color: '#86909c', fontSize: 14 }}>企业级 AI Agent 平台</span>
        </Header>
        <Content style={{ padding: 24, background: '#f7f8fa' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default MainLayout
