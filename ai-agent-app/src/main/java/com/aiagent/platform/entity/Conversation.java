package com.aiagent.platform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体，对应 ai_conversation 表
 */
@Data
@TableName("ai_conversation")
public class Conversation {

    /** 主键（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 会话标题（NULL 表示尚未命名，Task 4 自动生成） */
    private String title;

    /** 模型标识符（如 "deepseek-chat"） */
    private String model;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
