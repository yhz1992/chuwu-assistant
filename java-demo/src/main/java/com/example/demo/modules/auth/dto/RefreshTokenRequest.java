package com.example.demo.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 token 请求 DTO
 */
@Data
public class RefreshTokenRequest {

    /** Refresh token */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
