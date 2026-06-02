package com.example.demo.modules.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前用户信息响应 DTO
 */
@Data
@Builder
public class UserMeResponse {

    /** 用户 ID */
    private String id;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 */
    private String avatar;

    /** 统计数据 */
    private UserStats stats;

    /** 账号创建时间 */
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class UserStats {
        /** 藏品数量 */
        private long collectionCount;

        /** 出售清单数量 */
        private long saleListCount;

        /** 心愿单数量 */
        private long wishlistCount;

        /** 已售出数量 */
        private long soldCount;
    }
}
