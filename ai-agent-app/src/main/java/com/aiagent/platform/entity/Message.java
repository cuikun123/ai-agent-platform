package com.aiagent.platform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体，对应 ai_message 表
 */
@Data
@TableName("ai_message")
public class Message {

    /** 主键（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属会话 ID */
    private Long conversationId;

    /** 消息角色：system / user / assistant */
    private String role;

    /** 消息内容 */
    private String content;

    /** token 数量（Task 8 填充，当前允许 NULL） */
    private Integer tokenCount;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
