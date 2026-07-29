package com.aiagent.platform.service;

import com.aiagent.platform.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * <p>
 * 使用 Redis 存储已登出的 token，拦截时检查是否在黑名单中
 */
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;

    public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 将 token 加入黑名单（解析 JWT 获取剩余有效期）
     *
     * @param token JWT token
     */
    public void blacklistToken(String token) {
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) return;

        Date expiration = claims.getExpiration();
        if (expiration == null) return;

        long remaining = expiration.getTime() - System.currentTimeMillis();
        if (remaining > 0) {
            addToBlacklist(token, remaining);
        }
    }

    /**
     * 检查 token 是否在黑名单中
     *
     * @param token JWT token
     * @return true=已加入黑名单（已登出），false=不在黑名单
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 将 token 加入黑名单
     *
     * @param token      JWT token
     * @param expiration 过期时间（毫秒），token 剩余多久就存多久
     */
    private void addToBlacklist(String token, long expiration) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expiration, TimeUnit.MILLISECONDS);
    }
}
