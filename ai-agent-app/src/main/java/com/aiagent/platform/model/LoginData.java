package com.aiagent.platform.model;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应数据
 */
@Data
@Builder
public class LoginData {

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;
}
