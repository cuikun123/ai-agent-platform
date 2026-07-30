package com.aiagent.platform.controller;

import com.aiagent.platform.common.result.Result;
import com.aiagent.platform.model.ChatRequest;
import com.aiagent.platform.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话控制器
 * <p>
 * 任务 2：同步对话接口，验证 AI 能回复。
 * 任务 3 会在此基础上扩展 SSE 流式接口。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送消息（同步返回 AI 回复）
     */
    @PostMapping("/send")
    public Result<String> send(@Valid @RequestBody ChatRequest request) {
        String reply = chatService.sendMessage(request.getMessage());
        return Result.ok(reply);
    }
}
