package com.example.demo.modules.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建反馈请求 DTO
 */
@Data
public class FeedbackCreateRequest {

    /** 反馈类型：feature-功能建议，bug-问题反馈，template-模板需求，other-其他 */
    @NotBlank(message = "反馈类型不能为空")
    private String type;

    /** 反馈内容 */
    @NotBlank(message = "反馈内容不能为空")
    private String content;

    /** 联系方式 */
    private String contact;

    /** 图片列表 */
    private List<String> images;
}
