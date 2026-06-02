package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出售清单状态枚举
 */
@Getter
@AllArgsConstructor
public enum SaleListStatusEnum {

    /** 草稿（编辑中） */
    DRAFT("draft", "草稿"),

    /** 已生成（可以预览和分享） */
    GENERATED("generated", "已生成"),

    /** 已分享（对外分享） */
    SHARED("shared", "已分享"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     */
    public static SaleListStatusEnum fromValue(String value) {
        for (SaleListStatusEnum status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
