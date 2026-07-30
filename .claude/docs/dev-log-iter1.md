# dev-log.md — 开发日志

## 2026-07-26

### #1 后端项目骨架 — Maven 多模块 + 目录结构

- **新增文件：**
  - `pom.xml` — 父 POM（Spring Boot 3.4.4 + JDK 21）
  - `ai-agent-app/pom.xml` — 应用模块 POM
  - `ai-agent-app/src/main/java/com/aiagent/platform/AiAgentApplication.java` — 启动类
  - `ai-agent-app/src/main/resources/application.yml` — 基础配置
  - `ai-agent-app/src/main/java/com/aiagent/platform/common/result/Result.java` — 统一返回格式
  - `ai-agent-app/src/main/java/com/aiagent/platform/common/result/ErrorCode.java` — 错误码定义
  - `ai-agent-app/src/main/java/com/aiagent/platform/common/exception/BusinessException.java` — 业务异常
  - `ai-agent-app/src/main/java/com/aiagent/platform/common/exception/GlobalExceptionHandler.java` — 全局异常处理器
  - `.gitignore`
- **依赖变更：**
  - spring-boot-starter-web、spring-boot-starter-actuator、spring-boot-starter-validation
  - spring-boot-devtools、lombok、spring-boot-starter-test
- **关键决策：**
  - 放弃 ai-agent-common 多模块设计，改为单模块 + 包级别分离，避免过度设计
  - common 层（Result、ErrorCode、异常）作为 `ai-agent-app` 内的包，后续有需要再拆
  - 包名：`com.aiagent.platform`，common 层：`com.aiagent.platform.common`
- **验证结果：**
  - `mvn clean install` 编译通过
  - `mvn spring-boot:run -pl ai-agent-app` 启动成功，`/actuator/health` 返回 `{"status":"UP"}`

### #2 数据库建表 — ai_user 表 + MyBatis-Plus 接入 + 数据源配置

- **新增文件：**
  - `ai-agent-app/src/main/resources/application-dev.yml` — 开发环境配置（数据源 + MyBatis-Plus + Redis）
  - `ai-agent-app/src/main/java/com/aiagent/platform/entity/User.java` — 用户实体类
  - `ai-agent-app/src/main/java/com/aiagent/platform/repository/UserMapper.java` — 用户 Mapper
  - `ai-agent-app/src/main/java/com/aiagent/platform/config/MybatisPlusConfig.java` — 分页插件配置
  - `ai-agent-app/src/main/java/com/aiagent/platform/config/WebConfig.java` — CORS 跨域配置
- **数据库变更：**
  - 新建 `ai_user` 表（id、username、password、nickname、email、status、created_at、updated_at）
  - 新建 `ai_agent` 数据库
- **依赖变更：**
  - mybatis-plus-spring-boot3-starter 3.5.9、mybatis-plus-jsqlparser 3.5.9
  - postgresql 驱动、spring-boot-starter-data-redis
- **配置变更：**
  - application.yml 激活 dev profile
  - application-dev.yml 配置 PostgreSQL 数据源 + MyBatis-Plus + Redis
  - 排除 Redis 健康检查（未安装 Redis 时避免 health DOWN）
- **遇到的问题：**
  - MyBatis-Plus 3.5.9 把分页插件拆到了 mybatis-plus-jsqlparser 独立模块，需要额外引入
- **验证结果：**
  - `mvn clean install` 编译通过
  - 启动成功，actuator/health 显示 db: UP，PostgreSQL 连接正常

### #3 前端项目骨架 — Vite + React + TypeScript + Arco Design 初始化

- **新增文件：**
  - `ai-agent-web/` — Vite + React + TypeScript 项目
  - `ai-agent-web/src/components/MainLayout.tsx` — 主布局（侧边栏 + 内容区）
  - `ai-agent-web/src/pages/LoginPage.tsx` — 登录页（占位）
  - `ai-agent-web/src/pages/HomePage.tsx` — 首页（占位）
  - `ai-agent-web/src/main.tsx` — 入口（Arco Design CSS + BrowserRouter）
  - `ai-agent-web/src/App.tsx` — 路由配置（/login、/）
  - `ai-agent-web/vite.config.ts` — 开发服务器 + API 代理（/api → localhost:8080）
- **依赖变更（npm）：**
  - @arco-design/web-react、react-router-dom、zustand
- **配置变更：**
  - vite.config.ts：端口 3000，API 代理到后端 8080
- **遇到的问题：**
  - Arco Design Menu 组件不支持 `items` prop，改用 Menu.Item 子组件写法
- **验证结果：**
  - `npm run build` 编译通过
  - `npm run dev` 启动成功，localhost:3000 返回 200

### #4 注册页 — 表单 UI + 前端校验 + 美化

- **新增文件：**
  - `ai-agent-web/src/styles/theme.css` — Arco 主题定制（蓝色系主色 + 认证页面通用样式）
  - `ai-agent-web/src/pages/RegisterPage.tsx` — 注册页（表单 + 校验 + lucide 图标）
- **修改文件：**
  - `ai-agent-web/src/pages/LoginPage.tsx` — 重写：统一认证页风格 + 加"去注册"链接
  - `ai-agent-web/src/App.tsx` — 添加 /register 路由
  - `ai-agent-web/src/main.tsx` — 引入 theme.css
- **依赖变更（npm）：**
  - lucide-react
- **配置变更：**
  - theme.css：蓝色系主题变量、认证页面布局样式、响应式适配
- **关键决策：**
  - 登录页和注册页统一使用 `auth-page` 布局（左侧品牌区 + 右侧表单）
  - 使用 lucide-react 的 Bot 图标作为品牌标识，后续可替换为正式 Logo
  - 注册页 mock 提交，后续接真实 API
- **验证结果：**
  - `npm run build` 编译通过
  - /register 和 /login 路由均返回 200

### #5 注册页优化（一）— 表单体验 + 按钮交互

- **修改文件：**
  - `ai-agent-web/src/pages/RegisterPage.tsx` — 标题加粗、占位符优化、昵称→真实姓名、安全承诺、按钮样式
  - `ai-agent-web/src/styles/theme.css` — 标题层次样式、渐变按钮、hover 发光、安全承诺样式
- **关键决策：**
  - 标题 font-weight: 700，副标题 13px + #9ca3af，拉开视觉层次
  - 占位符给出具体提示（密码："至少 8 位，含大小写字母及数字"）
  - 按钮用 linear-gradient 渐变 + hover scale(1.02) + box-shadow 发光
  - 安全承诺用 lucide ShieldCheck 图标 + 灰色文案
- **验证结果：**
  - `npm run build` 编译通过

### #6 注册页优化（二）— 视觉效果 + 即时校验

- **修改文件：**
  - `ai-agent-web/src/styles/theme.css` — 品牌区高级感（深色背景 + 3 个渐变光球 + 网格纹理）、卡片顶部渐变线、密码强度条样式、即时校验样式
  - `ai-agent-web/src/pages/RegisterPage.tsx` — 密码强度条（弱/中/强）、确认密码即时校验（✓/✗）、品牌区结构更新
  - `ai-agent-web/src/pages/LoginPage.tsx` — 品牌区同步更新 + 登录按钮改为渐变样式
- **关键决策：**
  - 品牌区用深色背景 (#080b16) + 3 个 CSS 渐变光球（蓝/紫/青）缓慢漂移，配合网格纹理叠加
  - 光球用 `filter: blur()` + `radial-gradient` 实现柔和光晕效果，纯 CSS 无 JS 开销
  - 卡片顶部用 3px 渐变线（蓝→紫→青）区分品牌区和表单区
  - 密码强度评分：长度≥8 + 大小写混合 + 数字 + 特殊字符，4 项打分映射到弱/中/强
  - 确认密码用受控组件 + useState 实现即时比对，不依赖 Form 的 onValuesChange
- **验证结果：**
  - `npm run build` 编译通过

### #7 注册接口 — Controller + 参数校验 + BCrypt 密码加密

- **新增文件：**
  - `ai-agent-app/.../model/RegisterRequest.java` — 注册请求参数（含校验注解）
  - `ai-agent-app/.../service/UserService.java` — 用户服务（注册逻辑：查重 + BCrypt + 入库）
  - `ai-agent-app/.../controller/AuthController.java` — 认证控制器（POST /api/auth/register）
  - `ai-agent-app/.../config/PasswordConfig.java` — BCryptPasswordEncoder Bean
- **依赖变更：**
  - spring-security-crypto（只引密码编码器，不引整个 Security 框架）
- **接口变更：**
  - `POST /api/auth/register` — 用户注册，返回 Result<Void>
- **关键决策：**
  - 单独引 spring-security-crypto 而非整个 spring-boot-starter-security，避免 Security 框架自动拦截请求
  - 用户名和邮箱查重用 MyBatis-Plus LambdaQueryWrapper
  - 密码用 BCryptPasswordEncoder 加密，明文密码不落库
- **验证结果：**
  - `mvn clean install` 编译通过
  - 注册成功：`{"code":0,"message":"success"}`
  - 用户名重复：`{"code":1006,"message":"用户名已存在"}`
  - 参数校验失败：`{"code":2001,"message":"realName: 真实姓名不能为空"}`
  - 数据库密码为 BCrypt 密文 `$2a$10$...`

### #7 补充 — 前端接入 API + 目录重构 + 表单修复

- **新增文件：**
  - `ai-agent-web/src/types/api.ts` — API 类型定义（ApiResponse、RegisterRequest、LoginRequest、LoginData）
  - `ai-agent-web/src/api/request.ts` — 全局请求方法（统一响应解析、错误拦截、Message 提示）
  - `ai-agent-web/src/hooks/useAsync.ts` — 通用异步请求 hook（loading 状态管理）
  - `ai-agent-web/src/hooks/usePasswordStrength.ts` — 密码强度计算
  - `ai-agent-web/src/pages/register/RegisterPage.tsx` — 注册页（重组后）
  - `ai-agent-web/src/pages/register/components/PasswordStrengthBar.tsx` — 密码强度条组件
- **删除文件：**
  - `ai-agent-web/src/pages/RegisterPage.tsx` — 移动到 pages/register/ 目录
- **修改文件：**
  - `ai-agent-web/src/api/auth.ts` — 改用全局 request，类型引用 types/api.ts
  - `ai-agent-web/src/pages/LoginPage.tsx` — 表单加 label 标题
  - `ai-agent-web/src/App.tsx` — 更新 RegisterPage import 路径
- **遇到的问题：**
  - Input.Password 被 div 包裹后 Arco Form 无法自动绑定值，导致"请确认密码"误报 → 改用 Form.useWatch 获取值，强度条移到 FormItem 外部
- **关键决策：**
  - 前端 hooks 统一放 hooks/ 目录，pages/ 下只放页面组件和专属 UI 组件（简洁项目适用）
  - 全局 request.ts 统一处理错误拦截（认证错误跳登录、业务错误 Message 提示），页面层不写 try-catch 错误处理
  - 类型定义统一放 types/api.ts，api/ 层只写接口调用

### #7 补充 — Arco Design React 18 兼容 + 表单 label

- **修改文件：**
  - `ai-agent-web/src/main.tsx` — 调用 `setCreateRoot(createRoot)` 解决 Message/Notification 在 React 18 下报错
  - `ai-agent-web/src/pages/register/RegisterPage.tsx` — 表单字段加 label（用户名、真实姓名、邮箱、密码、确认密码）
  - `ai-agent-web/src/pages/LoginPage.tsx` — 表单字段加 label（用户名、密码）
- **遇到的问题：**
  - `Message.success/error` 报错 `CopyReactDOM.render is not a function` — Arco 内部从 react-dom 导入，React 18 的 createRoot 在 react-dom/client 中
- **解决方案：**
  - 使用 Arco 官方提供的 `setCreateRoot` 方法注入 createRoot（`import { setCreateRoot } from '@arco-design/web-react/es/_util/react-dom'`）
  - 不再用补丁 ReactDOM.render，无技术债
- **验证结果：**
  - 注册成功提示正常弹出，`npm run build` 编译通过

---

### #8 登录接口 — 密码验证 + JWT 生成 + 前后端联通

- **新增文件：**
  - `ai-agent-app/.../model/LoginRequest.java` — 登录请求参数
  - `ai-agent-app/.../model/LoginData.java` — 登录响应（accessToken + refreshToken）
  - `ai-agent-app/.../config/JwtConfig.java` — JWT 配置类（绑定 jwt.* 配置项）
  - `ai-agent-app/.../util/JwtUtils.java` — JWT 工具类（生成/解析/验证 token）
  - `ai-agent-web/src/utils/auth.ts` — 前端 token 存取工具（localStorage）
- **修改文件：**
  - `ai-agent-app/.../service/UserService.java` — 新增 login() 方法
  - `ai-agent-app/.../controller/AuthController.java` — 新增 POST /api/auth/login
  - `ai-agent-web/src/pages/LoginPage.tsx` — 接入真实 API + 存 token + 跳转首页
  - `ai-agent-web/src/api/request.ts` — 自动携带 Authorization: Bearer token
- **依赖变更：**
  - jjwt-api + jjwt-impl + jjwt-jackson 0.12.6（JWT 生成和解析）
- **配置变更：**
  - application-dev.yml 新增 jwt.* 配置（密钥、accessToken 2h、refreshToken 7d）
- **接口变更：**
  - `POST /api/auth/login` — 登录，返回 Result<LoginData>
- **关键决策：**
  - jjwt 库选型：0.12.6 版本，支持 HMAC-SHA384
  - JWT 密钥放 application-dev.yml，不硬编码（CLAUDE.md 红线）
  - 用户名不存在和密码错误统一返回"用户名或密码错误"，不区分（安全考虑）
  - 前端 token 存 localStorage，request.ts 自动读取并注入 Authorization 头
  - 认证错误（1001-1999）自动清除 token 并跳转登录页
- **验证结果：**
  - 后端编译通过，前端编译通过
  - 登录成功：返回 accessToken + refreshToken
  - 密码错误：`{"code":1005,"message":"用户名或密码错误"}`
  - 用户不存在：同上

### #9 JWT 认证拦截器 + Redis 黑名单登出

- **新增文件：**
  - `ai-agent-app/.../filter/JwtAuthenticationFilter.java` — JWT 认证拦截器（OncePerRequestFilter）
  - `ai-agent-app/.../service/TokenBlacklistService.java` — Redis 黑名单服务
  - `ai-agent-app/.../common/exception/AuthException.java` — 认证异常
- **修改文件：**
  - `ai-agent-app/.../controller/AuthController.java` — 新增 POST /api/auth/logout
  - `ai-agent-app/src/main/resources/application-dev.yml` — Redis 加密码配置
- **接口变更：**
  - `POST /api/auth/logout` — 登出，token 加入 Redis 黑名单
- **关键决策：**
  - 拦截器用 OncePerRequestFilter，保证每个请求只拦截一次
  - 白名单：login、register、actuator（不拦截）
  - logout 不在白名单，需要验证 token 后才能登出（登出后 token 立即失效）
  - 黑名单存 Redis，key = `token:blacklist:{token}`，过期时间 = token 剩余有效期
  - Redis 密码配置在 application-dev.yml（admin123），不硬编码
- **遇到的问题：**
  - Redis 容器有密码（admin123），application-dev.yml 未配置导致连接失败 → 补上 password 配置
  - logout 放白名单导致已登出 token 仍能访问 → 移出白名单，让拦截器检查黑名单
- **验证结果：**
  - 登录获取 token → 带 token 登出 → token 加入黑名单 → 再次使用返回 401
  - `{"code":1001,"message":"认证令牌已失效，请重新登录"}`

### #9 补充 — 前端路由守卫

- **新增文件：**
  - `ai-agent-web/src/components/AuthGuard.tsx` — 路由守卫组件（检查 localStorage token，无则跳转 /login）
- **修改文件：**
  - `ai-agent-web/src/App.tsx` — MainLayout 路由包裹 AuthGuard
- **关键决策：**
  - AuthGuard 放在 components/ 目录（跨页面复用的通用组件）
  - 只保护需要登录的路由（/），login 和 register 不包裹
  - 与后端 JwtAuthenticationFilter 配合形成完整认证链路：前端守卫拦截页面访问，后端拦截器拦截 API 请求

### 代码审查修复（codex review 建议采纳）

- **修改文件：**
  - `JwtUtils.java` — 新增 `TokenInfo` record + `parseTokenInfo()` 一次性解析方法
  - `JwtAuthenticationFilter.java` — 改用 `parseTokenInfo()`，3 次 HMAC 计算降为 1 次
  - `UserService.java` — insert 用 `try-catch DuplicateKeyException` 兜底并发竞态
  - `User.java` — createdAt/updatedAt 加 `@TableField(fill=...)` 自动填充注解
  - `JwtConfig.java` — 加 `@PostConstruct` 密钥长度校验
  - `request.ts` — HTTP 非 200 时读取响应体 JSON 错误信息
  - `auth.ts` — register 加泛型 `ApiResponse<null>`
  - `application-dev.yml` — Redis 密码改为 `${REDIS_PASSWORD:admin123}` 环境变量
  - `LoginPage.tsx` — 改用 AuthLayout 组件
  - `RegisterPage.tsx` — 改用 AuthLayout 组件
- **新增文件：**
  - `AutoFillHandler.java` — MyBatis-Plus 自动填充处理器
  - `AuthLayout.tsx` — 认证页面共用布局组件（品牌区 + 表单区）
- **数据库变更：**
  - `ai_user` 表加 `uk_username`、`uk_email` 唯一约束
- **关键决策：**
  - JwtUtils 保留 validateToken/getUserId/getTokenType 方法供其他场景使用，Filter 层用 parseTokenInfo 一次解析
  - selectCount 查重保留为快速路径（错误信息更具体），DuplicateKeyException 作为并发兜底
  - AuthLayout footer 用 props 传入，登录页和注册页各自定义底部链接

### 迭代一验收问题修复（codex + 自检发现）

- **修改文件：**
  - `JwtAuthenticationFilter.java` — writeUnauthorized 改用 ObjectMapper 序列化，避免 JSON 注入
  - `WebConfig.java` — CORS 限定 localhost:3000/3001，不再允许所有来源
  - `application-dev.yml` — PostgreSQL 密码 `${DB_PASSWORD:admin123}`、JWT 密钥 `${JWT_SECRET:...}`、Redis host/port 也改为环境变量
  - `AuthController.java` — logout 方法去掉 JWT 解析逻辑，改调 blacklistService.blacklistToken()
  - `TokenBlacklistService.java` — 新增 blacklistToken() 方法，内部解析 JWT 获取 TTL，null 安全
  - `GlobalExceptionHandler.java` — 新增 NullPointerException 单独处理，记录完整堆栈
  - `useAsync.ts` — 泛型约束 `never[]` 改为 `unknown[]`
  - `request.ts` — 提取 handleAuthError() 函数，避免 token 错误重复弹 Message
- **修复问题：**
  - JSON 注入漏洞（String.format → ObjectMapper）
  - CORS 过于宽松（* → localhost）
  - YAML 硬编码凭证（改环境变量）
  - logout NPE 风险（claims.getExpiration() null 检查）
  - useAsync 类型限制过严（never[] → unknown[]）
  - token 错误重复提示（提取公共函数）
  - NPE 无日志（单独 handler 记录堆栈）
- **验证结果：**
  - 后端编译通过，前端编译通过

### 字段命名统一（realName → nickname）

- **修改文件：**
  - `RegisterRequest.java` — realName → nickname
  - `UserService.java` — request.getRealName() → request.getNickname()
  - `types/api.ts` — RegisterRequest.realName → nickname
  - `RegisterPage.tsx` — 表单字段 realName → nickname，register 调用参数同步更新
- **原因：** 前端用 realName，后端实体用 nickname，前后端字段名不一致
- **验证结果：** 后端编译通过，前端编译通过，全局 grep 无遗漏

---

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
