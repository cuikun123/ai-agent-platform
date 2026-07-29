package com.aiagent.platform.common.result;

/**
 * 错误码定义
 * <p>
 * 0：成功
 * 1001-1999：认证错误
 * 2001-2999：参数/业务错误
 * 5001-5999：服务端错误
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    // ========== 成功 ==========
    public static final int SUCCESS = 0;

    // ========== 认证错误 1001-1999 ==========
    public static final int UNAUTHORIZED = 1001;
    public static final int TOKEN_EXPIRED = 1002;
    public static final int TOKEN_INVALID = 1003;
    public static final int ACCOUNT_DISABLED = 1004;
    public static final int LOGIN_FAILED = 1005;
    public static final int REGISTER_FAILED = 1006;

    // ========== 参数/业务错误 2001-2999 ==========
    public static final int BAD_REQUEST = 2001;
    public static final int RESOURCE_NOT_FOUND = 2002;
    public static final int BUSINESS_ERROR = 2003;

    // ========== 服务端错误 5001-5999 ==========
    public static final int INTERNAL_ERROR = 5001;
}
