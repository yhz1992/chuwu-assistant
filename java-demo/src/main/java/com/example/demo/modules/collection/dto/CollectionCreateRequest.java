package com.example.demo.modules.collection.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建藏品请求 DTO
 */
@Data
public class CollectionCreateRequest {

    /** 藏品名称 */
    @NotBlank(message = "藏品名称不能为空")
    private String name;

    /** 图片列表（最多9张） */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;

    /** 作品名称 */
    private String workName;

    /** 角色名称 */
    private String characterName;

    /** 藏品分类 */
    @NotBlank(message = "藏品分类不能为空")
    private String itemType;

    /** 购入价格 */
    @DecimalMin(value = "0.00", message = "购入价格不能为负")
    private BigDecimal purchasePrice;

    /** 数量 */
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 购入渠道 */
    private String purchaseChannel;

    /** 购入日期（yyyy-MM-dd） */
    private String purchaseDate;

    /** 状态 */
    @NotBlank(message = "状态不能为空")
    private String status;

    /** 备注 */
    private String note;

    /** 是否待出物（默认false） */
    private Boolean isForSale;

    /** 出售价格 */
    @DecimalMin(value = "0.00", message = "出售价格不能为负")
    private BigDecimal salePrice;

    /** 瑕疵说明 */
    private String flawNote;

    /** 包邮规则 */
    private String shippingRule;

    /** 砍价规则 */
    private String bargainRule;

    /** 捆出规则 */
    private String bundleRule;
}
