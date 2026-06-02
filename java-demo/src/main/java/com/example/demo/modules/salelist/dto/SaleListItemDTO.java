package com.example.demo.modules.salelist.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出售清单商品 DTO
 * 用于请求和响应中传递商品信息
 */
@Data
public class SaleListItemDTO {

    /** 商品 ID */
    private String id;

    /** 所属清单 ID */
    private String saleListId;

    /** 关联的藏品 ID */
    private String collectionItemId;

    /** 商品名称 */
    private String name;

    /** 商品图片 URL */
    private String image;

    /** 价格 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 状态：available-可出，sold-已出，reserved-暂挂 */
    private String status;

    /** 瑕疵说明 */
    private String flawNote;

    /** 包邮规则 */
    private String shippingRule;

    /** 砍价规则 */
    private String bargainRule;

    /** 捆出规则 */
    private String bundleRule;

    /** 备注 */
    private String note;

    /** 排序序号 */
    private Integer sortOrder;

    /** 售出时间 */
    private LocalDateTime soldAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
