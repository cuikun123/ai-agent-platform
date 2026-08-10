package com.aiagent.platform.config;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * Token 截断 Advisor
 * <p>
 * 在消息发送给 LLM 之前，检查历史消息的 token 总量。
 * 超过上限时，从最旧的消息开始丢弃，保留最新的对话内容。
 * <p>
 * 工作流程（在 Advisor 链中的位置）：
 * MessageChatMemoryAdvisor（加载历史） → TokenTruncationAdvisor（截断） → LLM
 */
@Component
public class TokenTruncationAdvisor implements BaseAdvisor {

    @Value("${ai.chat.max-history-tokens}")
    private int maxHistoryTokens;

    @Override
    public int getOrder() {
        // 在 MessageChatMemoryAdvisor 之后执行（数值越大越晚执行）
        return Ordered.LOWEST_PRECEDENCE - 1;
    }

    @Override
    @NonNull
    public ChatClientRequest before(@NonNull ChatClientRequest request, @NonNull AdvisorChain chain) {
        Prompt prompt = request.prompt();
        List<Message> messages = prompt.getInstructions();

        if (messages == null || messages.isEmpty()) {
            return request;
        }

        // 分离 System Message 和对话消息
        List<Message> systemMessages = new ArrayList<>();
        List<Message> conversationMessages = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getMessageType() == MessageType.SYSTEM) {
                systemMessages.add(msg);
            } else {
                conversationMessages.add(msg);
            }
        }

        // 从最新的消息往前累加 token，超限的旧消息丢弃
        List<Message> truncated = truncateFromNewest(conversationMessages, maxHistoryTokens);

        // 重新组装：System Message + 截断后的对话消息
        List<Message> finalMessages = new ArrayList<>(systemMessages);
        finalMessages.addAll(truncated);

        // 构建新的 Prompt 和 ChatClientRequest
        Prompt newPrompt = new Prompt(finalMessages, prompt.getOptions());
        return request.mutate().prompt(newPrompt).build();
    }

    @Override
    @NonNull
    public ChatClientResponse after(@NonNull ChatClientResponse response, @NonNull AdvisorChain chain) {
        return response;
    }

    @Override
    @NonNull
    public String getName() {
        return "TokenTruncationAdvisor";
    }

    /**
     * 从最新的消息往前累加 token，超过上限的旧消息丢弃
     */
    private List<Message> truncateFromNewest(List<Message> messages, int maxTokens) {
        int totalTokens = 0;
        List<Message> kept = new ArrayList<>();

        // 从后往前遍历（最新 → 最旧）
        for (int i = messages.size() - 1; i >= 0; i--) {
            int tokens = estimateTokens(messages.get(i).getText());
            if (totalTokens + tokens > maxTokens && !kept.isEmpty()) {
                break;
            }
            totalTokens += tokens;
            kept.addFirst(messages.get(i));
        }

        return kept;
    }

    /**
     * 粗略估算 token 数
     * <p>
     * 策略：每 3 个字符约 1 个 token（对中文偏保守，安全余量足够）
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        return (int) Math.ceil(text.length() / 3.0);
    }
}
