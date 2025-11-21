package org.xiaobuding.hotsearchaiplatform.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.SearchService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private final HotSearchRepository hotSearchRepository;

    public SearchServiceImpl(HotSearchRepository hotSearchRepository) {
        this.hotSearchRepository = hotSearchRepository;
    }

    @Override
    @Cacheable(value = "searchResults", key = "#keyword.toLowerCase()")
    public List<HotSearchItem> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String normalizedKeyword = keyword.trim().toLowerCase();
        logger.debug("Searching for keyword: {}", normalizedKeyword);
        
        // 获取所有数据（考虑数据量大时，可以限制时间范围）
        List<HotSearchItem> allItems = hotSearchRepository.findAll();
        
        // 多维度搜索和评分
        List<HotSearchItem> results = allItems.stream()
                .filter(item -> item.getTitle() != null)
                .map(item -> {
                    int score = calculateRelevanceScore(item, normalizedKeyword);
                    return new ScoredItem(item, score);
                })
                .filter(scored -> scored.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // 按分数降序
                .limit(50) // 限制返回数量
                .map(scored -> scored.item)
                .collect(Collectors.toList());
        
        logger.debug("Found {} results for keyword: {}", results.size(), normalizedKeyword);
        return results;
    }
    
    /**
     * 计算相关性分数（最简化增强）
     * 1. 完全匹配：100分
     * 2. 开头匹配：80分
     * 3. 包含匹配：60分
     * 4. 热度加成：+10分（如果热度>100万）
     * 5. 时间加成：+5分（如果是最新的）
     */
    private int calculateRelevanceScore(HotSearchItem item, String keyword) {
        String title = item.getTitle().toLowerCase();
        int score = 0;
        
        // 完全匹配
        if (title.equals(keyword)) {
            score = 100;
        }
        // 开头匹配
        else if (title.startsWith(keyword)) {
            score = 80;
        }
        // 包含匹配
        else if (title.contains(keyword)) {
            score = 60;
        }
        // 分词匹配（简单实现）
        else {
            String[] keywordParts = keyword.split("[\\s,，。！？]+");
            int matchCount = 0;
            for (String part : keywordParts) {
                if (part.length() >= 2 && title.contains(part)) {
                    matchCount++;
                }
            }
            if (matchCount > 0) {
                score = 30 + (matchCount * 10); // 每个匹配词+10分
            }
        }
        
        // 热度加成
        if (item.getHeat() != null && item.getHeat() > 1000000) {
            score += 10;
        }
        
        // 时间加成（最近1小时内的数据）
        if (item.getCapturedAt() != null) {
            long hoursAgo = java.time.Duration.between(item.getCapturedAt(), 
                    java.time.LocalDateTime.now()).toHours();
            if (hoursAgo <= 1) {
                score += 5;
            }
        }
        
        return score;
    }
    
    /**
     * 内部类：带分数的搜索结果项
     */
    private static class ScoredItem {
        final HotSearchItem item;
        final int score;
        
        ScoredItem(HotSearchItem item, int score) {
            this.item = item;
            this.score = score;
        }
    }
}
