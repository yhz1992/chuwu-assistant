package com.example.demo.modules.salelist.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出售清单详情响应 DTO
 */
@Data
@Builder
public class SaleListDetailResponse {

    /** 清单 ID */
    private String id;

    /** 清单标题 */
    private String title;

    /** 清单描述 */
    private String description;

    /** 使用的模板 ID */
    private String templateId;

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

    /** 交易规则 */
    private TradeRuleDTO tradeRule;

    /** 是否添加水印 */
    private Boolean watermark;

    /** 商品列表 */
    private List<SaleListItemDTO> items;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
