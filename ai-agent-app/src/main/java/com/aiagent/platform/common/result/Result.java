package com.aiagent.platform.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一返回格式
 *
 * @param <T> 数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 静态工厂方法 ==========

    /** 成功（无数据） */
    public static <T> Result<T> ok() {
        return new Result<>(ErrorCode.SUCCESS, "success", null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS, "success", data);
    }

    /** 失败（错误码 + 消息） */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败（使用默认错误码） */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ErrorCode.BUSINESS_ERROR, message, null);
    }

    // ========== getter / setter ==========

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
