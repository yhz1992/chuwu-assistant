package com.example.demo.modules.salelist.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.PageResponse;
import com.example.demo.modules.salelist.dto.*;
import com.example.demo.modules.salelist.service.SaleListService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 出售清单控制器
 * 处理清单的 CRUD、生成、复制等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sale-lists")
public class SaleListController {

    @Autowired
    private SaleListService saleListService;

    /**
     * 分页查询清单列表
     * GET /api/v1/sale-lists?status=&page=&pageSize=
     */
    @GetMapping
    public PageResponse<SaleListListResponse> getList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        String userId = UserContext.getUserId();
        log.info("查询出售清单列表，userId: {}, status: {}, page: {}, pageSize: {}", userId, status, page, pageSize);
        return saleListService.getList(userId, status, page, pageSize);
    }

    /**
     * 查询清单详情
     * GET /api/v1/sale-lists/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<SaleListDetailResponse> getDetail(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("查询出售清单详情，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(saleListService.getDetail(userId, id));
    }

    /**
     * 创建清单
     * POST /api/v1/sale-lists
     */
    @PostMapping
    public ApiResponse<Map<String, String>> create(@Valid @RequestBody SaleListCreateRequest req) {
        String userId = UserContext.getUserId();
        log.info("创建出售清单，userId: {}, title: {}", userId, req.getTitle());
        return ApiResponse.ok(saleListService.create(userId, req));
    }

    /**
     * 更新清单
     * PUT /api/v1/sale-lists/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String id, @Valid @RequestBody SaleListUpdateRequest req) {
        String userId = UserContext.getUserId();
        log.info("更新出售清单，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(saleListService.update(userId, id, req));
    }

    /**
     * 删除清单（逻辑删除）
     * DELETE /api/v1/sale-lists/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("删除出售清单，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(saleListService.delete(userId, id));
    }

    /**
     * 复制清单
     * POST /api/v1/sale-lists/{id}/duplicate
     */
    @PostMapping("/{id}/duplicate")
    public ApiResponse<Map<String, String>> duplicate(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("复制出售清单，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(saleListService.duplicate(userId, id));
    }

    /**
     * 生成清单（创建分享、生成文案）
     * POST /api/v1/sale-lists/{id}/generate
     */
    @PostMapping("/{id}/generate")
    public ApiResponse<SaleListGenerateResponse> generate(@PathVariable String id, @Valid @RequestBody SaleListGenerateRequest req) {
        String userId = UserContext.getUserId();
        log.info("生成出售清单，userId: {}, id: {}, templateId: {}", userId, id, req.getTemplateId());
        return ApiResponse.ok(saleListService.generate(userId, id, req));
    }

    /**
     * 更新清单分享状态为已分享
     * PATCH /api/v1/sale-lists/{id}/share-status
     */
    @PatchMapping("/{id}/share-status")
    public ApiResponse<Map<String, Object>> updateShareStatus(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("更新出售清单分享状态，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(saleListService.updateShareStatus(userId, id));
    }
}
