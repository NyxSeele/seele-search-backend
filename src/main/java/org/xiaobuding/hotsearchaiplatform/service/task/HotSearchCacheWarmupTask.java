package org.xiaobuding.hotsearchaiplatform.service.task;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;
import org.xiaobuding.hotsearchaiplatform.service.HotSearchService;

/**
 * 热搜缓存预热任务
 * 定期后台更新缓存，确保用户查询时总能获得最新的缓存数据
 * 即使爬取失败，也不影响用户体验
 */
@Component
public class HotSearchCacheWarmupTask {

    private static final Logger LOG = LoggerFactory.getLogger(HotSearchCacheWarmupTask.class);

    private final HotSearchService hotSearchService;

    // 存储各平台最后一次刷新时间
    private static final Map<PlatformType, LocalDateTime> lastRefreshTime = new ConcurrentHashMap<>();
    private static final Map<PlatformType, Boolean> lastRefreshSuccess = new ConcurrentHashMap<>();

    public HotSearchCacheWarmupTask(HotSearchService hotSearchService) {
        this.hotSearchService = hotSearchService;
    }

    /**
     * 获取平台最后刷新时间
     */
    public static LocalDateTime getLastRefreshTime(PlatformType platform) {
        return lastRefreshTime.get(platform);
    }

    /**
     * 获取平台最后刷新是否成功
     */
    public static Boolean getLastRefreshSuccess(PlatformType platform) {
        return lastRefreshSuccess.getOrDefault(platform, false);
    }

    /**
     * 每30秒预热一次B站缓存
     * 失败时保持现有缓存，不影响用户查询
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 5000) // 初始延迟5秒，后续每30秒执行一次
    public void warmupBilibiliCache() {
        LOG.info("========== 开始预热B站缓存 ==========");
        long startTime = System.currentTimeMillis();

        try {
            hotSearchService.getHotSearchesByPlatform(PlatformType.BILIBILI);
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.BILIBILI, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.BILIBILI, true);
            LOG.info("========== B站缓存预热成功，耗时: {}ms ==========", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.BILIBILI, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.BILIBILI, false);
            LOG.warn("========== B站缓存预热失败，耗时: {}ms，保持现有缓存 ==========", duration, e);
        }
    }

    /**
     * 每30秒预热一次微博缓存
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void warmupWeiboCache() {
        LOG.info("========== 开始预热微博缓存 ==========");
        long startTime = System.currentTimeMillis();

        try {
            hotSearchService.getHotSearchesByPlatform(PlatformType.WEIBO);
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.WEIBO, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.WEIBO, true);
            LOG.info("========== 微博缓存预热成功，耗时: {}ms ==========", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.WEIBO, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.WEIBO, false);
            LOG.warn("========== 微博缓存预热失败，耗时: {}ms，保持现有缓存 ==========", duration, e);
        }
    }

    /**
     * 每30秒预热一次今日头条缓存
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    public void warmupTOUTIAOCache() {
        LOG.info("========== 开始预热今日头条缓存 ==========");
        long startTime = System.currentTimeMillis();

        try {
            hotSearchService.getHotSearchesByPlatform(PlatformType.TOUTIAO);
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.TOUTIAO, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.TOUTIAO, true);
            LOG.info("========== 今日头条缓存预热成功，耗时: {}ms ==========", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.TOUTIAO, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.TOUTIAO, false);
            LOG.warn("========== 今日头条缓存预热失败，耗时: {}ms，保持现有缓存 ==========", duration, e);
        }
    }

    /**
     * 每30秒预热一次抖音缓存
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 20000)
    public void warmupDouyinCache() {
        LOG.info("========== 开始预热抖音缓存 ==========");
        long startTime = System.currentTimeMillis();

        try {
            hotSearchService.getHotSearchesByPlatform(PlatformType.DOUYIN);
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.DOUYIN, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.DOUYIN, true);
            LOG.info("========== 抖音缓存预热成功，耗时: {}ms ==========", duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            lastRefreshTime.put(PlatformType.DOUYIN, LocalDateTime.now());
            lastRefreshSuccess.put(PlatformType.DOUYIN, false);
            LOG.warn("========== 抖音缓存预热失败，耗时: {}ms，保持现有缓存 ==========", duration, e);
        }
    }
}
