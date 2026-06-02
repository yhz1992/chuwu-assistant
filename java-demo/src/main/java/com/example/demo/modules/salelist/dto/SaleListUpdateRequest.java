package com.example.demo.modules.salelist.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * 更新出售清单请求 DTO
 */
@Data
public class SaleListUpdateRequest {

    /** 清单标题 */
    private String title;

    /** 清单描述 */
    private String description;

    /** 商品列表 */
    @Valid
    private List<SaleListItemDTO> items;

    /** 交易规则 */
    @Valid
    private TradeRuleDTO tradeRule;
}
