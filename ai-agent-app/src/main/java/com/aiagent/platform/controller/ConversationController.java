package com.aiagent.platform.controller;

import com.aiagent.platform.common.result.Result;
import com.aiagent.platform.entity.Conversation;
import com.aiagent.platform.repository.ConversationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 会话管理控制器
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationMapper conversationMapper;

    public ConversationController(ConversationMapper conversationMapper) {
        this.conversationMapper = conversationMapper;
    }

    /**
     * 获取当前用户的会话列表（按更新时间倒序）
     */
    @GetMapping
    public Result<List<Conversation>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Conversation> conversations = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getUpdatedAt)
        );
        return Result.ok(conversations);
    }

    /**
     * 更新会话（标题、模型）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Conversation conversation = conversationMapper.selectById(id);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return Result.fail(2002, "会话不存在");
        }

        if (body.containsKey("title")) {
            conversation.setTitle(body.get("title"));
        }
        if (body.containsKey("model")) {
            conversation.setModel(body.get("model"));
        }
        conversationMapper.updateById(conversation);
        return Result.ok();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Conversation conversation = conversationMapper.selectById(id);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return Result.fail(2002, "会话不存在");
        }
        conversationMapper.deleteById(id);
        return Result.ok();
    }
}
