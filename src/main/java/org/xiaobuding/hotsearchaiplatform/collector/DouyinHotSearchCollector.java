package org.xiaobuding.hotsearchaiplatform.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.platform.ThirdPartyHotSearchService;

import java.util.List;

@Component
public class DouyinHotSearchCollector {
    private static final Logger LOG = LoggerFactory.getLogger(DouyinHotSearchCollector.class);

    private final ThirdPartyHotSearchService thirdPartyService;
    private final HotSearchRepository repository;

    public DouyinHotSearchCollector(ThirdPartyHotSearchService thirdPartyService,
                                    HotSearchRepository repository) {
        this.thirdPartyService = thirdPartyService;
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 300000)
    public void collectDouyinHotSearch() {
        LOG.info("Start collecting Douyin hot search");
        
        try {
            List<HotSearchItem> items = thirdPartyService.fetchDouyinHotSearch();
            
            if (items == null || items.isEmpty()) {
                LOG.warn("Douyin hot search data is empty");
                return;
            }
            
            repository.deleteByPlatform(PlatformType.DOUYIN);
            repository.saveAll(items);
            
            LOG.info("Douyin hot search collection completed, total: {}", items.size());
            
        } catch (Exception e) {
            LOG.error("Collect Douyin hot search failed", e);
        }
    }
}
