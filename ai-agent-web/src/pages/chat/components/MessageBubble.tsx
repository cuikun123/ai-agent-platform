import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { oneDark } from 'react-syntax-highlighter/dist/esm/styles/prism'
import { IconUser, IconRobot } from '@arco-design/web-react/icon'
import type { Components } from 'react-markdown'

/** 消息类型 */
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
}

/** Markdown 组件映射（代码块语法高亮） */
const markdownComponents: Components = {
  code({ className, children, ...props }) {
    const match = /language-(\w+)/.exec(className || '')
    const codeString = String(children).replace(/\n$/, '')
    if (match) {
      return (
        <SyntaxHighlighter style={oneDark} language={match[1]} PreTag="div">
          {codeString}
        </SyntaxHighlighter>
      )
    }
    return <code className={className} {...props}>{children}</code>
  },
}

/** 单条消息（DeepSeek 风格：用户靠右气泡，AI 靠左纯文字） */
function MessageBubble({ message }: { message: ChatMessage }) {
  const isUser = message.role === 'user'

  return (
    <div style={{
      display: 'flex',
      justifyContent: isUser ? 'flex-end' : 'flex-start',
      padding: '8px 0',
      maxWidth: 900,
      margin: '0 auto',
      width: '100%',
    }}>
      {/* AI 头像（左侧） */}
      {!isUser && (
        <div style={{
          width: 32,
          height: 32,
          borderRadius: '50%',
          background: '#00B42A',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
          marginRight: 12,
        }}>
          <IconRobot style={{ color: '#fff', fontSize: 16 }} />
        </div>
      )}

      {/* 消息内容 */}
      {isUser ? (
        <div style={{
          maxWidth: '75%',
          padding: '10px 16px',
          borderRadius: 12,
          background: '#165DFF',
          color: '#fff',
          lineHeight: 1.7,
        }}>
          <div style={{ whiteSpace: 'pre-wrap' }}>{message.content}</div>
        </div>
      ) : (
        <div className="message-content" style={{ lineHeight: 1.7, flex: 1, minWidth: 0 }}>
          <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
            {message.content}
          </ReactMarkdown>
        </div>
      )}

      {/* 用户头像（右侧） */}
      {isUser && (
        <div style={{
          width: 32,
          height: 32,
          borderRadius: '50%',
          background: '#165DFF',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
          marginLeft: 12,
        }}>
          <IconUser style={{ color: '#fff', fontSize: 16 }} />
        </div>
      )}
    </div>
  )
}

export default MessageBubble
