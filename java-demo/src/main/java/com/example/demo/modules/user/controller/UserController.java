package com.example.demo.modules.user.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.user.dto.UpdateUserRequest;
import com.example.demo.modules.user.dto.UserMeResponse;
import com.example.demo.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> getMe() {
        String userId = UserContext.getUserId();
        return ApiResponse.ok(userService.getMe(userId));
    }

    @PutMapping("/me")
    public ApiResponse<UserMeResponse> updateMe(@Valid @RequestBody UpdateUserRequest req) {
        String userId = UserContext.getUserId();
        return ApiResponse.ok(userService.updateMe(userId, req));
    }
}
