package org.xiaobuding.hotsearchaiplatform.service.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;

import java.time.LocalDateTime;

/**
 * 数据库定时清理任务
 * 每10分钟清理一次超过10分钟的旧数据
 */
@Component
public class DatabaseCleanupTask {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupTask.class);
    
    private final HotSearchRepository repository;
    
    public DatabaseCleanupTask(HotSearchRepository repository) {
        this.repository = repository;
    }
    
    /**
     * 每10分钟执行一次清理
     * 删除10分钟前的数据
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 600000) // 10分钟
    public void cleanupOldData() {
        try {
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            long deletedCount = repository.deleteByCapturedAtBefore(tenMinutesAgo);
            
            if (deletedCount > 0) {
                logger.info("🗑️ Cleaned up {} old records (before {})", deletedCount, tenMinutesAgo);
            } else {
                logger.debug("No old records to clean up");
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old data", e);
        }
    }
}
