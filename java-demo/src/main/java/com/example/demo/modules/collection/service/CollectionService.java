package com.example.demo.modules.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.PageResponse;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.collection.dto.CollectionCreateRequest;
import com.example.demo.modules.collection.dto.CollectionDetailResponse;
import com.example.demo.modules.collection.dto.CollectionQueryRequest;
import com.example.demo.modules.collection.dto.CollectionUpdateRequest;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 藏品服务
 * 处理藏品的增删改查、批量操作等业务逻辑
 */
@Slf4j
@Service
public class CollectionService {

    private static final long FREE_MAX_COUNT = 50L;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("created_at", "updated_at", "purchase_price", "name");

    private final CollectionMapper collectionMapper;
    private final SnowflakeIdUtils snowflakeIdUtils;
    private final ObjectMapper objectMapper;

    public CollectionService(CollectionMapper collectionMapper, SnowflakeIdUtils snowflakeIdUtils, ObjectMapper objectMapper) {
        this.collectionMapper = collectionMapper;
        this.snowflakeIdUtils = snowflakeIdUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 分页查询藏品列表
     */
    public PageResponse<CollectionDetailResponse> getList(String userId, CollectionQueryRequest query) {
        // 构建查询条件
        QueryWrapper<CollectionItem> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.isNull("deleted_at");

        // 关键词模糊搜索
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w
                    .like("search_text", keyword)
                    .or().like("name", keyword)
                    .or().like("work_name", keyword)
                    .or().like("character_name", keyword)
                    .or().like("note", keyword)
            );
        }

        // 状态筛选
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq("status", query.getStatus());
        }

        // 类型筛选
        if (StringUtils.hasText(query.getItemType())) {
            wrapper.eq("item_type", query.getItemType());
        }

        // 作品筛选
        if (StringUtils.hasText(query.getWorkName())) {
            wrapper.eq("work_name", query.getWorkName());
        }

        // 角色筛选
        if (StringUtils.hasText(query.getCharacterName())) {
            wrapper.eq("character_name", query.getCharacterName());
        }

        // 待出物筛选
        if (query.getIsForSale() != null) {
            wrapper.eq("is_for_sale", query.getIsForSale());
        }

        // 排序
        String sortField = resolveSortField(query.getSortBy());
        boolean isAsc = !"desc".equalsIgnoreCase(query.getSortOrder());
        if (sortField != null) {
            wrapper.orderBy(true, isAsc, sortField);
        } else {
            wrapper.orderByDesc("created_at");
        }

        // 分页查询
        Page<CollectionItem> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<CollectionItem> result = collectionMapper.selectPageByUser(page, userId, wrapper);

        List<CollectionDetailResponse> list = result.getRecords().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        return PageResponse.ok(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    /**
     * 查询藏品详情
     */
    public CollectionDetailResponse getDetail(String userId, String id) {
        CollectionItem item = collectionMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND);
        }
        return toDetailResponse(item);
    }

    /**
     * 创建藏品
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> create(String userId, CollectionCreateRequest req) {
        // 校验免费版上限（50条）
        Long count = collectionMapper.selectCount(
                new QueryWrapper<CollectionItem>().eq("user_id", userId));
        if (count >= FREE_MAX_COUNT) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "免费版最多添加50件藏品，请升级会员");
        }

        CollectionItem item = new CollectionItem();
        item.setId(snowflakeIdUtils.nextIdWithPrefix("ci_"));
        item.setUserId(userId);
        item.setName(req.getName());
        item.setImages(JSONUtil.toJsonStr(req.getImages()));

        // 封面图默认取第一张
        if (req.getImages() != null && !req.getImages().isEmpty()) {
            item.setCoverImage(req.getImages().get(0));
        }

        item.setWorkName(req.getWorkName());
        item.setCharacterName(req.getCharacterName());
        item.setItemType(req.getItemType());
        item.setPurchasePrice(req.getPurchasePrice());
        item.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1);
        item.setPurchaseChannel(req.getPurchaseChannel());

        if (StringUtils.hasText(req.getPurchaseDate())) {
            item.setPurchaseDate(LocalDate.parse(req.getPurchaseDate()));
        }

        item.setStatus(req.getStatus());

        // isForSale：请求指定则用指定值，否则根据状态自动判断
        item.setIsForSale(req.getIsForSale() != null ? req.getIsForSale() : "for_sale".equals(req.getStatus()));

        item.setNote(req.getNote());
        item.setSalePrice(req.getSalePrice());
        item.setFlawNote(req.getFlawNote());
        item.setShippingRule(req.getShippingRule());
        item.setBargainRule(req.getBargainRule());
        item.setBundleRule(req.getBundleRule());

        // 默认审核状态
        item.setAuditStatus("pending");
        // 搜索全文
        item.setSearchText(buildSearchText(req.getName(), req.getWorkName(), req.getCharacterName()));
        // 排序序号默认当前时间戳
        item.setSortIndex(System.currentTimeMillis());
        item.setVersion(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        collectionMapper.insert(item);
        log.info("藏品创建成功，id: {}, userId: {}, name: {}", item.getId(), userId, req.getName());

        Map<String, String> result = new HashMap<>();
        result.put("id", item.getId());
        return result;
    }

    /**
     * 更新藏品
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(String userId, String id, CollectionUpdateRequest req) {
        CollectionItem item = collectionMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND);
        }

        // 更新字段（仅更新非 null 值）
        if (req.getName() != null) item.setName(req.getName());
        if (req.getImages() != null) {
            item.setImages(JSONUtil.toJsonStr(req.getImages()));
            item.setCoverImage(req.getImages().isEmpty() ? null : req.getImages().get(0));
        }
        if (req.getWorkName() != null) item.setWorkName(req.getWorkName());
        if (req.getCharacterName() != null) item.setCharacterName(req.getCharacterName());
        if (req.getItemType() != null) item.setItemType(req.getItemType());
        if (req.getPurchasePrice() != null) item.setPurchasePrice(req.getPurchasePrice());
        if (req.getQuantity() != null) item.setQuantity(req.getQuantity());
        if (req.getPurchaseChannel() != null) item.setPurchaseChannel(req.getPurchaseChannel());
        if (req.getPurchaseDate() != null) item.setPurchaseDate(LocalDate.parse(req.getPurchaseDate()));
        if (req.getStatus() != null) {
            item.setStatus(req.getStatus());
            // 状态变更时同步更新 isForSale
            item.setIsForSale("for_sale".equals(req.getStatus()));
        }
        if (req.getNote() != null) item.setNote(req.getNote());
        if (req.getIsForSale() != null) item.setIsForSale(req.getIsForSale());
        if (req.getSalePrice() != null) item.setSalePrice(req.getSalePrice());
        if (req.getFlawNote() != null) item.setFlawNote(req.getFlawNote());
        if (req.getShippingRule() != null) item.setShippingRule(req.getShippingRule());
        if (req.getBargainRule() != null) item.setBargainRule(req.getBargainRule());
        if (req.getBundleRule() != null) item.setBundleRule(req.getBundleRule());

        // 重建搜索全文
        item.setSearchText(buildSearchText(item.getName(), item.getWorkName(), item.getCharacterName()));
        item.setUpdatedAt(LocalDateTime.now());

        collectionMapper.updateById(item);
        log.info("藏品更新成功，id: {}", id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("updatedAt", item.getUpdatedAt());
        return result;
    }

    /**
     * 逻辑删除藏品
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String userId, String id) {
        CollectionItem item = collectionMapper.selectById(id);
        if (item == null || item.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND);
        }

        item.setDeletedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        collectionMapper.updateById(item);
        log.info("藏品已删除（逻辑删除），id: {}", id);
        return true;
    }

    /**
     * 批量更新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdateStatus(String userId, List<String> ids, String status) {
        List<CollectionItem> items = collectionMapper.selectBatchIds(ids);
        List<CollectionItem> toUpdate = new ArrayList<>();

        for (CollectionItem item : items) {
            if (item == null || item.getDeletedAt() != null) continue;
            if (!item.getUserId().equals(userId)) continue;
            item.setStatus(status);
            item.setIsForSale("for_sale".equals(status));
            item.setUpdatedAt(LocalDateTime.now());
            toUpdate.add(item);
        }

        if (toUpdate.isEmpty()) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND, "没有找到可更新的藏品");
        }

        for (CollectionItem item : toUpdate) {
            collectionMapper.updateById(item);
        }

        log.info("批量更新状态完成，userId: {}, count: {}, status: {}", userId, toUpdate.size(), status);

        Map<String, Object> result = new HashMap<>();
        result.put("updatedCount", toUpdate.size());
        return result;
    }

    /**
     * 批量逻辑删除
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchDelete(String userId, List<String> ids) {
        List<CollectionItem> items = collectionMapper.selectBatchIds(ids);
        LocalDateTime now = LocalDateTime.now();
        List<CollectionItem> toDelete = new ArrayList<>();

        for (CollectionItem item : items) {
            if (item == null || item.getDeletedAt() != null) continue;
            if (!item.getUserId().equals(userId)) continue;
            item.setDeletedAt(now);
            item.setUpdatedAt(now);
            toDelete.add(item);
        }

        if (toDelete.isEmpty()) {
            throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND, "没有找到可删除的藏品");
        }

        for (CollectionItem item : toDelete) {
            collectionMapper.updateById(item);
        }

        log.info("批量删除完成，userId: {}, count: {}", userId, toDelete.size());

        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", toDelete.size());
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 将实体转换为详情响应 DTO
     */
    @SuppressWarnings("unchecked")
    private CollectionDetailResponse toDetailResponse(CollectionItem item) {
        // 解析 images JSON 字符串为数组
        List<String> imageList = new ArrayList<>();
        String rawImages = item.getImages();
        if (StringUtils.hasText(rawImages)) {
            try {
                imageList = objectMapper.readValue(rawImages, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (Exception e) {
                log.warn("解析 images JSON 失败: {} - 原始值: {}", e.getMessage(), rawImages);
                // 兜底: 如果解析失败，至少把 coverImage 放进去
                if (item.getCoverImage() != null) {
                    imageList.add(item.getCoverImage());
                }
            }
        }

        return CollectionDetailResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .name(item.getName())
                .images(imageList)
                .coverImage(item.getCoverImage())
                .workName(item.getWorkName())
                .characterName(item.getCharacterName())
                .itemType(item.getItemType())
                .purchasePrice(item.getPurchasePrice())
                .quantity(item.getQuantity())
                .purchaseChannel(item.getPurchaseChannel())
                .purchaseDate(item.getPurchaseDate())
                .status(item.getStatus())
                .note(item.getNote())
                .isForSale(item.getIsForSale())
                .salePrice(item.getSalePrice())
                .flawNote(item.getFlawNote())
                .shippingRule(item.getShippingRule())
                .bargainRule(item.getBargainRule())
                .bundleRule(item.getBundleRule())
                .auditStatus(item.getAuditStatus())
                .auditMessage(item.getAuditMessage())
                .customTypeId(item.getCustomTypeId())
                .searchText(item.getSearchText())
                .sortIndex(item.getSortIndex())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    /**
     * 构建搜索全文（name + workName + characterName）
     */
    private String buildSearchText(String name, String workName, String characterName) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(name)) sb.append(name.trim()).append(" ");
        if (StringUtils.hasText(workName)) sb.append(workName.trim()).append(" ");
        if (StringUtils.hasText(characterName)) sb.append(characterName.trim()).append(" ");
        return sb.toString().trim();
    }

    /**
     * 解析排序字段，映射驼峰到数据库下划线命名，返回 null 则使用默认排序
     */
    private String resolveSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return null;
        }
        String dbField = switch (sortBy) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            case "purchasePrice" -> "purchase_price";
            case "name" -> "name";
            default -> null;
        };
        return dbField;
    }
}
