import { useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useChatStream } from '../../hooks/useChatStream'
import MessageList from './components/MessageList'
import ChatInput from './components/ChatInput'
import ModelSelector from './components/ModelSelector'

/** 对话页：全屏对话风格，消息列表 + 输入框 + 模型选择 */
function ChatPage() {
  const { conversationId } = useParams()
  const navigate = useNavigate()
  const convId = conversationId ? Number(conversationId) : null
  const [model, setModel] = useState('deepseek-chat')

  // 会话创建后更新 URL（刷新页面可恢复历史）
  const handleConversationCreated = useCallback((id: number) => {
    navigate(`/chat/${id}`, { replace: true })
  }, [navigate])

  const { messages, loading, sendMessage } = useChatStream(convId, {
    onConversationCreated: handleConversationCreated,
  })

  const handleSend = (content: string) => {
    sendMessage(content, model)
  }

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      background: '#f7f8fa',
    }}>
      <MessageList messages={messages} />
      <div style={{ maxWidth: 800, margin: '0 auto', width: '100%', padding: '0 24px' }}>
        <ModelSelector value={model} onChange={setModel} />
      </div>
      <ChatInput onSend={handleSend} loading={loading} />
    </div>
  )
}

export default ChatPage
