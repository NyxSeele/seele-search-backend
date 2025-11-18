package org.xiaobuding.hotsearchaiplatform.service.impl;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Service
public class DataCleanupServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(DataCleanupServiceImpl.class);
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldData() {
        logger.info("Cleanup old data started");
    }
}
