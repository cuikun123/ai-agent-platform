package com.aiagent.platform.controller;

import com.aiagent.platform.common.result.Result;
import com.aiagent.platform.model.LoginData;
import com.aiagent.platform.model.LoginRequest;
import com.aiagent.platform.model.RegisterRequest;
import com.aiagent.platform.service.TokenBlacklistService;
import com.aiagent.platform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenBlacklistService blacklistService;

    public AuthController(UserService userService, TokenBlacklistService blacklistService) {
        this.userService = userService;
        this.blacklistService = blacklistService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.ok();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginData> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(userService.login(request));
    }

    /**
     * 用户登出
     * <p>
     * 将当前 token 加入 Redis 黑名单，使其立即失效
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            blacklistService.blacklistToken(token);
        }
        return Result.ok();
    }
}
