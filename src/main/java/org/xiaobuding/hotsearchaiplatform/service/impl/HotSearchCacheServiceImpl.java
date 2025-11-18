package org.xiaobuding.hotsearchaiplatform.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;
import org.xiaobuding.hotsearchaiplatform.service.HotSearchCacheService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class HotSearchCacheServiceImpl implements HotSearchCacheService {
    private static final long CACHE_TTL_MINUTES = 10;
    
    private final RedisTemplate<String, Object> redisTemplate;

    public HotSearchCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheAll(List<HotSearchItem> items) {
        redisTemplate.opsForValue().set("hotsearch:all", items, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void cachePlatform(PlatformType platform, List<HotSearchItem> items) {
        redisTemplate.opsForValue().set("hotsearch:platform:" + platform, items, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HotSearchItem> getAllCached() {
        Object cached = redisTemplate.opsForValue().get("hotsearch:all");
        return cached instanceof List ? (List<HotSearchItem>) cached : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HotSearchItem> getPlatformCached(PlatformType platform) {
        Object cached = redisTemplate.opsForValue().get("hotsearch:platform:" + platform);
        return cached instanceof List ? (List<HotSearchItem>) cached : null;
    }

    @Override
    public void clearAll() {
        redisTemplate.delete("hotsearch:all");
        for (PlatformType platform : PlatformType.values()) {
            redisTemplate.delete("hotsearch:platform:" + platform);
        }
    }
    
    @Override
    public void clearPlatformCache(PlatformType platform) {
        redisTemplate.delete("hotsearch:platform:" + platform);
    }
}
