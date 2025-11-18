package org.xiaobuding.hotsearchaiplatform.service;

import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

import java.util.List;

public interface HotSearchCacheService {
    void cacheAll(List<HotSearchItem> items);
    void cachePlatform(PlatformType platform, List<HotSearchItem> items);
    List<HotSearchItem> getAllCached();
    List<HotSearchItem> getPlatformCached(PlatformType platform);
    void clearAll();
    void clearPlatformCache(PlatformType platform);
}
