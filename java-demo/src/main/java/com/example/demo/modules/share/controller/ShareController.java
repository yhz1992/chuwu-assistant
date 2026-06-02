package com.example.demo.modules.share.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shares")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @GetMapping("/{shareId}")
    public ApiResponse<Map<String, Object>> getShareDetail(@PathVariable String shareId) {
        return ApiResponse.ok(shareService.getShareDetail(shareId));
    }
}
