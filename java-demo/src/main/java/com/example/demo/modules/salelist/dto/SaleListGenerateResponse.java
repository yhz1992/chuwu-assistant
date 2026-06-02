package com.example.demo.modules.salelist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 生成出售清单响应 DTO
 */
@Data
@Builder
public class SaleListGenerateResponse {

    /** 清单 ID */
    @JsonProperty("saleListId")
    private String saleListId;

    /** 生成的分享图 URL（前端字段名：imageUrl） */
    @JsonProperty("imageUrl")
    private String generatedImage;

    /** 分享链接 ID（前端字段名：shareUrl） */
    @JsonProperty("shareUrl")
    private String shareId;

    /** 各平台生成文案，key 为平台标识，value 为文案内容 */
    private Map<String, String> texts;
}
