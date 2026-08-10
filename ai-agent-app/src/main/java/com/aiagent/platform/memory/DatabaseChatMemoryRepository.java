package com.aiagent.platform.memory;

import com.aiagent.platform.entity.Message;
import com.aiagent.platform.repository.MessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 PostgreSQL 的 ChatMemory 存储实现
 * <p>
 * 将 Spring AI 的 Message 对象与 ai_message 表互相转换。
 * 由 MessageWindowChatMemory 包装使用，提供滑动窗口能力。
 */
@Component
public class DatabaseChatMemoryRepository implements ChatMemoryRepository {

    private final MessageMapper messageMapper;

    public DatabaseChatMemoryRepository(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    @NonNull
    public List<String> findConversationIds() {
        // 查询去重的 conversationId 列表
        List<Message> messages = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .select(Message::getConversationId)
                        .groupBy(Message::getConversationId)
        );
        return messages.stream()
                .map(m -> String.valueOf(m.getConversationId()))
                .toList();
    }

    @Override
    @NonNull
    public List<org.springframework.ai.chat.messages.Message> findByConversationId(@NonNull String conversationId) {
        List<Message> entities = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, Long.parseLong(conversationId))
                        .orderByAsc(Message::getCreatedAt)
        );
        return entities.stream()
                .map(this::toAiMessage)
                .toList();
    }

    @Override
    public void saveAll(@NonNull String conversationId, @NonNull List<org.springframework.ai.chat.messages.Message> messages) {
        Long convId = Long.parseLong(conversationId);

        // 先删除该会话的旧消息，再全量写入（保证一致性）
        messageMapper.delete(
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getConversationId, convId)
        );

        // 批量插入
        List<Message> entities = messages.stream()
                .map(aiMsg -> toEntity(convId, aiMsg))
                .toList();
        for (Message entity : entities) {
            messageMapper.insert(entity);
        }
    }

    @Override
    public void deleteByConversationId(@NonNull String conversationId) {
        messageMapper.delete(
                new LambdaUpdateWrapper<Message>()
                        .eq(Message::getConversationId, Long.parseLong(conversationId))
        );
    }

    /**
     * 数据库实体 → Spring AI Message
     */
    private org.springframework.ai.chat.messages.Message toAiMessage(Message entity) {
        return switch (MessageType.valueOf(entity.getRole().toUpperCase())) {
            case USER -> new UserMessage(entity.getContent());
            case ASSISTANT -> new AssistantMessage(entity.getContent());
            case SYSTEM -> new SystemMessage(entity.getContent());
            default -> new UserMessage(entity.getContent());
        };
    }

    /**
     * Spring AI Message → 数据库实体
     */
    private Message toEntity(Long conversationId, org.springframework.ai.chat.messages.Message aiMessage) {
        Message entity = new Message();
        entity.setConversationId(conversationId);
        entity.setRole(aiMessage.getMessageType().getValue());
        entity.setContent(aiMessage.getText());
        return entity;
    }
}
