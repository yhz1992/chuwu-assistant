package com.example.demo.modules.salelist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 出售清单商品实体
 * 对应数据库表 sale_list_items
 */
@Data
@TableName("sale_list_items")
public class SaleListItem {

    /** 商品 ID，格式：sli_ + 雪花ID */
    @TableId
    private String id;

    /** 所属清单 ID */
    private String saleListId;

    /** 关联的藏品 ID */
    private String collectionItemId;

    /** 藏品快照（JSON，记录创建时的藏品信息） */
    @TableField()
    private String collectionSnapshot;

    /** 商品名称（创建时的快照） */
    private String name;

    /** 商品图片 URL */
    private String image;

    /** 价格 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 状态：available-可出，sold-已出，reserved-暂挂 */
    private String status;

    /** 瑕疵说明 */
    private String flawNote;

    /** 包邮规则：included-包邮，not_included-不包邮，conditional-满额包邮 */
    private String shippingRule;

    /** 砍价规则：bargain-可小刀，no_bargain-不刀，bundle_first-打包优先 */
    private String bargainRule;

    /** 捆出规则 */
    private String bundleRule;

    /** 备注 */
    private String note;

    /** 排序序号 */
    private Integer sortOrder;

    /** 售出时间 */
    private LocalDateTime soldAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
