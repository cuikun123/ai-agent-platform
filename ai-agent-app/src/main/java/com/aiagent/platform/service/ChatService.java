package com.aiagent.platform.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * 对话服务
 * <p>
 * 调用 ChatClient 完成单轮对话，任务 2 为同步验证，后续任务叠加流式和持久化。
 */
@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 发送消息并同步返回 AI 回复
     *
     * @param message 用户消息内容
     * @return AI 回复文本
     */
    public String sendMessage(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
