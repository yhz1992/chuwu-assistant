package com.example.demo.modules.user.dto;

import lombok.Data;

/**
 * 更新用户信息请求 DTO
 */
@Data
public class UpdateUserRequest {

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 URL */
    private String avatar;
}
