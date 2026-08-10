package com.aiagent.platform.controller;

import com.aiagent.platform.common.result.Result;
import com.aiagent.platform.entity.Message;
import com.aiagent.platform.model.ChatRequest;
import com.aiagent.platform.repository.MessageMapper;
import com.aiagent.platform.service.ChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 对话控制器
 * <p>
 * - /api/chat/send：同步对话（快速验证）
 * - /api/chat/stream：SSE 流式对话（正式使用，支持会话持久化）
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public ChatController(ChatService chatService, MessageMapper messageMapper, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 加载对话历史消息
     */
    @GetMapping("/messages")
    public Result<List<Map<String, Object>>> getMessages(@RequestParam Long conversationId) {
        List<Message> messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreatedAt)
        );
        List<Map<String, Object>> result = messages.stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "role", m.getRole(),
                        "content", m.getContent()
                ))
                .toList();
        return Result.ok(result);
    }

    /**
     * 发送消息（同步返回 AI 回复）
     */
    @PostMapping("/send")
    public Result<String> send(@Valid @RequestBody ChatRequest request) {
        String reply = chatService.sendMessage(request.getMessage());
        return Result.ok(reply);
    }

    /**
     * 发送消息（SSE 流式返回）
     * <p>
     * 事件类型：
     * - text：文字片段，前端追加显示
     * - error：错误信息，前端显示提示
     * - done：生成结束，data 中包含 conversationId
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@Valid @RequestBody ChatRequest request,
                                                HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        ChatService.StreamResult result = chatService.streamMessage(
                request.getMessage(), request.getConversationId(), userId, request.getModel()
        );

        Long conversationId = result.conversationId();

        // 文字片段流 → 包装成 text 事件
        Flux<ServerSentEvent<String>> textEvents = result.stream()
                .map(chunk -> ServerSentEvent.builder(chunk)
                        .event("text")
                        .build());

        // 正常结束：done 事件携带 conversationId
        String doneData = toJson(Map.of("conversationId", conversationId));
        return textEvents
                .concatWith(Flux.just(ServerSentEvent.builder(doneData).event("done").build()))
                .onErrorResume(e -> {
                    ServerSentEvent<String> errorEvent = ServerSentEvent.builder(e.getMessage())
                            .event("error")
                            .build();
                    ServerSentEvent<String> doneEvent = ServerSentEvent.builder(doneData)
                            .event("done")
                            .build();
                    return Flux.just(errorEvent, doneEvent);
                });
    }

    /** 安全序列化 JSON，避免异常时崩溃 */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
