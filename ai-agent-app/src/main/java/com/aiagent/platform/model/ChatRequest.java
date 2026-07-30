package com.aiagent.platform.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求体
 */
@Data
public class ChatRequest {

    /** 用户消息 */
    @NotBlank(message = "消息不能为空")
    private String message;
}
