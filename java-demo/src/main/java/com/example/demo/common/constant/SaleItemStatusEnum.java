package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出售商品状态枚举
 */
@Getter
@AllArgsConstructor
public enum SaleItemStatusEnum {

    /** 可出 */
    AVAILABLE("available", "可出"),

    /** 已出 */
    SOLD("sold", "已出"),

    /** 暂挂 */
    RESERVED("reserved", "暂挂"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     */
    public static SaleItemStatusEnum fromValue(String value) {
        for (SaleItemStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 商品是否可售
     */
    public boolean isAvailable() {
        return this == AVAILABLE;
    }
}
