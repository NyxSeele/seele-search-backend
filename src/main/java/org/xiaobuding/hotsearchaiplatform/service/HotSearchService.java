package org.xiaobuding.hotsearchaiplatform.service;

import java.util.List;

import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

public interface HotSearchService {

    List<HotSearchItem> getLatestHotSearches();

    List<HotSearchItem> getHotSearchesByPlatform(PlatformType platformType);

    List<HotSearchItem> refreshHotSearches();
}

