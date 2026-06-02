package com.example.demo.modules.upload.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.upload.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 上传控制器
 * 处理图片上传请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    /**
     * 上传图片
     *
     * @param file  图片文件
     * @param scene 业务场景（collection-藏品图片, avatar-头像等）
     */
    @PostMapping("/image")
    public ApiResponse<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scene") String scene) {
        String userId = UserContext.getUserId();
        log.info("上传图片，userId: {}, scene: {}, fileName: {}, size: {}",
                userId, scene, file.getOriginalFilename(), file.getSize());
        return ApiResponse.ok(uploadService.upload(file, scene, userId));
    }
}
