package com.example.demo.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求 DTO
 */
@Data
public class WechatLoginRequest {

    /** 微信 wx.login 返回的临时 code */
    @NotBlank(message = "登录code不能为空")
    private String code;

    /** 用户昵称（首次登录时传入） */
    private String nickname;

    /** 用户头像 URL（首次登录时传入） */
    private String avatar;
}
