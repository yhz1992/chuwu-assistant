package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 想要程度枚举
 */
@Getter
@AllArgsConstructor
public enum DesireLevelEnum {

    NORMAL("normal", "一般想买"),
    HIGH("high", "很想买"),
    MUST_HAVE("must_have", "必入"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     */
    public static DesireLevelEnum fromValue(String value) {
        for (DesireLevelEnum level : values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        return null;
    }
}
