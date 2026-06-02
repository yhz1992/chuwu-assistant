package com.example.demo.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 微信小程序登录响应 DTO
 */
@Data
@Builder
public class WechatLoginResponse {

    /** Access token */
    private String token;

    /** Refresh token */
    private String refreshToken;

    /** 用户基本信息 */
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        /** 用户 ID */
        private String id;

        /** 用户昵称 */
        private String nickname;

        /** 用户头像 */
        private String avatar;

        /** 账号创建时间 */
        private LocalDateTime createdAt;
    }
}
