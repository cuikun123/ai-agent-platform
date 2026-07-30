-- ============================
-- 迭代二：对话模块建表脚本
-- 数据库：PostgreSQL
-- ============================

-- 会话表
CREATE TABLE IF NOT EXISTS ai_conversation (
    id          BIGINT       PRIMARY KEY,                    -- 雪花算法主键
    user_id     BIGINT       NOT NULL,                      -- 所属用户
    title       VARCHAR(100),                                -- 会话标题（NULL = 尚未命名）
    model       VARCHAR(50)  NOT NULL DEFAULT 'deepseek-chat', -- 模型标识符
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 会话列表按用户查询，按更新时间倒序
CREATE INDEX idx_conversation_user_updated ON ai_conversation (user_id, updated_at DESC);

-- 消息表
CREATE TABLE IF NOT EXISTS ai_message (
    id              BIGINT    PRIMARY KEY,                    -- 雪花算法主键
    conversation_id BIGINT    NOT NULL,                      -- 所属会话
    role            VARCHAR(20) NOT NULL,                    -- system / user / assistant
    content         TEXT      NOT NULL,                      -- 消息内容
    token_count     INTEGER,                                 -- token 数量（Task 8 填充）
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 按会话查询消息列表，按创建时间正序
CREATE INDEX idx_message_conversation_created ON ai_message (conversation_id, created_at ASC);
