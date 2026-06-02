package com.example.demo.modules.salelist.service;

import com.example.demo.modules.salelist.entity.SaleList;
import com.example.demo.modules.salelist.entity.SaleListItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文案规则引擎
 * 根据清单信息生成各平台出售文案，参照需求文档 §20
 */
@Slf4j
@Service
public class TextGenerationService {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,##0.00");

    private static final int MAX_TEXT_LENGTH = 1000;

    /**
     * 生成所有平台的文案
     *
     * @param saleList 清单信息
     * @param items    商品列表
     * @return key=平台标识, value=文案内容
     */
    public Map<String, String> generateAll(SaleList saleList, List<SaleListItem> items) {
        // 统计信息
        int count = items.size();
        BigDecimal totalPrice = items.stream()
                .filter(item -> item.getPrice() != null)
                .map(SaleListItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 格式化商品列表
        String itemsText = formatItems(items);

        // 聚合物流规则
        String shippingText = aggregateShipping(items);

        // 聚合砍价规则
        String bargainText = aggregateBargain(items);

        // 交易规则
        String contactNote = "";
        String extraRule = "";

        // 构建模板占位符数据
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("title", saleList.getTitle() != null ? saleList.getTitle() : "");
        placeholders.put("description", saleList.getDescription() != null ? saleList.getDescription() : "");
        placeholders.put("count", String.valueOf(count));
        placeholders.put("totalPrice", PRICE_FORMAT.format(totalPrice));
        placeholders.put("items", itemsText);
        placeholders.put("shippingRule", shippingText);
        placeholders.put("bargainRule", bargainText);
        placeholders.put("contactNote", contactNote);
        placeholders.put("extraRule", extraRule);

        // 生成各平台文案
        Map<String, String> texts = new LinkedHashMap<>();
        texts.put("xianyu", generateXianyu(placeholders));
        texts.put("xiaohongshu", generateXiaohongshu(placeholders));
        texts.put("weibo", generateWeibo(placeholders));
        texts.put("wechat", generateWechat(placeholders));

        // 后处理：清理空占位符、标点、截断
        texts.replaceAll((platform, text) -> postProcess(text));

        return texts;
    }

    // ==================== 平台模板 ====================

    /**
     * 闲鱼文案模板
     */
    private String generateXianyu(Map<String, String> ph) {
        return "【出物】" + ph.get("title") + "\n" +
                ph.get("description") + "\n" +
                "---\n" +
                "物品清单：\n" +
                ph.get("items") + "\n" +
                "---\n" +
                "📦 " + ph.get("shippingRule") + "\n" +
                "💬 " + ph.get("bargainRule") + "\n" +
                ph.get("contactNote") + "\n" +
                ph.get("extraRule");
    }

    /**
     * 小红书文案模板
     */
    private String generateXiaohongshu(Map<String, String> ph) {
        return ph.get("title") + "\n" +
                ph.get("description") + "\n" +
                "---\n" +
                ph.get("items") + "\n" +
                "---\n" +
                "📦 " + ph.get("shippingRule") + " | 💬 " + ph.get("bargainRule") + "\n" +
                ph.get("contactNote") + "\n" +
                ph.get("extraRule");
    }

    /**
     * 微博文案模板
     */
    private String generateWeibo(Map<String, String> ph) {
        return "【出物】" + ph.get("title") + "\n" +
                ph.get("description") + "\n" +
                "物品清单：\n" +
                ph.get("items") + "\n" +
                ph.get("shippingRule") + " " + ph.get("bargainRule") + "\n" +
                ph.get("contactNote") + "\n" +
                ph.get("extraRule");
    }

    /**
     * 微信群文案模板
     */
    private String generateWechat(Map<String, String> ph) {
        return "【出物】" + ph.get("title") + "\n" +
                ph.get("description") + "\n" +
                ph.get("items") + "\n" +
                ph.get("shippingRule") + " " + ph.get("bargainRule") + "\n" +
                ph.get("contactNote") + "\n" +
                ph.get("extraRule");
    }

    // ==================== 聚合方法 ====================

    /**
     * 格式化商品列表文本
     */
    private String formatItems(List<SaleListItem> items) {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (SaleListItem item : items) {
            sb.append(index).append(". ");
            sb.append(item.getName() != null ? item.getName() : "未命名");

            if (item.getPrice() != null) {
                sb.append(" - ¥").append(PRICE_FORMAT.format(item.getPrice()));
            }

            if (StringUtils.hasText(item.getFlawNote())) {
                sb.append(" (").append(item.getFlawNote()).append(")");
            }

            sb.append("\n");
            index++;
        }
        return sb.toString().trim();
    }

    /**
     * 聚合物流规则
     * - 全部 included → "全部包邮"
     * - 全部 not_included → "默认不包邮"
     * - 全部 conditional → "满额包邮（详见图）"
     * - 混合 → "包邮规则见图"
     */
    String aggregateShipping(List<SaleListItem> items) {
        Set<String> rules = items.stream()
                .map(SaleListItem::getShippingRule)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (rules.isEmpty()) {
            return "默认不包邮";
        }

        if (rules.size() == 1) {
            String rule = rules.iterator().next();
            return switch (rule) {
                case "included" -> "全部包邮";
                case "not_included" -> "默认不包邮";
                case "conditional" -> "满额包邮（详见图）";
                default -> "包邮规则见图";
            };
        }

        return "包邮规则见图";
    }

    /**
     * 聚合砍价规则
     * - 全部 no_bargain → "不刀"
     * - 全部 bargain → "可小刀"
     * - 全部 bundle_first → "打包优先"
     * - 混合 → "小刀规则见图"
     */
    String aggregateBargain(List<SaleListItem> items) {
        Set<String> rules = items.stream()
                .map(SaleListItem::getBargainRule)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (rules.isEmpty()) {
            return "可小刀";
        }

        if (rules.size() == 1) {
            String rule = rules.iterator().next();
            return switch (rule) {
                case "no_bargain" -> "不刀";
                case "bargain" -> "可小刀";
                case "bundle_first" -> "打包优先";
                default -> "小刀规则见图";
            };
        }

        return "小刀规则见图";
    }

    // ==================== 后处理 ====================

    /**
     * 文案后处理：空占位符删句 + 多余标点清理 + 超长截断
     */
    String postProcess(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 1. 移除空白占位符行（仅包含空白字符或仅为占位符本身的行）
        result = result.replaceAll("(?m)^[\\s&&[^\\n]]*\\n", "");

        // 2. 移除多余的连续标点符号（如 "。。" -> "。"）
        result = result.replaceAll("([。，、！？；：,.;:!?])\\1+", "$1");

        // 3. 移除行尾多余空白
        result = result.replaceAll("(?m)[ \\t]+$", "");

        // 4. 移除连续空行（保留一个换行）
        result = result.replaceAll("\\n{3,}", "\n\n");

        // 5. 首尾去空白
        result = result.trim();

        // 6. 超 1000 字截断
        if (result.length() > MAX_TEXT_LENGTH) {
            result = result.substring(0, MAX_TEXT_LENGTH);
        }

        return result;
    }
}
