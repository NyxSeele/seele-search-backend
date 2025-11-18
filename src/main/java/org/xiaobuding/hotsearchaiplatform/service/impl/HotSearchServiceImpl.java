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
        repository.saveAll(items);
        cacheService.cacheAll(items);
        return items;
    }
    @Override
    @Transactional
    public List<HotSearchItem> getHotSearchesByPlatform(PlatformType platform) {
        logger.info("Get hot searches by platform: {}", platform);
        
        // 1. 先清除该平台的Redis缓存，确保不会读到旧数据
        cacheService.clearPlatformCache(platform);
        logger.info("Cleared {} cache from Redis", platform);
        
        // 2. 删除该平台的数据库旧数据，避免数据堆积
        repository.deleteByPlatform(platform);
        logger.info("Deleted old {} data from database", platform);
        
        // 3. 采集最新数据
        List<HotSearchItem> items = collectorService.collectByPlatform(platform, false);
        logger.info("Collected {} new items for {}", items.size(), platform);
        
        // 4. 立即进行分类
        items = categoryClassificationService.classifyItems(items);
        logger.info("Classified {} items for {}", items.size(), platform);
        
        // 5. 保存新数据到数据库
        repository.saveAll(items);
        logger.info("Saved {} new items for {} to database", items.size(), platform);
        
        // 6. 更新Redis缓存
        cacheService.cachePlatform(platform, items);
        logger.info("Updated {} cache in Redis with {} items", platform, items.size());
        
        return items;
    }
    @Override
    @Transactional
    public List<HotSearchItem> refreshHotSearches() {
        logger.info("Force refresh all platforms");
        List<HotSearchItem> items = collectorService.collectAll(true);
        repository.saveAll(items);
        cacheService.cacheAll(items);
        return items;
    }
}
