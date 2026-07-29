# conventions.md — 编码规范

## 命名规范

### Java 后端

- 类名：大驼峰，如 `ChatController`、`UserService`
- 方法名/变量名：小驼峰，如 `sendMessage`、`chatModel`
- 包名：全小写，如 `com.xxx.aiagent.controller`
- 常量：全大写下划线，如 `MAX_TOKEN_LIMIT`、`DEFAULT_MODEL_NAME`

### TypeScript 前端

- 组件文件：大驼峰，如 `ChatPage.tsx`、`Sidebar.tsx`
- 自定义 Hook：use 前缀，如 `useChat`、`useAuth`
- 函数/变量：小驼峰，如 `sendMessage`、`isLoading`
- 常量：全大写下划线，如 `API_BASE_URL`、`MAX_RETRY_COUNT`

### TypeScript 类型

- 接口/类型：大驼峰，如 `interface ChatMessage`、`type ModelType`
- 不用 `I` 前缀

### Java model 层

- 请求体：`RegisterRequest`、`ChatRequest`
- 响应体：`UserVO`、`ConversationVO`
- 不用 `DTO` 后缀

### 数据库

- 表名：`ai_` 前缀 + 下划线分隔，如 `ai_user`、`ai_conversation`
- 字段名：下划线分隔，如 `created_at`、`user_id`

## 分层规范

### 后端

| 层 | 放什么 | 不放什么 |
|----|--------|----------|
| controller | 接收请求、参数校验、调用 service、返回 Result | 不写业务逻辑 |
| service | 业务逻辑、事务管理、调用 repository | 不写 SQL |
| repository（Mapper） | 数据库操作，继承 BaseMapper | 不写业务逻辑 |
| model | Request/Response/VO，纯数据载体 | 不放方法实现 |
| entity | 数据库实体，对应表结构 | 不暴露给前端 |
| config | Spring 配置类 | 不放业务代码 |
| tool | 工具接口 + 实现 | 不调用 service（防循环依赖） |
| agent | Multi-Agent 编排 | — |
| mcp | MCP Client | — |
| common | Result、异常、工具类 | 不放业务代码 |

调用关系：
- controller → service
- service 是核心调度层，可调用 tool、agent、mcp
- tool 层不调用 service（防循环依赖）
- agent 通过 service 完成编排
- mcp 发现的工具注册到 tool，被 service 统一调用

### 前端

| 层 | 放什么 | 不放什么 |
|----|--------|----------|
| pages | 页面组件，组合 components + hooks | 不放通用 UI 逻辑 |
| components | 通用 UI 组件，跨页面复用 | 不放业务状态 |
| hooks | 自定义 Hook，封装可复用逻辑（含 SSE 流式） | 不放 UI 渲染 |
| stores | Zustand 状态管理 | 不放异步请求逻辑 |
| api | 接口请求封装（普通 HTTP） | 不放数据处理 |
| types | 类型定义 | 不放实现 |

## API 规范

### 返回格式

普通接口统一返回 Result<T>：
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

流式接口（对话）使用 SSE，返回 text/event-stream，不套 Result。

### HTTP 方法

- GET — 查询
- POST — 创建 / 操作
- PUT — 全量更新
- DELETE — 删除

### 错误码

- 0：成功
- 1001-1999：认证错误
- 2001-2999：参数/业务错误
- 5001-5999：服务端错误

### 请求参数

- 分页参数统一用 page（页码，从1开始）、size（每页条数，默认10）

## 代码风格

### 注释

- 统一使用中文注释
- 类、接口、公共方法必须写注释（说明职责和用途）
- 复杂逻辑必须写注释说明意图

以下关键功能必须有详细注释：
- SSE 流式对话：事件类型、连接生命周期、异常处理
- Token 管理与上下文截断：策略选择、窗口大小
- RAG 全流程：切片策略、向量化、混合检索权重配置
- 工具调用链：调用决策、执行、结果回传、二次推理
- Multi-Agent 编排：Agent 间上下文传递
- 认证与限流：JWT 生成/刷新/黑名单、令牌桶参数
- 模型切换：ChatModel 抽象层、切换逻辑

自解释代码不写注释：简单 getter/setter、标准 CRUD

## 异常处理规范

### 后端

业务异常自己抛，系统异常交给框架：
- BusinessException：参数校验失败、资源不存在、权限不足、业务规则不满足
- Exception：数据库异常、空指针、第三方服务超时，由全局异常处理器兜底

### 前端

前端统一拦截 API 错误，不写散落的 try-catch：
- 1001-1999（认证）：跳转登录页
- 2001-2999（业务）：提示用户具体错误信息
- 5001-5999（服务端）：提示"服务异常，请稍后重试"
- 网络错误：提示"网络连接失败"
