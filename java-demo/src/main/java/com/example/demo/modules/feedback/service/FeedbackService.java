package com.example.demo.modules.feedback.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.feedback.dto.FeedbackCreateRequest;
import com.example.demo.modules.feedback.entity.Feedback;
import com.example.demo.modules.feedback.mapper.FeedbackMapper;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 反馈服务
 * 处理用户反馈提交等业务逻辑
 */
@Slf4j
@Service
public class FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final SnowflakeIdUtils snowflakeIdUtils;

    public FeedbackService(FeedbackMapper feedbackMapper, SnowflakeIdUtils snowflakeIdUtils) {
        this.feedbackMapper = feedbackMapper;
        this.snowflakeIdUtils = snowflakeIdUtils;
    }

    /**
     * 提交反馈
     * 游客也可提交反馈（userId 可为 null）
     *
     * @param userId 用户 ID（可为 null）
     * @param req    创建请求
     * @return 包含反馈 ID 的字符串
     */
    @Transactional(rollbackFor = Exception.class)
    public String submit(String userId, FeedbackCreateRequest req) {
        // 校验内容必填（由 @NotBlank 保证，此处再检查一次）
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BusinessException(ResultCode.FEEDBACK_CONTENT_EMPTY);
        }

        Feedback feedback = new Feedback();
        feedback.setId(snowflakeIdUtils.nextIdWithPrefix("fb_"));
        feedback.setUserId(userId);
        feedback.setType(req.getType());
        feedback.setContent(req.getContent().trim());
        feedback.setContact(req.getContact());
        feedback.setImages(JSONUtil.toJsonStr(req.getImages()));
        feedback.setStatus("pending");
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        feedbackMapper.insert(feedback);
        log.info("反馈提交成功，id: {}, userId: {}, type: {}", feedback.getId(), userId, req.getType());

        return feedback.getId();
    }
}
