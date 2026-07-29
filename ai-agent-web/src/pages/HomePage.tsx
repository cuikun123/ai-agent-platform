import { Card, Typography } from '@arco-design/web-react'

const { Title, Paragraph } = Typography

/** 首页（占位） */
function HomePage() {
  return (
    <Card>
      <Title heading={4}>欢迎使用 AI Agent Platform</Title>
      <Paragraph style={{ color: '#86909c' }}>
        首页 — 待实现
      </Paragraph>
    </Card>
  )
}

export default HomePage
