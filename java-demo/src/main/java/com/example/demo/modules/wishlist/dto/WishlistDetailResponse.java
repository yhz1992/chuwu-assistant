package com.example.demo.modules.wishlist.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 心愿单详情响应 DTO
 */
@Data
@Builder
public class WishlistDetailResponse {

    /** 心愿单 ID */
    private String id;

    /** 用户 ID */
    private String userId;

    /** 商品名称 */
    private String name;

    /** 商品图片 */
    private String image;

    /** 作品名称 */
    private String workName;

    /** 角色名称 */
    private String characterName;

    /** 商品分类 */
    private String itemType;

    /** 目标价格 */
    private BigDecimal targetPrice;

    /** 想要程度 */
    private String desireLevel;

    /** 状态 */
    private String status;

    /** 备注 */
    private String note;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
