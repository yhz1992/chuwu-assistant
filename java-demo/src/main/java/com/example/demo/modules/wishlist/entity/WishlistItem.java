package com.example.demo.modules.wishlist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 心愿单实体
 * 对应数据库表 wishlist_items
 */
@Data
@TableName("wishlist_items")
public class WishlistItem {

    /** 心愿单 ID，格式：wi_ + 雪花ID */
    @TableId
    private String id;

    /** 用户 ID */
    private String userId;

    /** 商品名称 */
    private String name;

    /** 商品图片 */
    private String image;

    /** 作品名称 */
    private String workName;

    /** 角色名称 */
    private String characterName;

    /** 商品分类 */
    private String itemType;

    /** 目标价格 */
    private BigDecimal targetPrice;

    /** 想要程度：normal-一般想买，high-很想买，must_have-必入 */
    private String desireLevel;

    /** 状态：want-想要，bought-已购买，paused-暂时观望 */
    private String status;

    /** 备注 */
    private String note;

    /** 逻辑删除时间 */
    private LocalDateTime deletedAt;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
