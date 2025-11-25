package org.xiaobuding.hotsearchaiplatform.service;

import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

/**
 * 数据校验服务
 * 用于确认热搜条目是否真实存在以及获取其基础信息
 */
public interface DataValidationService {

    /**
     * 校验指定平台上的热搜条目是否存在
     *
     * @param platform 平台类型
     * @param itemId   热搜条目 ID
     * @return true 表示条目存在，false 表示不存在
     */
    boolean isItemExists(PlatformType platform, String itemId);

    /**
     * 获取热搜条目的标题
     *
     * @param platform 平台类型
     * @param itemId   热搜条目 ID
     * @return 条目标题，不存在时返回 null
     */
    String getItemTitle(PlatformType platform, String itemId);

    /**
     * 获取热搜条目的热度值
     *
     * @param platform 平台类型
     * @param itemId   热搜条目 ID
     * @return 热度数值，不存在时返回 -1
     */
    Long getItemHeat(PlatformType platform, String itemId);
}
