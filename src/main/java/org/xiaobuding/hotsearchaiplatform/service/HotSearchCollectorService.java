package org.xiaobuding.hotsearchaiplatform.service;

import java.util.List;

import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

public interface HotSearchCollectorService {

    List<HotSearchItem> collectAll(boolean forceRefresh);

    List<HotSearchItem> collectByPlatform(PlatformType platformType, boolean forceRefresh);
}

