import { Layout } from '@arco-design/web-react'
import { Outlet } from 'react-router-dom'
import { IconSettings } from '@arco-design/web-react/icon'
import ConversationList from '../pages/chat/components/ConversationList'

const { Sider, Content, Header } = Layout

/** 主布局：会话列表侧边栏 + 内容区 */
function MainLayout() {
  return (
    <Layout style={{ height: '100vh' }}>
      <Sider
        width={260}
        style={{ background: '#fff', borderRight: '1px solid #e5e6eb', display: 'flex', flexDirection: 'column' }}
      >
        {/* Logo */}
        <div style={{
          height: 48,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 16px',
          borderBottom: '1px solid #e5e6eb',
          fontWeight: 600,
          fontSize: 15,
        }}>
          <span>AI Agent Platform</span>
          <IconSettings style={{ fontSize: 18, cursor: 'pointer', color: '#86909c' }} />
        </div>

        {/* 会话列表 */}
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <ConversationList />
        </div>
      </Sider>
      <Layout>
        <Content style={{ background: '#f7f8fa' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default MainLayout
