import { useState, useRef, useEffect } from 'react'
import { Button } from '@arco-design/web-react'
import { IconSend } from '@arco-design/web-react/icon'

interface ChatInputProps {
  onSend: (message: string) => void
  loading?: boolean
}

/** 对话输入框：居中，Enter 发送，Shift+Enter 换行 */
function ChatInput({ onSend, loading }: ChatInputProps) {
  const [value, setValue] = useState('')
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  // 自动调整高度
  useEffect(() => {
    const textarea = textareaRef.current
    if (textarea) {
      textarea.style.height = 'auto'
      textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px'
    }
  }, [value])

  const handleSend = () => {
    const trimmed = value.trim()
    if (!trimmed || loading) return
    onSend(trimmed)
    setValue('')
  }

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div style={{
      padding: '16px 24px 24px 24px',
      maxWidth: 800,
      margin: '0 auto',
      width: '100%',
    }}>
      <div style={{
        display: 'flex',
        gap: 8,
        alignItems: 'flex-end',
        border: '1px solid #e5e6eb',
        borderRadius: 12,
        padding: '8px 12px',
        background: '#fff',
      }}>
        <textarea
          ref={textareaRef}
          value={value}
          onChange={(e) => setValue(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="输入消息，Enter 发送，Shift+Enter 换行"
          rows={1}
          style={{
            flex: 1,
            border: 'none',
            outline: 'none',
            resize: 'none',
            fontSize: 14,
            lineHeight: '22px',
            fontFamily: 'inherit',
            maxHeight: 150,
          }}
        />
        <Button
          type="primary"
          icon={<IconSend />}
          loading={loading}
          disabled={!value.trim()}
          onClick={handleSend}
          style={{ borderRadius: 8, flexShrink: 0 }}
        />
      </div>
    </div>
  )
}

export default ChatInput
