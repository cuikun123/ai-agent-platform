# CLAUDE.md

## 项目背景

Enterprise AI Agent Platform — 企业级 AI Agent 平台，为企业提供统一的 AI Agent 构建与管理能力。

**目标用户：** 企业员工，通过平台与 AI Agent 对话、管理知识库、调用工具完成日常工作

**核心能力：** 流式对话、RAG 知识库、Function Calling 工具系统、MCP 协议、Multi-Agent 编排

**核心策略：** 深度 > 广度，对话模块、RAG 知识库、工具系统做到极致，其余讲清设计即可。

## 硬性约束

技术栈锁定，不变更：
- 后端：Spring Boot 3.4 + Spring AI 1.0 + JDK 21 + MyBatis-Plus + PostgreSQL/PGVector + Redis
- 前端：React 18 + TypeScript + Vite + Arco Design + Zustand

## 开发红线

- 禁止硬编码敏感信息：密钥、密码、Token 必须走配置或环境变量
- 修改已有文件前，必须先说明要改什么、为什么改，用户确认后再动手
- 禁止变更技术栈或引入新依赖，用户明确要求除外
- 禁止使用未经验证的 API：必须确认方法、类、参数真实存在，不确定时查文档验证
- 只做被要求的事，不做额外改动
- 不确定时必须向用户确认，禁止猜测后直接实现

## 文档指引

| 需要什么 | 看哪里 |
|----------|--------|
| 编码规范 | `docs/conventions.md` |
| 技术踩坑点 | `docs/tech-patterns.md` |
| 架构参考 | `docs/architecture.md` |
| 迭代任务清单 | `docs/plan.md` |
| 当前迭代进度 | `docs/state.md` |
| 历史开发记录 | `docs/dev-log.md` |

## 命令

- `/plan` — 迭代拆任务
- `/build` — 日常开发
- `/review` — 质量验收
- `/status` — 查看进度
- `/context` — 切换迭代
