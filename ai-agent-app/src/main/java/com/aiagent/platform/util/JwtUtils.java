package com.aiagent.platform.util;

import com.aiagent.platform.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * <p>
 * 功能：生成 accessToken / refreshToken、解析 token、验证 token
 */
@Component
public class JwtUtils {

    /**
     * token 解析结果，避免多次解析同一个 token
     */
    public record TokenInfo(Long userId, String username, String type) {}

    private final JwtConfig jwtConfig;
    private final SecretKey key;

    public JwtUtils(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 accessToken
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return JWT token 字符串
     */
    public String generateAccessToken(Long userId, String username) {
        return generateToken(userId, username, jwtConfig.getAccessTokenExpiration(), "access");
    }

    /**
     * 生成 refreshToken
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return JWT token 字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        return generateToken(userId, username, jwtConfig.getRefreshTokenExpiration(), "refresh");
    }

    /**
     * 解析 token，返回 Claims
     *
     * @param token JWT token
     * @return Claims，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 验证 token 是否有效
     *
     * @param token JWT token
     * @return true=有效，false=无效或过期
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从 token 中获取用户 ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从 token 中获取 token 类型（access / refresh）
     */
    public String getTokenType(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("type", String.class) : null;
    }

    /**
     * 一次性解析 token，返回完整信息（避免多次 HMAC 计算）
     *
     * @param token JWT token
     * @return TokenInfo，解析失败返回 null
     */
    public TokenInfo parseTokenInfo(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return new TokenInfo(
                claims.get("userId", Long.class),
                claims.getSubject(),
                claims.get("type", String.class)
        );
    }

    /**
     * 通用 token 生成方法
     */
    private String generateToken(Long userId, String username, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}
