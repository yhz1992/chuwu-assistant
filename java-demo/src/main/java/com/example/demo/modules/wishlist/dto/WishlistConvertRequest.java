package com.example.demo.modules.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 心愿单转收藏请求 DTO
 */
@Data
public class WishlistConvertRequest {

    /** 购入价格 */
    private BigDecimal purchasePrice;

    /** 购入渠道 */
    private String purchaseChannel;

    /** 转为收藏后的初始状态 */
    @NotBlank(message = "收藏状态不能为空")
    private String status;
}
