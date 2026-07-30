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
