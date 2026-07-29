package com.aiagent.platform.common.exception;

/**
 * 业务异常
 * <p>
 * 用于参数校验失败、资源不存在、权限不足、业务规则不满足等场景。
 * 系统级异常（数据库异常、空指针等）不使用此类，交给全局异常处理器兜底。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 2003; // 默认业务错误码
    }

    public int getCode() {
        return code;
    }
}
