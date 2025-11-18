package org.xiaobuding.hotsearchaiplatform.service.impl;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.service.*;
import org.xiaobuding.hotsearchaiplatform.service.platform.ThirdPartyHotSearchService;
import java.util.*;
@Service
public class HotSearchCollectorServiceImpl implements HotSearchCollectorService {
    private static final Logger LOG = LoggerFactory.getLogger(HotSearchCollectorServiceImpl.class);
    private final ThirdPartyHotSearchService thirdPartyService;
    public HotSearchCollectorServiceImpl(ThirdPartyHotSearchService thirdPartyService) {
        this.thirdPartyService = thirdPartyService;
    }
    @Override
    public List<HotSearchItem> collectAll(boolean forceRefresh) {
        LOG.info("Collect all platform data");
        List<HotSearchItem> allItems = new ArrayList<>();
        allItems.addAll(collectByPlatform(PlatformType.WEIBO, forceRefresh));
        allItems.addAll(collectByPlatform(PlatformType.TOUTIAO, forceRefresh));
        allItems.addAll(collectByPlatform(PlatformType.BILIBILI, forceRefresh));
        allItems.addAll(collectByPlatform(PlatformType.DOUYIN, forceRefresh));
        return allItems;
    }
    @Override
    public List<HotSearchItem> collectByPlatform(PlatformType platform, boolean forceRefresh) {
        LOG.info("Collect platform data: {}", platform);
        return switch (platform) {
            case WEIBO -> thirdPartyService.fetchWeiboHotSearch();
            case TOUTIAO -> thirdPartyService.fetchToutiaoHotSearch();
            case BILIBILI -> thirdPartyService.fetchBilibiliHotSearch();
            case DOUYIN -> thirdPartyService.fetchDouyinHotSearch();
        };
    }
}
