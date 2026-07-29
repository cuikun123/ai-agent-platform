package com.aiagent.platform.service;

import com.aiagent.platform.common.exception.BusinessException;
import com.aiagent.platform.common.result.ErrorCode;
import com.aiagent.platform.entity.User;
import com.aiagent.platform.model.LoginData;
import com.aiagent.platform.model.LoginRequest;
import com.aiagent.platform.model.RegisterRequest;
import com.aiagent.platform.repository.UserMapper;
import com.aiagent.platform.util.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 用户注册
     *
     * @param request 注册参数
     * @throws BusinessException 用户名或邮箱已存在时抛出
     */
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (usernameCount > 0) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED, "用户名已存在");
        }

        // 检查邮箱是否已存在
        long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
        );
        if (emailCount > 0) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED, "邮箱已注册");
        }

        // 构建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus((short) 1);

        // 数据库唯一约束兜底（selectCount 查重存在并发竞态）
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.REGISTER_FAILED, "用户名或邮箱已存在");
        }
    }

    /**
     * 用户登录
     *
     * @param request 登录参数
     * @return LoginData（accessToken + refreshToken）
     * @throws BusinessException 用户名不存在、密码错误、账号禁用时抛出
     */
    public LoginData login(LoginRequest request) {
        // 查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
        }

        // 生成 JWT
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        return LoginData.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
