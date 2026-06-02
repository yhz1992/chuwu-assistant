package com.example.demo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 藏品分类枚举
 * 支持的类型：吧唧、立牌、小卡、色纸、挂件、娃娃、娃衣、手办、卡牌、其他
 */
@Getter
@AllArgsConstructor
public enum ItemTypeEnum {

    /** 吧唧（徽章） */
    BADGE("badge", "吧唧"),

    /** 立牌 */
    STANDEE("standee", "立牌"),

    /** 小卡 */
    PHOTO_CARD("photo_card", "小卡"),

    /** 色纸 */
    SHIKISHI("shikishi", "色纸"),

    /** 挂件 */
    CHARM("charm", "挂件"),

    /** 娃娃 */
    DOLL("doll", "娃娃"),

    /** 娃衣 */
    DOLL_CLOTHES("doll_clothes", "娃衣"),

    /** 手办 */
    FIGURE("figure", "手办"),

    /** 卡牌 */
    CARD("card", "卡牌"),

    /** 其他 */
    OTHER("other", "其他"),
    ;

    /** 枚举值 */
    private final String value;

    /** 中文描述 */
    private final String label;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 对应的枚举，未找到返回 null
     */
    public static ItemTypeEnum fromValue(String value) {
        for (ItemTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断 value 是否为有效的枚举值
     *
     * @param value 枚举值
     * @return true 有效
     */
    public static boolean isValid(String value) {
        return fromValue(value) != null;
    }
}
