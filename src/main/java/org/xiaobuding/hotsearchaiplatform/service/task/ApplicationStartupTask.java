package org.xiaobuding.hotsearchaiplatform.service.task;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.CacheManagementService;

/**
 * 应用启动时的初始化任务
 * 清空Redis缓存和数据库，确保没有旧数据
 */
@Component
public class ApplicationStartupTask {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupTask.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final HotSearchRepository hotSearchRepository;
    private final CacheManagementService cacheManagementService;
    
    public ApplicationStartupTask(HotSearchRepository hotSearchRepository, 
                                 CacheManagementService cacheManagementService) {
        this.hotSearchRepository = hotSearchRepository;
        this.cacheManagementService = cacheManagementService;
    }
    
    /**
     * 应用启动完成后执行清理
     * 使用ApplicationReadyEvent确保在所有Bean初始化完成后执行
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        logger.info("========================================");
        logger.info("🚀 Application startup - Cleaning old data and resetting ID...");
        logger.info("========================================");
        
        try {
            // 1. 清空数据库并重置自增ID（使用TRUNCATE）
            long dbCount = hotSearchRepository.count();
            if (dbCount > 0) {
                logger.info("Found {} records in database, truncating table...", dbCount);
                // TRUNCATE会清空数据并重置AUTO_INCREMENT
                entityManager.createNativeQuery("TRUNCATE TABLE hot_search_items")
                    .executeUpdate();
                logger.info("✅ Truncated table and reset AUTO_INCREMENT (deleted {} records)", dbCount);
            } else {
                logger.info("✅ Database is already empty");
            }
            
            // 2. 清空Redis缓存
            cacheManagementService.clearAllCache();
            logger.info("✅ Cleared all Redis cache");
            
            logger.info("========================================");
            logger.info("✨ Startup cleanup completed successfully!");
            logger.info("✨ Database ID will start from 1");
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("❌ Failed to cleanup on startup", e);
            logger.warn("⚠️ Falling back to DELETE method...");
            try {
                // 降级方案：使用DELETE
                hotSearchRepository.deleteAll();
                logger.info("✅ Deleted all records (ID not reset)");
                cacheManagementService.clearAllCache();
                logger.info("✅ Cleared all Redis cache");
            } catch (Exception fallbackError) {
                logger.error("❌ Fallback also failed", fallbackError);
            }
        }
    }
}
