import request from './request'
import type { ApiResponse } from '../types/api'

/** 历史消息 */
export interface HistoryMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
}

/** 加载对话历史消息 */
export function getMessages(conversationId: number): Promise<ApiResponse<HistoryMessage[]>> {
  return request(`/api/chat/messages?conversationId=${conversationId}`)
}

/** SSE 流式回调 */
export interface StreamCallbacks {
  onText: (chunk: string) => void
  onDone: (data: { conversationId: number }) => void
  onError: (message: string) => void
}

/**
 * 发送消息并以 SSE 流式接收回复
 * <p>
 * 使用 fetch + ReadableStream（不用 EventSource，因为 EventSource 不支持 POST + body）
 */
export async function sendStreamMessage(
  message: string,
  conversationId: number | null,
  model: string | null,
  token: string,
  callbacks: StreamCallbacks,
): Promise<AbortController> {
  const controller = new AbortController()

  const body: Record<string, unknown> = { message }
  if (conversationId) body.conversationId = conversationId
  if (model) body.model = model

  const response = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(body),
    signal: controller.signal,
  })

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: '请求失败' }))
    callbacks.onError(error.message || '请求失败')
    return controller
  }

  // 读取 SSE 流
  const reader = response.body!.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  const processBuffer = () => {
    // SSE 事件以 \n\n 分隔
    const events = buffer.split('\n\n')
    buffer = events.pop() || '' // 最后一个可能是不完整的事件

    for (const event of events) {
      if (!event.trim()) continue

      let eventType = ''
      let eventData = ''

      for (const line of event.split('\n')) {
        if (line.startsWith('event:')) {
          eventType = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          eventData = line.slice(5)
        }
      }

      if (eventType === 'text') {
        callbacks.onText(eventData)
      } else if (eventType === 'done') {
        try {
          callbacks.onDone(JSON.parse(eventData))
        } catch {
          callbacks.onDone({ conversationId: 0 })
        }
      } else if (eventType === 'error') {
        callbacks.onError(eventData)
      }
    }
  }

  // 循环读取流数据
  const read = async () => {
    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        processBuffer()
      }
      // 处理 buffer 中剩余数据
      if (buffer.trim()) {
        buffer += '\n\n'
        processBuffer()
      }
    } catch (err: unknown) {
      if (err instanceof Error && err.name !== 'AbortError') {
        callbacks.onError('连接中断')
      }
    }
  }

  read()

  return controller
}
