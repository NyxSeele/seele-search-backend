package org.xiaobuding.hotsearchaiplatform.service;

import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

/**
 * 鏁版嵁楠岃瘉鏈嶅姟
 * 楠岃瘉鐑悳椤圭洰鏄惁鐪熷疄瀛樺湪
 */
public interface DataValidationService {

    /**
     * 楠岃瘉鎸囧畾骞冲彴鐨勭儹鎼滈」鐩槸鍚﹀瓨鍦?
     *
     * @param platform 骞冲彴绫诲瀷
     * @param itemId   鐑悳椤圭洰ID
     * @return true 濡傛灉椤圭洰瀛樺湪锛宖alse 鍚﹀垯
     */
    boolean isItemExists(PlatformType platform, String itemId);

    /**
     * 鑾峰彇鐑悳椤圭洰鐨勬爣棰?
     *
     * @param platform 骞冲彴绫诲瀷
     * @param itemId   鐑悳椤圭洰ID
     * @return 椤圭洰鏍囬锛屽鏋滀笉瀛樺湪杩斿洖 null
     */
    String getItemTitle(PlatformType platform, String itemId);

    /**
     * 鑾峰彇鐑悳椤圭洰鐨勭儹搴﹀€?
     *
     * @param platform 骞冲彴绫诲瀷
     * @param itemId   鐑悳椤圭洰ID
     * @return 鐑害鍊硷紝濡傛灉涓嶅瓨鍦ㄨ繑鍥?-1
     */
    Long getItemHeat(PlatformType platform, String itemId);
}
