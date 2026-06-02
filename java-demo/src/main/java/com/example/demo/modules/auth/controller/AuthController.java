package com.example.demo.modules.auth.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.auth.dto.RefreshTokenRequest;
import com.example.demo.modules.auth.dto.WechatLoginRequest;
import com.example.demo.modules.auth.dto.WechatLoginResponse;
import com.example.demo.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证控制器
 * 处理微信登录、token 刷新等认证请求
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 微信小程序登录
     */
    @PostMapping("/wechat-login")
    public ApiResponse<WechatLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest req) {
        return ApiResponse.ok(authService.wechatLogin(req));
    }

    /**
     * 刷新 access token
     */
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ApiResponse.ok(authService.refreshToken(req.getRefreshToken()));
    }
}
