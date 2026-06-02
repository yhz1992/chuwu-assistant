package com.example.demo.modules.salelist.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出售清单列表摘要响应 DTO
 */
@Data
@Builder
public class SaleListListResponse {

    /** 清单 ID */
    private String id;

    /** 清单标题 */
    private String title;

    /** 清单描述 */
    private String description;

    /** 状态 */
    private String status;

    /** 商品总数 */
    private Integer totalCount;

    /** 总价 */
    private BigDecimal totalPrice;

    /** 生成的分享图 URL */
    private String generatedImage;

    /** 分享链接 ID */
    private String shareId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
