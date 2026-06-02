package com.example.demo.modules.salelist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 生成出售清单请求 DTO
 */
@Data
public class SaleListGenerateRequest {

    /** 使用的模板 ID */
    private String templateId;

    /** 是否添加水印（前端字段名：showWatermark） */
    @JsonProperty("showWatermark")
    private Boolean watermark;

    /** 是否显示价格 */
    private Boolean showPrice;

    /** 背景颜色 */
    private String backgroundColor;

    /** 布局方式 */
    private String layout;

    /** 各平台自定义文案（可选），key 为平台标识，value 为自定义文案 */
    @JsonProperty("platformTexts")
    private Map<String, String> platformTexts;
}
