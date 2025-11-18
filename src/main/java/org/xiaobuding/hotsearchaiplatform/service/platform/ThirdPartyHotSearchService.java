package org.xiaobuding.hotsearchaiplatform.service.platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;

import java.util.List;

/**
 * 第三方热搜服务聚合器
 * 使用独立的平台服务实现高速并行爬取
 */
@Service
public class ThirdPartyHotSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyHotSearchService.class);
    
    private final WeiboHotSearchService weiboService;
    private final BilibiliHotSearchService bilibiliService;
    private final ToutiaoHotSearchService toutiaoService;
    private final DouyinHotSearchService douyinService;
    
    public ThirdPartyHotSearchService(WeiboHotSearchService weiboService,
                                     BilibiliHotSearchService bilibiliService,
                                     ToutiaoHotSearchService toutiaoService,
                                     DouyinHotSearchService douyinService) {
        this.weiboService = weiboService;
        this.bilibiliService = bilibiliService;
        this.toutiaoService = toutiaoService;
        this.douyinService = douyinService;
    }
    
    public List<HotSearchItem> fetchWeiboHotSearch() {
        return weiboService.fetchHotSearch();
    }
    
    public List<HotSearchItem> fetchBilibiliHotSearch() {
        return bilibiliService.fetchHotSearch();
    }
    
    public List<HotSearchItem> fetchToutiaoHotSearch() {
        return toutiaoService.fetchHotSearch();
    }
    
    public List<HotSearchItem> fetchDouyinHotSearch() {
        return douyinService.fetchHotSearch();
    }
}
