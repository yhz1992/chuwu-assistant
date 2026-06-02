package com.example.demo.modules.wishlist.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建心愿单请求 DTO
 */
@Data
public class WishlistCreateRequest {

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
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
    @DecimalMin(value = "0.00", message = "目标价格不能为负")
    private BigDecimal targetPrice;

    /** 想要程度 */
    private String desireLevel;

    /** 状态 */
    private String status;

    /** 备注 */
    private String note;
}
