package com.example.demo.modules.wishlist.controller;

import com.example.demo.common.interceptor.UserContext;
import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.PageResponse;
import com.example.demo.modules.wishlist.dto.WishlistConvertRequest;
import com.example.demo.modules.wishlist.dto.WishlistCreateRequest;
import com.example.demo.modules.wishlist.dto.WishlistDetailResponse;
import com.example.demo.modules.wishlist.dto.WishlistUpdateRequest;
import com.example.demo.modules.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 心愿单控制器
 * 处理心愿单 CRUD、心愿转收藏等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    /**
     * 分页查询心愿单列表
     * GET /api/v1/wishlist?keyword=&status=&page=&pageSize=
     */
    @GetMapping
    public PageResponse<WishlistDetailResponse> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        String userId = UserContext.getUserId();
        log.info("查询心愿单列表，userId: {}, keyword: {}, status: {}, page: {}", userId, keyword, status, page);
        return wishlistService.getList(userId, keyword, status, page, pageSize);
    }

    /**
     * 查询心愿单详情
     * GET /api/v1/wishlist/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<WishlistDetailResponse> getDetail(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("查询心愿单详情，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(wishlistService.getDetail(userId, id));
    }

    /**
     * 创建心愿单
     * POST /api/v1/wishlist
     */
    @PostMapping
    public ApiResponse<Map<String, String>> create(@Valid @RequestBody WishlistCreateRequest req) {
        String userId = UserContext.getUserId();
        log.info("创建心愿单，userId: {}, name: {}", userId, req.getName());
        return ApiResponse.ok(wishlistService.create(userId, req));
    }

    /**
     * 更新心愿单
     * PUT /api/v1/wishlist/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String id,
                                                   @Valid @RequestBody WishlistUpdateRequest req) {
        String userId = UserContext.getUserId();
        log.info("更新心愿单，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(wishlistService.update(userId, id, req));
    }

    /**
     * 删除心愿单（逻辑删除）
     * DELETE /api/v1/wishlist/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        String userId = UserContext.getUserId();
        log.info("删除心愿单，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(wishlistService.delete(userId, id));
    }

    /**
     * 心愿单转收藏
     * POST /api/v1/wishlist/{id}/convert-to-collection
     */
    @PostMapping("/{id}/convert-to-collection")
    public ApiResponse<Map<String, String>> convertToCollection(@PathVariable String id,
                                                                @Valid @RequestBody WishlistConvertRequest req) {
        String userId = UserContext.getUserId();
        log.info("心愿单转收藏，userId: {}, id: {}", userId, id);
        return ApiResponse.ok(wishlistService.convertToCollection(userId, id, req));
    }
}
