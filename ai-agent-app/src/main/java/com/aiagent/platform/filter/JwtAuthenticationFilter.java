package com.aiagent.platform.filter;

import com.aiagent.platform.service.TokenBlacklistService;
import com.aiagent.platform.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证拦截器
 * <p>
 * 工作流程：
 * 1. 检查请求路径是否在白名单中，是则放行
 * 2. 从 Authorization 头获取 token
 * 3. 验证 token 有效性（签名、过期）
 * 4. 检查 token 是否在 Redis 黑名单中
 * 5. 将用户信息存入 request attribute，供后续使用
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** 不需要认证的路径 */
    private static final List<String> WHITE_LIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/chat/send",
            "/actuator/**"
    );

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService blacklistService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtils jwtUtils, TokenBlacklistService blacklistService, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.blacklistService = blacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 白名单放行
        if (isWhiteListed(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未提供认证令牌");
            return;
        }

        String token = authHeader.substring(7);

        // 一次性解析 token（避免 3 次重复 HMAC 计算）
        JwtUtils.TokenInfo tokenInfo = jwtUtils.parseTokenInfo(token);
        if (tokenInfo == null) {
            writeUnauthorized(response, "认证令牌无效或已过期");
            return;
        }

        // 检查 token 类型（必须是 accessToken）
        if (!"access".equals(tokenInfo.type())) {
            writeUnauthorized(response, "认证令牌类型错误");
            return;
        }

        // 检查 Redis 黑名单
        if (blacklistService.isBlacklisted(token)) {
            writeUnauthorized(response, "认证令牌已失效，请重新登录");
            return;
        }

        // 将用户信息存入 request attribute
        request.setAttribute("userId", tokenInfo.userId());

        filterChain.doFilter(request, response);
    }

    /** 判断路径是否在白名单中 */
    private boolean isWhiteListed(String path) {
        return WHITE_LIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /** 返回 401 响应（用 ObjectMapper 安全序列化，避免 JSON 注入） */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 1001);
        body.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
