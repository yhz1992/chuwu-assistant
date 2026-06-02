package com.example.demo.modules.collection.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 藏品查询请求 DTO
 */
@Data
public class CollectionQueryRequest {

    /** 关键词（搜索名称/作品/角色/备注） */
    private String keyword;

    /** 状态筛选 */
    private String status;

    /** 类型筛选 */
    private String itemType;

    /** 作品筛选 */
    private String workName;

    /** 角色筛选 */
    private String characterName;

    /** 待出物筛选 */
    private Boolean isForSale;

    /** 排序字段：createdAt/updatedAt/purchasePrice/name */
    private String sortBy;

    /** 排序方向：asc/desc */
    private String sortOrder;

    /** 当前页码 */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数最小为1")
    private Integer pageSize = 20;
}
