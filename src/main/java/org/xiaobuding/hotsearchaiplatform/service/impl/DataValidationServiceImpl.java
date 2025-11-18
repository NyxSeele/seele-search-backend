package org.xiaobuding.hotsearchaiplatform.service.impl;

import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;
import org.xiaobuding.hotsearchaiplatform.repository.HotSearchRepository;
import org.xiaobuding.hotsearchaiplatform.service.DataValidationService;

@Service
public class DataValidationServiceImpl implements DataValidationService {
    private final HotSearchRepository hotSearchRepository;

    public DataValidationServiceImpl(HotSearchRepository hotSearchRepository) {
        this.hotSearchRepository = hotSearchRepository;
    }

    @Override
    public boolean isItemExists(PlatformType platform, String itemId) {
        return true;
    }

    @Override
    public String getItemTitle(PlatformType platform, String itemId) {
        return itemId;
    }

    @Override
    public Long getItemHeat(PlatformType platform, String itemId) {
        return 0L;
    }
}
