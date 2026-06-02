package com.example.demo.modules.wishlist.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.PageResponse;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.example.demo.modules.wishlist.dto.WishlistConvertRequest;
import com.example.demo.modules.wishlist.dto.WishlistCreateRequest;
import com.example.demo.modules.wishlist.dto.WishlistDetailResponse;
import com.example.demo.modules.wishlist.dto.WishlistUpdateRequest;
import com.example.demo.modules.wishlist.entity.WishlistItem;
import com.example.demo.modules.wishlist.mapper.WishlistMapper;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 心愿单服务
 * 处理心愿单的增删改查、心愿转收藏等业务逻辑
 */
@Slf4j
@Service
public class WishlistService {

    private final WishlistMapper wishlistMapper;
    private final CollectionMapper collectionMapper;
    private final SnowflakeIdUtils snowflakeIdUtils;

    public WishlistService(WishlistMapper wishlistMapper, CollectionMapper collectionMapper, SnowflakeIdUtils snowflakeIdUtils) {
        this.wishlistMapper = wishlistMapper;
        this.collectionMapper = collectionMapper;
        this.snowflakeIdUtils = snowflakeIdUtils;
    }

    /**
     * 分页查询心愿单列表
     *
     * @param userId   用户 ID
     * @param keyword  关键词（模糊搜索 name/workName/characterName）
     * @param status   状态筛选
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<WishlistDetailResponse> getList(String userId, String keyword, String status,
                                                        long page, long pageSize) {
        QueryWrapper<WishlistItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);

        // 关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                    .like("name", kw)
                    .or().like("work_name", kw)
                    .or().like("character_name", kw)
            );
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("created_at");

        Page<WishlistItem> pageParam = new Page<>(page, pageSize);
        IPage<WishlistItem> result = wishlistMapper.selectPage(pageParam, wrapper);

        List<WishlistDetailResponse> list = result.getRecords().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return PageResponse.ok(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    /**
     * 查询心愿单详情
     *
     * @param userId 用户 ID
     * @param id     心愿单 ID
     * @return 详情
     */
    public WishlistDetailResponse getDetail(String userId, String id) {
        WishlistItem item = wishlistMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }
        return toDetailResponse(item);
    }

    /**
     * 创建心愿单
     *
     * @param userId 用户 ID
     * @param req    创建请求
     * @return 包含心愿单 ID 的 Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> create(String userId, WishlistCreateRequest req) {
        WishlistItem item = new WishlistItem();
        item.setId(snowflakeIdUtils.nextIdWithPrefix("wi_"));
        item.setUserId(userId);
        item.setName(req.getName());
        item.setImage(req.getImage());
        item.setWorkName(req.getWorkName());
        item.setCharacterName(req.getCharacterName());
        item.setItemType(req.getItemType());
        item.setTargetPrice(req.getTargetPrice());
        item.setDesireLevel(StringUtils.hasText(req.getDesireLevel()) ? req.getDesireLevel() : "normal");
        item.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus() : "want");
        item.setNote(req.getNote());
        item.setVersion(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        wishlistMapper.insert(item);
        log.info("心愿单创建成功，id: {}, userId: {}, name: {}", item.getId(), userId, req.getName());

        Map<String, String> result = new HashMap<>();
        result.put("id", item.getId());
        return result;
    }

    /**
     * 更新心愿单
     *
     * @param userId 用户 ID
     * @param id     心愿单 ID
     * @param req    更新请求
     * @return 包含心愿单 ID 和更新时间的 Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(String userId, String id, WishlistUpdateRequest req) {
        WishlistItem item = wishlistMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }

        // 更新字段（仅更新非 null 值）
        if (req.getName() != null) item.setName(req.getName());
        if (req.getImage() != null) item.setImage(req.getImage());
        if (req.getWorkName() != null) item.setWorkName(req.getWorkName());
        if (req.getCharacterName() != null) item.setCharacterName(req.getCharacterName());
        if (req.getItemType() != null) item.setItemType(req.getItemType());
        if (req.getTargetPrice() != null) item.setTargetPrice(req.getTargetPrice());
        if (req.getDesireLevel() != null) item.setDesireLevel(req.getDesireLevel());
        if (req.getStatus() != null) item.setStatus(req.getStatus());
        if (req.getNote() != null) item.setNote(req.getNote());

        item.setUpdatedAt(LocalDateTime.now());
        wishlistMapper.updateById(item);
        log.info("心愿单更新成功，id: {}", id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("updatedAt", item.getUpdatedAt());
        return result;
    }

    /**
     * 逻辑删除心愿单
     *
     * @param userId 用户 ID
     * @param id     心愿单 ID
     * @return true 删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String userId, String id) {
        WishlistItem item = wishlistMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }

        item.setDeletedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        wishlistMapper.updateById(item);
        log.info("心愿单已删除（逻辑删除），id: {}", id);
        return true;
    }

    /**
     * 心愿单转收藏
     * 将心愿单转为收藏品，同时更新心愿单状态为已购买
     *
     * @param userId 用户 ID
     * @param id     心愿单 ID
     * @param req    转换请求
     * @return 包含 collectionId 和 wishlistId 的 Map
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> convertToCollection(String userId, String id, WishlistConvertRequest req) {
        WishlistItem item = wishlistMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.WISHLIST_NOT_FOUND);
        }

        // 创建收藏品
        CollectionItem collectionItem = new CollectionItem();
        collectionItem.setId(snowflakeIdUtils.nextIdWithPrefix("ci_"));
        collectionItem.setUserId(userId);
        collectionItem.setName(item.getName());
        collectionItem.setImages(item.getImage() != null ? JSONUtil.toJsonStr(List.of(item.getImage())) : null);
        collectionItem.setCoverImage(item.getImage());
        collectionItem.setWorkName(item.getWorkName());
        collectionItem.setCharacterName(item.getCharacterName());
        collectionItem.setItemType(item.getItemType());
        collectionItem.setPurchasePrice(req.getPurchasePrice());
        collectionItem.setQuantity(1);
        collectionItem.setPurchaseChannel(req.getPurchaseChannel());
        collectionItem.setStatus(req.getStatus());
        collectionItem.setIsForSale("for_sale".equals(req.getStatus()));
        collectionItem.setAuditStatus("pending");
        collectionItem.setVersion(1);
        collectionItem.setSortIndex(System.currentTimeMillis());
        collectionItem.setCreatedAt(LocalDateTime.now());
        collectionItem.setUpdatedAt(LocalDateTime.now());

        collectionMapper.insert(collectionItem);
        log.info("心愿单转收藏成功，collectionId: {}, wishlistId: {}", collectionItem.getId(), id);

        // 更新心愿单状态为已购买
        item.setStatus("bought");
        item.setUpdatedAt(LocalDateTime.now());
        wishlistMapper.updateById(item);
        log.info("心愿单状态更新为已购买，id: {}", id);

        Map<String, String> result = new HashMap<>();
        result.put("collectionId", collectionItem.getId());
        result.put("wishlistId", id);
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 将实体转换为详情响应 DTO
     */
    private WishlistDetailResponse toDetailResponse(WishlistItem item) {
        return WishlistDetailResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .name(item.getName())
                .image(item.getImage())
                .workName(item.getWorkName())
                .characterName(item.getCharacterName())
                .itemType(item.getItemType())
                .targetPrice(item.getTargetPrice())
                .desireLevel(item.getDesireLevel())
                .status(item.getStatus())
                .note(item.getNote())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
