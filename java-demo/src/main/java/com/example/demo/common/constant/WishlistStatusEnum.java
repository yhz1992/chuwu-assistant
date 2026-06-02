package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 心愿单状态枚举
 */
@Getter
@AllArgsConstructor
public enum WishlistStatusEnum {

    /** 想要 */
    WANT("want", "想要"),

    /** 已购买 */
    BOUGHT("bought", "已购买"),

    /** 暂时观望 */
    PAUSED("paused", "暂时观望"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     */
    public static WishlistStatusEnum fromValue(String value) {
        for (WishlistStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
