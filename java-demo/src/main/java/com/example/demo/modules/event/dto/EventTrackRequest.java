package com.example.demo.modules.event.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 埋点事件追踪请求 DTO
 */
@Data
public class EventTrackRequest {

    /** 事件名称 */
    @NotBlank(message = "事件名称不能为空")
    private String eventName;

    /** 事件属性 */
    private Map<String, Object> properties;

    /** 事件时间戳 */
    private String timestamp;
}
