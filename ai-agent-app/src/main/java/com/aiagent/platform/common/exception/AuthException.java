package com.aiagent.platform.common.exception;

import com.aiagent.platform.common.result.ErrorCode;

/**
 * 认证异常
 * <p>
 * 用于 token 无效、过期、缺失等认证失败场景
 */
public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
