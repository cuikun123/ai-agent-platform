package com.aiagent.platform.common.exception;

import com.aiagent.platform.common.result.ErrorCode;
import com.aiagent.platform.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 分层处理：
 * - BusinessException：业务异常，返回具体错误码和消息
 * - MethodArgumentNotValidException：参数校验失败，返回第一个错误
 * - NullPointerException：空指针异常，记录日志，返回友好提示
 * - Exception：兜底，返回 5001，记录日志
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常（@Valid 触发） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        log.warn("参数校验失败：{}", message);
        return Result.fail(ErrorCode.BAD_REQUEST, message);
    }

    /** 空指针异常（代码 bug，记录完整堆栈便于排查） */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "服务异常，请稍后重试");
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "服务异常，请稍后重试");
    }
}
