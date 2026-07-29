# 🔍 代码审查报告：ai-agent-platform

**审查日期**: 2026-07-29
**代码质量评分**: 7.5/10（良好，部分问题已修复）

---

## ✅ 已修复的问题（对比初版审查）

| # | 原问题 | 修复状态 | 修复方式 |
|---|--------|----------|----------|
| 1 | JWT过滤器JSON注入漏洞 | ✅ 已修复 | 改用 `ObjectMapper` + `LinkedHashMap` 安全序列化 |
| 2 | CORS配置过于宽松 | ✅ 已修复 | 限定为 `localhost:3000` / `localhost:3001`，添加 `maxAge(3600)` |
| 3 | JWT密钥硬编码 | ✅ 已修复 | 改为 `${JWT_SECRET:默认值}` 环境变量模式 |
| 4 | 数据库密码硬编码 | ✅ 已修复 | 改为 `${DB_USERNAME:admin}` / `${DB_PASSWORD:admin123}` 环境变量模式 |
| 5 | Redis密码硬编码 | ✅ 已修复 | 改为 `${REDIS_HOST:localhost}` / `${REDIS_PASSWORD:admin123}` 环境变量模式 |
| 6 | logout方法潜在NPE | ✅ 已修复 | 重构为 `TokenBlacklistService.blacklistToken()`，内部做了完整的空值检查 |
| 7 | useAsync类型限制过于严格 | ✅ 已修复 | `never[]` → `unknown[]`，支持带参数的异步函数 |
| 8 | JwtConfig缺少密钥长度校验 | ✅ 已修复 | 添加 `@PostConstruct` 校验 HMAC-SHA384 至少 48 字节 |

---

## ⚠️ 仍然存在的问题

### 🚨 高严重度

#### 1. 路由配置与菜单不匹配（功能缺失）
**文件**: `ai-agent-web/src/App.tsx`
**现状**: MainLayout 菜单包含 `/chat`、`/knowledge`、`/tools`、`/settings`，但 App.tsx 中只定义了 `/`、`/login`、`/register` 三个路由。
**影响**: 用户点击侧边栏菜单项无任何反应，导航完全失效。
**修复建议**: 添加缺失的路由配置或创建对应的占位页面：
```tsx
<Route path="/chat" element={<ChatPage />} />
<Route path="/knowledge" element={<KnowledgePage />} />
<Route path="/tools" element={<ToolsPage />} />
<Route path="/settings" element={<SettingsPage />} />
```

### ⚠️ 中严重度

#### 2. AuthGuard 未验证 Token 过期
**文件**: `ai-agent-web/src/components/AuthGuard.tsx`
**现状**: 仅检查 `getAccessToken()` 是否存在，未解析 JWT 判断是否过期。
**影响**: 过期 token 仍可访问受保护页面，直到后端 API 返回 401 才会跳转。
**修复建议**: 解析 JWT 的 `exp` claim，过期时清除 token 并跳转登录页。

#### 3. 全局异常处理只返回第一个验证错误
**文件**: `ai-agent-app/.../GlobalExceptionHandler.java`
**现状**: `handleValidationException` 使用 `.findFirst()` 只返回第一个字段校验错误。
**影响**: 用户提交表单时只能看到一个错误，需多次提交才能发现所有问题。
**修复建议**: 使用 `.collect(Collectors.joining("; "))` 返回所有错误，或改为返回错误列表。

#### 4. 前端确认密码验证使用 `form.getFieldValue`
**文件**: `ai-agent-web/src/pages/register/RegisterPage.tsx`
**现状**: 虽然已用 `Form.useWatch` 实现了实时匹配提示（`confirmMatch`），但 `rules` 中的 `validator` 仍使用 `form.getFieldValue('password')` 获取密码值。
**影响**: validator 触发时可能获取到旧值，导致验证结果不准确。
**修复建议**: 将 validator 改为使用已通过 `Form.useWatch` 获取的 `password` 变量，或移除 validator（已通过实时提示覆盖）。

#### 5. 错误处理中 token 失效时可能显示重复消息
**文件**: `ai-agent-web/src/api/request.ts`
**现状**: `handleAuthError` 清除 token 并跳转登录页后，外层仍会执行 `Message.error(errData.message)`。
**影响**: token 失效时先弹错误提示，然后跳转，用户体验不佳。
**修复建议**: 在 `handleAuthError` 中返回后跳过 `Message.error`，或添加防重复机制。

#### 6. JWT 缺少 issuer 声明
**文件**: `ai-agent-app/.../util/JwtUtils.java`
**现状**: `generateToken` 方法未设置 `.issuer()`。
**影响**: 不符合 JWT 最佳实践，无法区分 token 来源。
**修复建议**: 添加 `.issuer("ai-agent-platform")`。

### 📋 低严重度

#### 7. Redis 黑名单 key 使用完整 token
**文件**: `ai-agent-app/.../service/TokenBlacklistService.java`
**现状**: `BLACKLIST_PREFIX + token`，JWT 通常 300-500 字符。
**影响**: Redis key 过长，浪费内存。
**修复建议**: 使用 `DigestUtils.md5Hex(token)` 或 `token.hashCode()` 作为 key。

#### 8. API 请求缺少 baseURL 配置
**文件**: `ai-agent-web/src/api/request.ts`
**现状**: `fetch(url, ...)` 使用相对路径。
**影响**: 前后端分离部署时需要手动修改。
**修复建议**: 通过环境变量 `VITE_API_BASE_URL` 配置。

#### 9. 前后端字段命名不一致
**文件**: `RegisterRequest.java` (realName) vs `User.java` (nickname)
**现状**: 前端传 `realName`，后端用 `setNickname(request.getRealName())` 做映射。
**影响**: 字段语义不一致，维护时容易混淆。
**修复建议**: 统一为同一字段名，或添加注释说明映射关系。

#### 10. 密码强度评分未考虑长密码
**文件**: `ai-agent-web/src/hooks/usePasswordStrength.ts`
**现状**: 最高分4分（长度≥8 + 大小写 + 数字 + 特殊字符），不区分16位以上强密码。
**影响**: 密码长度超过16位时无额外强度加分。
**修复建议**: 添加 `if (password.length >= 16) score++` 规则。

#### 11. MainLayout 硬编码内联样式
**文件**: `ai-agent-web/src/components/MainLayout.tsx`
**现状**: 大量 `style={{ ... }}` 内联样式。
**影响**: 不利于主题定制和 CSS 统一管理。
**修复建议**: 使用 CSS 类或 Arco Design 主题变量。

#### 12. Token 存储在 localStorage
**文件**: `ai-agent-web/src/utils/auth.ts`
**现状**: 使用 `localStorage` 存储 JWT token。
**影响**: 容易受到 XSS 攻击窃取。
**修复建议**: 考虑 httpOnly cookie 或 sessionStorage（中等风险，视业务安全要求而定）。

---

## 📊 修复状态总结

| 类别 | 总数 | 已修复 | 待修复 |
|------|------|--------|--------|
| 高严重度 | 5 | 4 | 1（路由缺失） |
| 中严重度 | 8 | 2 | 6 |
| 低严重度 | 6 | 2 | 4 |
| **合计** | **19** | **8** | **11** |

---

## 🎯 下一步修复建议（优先级排序）

1. **立即修复**: 添加缺失的路由配置（`/chat`、`/knowledge`、`/tools`、`/settings`）
2. **尽快修复**: AuthGuard 添加 token 过期检查
3. **尽快修复**: GlobalExceptionHandler 返回所有验证错误
4. **尽快修复**: request.ts 错误处理去重
5. **计划修复**: JWT issuer、Redis key 优化、baseURL 配置

---

## ✅ 项目优点（保持不变）

- 清晰的项目结构（前后端分离）
- 良好的异常处理框架
- 合理的 JWT 认证设计（TokenInfo record 避免重复解析）
- 前后端类型定义基本一致
- 完善的密码强度提示功能
- JwtConfig 启动时校验密钥长度（新增优点）
