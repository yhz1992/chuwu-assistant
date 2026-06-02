package com.example.demo.modules.feedback.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.feedback.dto.FeedbackCreateRequest;
import com.example.demo.modules.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 反馈控制器
 * 处理用户反馈提交请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 提交反馈
     * POST /api/v1/feedback
     * 允许游客提交（UserContext.getUserId() 可为 null）
     */
    @PostMapping
    public ApiResponse<Map<String, String>> submit(@Valid @RequestBody FeedbackCreateRequest req) {
        String userId = UserContext.getUserId();
        log.info("提交反馈，userId: {}, type: {}, content: {}", userId, req.getType(), req.getContent());
        String feedbackId = feedbackService.submit(userId, req);
        Map<String, String> result = new HashMap<>();
        result.put("id", feedbackId);
        return ApiResponse.ok(result);
    }
}
