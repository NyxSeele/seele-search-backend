package org.xiaobuding.hotsearchaiplatform.service;

import java.util.List;

import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;

/**
 * 鐑悳鍒嗙被鏈嶅姟
 * 浣跨敤AI鍒嗘瀽鐑悳鏍囬锛岃嚜鍔ㄥ垎绫讳负锛氱粡娴庛€佸啗浜嬨€佷綋鑲层€佸ū涔愩€佺鎶€銆佺ぞ浼氱瓑
 */
public interface CategoryClassificationService {

    /**
     * 瀵瑰崟涓儹鎼滈」杩涜鍒嗙被
     *
     * @param title 鐑悳鏍囬
     * @return 鍒嗙被缁撴灉锛堝锛?缁忔祹"銆?鍐涗簨"銆?浣撹偛"绛夛級
     */
    String classifyTitle(String title);

    /**
     * 鎵归噺鍒嗙被鐑悳椤?
     *
     * @param items 鐑悳椤瑰垪琛?
     * @return 鍒嗙被鍚庣殑鐑悳椤瑰垪琛?
     */
    List<HotSearchItem> classifyItems(List<HotSearchItem> items);
}
