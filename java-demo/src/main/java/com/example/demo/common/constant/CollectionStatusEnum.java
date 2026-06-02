package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收藏品状态枚举（8种状态，与需求文档 §17.12 一致）
 */
@Getter
@AllArgsConstructor
public enum CollectionStatusEnum {

    ARRIVED("arrived", "已到货"),
    PREORDER("preorder", "预售"),
    PENDING_PAYMENT("pending_payment", "待补款"),
    PENDING_SHIPMENT("pending_shipment", "待发货"),
    PENDING_RECEIPT("pending_receipt", "待收货"),
    FOR_SALE("for_sale", "待出物"),
    SOLD("sold", "已出物"),
    NOT_FOR_SALE("not_for_sale", "不出"),
    ;

    private final String value;
    private final String label;

    public static CollectionStatusEnum fromValue(String value) {
        for (CollectionStatusEnum s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        return null;
    }

    /** 判断是否为"待到货"相关状态（预售/待补款/待发货/待收货） */
    public boolean isPending() {
        return this == PREORDER || this == PENDING_PAYMENT
            || this == PENDING_SHIPMENT || this == PENDING_RECEIPT;
    }
}
