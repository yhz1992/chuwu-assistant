package com.example.demo.modules.salelist.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.constant.CollectionStatusEnum;
import com.example.demo.common.constant.SaleListStatusEnum;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.PageResponse;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.example.demo.modules.salelist.dto.*;
import com.example.demo.modules.salelist.entity.SaleList;
import com.example.demo.modules.salelist.entity.SaleListItem;
import com.example.demo.modules.salelist.mapper.SaleListItemMapper;
import com.example.demo.modules.salelist.mapper.SaleListMapper;
import com.example.demo.modules.share.entity.Share;
import com.example.demo.modules.share.mapper.ShareMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 出售清单服务
 * 处理清单的增删改查、生成、复制等业务逻辑
 */
@Slf4j
@Service
public class SaleListService {

    private final SaleListMapper saleListMapper;
    private final SaleListItemMapper saleListItemMapper;
    private final CollectionMapper collectionMapper;
    private final ShareMapper shareMapper;
    private final SnowflakeIdUtils snowflakeIdUtils;
    private final TextGenerationService textGenerationService;
    private final ObjectMapper objectMapper;

    public SaleListService(SaleListMapper saleListMapper,
                           SaleListItemMapper saleListItemMapper,
                           CollectionMapper collectionMapper,
                           ShareMapper shareMapper,
                           SnowflakeIdUtils snowflakeIdUtils,
                           TextGenerationService textGenerationService,
                           ObjectMapper objectMapper) {
        this.saleListMapper = saleListMapper;
        this.saleListItemMapper = saleListItemMapper;
        this.collectionMapper = collectionMapper;
        this.shareMapper = shareMapper;
        this.snowflakeIdUtils = snowflakeIdUtils;
        this.textGenerationService = textGenerationService;
        this.objectMapper = objectMapper;
    }

    /**
     * 分页查询清单列表
     *
     * @param userId   用户 ID
     * @param status   状态筛选（可选）
     * @param page     页码
     * @param pageSize 每页大小
     * @return 分页响应
     */
    public PageResponse<SaleListListResponse> getList(String userId, String status, long page, long pageSize) {
        QueryWrapper<SaleList> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.isNull("deleted_at");

        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("created_at");

        Page<SaleList> pageParam = new Page<>(page, pageSize);
        IPage<SaleList> result = saleListMapper.selectPage(pageParam, wrapper);

        List<SaleListListResponse> list = result.getRecords().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());

        return PageResponse.ok(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    /**
     * 查询清单详情（含商品列表）
     *
     * @param userId 用户 ID
     * @param id     清单 ID
     * @return 清单详情
     */
    public SaleListDetailResponse getDetail(String userId, String id) {
        SaleList saleList = getOwnedSaleList(userId, id);
        List<SaleListItem> items = saleListItemMapper.selectBySaleListId(id);

        return toDetailResponse(saleList, items);
    }

    /**
     * 创建出售清单
     *
     * @param userId 用户 ID
     * @param req    创建请求
     * @return 创建的清单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> create(String userId, SaleListCreateRequest req) {
        // 创建清单
        SaleList saleList = new SaleList();
        String saleListId = snowflakeIdUtils.nextIdWithPrefix("sl_");
        saleList.setId(saleListId);
        saleList.setUserId(userId);
        saleList.setTitle(req.getTitle());
        saleList.setDescription(req.getDescription());
        saleList.setStatus(SaleListStatusEnum.DRAFT.getValue());
        saleList.setTotalCount(0);
        saleList.setTotalPrice(BigDecimal.ZERO);

        // 交易规则转为 JSON
        if (req.getTradeRule() != null) {
            saleList.setTradeRule(toJson(req.getTradeRule()));
        }

        saleList.setVersion(1);
        LocalDateTime now = LocalDateTime.now();
        saleList.setCreatedAt(now);
        saleList.setUpdatedAt(now);
        saleListMapper.insert(saleList);

        // 创建商品
        if (req.getItems() != null && !req.getItems().isEmpty()) {
            createItems(saleListId, userId, req.getItems());
        }

        // 重新统计总数和总价
        updateListSummary(saleListId);

        log.info("出售清单创建成功，id: {}, userId: {}, title: {}", saleListId, userId, req.getTitle());

        Map<String, String> result = new HashMap<>();
        result.put("id", saleListId);
        return result;
    }

    /**
     * 更新出售清单（全量更新商品）
     *
     * @param userId 用户 ID
     * @param id     清单 ID
     * @param req    更新请求
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> update(String userId, String id, SaleListUpdateRequest req) {
        SaleList saleList = getOwnedSaleList(userId, id);

        // 仅草稿状态可修改
        if (!SaleListStatusEnum.DRAFT.getValue().equals(saleList.getStatus())) {
            throw new BusinessException(ResultCode.SALE_LIST_ALREADY_GENERATED);
        }

        // 更新清单字段
        if (req.getTitle() != null) {
            saleList.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            saleList.setDescription(req.getDescription());
        }
        if (req.getTradeRule() != null) {
            saleList.setTradeRule(toJson(req.getTradeRule()));
        }

        saleList.setUpdatedAt(LocalDateTime.now());
        saleListMapper.updateById(saleList);

        // 删除旧商品，重新创建
        if (req.getItems() != null) {
            saleListItemMapper.deleteBySaleListId(id);
            createItems(id, userId, req.getItems());
            updateListSummary(id);
        }

        log.info("出售清单更新成功，id: {}", id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("updatedAt", saleList.getUpdatedAt());
        return result;
    }

    /**
     * 删除出售清单（逻辑删除清单，物理删除商品）
     *
     * @param userId 用户 ID
     * @param id     清单 ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String userId, String id) {
        SaleList saleList = getOwnedSaleList(userId, id);

        // 逻辑删除清单
        saleList.setDeletedAt(LocalDateTime.now());
        saleList.setUpdatedAt(LocalDateTime.now());
        saleListMapper.updateById(saleList);

        // 物理删除商品
        int deletedCount = saleListItemMapper.deleteBySaleListId(id);

        log.info("出售清单已删除，id: {}, deletedItems: {}", id, deletedCount);
        return true;
    }

    /**
     * 复制清单
     *
     * @param userId 用户 ID
     * @param id     清单 ID
     * @return 新清单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> duplicate(String userId, String id) {
        SaleList source = getOwnedSaleList(userId, id);
        List<SaleListItem> sourceItems = saleListItemMapper.selectBySaleListId(id);

        // 创建新清单
        SaleList newList = new SaleList();
        String newId = snowflakeIdUtils.nextIdWithPrefix("sl_");
        newList.setId(newId);
        newList.setUserId(userId);
        newList.setTitle(source.getTitle() + "(副本)");
        newList.setDescription(source.getDescription());
        newList.setStatus(SaleListStatusEnum.DRAFT.getValue());
        newList.setTotalCount(0);
        newList.setTotalPrice(BigDecimal.ZERO);
        newList.setTradeRule(source.getTradeRule());
        newList.setWatermark(source.getWatermark());
        newList.setVersion(1);
        LocalDateTime now = LocalDateTime.now();
        newList.setCreatedAt(now);
        newList.setUpdatedAt(now);
        saleListMapper.insert(newList);

        // 复制商品
        for (SaleListItem sourceItem : sourceItems) {
            SaleListItem newItem = new SaleListItem();
            newItem.setId(snowflakeIdUtils.nextIdWithPrefix("sli_"));
            newItem.setSaleListId(newId);
            newItem.setCollectionItemId(sourceItem.getCollectionItemId());
            newItem.setCollectionSnapshot(sourceItem.getCollectionSnapshot());
            newItem.setName(sourceItem.getName());
            newItem.setImage(sourceItem.getImage());
            newItem.setPrice(sourceItem.getPrice());
            newItem.setQuantity(sourceItem.getQuantity() != null ? sourceItem.getQuantity() : 1);
            newItem.setStatus(sourceItem.getStatus());
            newItem.setFlawNote(sourceItem.getFlawNote());
            newItem.setShippingRule(sourceItem.getShippingRule());
            newItem.setBargainRule(sourceItem.getBargainRule());
            newItem.setBundleRule(sourceItem.getBundleRule());
            newItem.setNote(sourceItem.getNote());
            newItem.setSortOrder(sourceItem.getSortOrder());
            newItem.setCreatedAt(now);
            newItem.setUpdatedAt(now);
            saleListItemMapper.insert(newItem);
        }

        // 重新统计
        updateListSummary(newId);

        log.info("出售清单复制成功，sourceId: {}, newId: {}, userId: {}", id, newId, userId);

        Map<String, String> result = new HashMap<>();
        result.put("id", newId);
        return result;
    }

    /**
     * 生成清单（创建分享记录、生成文案）
     *
     * @param userId 用户 ID
     * @param id     清单 ID
     * @param req    生成请求
     * @return 生成结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SaleListGenerateResponse generate(String userId, String id, SaleListGenerateRequest req) {
        SaleList saleList = getOwnedSaleList(userId, id);
        List<SaleListItem> items = saleListItemMapper.selectBySaleListId(id);

        if (items.isEmpty()) {
            throw new BusinessException(ResultCode.SALE_LIST_EMPTY);
        }

        // 生成分享 ID
        String shareId = snowflakeIdUtils.nextIdWithPrefix("shr_");

        // 创建分享记录
        Share share = new Share();
        share.setId(shareId);
        share.setSaleListId(id);
        share.setUserId(userId);
        share.setIsPublic(true);
        share.setViewCount(0);
        LocalDateTime now = LocalDateTime.now();
        share.setCreatedAt(now);
        share.setUpdatedAt(now);
        shareMapper.insert(share);

        // 生成文案
        Map<String, String> texts = textGenerationService.generateAll(saleList, items);

        // 如果有自定义平台文案，覆盖生成结果
        if (req.getPlatformTexts() != null && !req.getPlatformTexts().isEmpty()) {
            req.getPlatformTexts().forEach((platform, customText) -> {
                if (StringUtils.hasText(customText)) {
                    texts.put(platform, customText);
                }
            });
        }

        // 更新清单状态
        saleList.setStatus(SaleListStatusEnum.GENERATED.getValue());
        if (req.getTemplateId() != null) {
            saleList.setTemplateId(req.getTemplateId());
        }
        if (req.getWatermark() != null) {
            saleList.setWatermark(req.getWatermark());
        }
        saleList.setShareId(shareId);
        saleList.setUpdatedAt(now);
        saleListMapper.updateById(saleList);

        log.info("出售清单生成成功，id: {}, shareId: {}, userId: {}", id, shareId, userId);

        return SaleListGenerateResponse.builder()
                .saleListId(id)
                .generatedImage(saleList.getGeneratedImage())
                .shareId(shareId)
                .texts(texts)
                .build();
    }

    /**
     * 更新清单分享状态为已分享
     *
     * @param userId 用户 ID
     * @param id     清单 ID
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateShareStatus(String userId, String id) {
        SaleList saleList = getOwnedSaleList(userId, id);

        if (!SaleListStatusEnum.GENERATED.getValue().equals(saleList.getStatus())) {
            throw new BusinessException(ResultCode.UNSUPPORTED_OPERATION, "仅已生成的清单可以标记为已分享");
        }

        saleList.setStatus(SaleListStatusEnum.SHARED.getValue());
        saleList.setUpdatedAt(LocalDateTime.now());
        saleListMapper.updateById(saleList);

        log.info("出售清单分享状态已更新，id: {}, userId: {}", id, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("status", SaleListStatusEnum.SHARED.getValue());
        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取用户拥有的清单，校验归属权和有效性
     */
    private SaleList getOwnedSaleList(String userId, String id) {
        SaleList saleList = saleListMapper.selectById(id);
        if (saleList == null || saleList.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.SALE_LIST_NOT_FOUND);
        }
        if (!saleList.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SALE_LIST_NOT_FOUND);
        }
        return saleList;
    }

    /**
     * 创建清单中的商品
     */
    private void createItems(String saleListId, String userId, List<SaleListItemDTO> itemDTOs) {
        LocalDateTime now = LocalDateTime.now();
        int sortOrder = 1;

        for (SaleListItemDTO dto : itemDTOs) {
            SaleListItem item = new SaleListItem();
            item.setId(snowflakeIdUtils.nextIdWithPrefix("sli_"));
            item.setSaleListId(saleListId);
            item.setCollectionItemId(dto.getCollectionItemId());
            item.setPrice(dto.getPrice());
            item.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
            item.setStatus(dto.getStatus() != null ? dto.getStatus() : "available");
            item.setFlawNote(dto.getFlawNote());
            item.setShippingRule(dto.getShippingRule());
            item.setBargainRule(dto.getBargainRule());
            item.setBundleRule(dto.getBundleRule());
            item.setNote(dto.getNote());
            item.setSortOrder(sortOrder++);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);

            // 从藏品中获取快照信息
            if (dto.getCollectionItemId() != null) {
                CollectionItem collectionItem = collectionMapper.selectById(dto.getCollectionItemId());
                if (collectionItem != null && !collectionItem.getUserId().equals(userId)) {
                    throw new BusinessException(ResultCode.COLLECTION_NOT_FOUND, "藏品不属于当前用户");
                }
                if (collectionItem != null) {
                    item.setName(dto.getName() != null ? dto.getName() : collectionItem.getName());
                    item.setImage(collectionItem.getCoverImage());

                    // 创建藏品快照
                    Map<String, Object> snapshot = new LinkedHashMap<>();
                    snapshot.put("name", collectionItem.getName());

                    // 解析 images JSON 字符串为列表，避免双重序列化
                    List<String> parsedImages = new ArrayList<>();
                    if (collectionItem.getImages() != null && !collectionItem.getImages().isEmpty()) {
                        try {
                            parsedImages = JSONUtil.toList(collectionItem.getImages(), String.class);
                        } catch (Exception e) {
                            log.warn("解析 images JSON 失败，使用空列表: {}", collectionItem.getImages(), e);
                        }
                    }
                    snapshot.put("images", parsedImages);
                    snapshot.put("coverImage", collectionItem.getCoverImage());
                    snapshot.put("workName", collectionItem.getWorkName());
                    snapshot.put("characterName", collectionItem.getCharacterName());
                    snapshot.put("itemType", collectionItem.getItemType());
                    snapshot.put("purchasePrice", collectionItem.getPurchasePrice());
                    snapshot.put("quantity", collectionItem.getQuantity());
                    snapshot.put("purchaseChannel", collectionItem.getPurchaseChannel());
                    snapshot.put("purchaseDate", collectionItem.getPurchaseDate());
                    snapshot.put("note", collectionItem.getNote());
                    item.setCollectionSnapshot(JSONUtil.toJsonStr(snapshot));

                    // 更新藏品状态为"待出物"
                    if (!CollectionStatusEnum.FOR_SALE.getValue().equals(collectionItem.getStatus())) {
                        collectionItem.setStatus(CollectionStatusEnum.FOR_SALE.getValue());
                        collectionItem.setIsForSale(true);
                        collectionItem.setUpdatedAt(now);
                        collectionMapper.updateById(collectionItem);
                    }
                }
            }

            if (item.getName() == null) {
                item.setName(dto.getName() != null ? dto.getName() : "未命名");
            }

            saleListItemMapper.insert(item);
        }
    }

    /**
     * 更新清单的统计信息（商品总数和总价）
     */
    private void updateListSummary(String saleListId) {
        List<SaleListItem> items = saleListItemMapper.selectBySaleListId(saleListId);

        int totalCount = items.size();
        BigDecimal totalPrice = items.stream()
                .filter(item -> item.getPrice() != null)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(
                        item.getQuantity() != null ? item.getQuantity() : 1)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SaleList saleList = saleListMapper.selectById(saleListId);
        if (saleList != null) {
            saleList.setTotalCount(totalCount);
            saleList.setTotalPrice(totalPrice);
            saleList.setUpdatedAt(LocalDateTime.now());
            saleListMapper.updateById(saleList);
        }
    }

    /**
     * 将对象转为 JSON 字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败", e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串转为指定类型
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 反序列化失败", e);
            return null;
        }
    }

    /**
     * 转换为列表响应 DTO
     */
    private SaleListListResponse toListResponse(SaleList saleList) {
        return SaleListListResponse.builder()
                .id(saleList.getId())
                .title(saleList.getTitle())
                .description(saleList.getDescription())
                .status(saleList.getStatus())
                .totalCount(saleList.getTotalCount())
                .totalPrice(saleList.getTotalPrice())
                .generatedImage(saleList.getGeneratedImage())
                .shareId(saleList.getShareId())
                .createdAt(saleList.getCreatedAt())
                .updatedAt(saleList.getUpdatedAt())
                .build();
    }

    /**
     * 转换为详情响应 DTO
     */
    private SaleListDetailResponse toDetailResponse(SaleList saleList, List<SaleListItem> items) {
        List<SaleListItemDTO> itemDTOs = items.stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());

        return SaleListDetailResponse.builder()
                .id(saleList.getId())
                .title(saleList.getTitle())
                .description(saleList.getDescription())
                .templateId(saleList.getTemplateId())
                .status(saleList.getStatus())
                .totalCount(saleList.getTotalCount())
                .totalPrice(saleList.getTotalPrice())
                .generatedImage(saleList.getGeneratedImage())
                .shareId(saleList.getShareId())
                .tradeRule(fromJson(saleList.getTradeRule(), TradeRuleDTO.class))
                .watermark(saleList.getWatermark())
                .items(itemDTOs)
                .createdAt(saleList.getCreatedAt())
                .updatedAt(saleList.getUpdatedAt())
                .build();
    }

    /**
     * 实体转为商品 DTO
     */
    private SaleListItemDTO toItemDTO(SaleListItem item) {
        SaleListItemDTO dto = new SaleListItemDTO();
        dto.setId(item.getId());
        dto.setSaleListId(item.getSaleListId());
        dto.setCollectionItemId(item.getCollectionItemId());
        dto.setName(item.getName());
        dto.setImage(item.getImage());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setStatus(item.getStatus());
        dto.setFlawNote(item.getFlawNote());
        dto.setShippingRule(item.getShippingRule());
        dto.setBargainRule(item.getBargainRule());
        dto.setBundleRule(item.getBundleRule());
        dto.setNote(item.getNote());
        dto.setSortOrder(item.getSortOrder());
        dto.setSoldAt(item.getSoldAt());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}
