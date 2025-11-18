package org.xiaobuding.hotsearchaiplatform.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据去重工具类
 */
public class DataDeduplicationUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataDeduplicationUtil.class);

    /**
     * 按标题去重（保留热度最高的）
     */
    public static List<HotSearchItem> deduplicateByTitle(List<HotSearchItem> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        Map<String, HotSearchItem> deduplicatedMap = new LinkedHashMap<>();
        
        for (HotSearchItem item : items) {
            String title = normalizeTitle(item.getTitle());
            
            if (!deduplicatedMap.containsKey(title)) {
                deduplicatedMap.put(title, item);
            } else {
                HotSearchItem existing = deduplicatedMap.get(title);
                if (item.getHeat() > existing.getHeat()) {
                    deduplicatedMap.put(title, item);
                }
            }
        }
        
        return new ArrayList<>(deduplicatedMap.values());
    }

    /**
     * 按标题和URL去重
     */
    public static List<HotSearchItem> deduplicateByTitleAndUrl(List<HotSearchItem> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        Set<String> seenKeys = new HashSet<>();
        List<HotSearchItem> result = new ArrayList<>();
        
        for (HotSearchItem item : items) {
            String key = normalizeTitle(item.getTitle()) + "|" + item.getUrl();
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                result.add(item);
            }
        }
        
        return result;
    }

    /**
     * 标准化标题（去除空格、转小写等）
     */
    private static String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.trim().toLowerCase();
    }

    /**
     * 按平台分组去重
     */
    public static Map<String, List<HotSearchItem>> deduplicateByPlatform(List<HotSearchItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }

        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getPlatform().name(),
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                DataDeduplicationUtil::deduplicateByTitle
                        )
                ));
    }
}
