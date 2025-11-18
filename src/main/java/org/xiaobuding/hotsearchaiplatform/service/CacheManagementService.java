package org.xiaobuding.hotsearchaiplatform.service;

import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CacheManagementService {
    private static final Logger logger = LoggerFactory.getLogger(CacheManagementService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheManagementService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void clearAllCache() {
        logger.info("Clear all cache");
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        } catch (Exception e) {
            logger.error("Clear cache failed", e);
        }
    }

    public void clearPlatformCache(String platform) {
        logger.info("Clear platform cache: {}", platform);
        redisTemplate.delete("hotsearch:platform:" + platform);
    }
}
