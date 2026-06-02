package com.example.demo.modules.salelist.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出售清单实体
 * 对应数据库表 sale_lists
 */
@Data
@TableName("sale_lists")
public class SaleList {

    /** 清单 ID，格式：sl_ + 雪花ID */
    @TableId
    private String id;

    /** 用户 ID */
    private String userId;

    /** 清单标题 */
    private String title;

    /** 清单描述 */
    private String description;

    /** 使用的模板 ID */
    private String templateId;

    /** 状态：draft-草稿，generated-已生成，shared-已分享 */
    private String status;

    /** 商品总数 */
    private Integer totalCount;

    /** 总价 */
    private BigDecimal totalPrice;

    /** 生成的分享图 URL */
    private String generatedImage;

    /** 生成的页面列表（JSON 数组） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> generatedPages;

    /** 分享链接 ID */
    private String shareId;

    /** 交易规则（JSON） */
    private String tradeRule;

    /** 是否添加水印 */
    private Boolean watermark;

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
