package org.xiaobuding.hotsearchaiplatform.controller;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.*;
import org.xiaobuding.hotsearchaiplatform.util.DataDeduplicationUtil;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/hot-search")
@CrossOrigin(origins = "*")
public class HotSearchController {
    private static final Logger logger = LoggerFactory.getLogger(HotSearchController.class);
    private final HotSearchService hotSearchService;
    private final HotSearchCacheService cacheService;
    private final HotSearchRepository hotSearchRepository;
    private final CategoryClassificationService categoryClassificationService;
    private final ClassificationStatusService classificationStatusService;
    public HotSearchController(HotSearchService hotSearchService, HotSearchCacheService cacheService,
                               HotSearchRepository hotSearchRepository,
                               CategoryClassificationService categoryClassificationService,
                               ClassificationStatusService classificationStatusService) {
        this.hotSearchService = hotSearchService;
        this.cacheService = cacheService;
        this.hotSearchRepository = hotSearchRepository;
        this.categoryClassificationService = categoryClassificationService;
        this.classificationStatusService = classificationStatusService;
    }
    @GetMapping("/last-update")
    public ResponseEntity<ApiResponse<LastUpdateInfo>> getLastUpdateTime() {
        try {
            LastUpdateInfo updateInfo = new LastUpdateInfo();
            LocalDateTime globalLastUpdate = hotSearchRepository.findLatestUpdateTime().orElse(LocalDateTime.now());
            updateInfo.setLastUpdate(globalLastUpdate);
            Map<String, LocalDateTime> platforms = new HashMap<>();
            for (PlatformType platform : PlatformType.values()) {
                LocalDateTime platformLastUpdate = hotSearchRepository.findLatestUpdateTimeByPlatform(platform).orElse(null);
                if (platformLastUpdate != null) {
                    platforms.put(platform.name().toLowerCase(), platformLastUpdate);
                }
            }
            updateInfo.setPlatforms(platforms);
            return ResponseEntity.ok(ApiResponse.success(updateInfo));
        } catch (Exception ex) {
            logger.error("Get last update time failed", ex);
            return ResponseEntity.status(500).body(ApiResponse.serverError("Get update time failed"));
        }
    }
    @GetMapping
    public ResponseEntity<List<HotSearchItem>> getLatestHotSearches(
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "category", required = false) String category) {
        try {
            logger.info("Get hot search list: platform={}, category={}", platform, category);
            List<HotSearchItem> items;
            if (platform != null && !platform.isEmpty()) {
                PlatformType platformType = PlatformType.valueOf(platform.toUpperCase());
                items = getHotSearchWithFallback(platformType);
            } else {
                items = getLatestHotSearchWithFallback();
            }
            items = filterDegradedData(items);
            items = repairHotSearchItems(items);
            items = DataDeduplicationUtil.deduplicateByTitle(items);
            if (category != null && !category.isEmpty()) {
                items = items.stream().filter(item -> item.getCategory() != null && item.getCategory().equals(category)).collect(Collectors.toList());
            }
            // 按rank升序排序（rank 1是第一名，rank小的排前面）
            items = items.stream().sorted(Comparator.comparing(HotSearchItem::getRank)).collect(Collectors.toList());
            asyncClassifyItems(items);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Cache-Control", "max-age=60");
            return ResponseEntity.ok().headers(headers).body(items);
        } catch (Exception ex) {
            logger.error("Get hot search data exception", ex);
            return ResponseEntity.status(500).build();
        }
    }
    @PostMapping("/refresh")
    public ResponseEntity<List<HotSearchItem>> refreshHotSearches() {
        try {
            logger.info("Frontend refresh request");
            List<HotSearchItem> items = hotSearchService.refreshHotSearches();
            items = filterDegradedData(items);
            items = repairHotSearchItems(items);
            // 按rank升序排序（rank 1是第一名，rank小的排前面）
            items = items.stream().sorted(Comparator.comparing(HotSearchItem::getRank)).collect(Collectors.toList());
            asyncClassifyItems(items);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Cache-Control", "max-age=60");
            return ResponseEntity.ok().headers(headers).body(items);
        } catch (Exception ex) {
            logger.error("Refresh failed", ex);
            return ResponseEntity.status(500).build();
        }
    }
    @GetMapping("/classification-stats")
    public ResponseEntity<ClassificationStats> getClassificationStats() {
        try {
            List<HotSearchItem> items = getLatestHotSearchWithFallback();
            long pendingCount = items.stream().filter(item -> "pending".equals(item.getCategory()) || "evaluating".equals(item.getCategory())).count();
            long successCount = items.stream().filter(item -> item.getCategory() != null && !item.getCategory().isEmpty() && !"pending".equals(item.getCategory()) && !"other".equals(item.getCategory()) && !"evaluating".equals(item.getCategory()) && !"degraded".equals(item.getCategory())).count();
            ClassificationStats stats = new ClassificationStats();
            stats.setTotalCount(items.size());
            stats.setPendingCount((int) pendingCount);
            stats.setClassifiedCount((int) successCount);
            stats.setClassificationPercentage(items.size() > 0 ? (int) (successCount * 100 / items.size()) : 0);
            stats.setClassifying(pendingCount > 0);
            return ResponseEntity.ok(stats);
        } catch (Exception ex) {
            logger.error("Get classification stats failed", ex);
            return ResponseEntity.status(500).build();
        }
    }
    private List<HotSearchItem> filterDegradedData(List<HotSearchItem> items) {
        LocalDateTime now = LocalDateTime.now();
        List<HotSearchItem> filtered = items.stream().filter(item -> {
            if ("degraded".equals(item.getCategory())) return false;
            if (item.getCapturedAt() != null) {
                long minutesAgo = java.time.Duration.between(item.getCapturedAt(), now).toMinutes();
                if (minutesAgo > 10) return false;
            }
            return true;
        }).collect(Collectors.toList());
        if (filtered.isEmpty() && !items.isEmpty()) return items;
        return filtered;
    }
    private List<HotSearchItem> repairHotSearchItems(List<HotSearchItem> items) {
        items.forEach(item -> {
            if (item.getUrl() == null || item.getUrl().isEmpty()) {
                item.setUrl(generateUrlForPlatform(item.getTitle(), item.getPlatform()));
            }
            if (item.getCategory() == null || item.getCategory().isEmpty()) {
                item.setCategory("pending");
            }
        });
        return items;
    }
    private String generateUrlForPlatform(String title, PlatformType platform) {
        try {
            String encodedTitle = java.net.URLEncoder.encode(title, "UTF-8");
            return switch (platform) {
                case DOUYIN -> "https://www.douyin.com/search/" + encodedTitle;
                case BILIBILI -> "https://search.bilibili.com/all?keyword=" + encodedTitle;
                case WEIBO -> "https://s.weibo.com/weibo?q=" + encodedTitle;
                case TOUTIAO -> "https://www.toutiao.com/search/?keyword=" + encodedTitle;
            };
        } catch (Exception e) { return ""; }
    }
    private List<HotSearchItem> getLatestHotSearchWithFallback() {
        // 1. 先查Redis缓存
        List<HotSearchItem> cached = cacheService.getAllCached();
        if (cached != null && !cached.isEmpty()) {
            logger.debug("Cache hit for all platforms, returning {} items", cached.size());
            return cached;
        }
        // 2. 缓存未命中，查询数据库（只查询5分钟内的数据，避免旧数据）
        logger.info("Cache miss for all platforms, querying database");
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<HotSearchItem> dbItems = hotSearchRepository.findByCapturedAtAfter(fiveMinutesAgo);
        if (dbItems != null && !dbItems.isEmpty()) {
            logger.info("Found {} items from database (within 5 minutes)", dbItems.size());
            cacheService.cacheAll(dbItems);
            return dbItems;
        }
        // 3. 数据库也没有，触发实时采集
        logger.info("No recent data in database, triggering real-time collection");
        List<HotSearchItem> items = hotSearchService.getLatestHotSearches();
        if (items != null && !items.isEmpty()) {
            logger.info("Real-time collection successful, got {} items", items.size());
            return items;
        }
        // 4. 采集失败，返回空列表
        logger.warn("Failed to get data, returning empty list");
        return new ArrayList<>();
    }
    private List<HotSearchItem> getHotSearchWithFallback(PlatformType platformType) {
        // 1. 先查Redis缓存
        List<HotSearchItem> cached = cacheService.getPlatformCached(platformType);
        if (cached != null && !cached.isEmpty()) {
            logger.debug("Cache hit for {}, returning {} items", platformType, cached.size());
            return cached;
        }
        // 2. 缓存未命中，直接触发实时采集（确保数据新鲜）
        logger.info("Cache miss for {}, triggering real-time collection", platformType);
        List<HotSearchItem> items = hotSearchService.getHotSearchesByPlatform(platformType);
        if (items != null && !items.isEmpty()) {
            logger.info("Real-time collection successful for {}, got {} items", platformType, items.size());
            return items;
        }
        // 3. 兜底：返回空列表，避免返回旧数据
        logger.warn("Failed to get data for {}, returning empty list", platformType);
        return new ArrayList<>();
    }
    @Async
    private void asyncClassifyItems(List<HotSearchItem> items) {
        try {
            if (!classificationStatusService.tryStartClassification()) return;
            try {
                List<HotSearchItem> needClassification = items.stream().filter(item -> "pending".equals(item.getCategory()) || "other".equals(item.getCategory()) || "evaluating".equals(item.getCategory())).collect(Collectors.toList());
                if (needClassification.isEmpty()) return;
                Thread.sleep(500);
                List<HotSearchItem> classifiedItems = categoryClassificationService.classifyItems(needClassification);
                hotSearchRepository.saveAll(classifiedItems);
                cacheService.clearAll();
            } finally {
                classificationStatusService.endClassification();
            }
        } catch (Exception e) {
            logger.error("Async classification failed", e);
        }
    }
    public static class ClassificationStats {
        private int totalCount;
        private int pendingCount;
        private int classifiedCount;
        private int classificationPercentage;
        private boolean classifying;
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getClassifiedCount() { return classifiedCount; }
        public void setClassifiedCount(int classifiedCount) { this.classifiedCount = classifiedCount; }
        public int getClassificationPercentage() { return classificationPercentage; }
        public void setClassificationPercentage(int classificationPercentage) { this.classificationPercentage = classificationPercentage; }
        public boolean isClassifying() { return classifying; }
        public void setClassifying(boolean classifying) { this.classifying = classifying; }
    }
}