import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button, Dropdown, Message, Modal } from '@arco-design/web-react'
import { IconPlus, IconMore, IconDelete, IconEdit } from '@arco-design/web-react/icon'
import { getConversations, deleteConversation, updateConversation } from '../../../api/conversation'
import type { ConversationData } from '../../../api/conversation'

/** 会话列表组件（替换 MainLayout Sider） */
function ConversationList() {
  const [conversations, setConversations] = useState<ConversationData[]>([])
  const navigate = useNavigate()
  const { conversationId } = useParams()

  const loadConversations = () => {
    getConversations().then((res) => {
      setConversations(res.data)
    })
  }

  useEffect(() => {
    loadConversations()
  }, [])

  // 删除会话
  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '删除后不可恢复，确认删除这个会话吗？',
      onOk: async () => {
        await deleteConversation(id)
        Message.success('已删除')
        loadConversations()
        if (String(id) === conversationId) {
          navigate('/chat')
        }
      },
    })
  }

  // 重命名会话
  const handleRename = (id: number, oldTitle: string) => {
    const newTitle = prompt('请输入新标题', oldTitle || '')
    if (newTitle !== null && newTitle.trim()) {
      updateConversation(id, { title: newTitle.trim() }).then(() => {
        loadConversations()
      })
    }
  }

  // 格式化时间
  const formatTime = (dateStr: string) => {
    const date = new Date(dateStr)
    const now = new Date()
    const isToday = date.toDateString() === now.toDateString()
    if (isToday) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* 新建按钮 */}
      <div style={{ padding: '12px 12px 8px' }}>
        <Button
          long
          type="outline"
          icon={<IconPlus />}
          onClick={() => navigate('/chat')}
          style={{ borderRadius: 8 }}
        >
          新建对话
        </Button>
      </div>

      {/* 会话列表 */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '0 8px' }}>
        {conversations.map((conv) => {
          const isActive = String(conv.id) === conversationId
          return (
            <div
              key={conv.id}
              className="conv-item"
              onClick={() => navigate(`/chat/${conv.id}`)}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '10px 12px',
                marginBottom: 2,
                borderRadius: 8,
                cursor: 'pointer',
                background: isActive ? '#e8f3ff' : 'transparent',
                color: isActive ? '#165DFF' : '#1d2129',
                transition: 'background 0.2s',
              }}
              onMouseEnter={(e) => {
                if (!isActive) e.currentTarget.style.background = '#f2f3f5'
              }}
              onMouseLeave={(e) => {
                if (!isActive) e.currentTarget.style.background = 'transparent'
              }}
            >
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{
                  fontSize: 14,
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace: 'nowrap',
                }}>
                  {conv.title || '新对话'}
                </div>
                <div style={{ fontSize: 12, color: '#86909c', marginTop: 2 }}>
                  {formatTime(conv.updatedAt)}
                </div>
              </div>

              {/* 更多菜单（hover 显示） */}
              <Dropdown
                trigger="click"
                droplist={
                  <div style={{ padding: 4 }}>
                    <div
                      style={{ padding: '6px 12px', cursor: 'pointer', borderRadius: 4, fontSize: 13 }}
                      onClick={(e) => { e.stopPropagation(); handleRename(conv.id, conv.title || '') }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#f2f3f5'}
                      onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                    >
                      <IconEdit style={{ marginRight: 8 }} />重命名
                    </div>
                    <div
                      style={{ padding: '6px 12px', cursor: 'pointer', borderRadius: 4, fontSize: 13, color: '#F53F3F' }}
                      onClick={(e) => { e.stopPropagation(); handleDelete(conv.id) }}
                      onMouseEnter={(e) => e.currentTarget.style.background = '#fff2f0'}
                      onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                    >
                      <IconDelete style={{ marginRight: 8 }} />删除
                    </div>
                  </div>
                }
              >
                <div
                  onClick={(e) => e.stopPropagation()}
                  style={{
                    opacity: 0,
                    padding: 4,
                    borderRadius: 4,
                    cursor: 'pointer',
                    transition: 'opacity 0.2s',
                  }}
                  className="conv-more-btn"
                >
                  <IconMore style={{ fontSize: 14, color: '#86909c' }} />
                </div>
              </Dropdown>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default ConversationList
