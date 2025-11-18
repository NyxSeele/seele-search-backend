package org.xiaobuding.hotsearchaiplatform.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

/**
 * 平台健康检查服务（简化版）
 * 监控各平台数据采集状态
 */
@Service
public class PlatformHealthService {

    public enum Status {
        UP,        // 正常
        DEGRADED,  // 降级
        DOWN       // 停止
    }

    public record PlatformHealth(
            Status status,
            long failureCount,
            boolean degraded,
            String message,
            LocalDateTime lastUpdated,
            int datasetSize
    ) {}

    private record Metadata(
            String cacheKey,
            String failureKey,
            int alertThreshold,
            String degradePrefix
    ) {}

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<PlatformType, Metadata> metadataMap = new EnumMap<>(PlatformType.class);

    public PlatformHealthService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 初始化各平台元数据
        metadataMap.put(PlatformType.TOUTIAO,
                new Metadata("hotsearch:platform:TOUTIAO", "hotsearch:TOUTIAO:failure:count", 3, "【降级数据】"));
        metadataMap.put(PlatformType.WEIBO,
                new Metadata("hotsearch:platform:weibo", "hotsearch:weibo:failure:count", 3, "【降级数据】"));
        metadataMap.put(PlatformType.BILIBILI,
                new Metadata("hotsearch:platform:BILIBILI", "hotsearch:BILIBILI:failure:count", 3, "【降级数据】"));
        metadataMap.put(PlatformType.DOUYIN,
                new Metadata("hotsearch:platform:DOUYIN", "hotsearch:DOUYIN:failure:count", 3, "【降级数据】"));
    }

    /**
     * 获取平台健康状态
     */
    public PlatformHealth getPlatformHealth(PlatformType platformType) {
        Metadata metadata = metadataMap.get(platformType);
        if (metadata == null) {
            return new PlatformHealth(Status.DOWN, 0, true, "未找到平台监控配置", null, 0);
        }

        long failureCount = getFailureCount(metadata.failureKey());
        List<HotSearchItem> cachedItems = getCachedItems(metadata.cacheKey());
        boolean degraded = isDegraded(cachedItems, metadata.degradePrefix());
        LocalDateTime lastUpdated = cachedItems.isEmpty() ? null : cachedItems.getFirst().getCapturedAt();

        Status status = determineStatus(failureCount, metadata.alertThreshold(), degraded, cachedItems.isEmpty());
        String message = buildMessage(status, failureCount, cachedItems.size());

        return new PlatformHealth(status, failureCount, degraded, message, lastUpdated, cachedItems.size());
    }

    /**
     * 记录失败次数
     */
    public void recordFailure(PlatformType platformType) {
        Metadata metadata = metadataMap.get(platformType);
        if (metadata != null) {
            redisTemplate.opsForValue().increment(metadata.failureKey());
        }
    }

    /**
     * 清除失败计数
     */
    public void clearFailure(PlatformType platformType) {
        Metadata metadata = metadataMap.get(platformType);
        if (metadata != null) {
            redisTemplate.delete(metadata.failureKey());
        }
    }

    // 私有辅助方法
    private long getFailureCount(String failureKey) {
        Object count = redisTemplate.opsForValue().get(failureKey);
        return count != null ? Long.parseLong(count.toString()) : 0;
    }

    @SuppressWarnings("unchecked")
    private List<HotSearchItem> getCachedItems(String cacheKey) {
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        return (cached instanceof List) ? (List<HotSearchItem>) cached : new ArrayList<>();
    }

    private boolean isDegraded(List<HotSearchItem> items, String degradePrefix) {
        return items.stream().anyMatch(item -> item.getTitle() != null && item.getTitle().startsWith(degradePrefix));
    }

    private Status determineStatus(long failureCount, int threshold, boolean degraded, boolean isEmpty) {
        if (failureCount >= threshold || isEmpty) return Status.DOWN;
        if (degraded) return Status.DEGRADED;
        return Status.UP;
    }

    private String buildMessage(Status status, long failureCount, int dataSize) {
        return switch (status) {
            case UP -> "平台运行正常，数据" + dataSize + "条";
            case DEGRADED -> "平台降级运行，使用历史数据";
            case DOWN -> "平台服务异常，失败" + failureCount + "次";
        };
    }
}
