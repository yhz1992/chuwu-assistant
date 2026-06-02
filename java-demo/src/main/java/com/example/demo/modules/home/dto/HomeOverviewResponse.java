package com.example.demo.modules.home.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 首页概览响应 DTO
 */
@Data
@Builder
public class HomeOverviewResponse {

    /** 统计数据 */
    private HomeStats stats;

    /** 最近收藏列表（最近 6 条） */
    private List<RecentCollection> recentCollections;

    /** 最近清单列表（最近 3 条） */
    private List<RecentSaleList> recentSaleLists;

    /**
     * 首页统计数据
     */
    @Data
    @Builder
    public static class HomeStats {
        /** 藏品总数 */
        private long totalCollections;

        /** 出售清单总数 */
        private long totalSaleLists;

        /** 已售出商品数 */
        private long totalSoldItems;

        /** 心愿单数量 */
        private long wishlistCount;
    }

    /**
     * 最近收藏条目
     */
    @Data
    @Builder
    public static class RecentCollection {
        private String id;
        private String name;
        private String coverImage;
        private String status;
        private String itemType;
        private String workName;
        private String characterName;
    }

    /**
     * 最近清单条目
     */
    @Data
    @Builder
    public static class RecentSaleList {
        private String id;
        private String title;
        private String status;
        private Integer totalCount;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
    }
}
