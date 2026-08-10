import { useState, useEffect, useRef, useCallback } from 'react'
import { sendStreamMessage, getMessages } from '../api/chat'
import { getAccessToken } from '../utils/auth'
import type { ChatMessage } from '../pages/chat/components/MessageBubble'

/**
 * 对话流式 hook：管理消息列表 + SSE 流式 + 历史加载
 */
export function useChatStream(
  conversationId: number | null,
  options?: { onConversationCreated?: (id: number) => void }
) {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [loading, setLoading] = useState(false)
  const [currentConversationId, setCurrentConversationId] = useState<number | null>(conversationId)
  const abortRef = useRef<AbortController | null>(null)
  const callbackRef = useRef(options?.onConversationCreated)

  // 同步 callback 到 ref（避免 sendMessage 闭包捕获旧值）
  useEffect(() => {
    callbackRef.current = options?.onConversationCreated
  }, [options?.onConversationCreated])

  // URL 的 conversationId 变化时，同步到内部状态（切换会话时触发）
  useEffect(() => {
    // 切换会话时，立即中断流式 + 清空消息
    abortRef.current?.abort()
    setLoading(false)
    setMessages([])
    setCurrentConversationId(conversationId)
  }, [conversationId])

  // 加载历史消息
  useEffect(() => {
    if (!currentConversationId) {
      setMessages([])
      return
    }
    getMessages(currentConversationId).then((res) => {
      const history: ChatMessage[] = res.data.map((m) => ({
        id: String(m.id),
        role: m.role,
        content: m.content,
      }))
      setMessages(history)
    })
  }, [currentConversationId])

  // 发送消息
  const sendMessage = useCallback(async (content: string, model?: string) => {
    const token = getAccessToken()
    if (!token || loading) return

    // 添加用户消息
    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      content,
    }
    setMessages((prev) => [...prev, userMsg])
    setLoading(true)

    // 添加 AI 占位消息（流式更新）
    const aiMsgId = `ai-${Date.now()}`
    setMessages((prev) => [...prev, { id: aiMsgId, role: 'assistant', content: '' }])

    try {
      abortRef.current = await sendStreamMessage(
        content,
        currentConversationId,
        model || null,
        token,
        {
          onText: (chunk) => {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === aiMsgId ? { ...m, content: m.content + chunk } : m
              )
            )
          },
          onDone: (data) => {
            if (!currentConversationId && data.conversationId) {
              callbackRef.current?.(data.conversationId)
            }
            setCurrentConversationId(data.conversationId)
            setLoading(false)
          },
          onError: (errMsg) => {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === aiMsgId ? { ...m, content: `⚠️ ${errMsg}` } : m
              )
            )
            setLoading(false)
          },
        },
      )
    } catch {
      setMessages((prev) =>
        prev.map((m) =>
          m.id === aiMsgId ? { ...m, content: '⚠️ 请求失败' } : m
        )
      )
      setLoading(false)
    }
  }, [currentConversationId, loading])

  // 停止生成
  const stop = useCallback(() => {
    abortRef.current?.abort()
    setLoading(false)
  }, [])

  // 新建会话（清空消息）
  const newChat = useCallback(() => {
    stop()
    setMessages([])
    setCurrentConversationId(null)
  }, [stop])

  return { messages, loading, currentConversationId, sendMessage, stop, newChat }
}
