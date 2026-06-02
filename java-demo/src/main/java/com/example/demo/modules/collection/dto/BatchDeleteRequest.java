package com.example.demo.modules.collection.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除请求 DTO
 */
@Data
public class BatchDeleteRequest {

    /** 藏品 ID 列表 */
    @NotEmpty(message = "ID列表不能为空")
    private List<String> ids;
}
