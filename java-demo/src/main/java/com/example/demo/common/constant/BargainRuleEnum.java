package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 砍价规则枚举
 */
@Getter
@AllArgsConstructor
public enum BargainRuleEnum {

    /** 可小刀 */
    BARGAIN("bargain", "可小刀"),

    /** 不刀 */
    NO_BARGAIN("no_bargain", "不刀"),

    /** 打包优先 */
    BUNDLE_FIRST("bundle_first", "打包优先"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     */
    public static BargainRuleEnum fromValue(String value) {
        for (BargainRuleEnum rule : values()) {
            if (rule.value.equals(value)) {
                return rule;
            }
        }
        return null;
    }
}
