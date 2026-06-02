package com.example.demo.modules.collection.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 藏品详情响应 DTO
 */
@Data
@Builder
public class CollectionDetailResponse {

    /** 藏品 ID */
    private String id;

    /** 用户 ID */
    private String userId;

    /** 藏品名称 */
    private String name;

    /** 图片列表 */
    private List<String> images;

    /** 封面图 */
    private String coverImage;

    /** 作品名称 */
    private String workName;

    /** 角色名称 */
    private String characterName;

    /** 藏品分类 */
    private String itemType;

    /** 购入价格 */
    private BigDecimal purchasePrice;

    /** 数量 */
    private Integer quantity;

    /** 购入渠道 */
    private String purchaseChannel;

    /** 购入日期 */
    private LocalDate purchaseDate;

    /** 状态 */
    private String status;

    /** 备注 */
    private String note;

    /** 是否待出物 */
    private Boolean isForSale;

    /** 出售价格 */
    private BigDecimal salePrice;

    /** 瑕疵说明 */
    private String flawNote;

    /** 包邮规则 */
    private String shippingRule;

    /** 砍价规则 */
    private String bargainRule;

    /** 捆出规则 */
    private String bundleRule;

    /** 审核状态 */
    private String auditStatus;

    /** 审核消息 */
    private String auditMessage;

    /** 自定义分类 ID */
    private String customTypeId;

    /** 搜索全文 */
    private String searchText;

    /** 排序序号 */
    private Long sortIndex;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
