package com.aiagent.platform.service;

import com.aiagent.platform.entity.Conversation;
import com.aiagent.platform.repository.ConversationMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * 对话服务
 * <p>
 * 负责会话管理和调用 ChatClient。
 * 消息持久化由 ChatMemory Advisor 自动处理（读取历史 + 保存新消息）。
 */
@Service
public class ChatService {

    private static final int TITLE_MAX_LENGTH = 20;

    private final ChatClient chatClient;
    private final ConversationMapper conversationMapper;

    public ChatService(ChatClient chatClient, ConversationMapper conversationMapper) {
        this.chatClient = chatClient;
        this.conversationMapper = conversationMapper;
    }

    /**
     * 发送消息并同步返回 AI 回复
     */
    public String sendMessage(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * �式对话结果：包含会话 ID 和文字片段流
     */
    public record StreamResult(Long conversationId, Flux<String> stream) {}

    /**
     * 发送消息并以流式方式返回 AI 回复
     * <p>
     * 自动处理：会话创建、标题生成、历史加载（由 ChatMemory Advisor 完成）。
     *
     * @param message        用户消息
     * @param conversationId 会话 ID（为空时自动创建新会话）
     * @param userId         当前用户 ID
     * @param model          模型标识符（为空时使用默认模型）
     * @return StreamResult 包含 conversationId 和流式内容
     */
    public StreamResult streamMessage(String message, Long conversationId, Long userId, String model) {
        // 确保会话存在（conversationId 为空时自动创建）
        Long convId = ensureConversation(conversationId, message, userId, model);

        // 流式调用 LLM，ChatMemory Advisor 自动加载历史 + 保存新消息
        Flux<String> stream = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, String.valueOf(convId)))
                .stream()
                .content();

        return new StreamResult(convId, stream);
    }

    /**
     * 确保会话存在，不存在则自动创建
     *
     * @param conversationId 会话 ID
     * @param firstMessage   第一条消息（用于生成标题）
     * @param userId         当前用户 ID
     * @param model          模型标识符
     * @return 有效的会话 ID
     */
    private Long ensureConversation(Long conversationId, String firstMessage, Long userId, String model) {
        if (conversationId != null) {
            return conversationId;
        }

        // 自动创建新会话，标题截取前 20 字
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        String title = firstMessage.length() > TITLE_MAX_LENGTH
                ? firstMessage.substring(0, TITLE_MAX_LENGTH)
                : firstMessage;
        conversation.setTitle(title);
        conversation.setModel(model != null ? model : "deepseek-chat");
        conversationMapper.insert(conversation);
        return conversation.getId();
    }
}
