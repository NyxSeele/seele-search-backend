package org.xiaobuding.hotsearchaiplatform.service.impl;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.*;
import java.util.*;
@Service
public class HotSearchServiceImpl implements HotSearchService {
    private static final Logger logger = LoggerFactory.getLogger(HotSearchServiceImpl.class);
    private final HotSearchCollectorService collectorService;
    private final HotSearchCacheService cacheService;
    private final HotSearchRepository repository;
    private final CategoryClassificationService categoryClassificationService;
    
    public HotSearchServiceImpl(HotSearchCollectorService collectorService,
                                HotSearchCacheService cacheService,
                                HotSearchRepository repository,
                                CategoryClassificationService categoryClassificationService) {
        this.collectorService = collectorService;
        this.cacheService = cacheService;
        this.repository = repository;
        this.categoryClassificationService = categoryClassificationService;
    }
    @Override
    @Transactional
    public List<HotSearchItem> getLatestHotSearches() {
        logger.info("Get latest hot searches");
        List<HotSearchItem> items = collectorService.collectAll(false);
        items = categoryClassificationService.classifyItems(items);
        repository.saveAll(items);
        cacheService.cacheAll(items);
        return items;
    }
    @Override
    @Transactional
    public List<HotSearchItem> getHotSearchesByPlatform(PlatformType platform) {
        logger.info("Get hot searches by platform: {}", platform);

        // 0. 读取旧数据（用于失败时回退）
        List<HotSearchItem> oldItems = cacheService.getPlatformCached(platform);
        if (oldItems == null || oldItems.isEmpty()) {
            oldItems = repository.findByPlatformOrderByRankAsc(platform);
        }
        logger.info("Loaded {} old items for {} as fallback", oldItems.size(), platform);

        // 1. 采集最新数据（不立即删除旧数据）
        List<HotSearchItem> items = collectorService.collectByPlatform(platform, false);
        logger.info("Collected {} new items for {}", items.size(), platform);

        if (items == null || items.isEmpty()) {
            // 爬取失败或返回为空，直接使用旧数据，避免前端看到空列表
            logger.warn("No new items collected for {}, using fallback data ({} old items)", platform, oldItems.size());
            if (oldItems != null && !oldItems.isEmpty()) {
                // 重新写入缓存一次，保证前端读取到的是旧数据
                cacheService.cachePlatform(platform, oldItems);
                logger.info("Re-cached {} fallback items for {}", oldItems.size(), platform);
            }
            return oldItems;
        }

        // 2. 只有在拿到有效新数据时，才清理旧缓存和数据库
        cacheService.clearPlatformCache(platform);
        logger.info("Cleared {} cache from Redis", platform);

        repository.deleteByPlatform(platform);
        logger.info("Deleted old {} data from database", platform);

        // 3. 立即进行分类
        items = categoryClassificationService.classifyItems(items);
        logger.info("Classified {} items for {}", items.size(), platform);

        // 4. 保存新数据到数据库
        repository.saveAll(items);
        logger.info("Saved {} new items for {} to database", items.size(), platform);

        // 5. 更新Redis缓存
        cacheService.cachePlatform(platform, items);
        logger.info("Updated {} cache in Redis with {} items", platform, items.size());

        return items;
    }
    @Override
    @Transactional
    public List<HotSearchItem> refreshHotSearches() {
        logger.info("Force refresh all platforms");
        List<HotSearchItem> items = collectorService.collectAll(true);
        items = categoryClassificationService.classifyItems(items);
        repository.saveAll(items);
        cacheService.cacheAll(items);
        return items;
    }
}
