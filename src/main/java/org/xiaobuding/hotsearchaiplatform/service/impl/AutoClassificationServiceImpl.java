package org.xiaobuding.hotsearchaiplatform.service.impl;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.service.*;
@Service
public class AutoClassificationServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(AutoClassificationServiceImpl.class);
    private final CategoryClassificationService classificationService;
    private final HotSearchService hotSearchService;
    public AutoClassificationServiceImpl(CategoryClassificationService classificationService,
                                         HotSearchService hotSearchService) {
        this.classificationService = classificationService;
        this.hotSearchService = hotSearchService;
    }
    @Scheduled(fixedDelay = 300000)
    public void autoClassifyHotSearches() {
        logger.info("Auto classification started");
    }
}
