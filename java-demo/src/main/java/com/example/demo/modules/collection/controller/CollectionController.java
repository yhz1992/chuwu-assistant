package com.example.demo.modules.collection.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.PageResponse;
import com.example.demo.modules.collection.dto.BatchDeleteRequest;
import com.example.demo.modules.collection.dto.BatchStatusRequest;
import com.example.demo.modules.collection.dto.CollectionCreateRequest;
import com.example.demo.modules.collection.dto.CollectionDetailResponse;
import com.example.demo.modules.collection.dto.CollectionQueryRequest;
import com.example.demo.modules.collection.dto.CollectionUpdateRequest;
import com.example.demo.modules.collection.service.CollectionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 藏品控制器
 * 处理藏品 CRUD、批量操作等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    /**
     * 分页查询藏品列表
     * GET /api/v1/collections?keyword=&status=&itemType=&workName=&characterName=&isForSale=&sortBy=&sortOrder=&page=&pageSize=
     */
    @GetMapping
    public PageResponse<CollectionDetailResponse> getList(@Valid CollectionQueryRequest query) {
        String userId = UserContext.getUserId();
        log.info("查询藏品列表，userId: {}, keyword: {}, status: {}, itemType: {}, page: {}",
                userId, query.getKeyword(), query.getStatus(), query.getItemType(), query.getPage());
        return collectionService.getList(userId, query);
    }

    /**
     * 查询藏品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CollectionDetailResponse> getDetail(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("查询藏品详情，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(collectionService.getDetail(userId, id));
    }

    /**
     * 创建藏品
     */
    @PostMapping
    public ApiResponse<Map<String, String>> create(@Valid @RequestBody CollectionCreateRequest req) {
        String userId = UserContext.getUserId();
        log.info("创建藏品，userId: {}, name: {}, itemType: {}", userId, req.getName(), req.getItemType());
        return ApiResponse.ok(collectionService.create(userId, req));
    }

    /**
     * 更新藏品
     */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String id, @Valid @RequestBody CollectionUpdateRequest req) {
        String userId = UserContext.getUserId();
        log.info("更新藏品，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(collectionService.update(userId, id, req));
    }

    /**
     * 删除藏品（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("删除藏品，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(collectionService.delete(userId, id));
    }

    /**
     * 批量更新状态
     */
    @PatchMapping("/batch-status")
    public ApiResponse<Map<String, Object>> batchUpdateStatus(@Valid @RequestBody BatchStatusRequest req) {
        String userId = UserContext.getUserId();
        log.info("批量更新藏品状态，userId: {}, ids: {}, status: {}", userId, req.getIds(), req.getStatus());
        return ApiResponse.ok(collectionService.batchUpdateStatus(userId, req.getIds(), req.getStatus()));
    }

    /**
     * 批量删除藏品
     */
    @DeleteMapping("/batch")
    public ApiResponse<Map<String, Object>> batchDelete(@Valid @RequestBody BatchDeleteRequest req) {
        String userId = UserContext.getUserId();
        log.info("批量删除藏品，userId: {}, ids: {}", userId, req.getIds());
        return ApiResponse.ok(collectionService.batchDelete(userId, req.getIds()));
    }
}
