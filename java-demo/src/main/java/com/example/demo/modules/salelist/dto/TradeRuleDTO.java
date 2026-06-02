package com.example.demo.modules.salelist.dto;

import lombok.Data;

/**
 * 交易规则 DTO
 */
@Data
public class TradeRuleDTO {

    /** 交易平台 */
    private String platform;

    /** 是否允许分开发货 */
    private Boolean allowSplit;

    /** 联系说明 */
    private String contactNote;

    /** 额外规则说明 */
    private String extraRule;
}
