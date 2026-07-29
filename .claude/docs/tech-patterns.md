# tech-patterns.md — 技术踩坑点

## Spring AI

**1. 用 ChatClient，不用 ChatModel**
- ChatModel 是低层 API，ChatClient 是推荐入口
- AI 容易默认用 ChatModel，实际应该用 ChatClient.Builder 构建

**2. 横切关注点走 Advisor 链，不要手动处理**
- 对话记忆、RAG 检索、日志都通过 Advisor 注入
- 不要在 service 里自己拼接历史消息、自己调向量检索

**3. ChatMemory 必须传 conversationId**
- 每次调用必须 `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))`
- 不传的话记忆不生效，且不会报错，很难排查

**4. 工具注册用 @Tool 注解，不要自造机制**
- Spring AI 原生支持 @Tool，自动扫描和 JSON Schema 生成
- 不要自己写 Tool 接口 + ToolRegistry，那是重复造轮子

**5. 需要 Java 对象时用 .entity()，不要手动解析 JSON**
- `chatClient.prompt().user(msg).call().entity(MyPojo.class)`
- 自己用 Jackson 解析 LLM 返回的 JSON 容易出格式问题

**6. DeepSeek 用 OpenAI 协议接入，base-url 必须改**
- 依赖：`spring-ai-openai-spring-boot-starter`
- base-url 必须设为 `https://api.deepseek.com`，不能用 OpenAI 默认地址

## SSE 流式

**1. 流式接口返回 Flux<String>，不能用普通返回**
- controller 方法返回 `Flux<String>`，配合 `produces = MediaType.TEXT_EVENT_STREAM_VALUE`
- 不能返回 `Result<Flux<String>>`，SSE 不套 Result

**2. 事件类型要约定，前端按类型分别处理**
- `text`：文字片段，追加显示
- `tool_call`：工具调用开始
- `tool_result`：工具返回结果
- `error`：错误信息
- `done`：生成结束

**3. 客户端断连必须清理资源**
- 用户关闭页面或网络断开，服务端要感知到并停止推流
- 不清理会导致服务端线程泄漏，LLM 还在生成但没人接收

**4. 流式过程中的异常不能吞掉**
- LLM 调用超时、限流等异常，要通过 SSE 推送 error 事件给前端
- 不能静默失败，前端会一直显示"生成中"

## PGVector

（待补充，等学完 PGVector 后填入踩坑点）

## JWT + Redis

**1. access_token 和 refresh_token 要分开存、分开校验**
- access_token：短效（2h），每次请求携带，用于身份验证
- refresh_token：长效（7d），只在 token 过期时用来换新 token
- 不能用同一个 token 同时做两件事

**2. 登出用 Redis 黑名单，不能只靠 token 过期**
- JWT 本身无法主动失效，签发了就一直有效到过期
- 登出时把 token 放入 Redis 黑名单，设置过期时间和 token 一致
- 每次请求先查黑名单，命中则拒绝

**3. refresh_token 必须和用户绑定，不能只存 token 本身**
- 用户修改密码后，应该让所有 refresh_token 失效
- Redis 存储结构：`refresh_token:{userId} → token`
- 修改密码时删除该 key，所有设备的 token 全部失效

**4. 密钥不能硬编码，必须走配置**
- JWT 签名密钥放 application.yml 或环境变量
- 这是 CLAUDE.md 红线的要求，AI 容易写成 `private static final String SECRET = "xxx"`

## 前端 SSE

**1. EventSource 只支持 GET，对话要用 fetch + ReadableStream**
- EventSource 是 GET 请求，无法传 JSON body（对话消息、conversationId 等）
- 对话流式接口应该用 `fetch` + `ReadableStream` 读取
- AI 容易默认用 EventSource，发现传参不了再改

**2. 流式数据是分块到达的，要处理"半条消息"**
- SSE 事件可能被 TCP 拆成多个包，一次 read 可能只收到半个 JSON
- 需要按换行符分割，攒够一条完整事件再处理
- 不能直接 JSON.parse(readData)，会报错

**3. 组件卸载时必须中断请求**
- 用户切换页面或关闭对话，必须中断正在进行的 fetch 请求
- 不中断会导致：后台还在推流浪费资源、setState 报内存泄漏
- 同时要在 cleanup 里通知后端停止生成

**4. 停止生成 = 中断连接 + 通知后端**
- 前端：中断 fetch 请求
- 后端：感知到连接断开后停止调用 LLM，否则模型还在生成但没人接收

## Zustand

**1. Zustand 用法要简洁，不要过度设计**
- 直接 create + set，不要搞 reducer、action type、middleware 那套
- store 方法直接调用 api/ 层拿到数据再 set
- AI 容易按 Redux 习惯把简单问题复杂化

**2. 不要滥用全局 store，能用局部状态就用局部状态**
- 只有跨页面共享的状态才放 Zustand（用户信息、当前会话）
- 单个组件内部的状态（输入框内容、弹窗开关）用 useState
- AI 容易把所有状态都塞进全局 store，导致不必要的重渲染

**3. store 按功能拆分，不要建一个大 store**
- `useUserStore` — 用户信息、登录状态、Token
- `useChatStore` — 会话列表、当前会话、消息列表、是否流式输出、当前模型
- `useKnowledgeStore` — 知识库相关
- 不要建一个 `useAppStore` 把所有东西放进去
- 模型选择放 chatStore，不是用户偏好而是每次对话选择的

## Arco Design + React 18

**1. Message/Notification 在 React 18 下报错 `ReactDOM.render is not a function`**
- Arco 内部从 `react-dom` 导入，但 React 18 的 `createRoot` 在 `react-dom/client` 中
- 解决：在 main.tsx 中调用 `setCreateRoot(createRoot)` 注入，这是 Arco 官方提供的适配方法
- `import { setCreateRoot } from '@arco-design/web-react/es/_util/react-dom'`
