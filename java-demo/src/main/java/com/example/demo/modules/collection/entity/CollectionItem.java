package com.example.demo.modules.collection.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 藏品实体
 * 对应数据库表 collection_items
 */
@Data
@TableName("collection_items")
public class CollectionItem {

    /** 藏品 ID，格式：ci_ + 雪花ID */
    @TableId
    private String id;

    /** 用户 ID */
    private String userId;

    /** 藏品名称 */
    private String name;

    /** 图片列表（JSON 数组） */
    @TableField()
    private String images;

    /** 封面图 */
    private String coverImage;

    /** 作品名称 */
    private String workName;

    /** 角色名称 */
    private String characterName;

    /** 藏品分类 */
    private String itemType;

    /** 购入价格 */
    private BigDecimal purchasePrice;

    /** 数量 */
    private Integer quantity;

    /** 购入渠道 */
    private String purchaseChannel;

    /** 购入日期 */
    private LocalDate purchaseDate;

    /** 状态：arrived-已到货，preorder-预售，pending_payment-待补款，pending_shipment-待发货，pending_receipt-待收货，for_sale-待出物，sold-已出物，not_for_sale-不出 */
    private String status;

    /** 备注 */
    private String note;

    /** 是否待出物 */
    private Boolean isForSale;

    /** 出售价格 */
    private BigDecimal salePrice;

    /** 瑕疵说明 */
    private String flawNote;

    /** 包邮规则 */
    private String shippingRule;

    /** 砍价规则 */
    private String bargainRule;

    /** 捆出规则 */
    private String bundleRule;

    /** 审核状态：pending-待审核，approved-已通过，rejected-已驳回 */
    private String auditStatus;

    /** 审核消息 */
    private String auditMessage;

    /** 自定义分类 ID */
    private String customTypeId;

    /** 搜索全文（name + workName + characterName 拼接） */
    private String searchText;

    /** 排序序号 */
    private Long sortIndex;

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
