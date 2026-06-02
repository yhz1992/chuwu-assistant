package com.example.demo.modules.salelist.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建出售清单请求 DTO
 */
@Data
public class SaleListCreateRequest {

    /** 清单标题 */
    @NotBlank(message = "清单标题不能为空")
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
