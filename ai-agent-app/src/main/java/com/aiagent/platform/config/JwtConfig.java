package com.aiagent.platform.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * JWT 配置
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /** 签名密钥 */
    private String secret;

    /** accessToken 有效期（毫秒） */
    private long accessTokenExpiration;

    /** refreshToken 有效期（毫秒） */
    private long refreshTokenExpiration;

    /** 启动时校验密钥长度（HMAC-SHA384 至少需要 48 字节） */
    @PostConstruct
    public void validate() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 48) {
            throw new IllegalStateException("jwt.secret 长度不足，HMAC-SHA384 至少需要 48 字节");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
