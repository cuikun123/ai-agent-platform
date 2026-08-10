package com.aiagent.platform.config;

import com.aiagent.platform.memory.DatabaseChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话客户端配置
 * <p>
 * 配置 ChatClient，集成：
 * - 默认 System Prompt
 * - MessageChatMemoryAdvisor（对话记忆，基于 PostgreSQL 持久化）
 */
@Configuration
public class ChatConfig {

    @Value("${ai.chat.system-prompt}")
    private String systemPrompt;

    /**
     * 对话记忆：滑动窗口 + 数据库存储
     * <p>
     * MessageWindowChatMemory 自动管理窗口内的消息，
     * 底层通过 DatabaseChatMemoryRepository 读写 ai_message 表。
     */
    @Bean
    public ChatMemory chatMemory(DatabaseChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .build();
    }

    /**
     * 配置 ChatClient，集成 System Prompt + 对话记忆 + Token 截断
     * <p>
     * Advisor 执行顺序：MessageChatMemoryAdvisor → TokenTruncationAdvisor → LLM
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                  TokenTruncationAdvisor tokenTruncationAdvisor) {
        return builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        tokenTruncationAdvisor
                )
                .build();
    }
}
