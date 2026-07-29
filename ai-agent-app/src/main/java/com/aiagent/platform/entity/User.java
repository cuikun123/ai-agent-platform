package com.aiagent.platform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 ai_user 表
 */
@Data
@TableName("ai_user")
public class User {

    /** 主键（雪花算法） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** BCrypt 加密后的密码 */
    private String password;

    /** 显示昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 账号状态（1=正常, 0=禁用） */
    private Short status;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
