package com.aiagent.platform.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话客户端配置
 * <p>
 * 基于 Spring AI 的 ChatClient 构建，注入默认 System Prompt。
 * 后续任务会在此基础上扩展（对话记忆 Advisor、RAG Advisor 等）。
 */
@Configuration
public class ChatConfig {

    @Value("${ai.chat.system-prompt}")
    private String systemPrompt;

    /**
     * 配置 ChatClient，设置默认 System Prompt
     *
     * @param builder Spring AI 自动注入的 ChatClient.Builder（已关联 ChatModel）
     * @return 配置好的 ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(systemPrompt)
                .build();
    }
}
