import request from './request'
import type { ApiResponse } from '../types/api'

/** 会话数据 */
export interface ConversationData {
  id: number
  userId: number
  title: string | null
  model: string
  createdAt: string
  updatedAt: string
}

/** 获取会话列表 */
export function getConversations(): Promise<ApiResponse<ConversationData[]>> {
  return request('/api/conversations')
}

/** 更新会话（标题、模型） */
export function updateConversation(id: number, data: { title?: string; model?: string }): Promise<ApiResponse<null>> {
  return request(`/api/conversations/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/** 删除会话 */
export function deleteConversation(id: number): Promise<ApiResponse<null>> {
  return request(`/api/conversations/${id}`, {
    method: 'DELETE',
  })
}
