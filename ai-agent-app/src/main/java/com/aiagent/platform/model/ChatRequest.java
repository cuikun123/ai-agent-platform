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

    /** 会话 ID（为空时自动创建新会话） */
    private Long conversationId;

    /** 模型标识符（为空时使用默认模型） */
    private String model;
}
