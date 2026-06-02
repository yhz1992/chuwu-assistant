package com.example.demo.modules.template.controller;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.modules.template.entity.Template;
import com.example.demo.modules.template.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public ApiResponse<Map<String, List<Template>>> getList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isPremium) {
        return ApiResponse.ok(Map.of("list", templateService.getList(type, isPremium)));
    }

    @GetMapping("/{id}")
    public ApiResponse<Template> getDetail(@PathVariable String id) {
        return ApiResponse.ok(templateService.getDetail(id));
    }
}
