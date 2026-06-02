package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运费规则枚举
 */
@Getter
@AllArgsConstructor
public enum ShippingRuleEnum {

    /** 包邮 */
    INCLUDED("included", "包邮"),

    /** 不包邮 */
    NOT_INCLUDED("not_included", "不包邮"),

    /** 满额包邮 */
    CONDITIONAL("conditional", "满额包邮"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     */
    public static ShippingRuleEnum fromValue(String value) {
        for (ShippingRuleEnum rule : values()) {
            if (rule.value.equals(value)) {
                return rule;
            }
        }
        return null;
    }
}
