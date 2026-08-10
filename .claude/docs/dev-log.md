# dev-log.md — 开发日志

> 迭代一开发记录已归档至 [dev-log-iter1.md](dev-log-iter1.md)

---

### [任务名称]

- **新增文件：** 文件列表
- **修改文件：** 文件列表
- **数据库变更：** 新增/修改了哪些表或字段
- **接口变更：** 新增/修改了哪些 API 接口
- **依赖变更：** 新增了哪些 Maven/npm 依赖
- **配置变更：** 修改了哪些配置项
- **关键决策：** 做了什么决策、为什么
- **遇到的问题：** 问题是什么、怎么解决的
- **遗留事项：** 还有什么没做完、技术债
- **验证结果：** 怎么验证通过的

（没有涉及的分类不用写）

---

### 任务 1：数据库建表 + 实体 + Mapper

- **新增文件：**
  - `ai-agent-app/src/main/resources/sql/init.sql` — 建表 SQL
  - `entity/Conversation.java` — 会话实体
  - `entity/Message.java` — 消息实体
  - `repository/ConversationMapper.java` — 会话 Mapper
  - `repository/MessageMapper.java` — 消息 Mapper
- **数据库变更：**
  - 新增 `ai_conversation` 表（id, user_id, title, model, created_at, updated_at）
  - 新增 `ai_message` 表（id, conversation_id, role, content, token_count, created_at）
  - 索引：`idx_conversation_user_updated`、`idx_message_conversation_created`
- **关键决策：**
  - 主键统一 Long 雪花算法，跟 ai_user 一致
  - title 允许 NULL，后续 Task 4 自动截取生成
  - model 存字符串标识符（VARCHAR(50)），灵活不耦合
  - role 存字符串（VARCHAR(20)），直接对应 Spring AI 的 Message 类型
  - content 用 PostgreSQL TEXT 类型，无长度限制
  - token_count 预留字段（INTEGER, NULL），Task 8 填充
  - SQL 手动执行，不用 Flyway

---

### 任务 2：Spring AI 接入 DeepSeek

- **新增文件：**
  - `config/ChatConfig.java` — ChatClient Bean，注入默认 System Prompt
  - `service/ChatService.java` — 对话服务，调用 ChatClient
  - `controller/ChatController.java` — POST /api/chat/send 同步接口
  - `model/ChatRequest.java` — 请求体（message 字段，@NotBlank 校验）
- **修改文件：**
  - `pom.xml`（父） — 新增 Spring AI BOM（1.0.0）
  - `ai-agent-app/pom.xml` — 新增 spring-ai-openai-spring-boot-starter 依赖
  - `application-dev.yml` — 新增 spring.ai.openai 配置（base-url、api-key 占位符、model）+ ai.chat.system-prompt 配置
- **接口变更：**
  - 新增 `POST /api/chat/send` — 同步对话，请求 `{ "message": "..." }`，返回 `Result<String>`
- **依赖变更：**
  - `spring-ai-starter-model-deepseek`（通过 Spring AI BOM 1.1.8 管理版本）
- **关键决策：**
  - 用 spring-ai-starter-model-deepseek（DeepSeek 专用 starter，非 OpenAI starter）
  - 配置前缀 `spring.ai.deepseek.*`（非 `spring.ai.openai.*`）
  - API Key 走占位符 `${DEEPSEEK_API_KEY}`，从环境变量读取
  - System Prompt 可配置，放 application-dev.yml，不硬编码
  - 用 ChatClient（不用 ChatModel），符合 tech-patterns.md 规范
  - 同步接口用 Result<String>，流式接口（任务 3）再换 SSE
  - 接口暂不加认证，任务 2 目标是验证 AI 能回复

---

### 任务 3：SSE 流式对话接口

- **修改文件：**
  - `service/ChatService.java` — 新增 `streamMessage()` 方法，返回 `Flux<String>`
  - `controller/ChatController.java` — 新增 `POST /api/chat/stream`，返回 `Flux<ServerSentEvent<String>>`
  - `filter/JwtAuthenticationFilter.java` — 白名单加 `/api/chat/stream`
- **接口变更：**
  - 新增 `POST /api/chat/stream` — SSE 流式对话，请求 `{ "message": "..." }`，返回 `text/event-stream`
- **关键决策：**
  - 新增流式接口，保留同步接口（方便对比测试）
  - 3 种事件类型：text（文字片段）、error（错误信息）、done（生成结束）
  - 事件格式：JSON — `{"type":"text","content":"..."}`
  - Service 层返回原始 Flux<String>，Controller 层包装成 SSE 事件（职责分离）
  - 错误处理：onErrorResume 捕获异常，推 error 事件 + done 事件
  - 正常结束时也推 done 事件（前端明确知道生成结束）

---

### 任务 4：消息持久化

- **新增文件：**
  - `memory/DatabaseChatMemoryRepository.java` — ChatMemoryRepository 实现，对接 ai_message 表
- **修改文件：**
  - `model/ChatRequest.java` — 新增 conversationId 可选字段
  - `config/ChatConfig.java` — 新增 ChatMemory Bean（MessageWindowChatMemory）+ MessageChatMemoryAdvisor
  - `service/ChatService.java` — streamMessage 改为接收 conversationId，自动创建会话 + 标题生成
  - `controller/ChatController.java` — 流式接口传 conversationId，done 事件携带 conversationId
- **关键决策：**
  - 用 Spring AI ChatMemory Advisor 管理历史，不用手动拼接消息
  - 实现 ChatMemoryRepository（而非 ChatMemory），由 MessageWindowChatMemory 包装
  - 流式过程中消息由 ChatMemory 自动管理，流式结束后自动持久化
  - 会话自动创建：conversationId 为空时自动新建，标题截取前 20 字
  - done 事件携带 conversationId，前端可更新会话列表

---

### 任务 5：前端对话页 UI

- **新增文件：**
  - `pages/chat/ChatPage.tsx` — 对话主页面（全屏风格，mock 数据）
  - `pages/chat/components/MessageList.tsx` — 消息列表（自动滚动 + 空状态）
  - `pages/chat/components/MessageBubble.tsx` — 消息气泡（左对齐 + Markdown 渲染 + 代码高亮）
  - `pages/chat/components/ChatInput.tsx` — 输入框（Enter 发送 + Shift+Enter 换行）
- **修改文件：**
  - `App.tsx` — 新增 /chat 路由
  - `components/MainLayout.tsx` — Content 区域去掉 padding（全屏对话风格）
  - `pages/HomePage.tsx` — 自行加 padding（适配 Content 样式变更）
  - `index.css` — 新增 Markdown 消息内容样式
- **依赖变更：**
  - `react-markdown` — Markdown 渲染
  - `remark-gfm` — GitHub 扩展语法支持
  - `react-syntax-highlighter` — 代码块语法高亮
  - `@types/react-syntax-highlighter` — TypeScript 类型
- **关键决策：**
  - 全屏对话风格，铺满内容区（对标 ChatGPT）
  - 消息左对齐，通过头像颜色和名称区分用户/AI
  - Markdown 一步到位：react-markdown + remark-gfm + 语法高亮
  - Enter 发送，Shift+Enter 换行（对话场景标准行为）
  - 当前用 mock 数据，任务 6 接入真实 SSE

---

### 任务 6：前端 SSE 接入 + 历史加载

- **新增文件：**
  - `api/chat.ts` — SSE 流式请求函数（fetch + ReadableStream）+ 历史加载 API
  - `hooks/useChatStream.ts` — 对话 hook：SSE 流式 + 消息状态管理 + 历史加载
- **修改文件：**
  - `pages/chat/ChatPage.tsx` — 替换 mock 数据，使用 useChatStream hook
  - `controller/ChatController.java` — 新增 GET /api/chat/messages 接口
- **接口变更：**
  - 新增 `GET /api/chat/messages?conversationId=xxx` — 加载对话历史消息
- **关键决策：**
  - SSE 客户端用 fetch + ReadableStream（不用 EventSource，因为需要 POST + body）
  - 用 useChatStream hook 封装 SSE + 状态管理（遵循 conventions.md hooks 规范）
  - 流式过程中先添加 AI 占位消息，逐字更新内容
  - 历史消息从后端 GET /api/chat/messages 加载
  - hook 暴露 sendMessage、stop、newChat 方法

---

### 任务 7：会话管理 + 多模型切换

- **新增文件：**
  - `controller/ConversationController.java` — 会话管理 API（GET/PUT/DELETE）
  - `api/conversation.ts` — 会话 API 封装
  - `pages/chat/components/ConversationList.tsx` — 会话列表组件（标题 + 时间 + 三个点菜单）
  - `pages/chat/components/ModelSelector.tsx` — 模型选择下拉框
- **修改文件：**
  - `model/ChatRequest.java` — 新增 model 字段
  - `service/ChatService.java` — streamMessage 接收 model 参数，创建会话时存 model
  - `controller/ChatController.java` — 传递 model 给 service
  - `components/MainLayout.tsx` — Sider 替换为会话列表（DeepSeek 风格）
  - `pages/chat/ChatPage.tsx` — 集成 ModelSelector，传递 model
  - `hooks/useChatStream.ts` — sendMessage 接收 model 参数
  - `api/chat.ts` — sendStreamMessage 接收 model 参数
  - `index.css` — 会话列表 hover 样式
- **接口变更：**
  - 新增 `GET /api/conversations` — 获取用户会话列表（按更新时间倒序）
  - 新增 `PUT /api/conversations/:id` — 更新会话（标题、模型）
  - 新增 `DELETE /api/conversations/:id` — 删除会话
- **关键决策：**
  - Sider 替换为会话列表（对标 DeepSeek 左侧导航）
  - 三个点菜单：重命名 + 删除（hover 显示）
  - 模型选择器在输入框上方，支持 deepseek-chat / deepseek-coder
  - "新建会话"复用流式接口（conversationId 为空自动创建）
  - 删除会话有确认弹窗

---

### 任务 8：Token 计算与上下文截断

- **新增文件：**
  - `config/TokenTruncationAdvisor.java` — Token 截断 Advisor，基于 BaseAdvisor 实现
- **修改文件：**
  - `application-dev.yml` — 新增 `ai.chat.max-history-tokens` 配置项（默认 40000）
  - `config/ChatConfig.java` — Advisor 链加入 TokenTruncationAdvisor
- **关键决策：**
  - Token 粗略估算：每 3 字符约 1 token（偏保守，安全余量足够）
  - 截断策略：保留 System Message + 从最新消息往前累加，超限丢弃旧消息
  - 实现为 Advisor（而非在 Repository 层截断）：职责分离、可复用、符合 Spring AI 规范
  - Token 上限可配置：开发时可调小方便测试，生产时调大
  - Advisor 链顺序：MessageChatMemoryAdvisor → TokenTruncationAdvisor → LLM
