package com.example.demo.modules.home.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.home.dto.HomeOverviewResponse;
import com.example.demo.modules.home.service.HomeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    /**
     * 获取首页概览
     * GET /api/v1/home/overview
     */
    @GetMapping("/overview")
    public ApiResponse<HomeOverviewResponse> overview() {
        String userId = UserContext.getUserId();
        log.info("查询首页概览，userId: {}", userId);
        return ApiResponse.ok(homeService.getOverview(userId));
    }
}
