package com.example.demo.modules.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新状态请求 DTO
 */
@Data
public class BatchStatusRequest {

    /** 藏品 ID 列表 */
    @NotEmpty(message = "ID列表不能为空")
    private List<String> ids;

    /** 目标状态 */
    @NotBlank(message = "状态不能为空")
    private String status;
}
