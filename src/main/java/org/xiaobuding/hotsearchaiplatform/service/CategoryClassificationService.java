package org.xiaobuding.hotsearchaiplatform.service;

import java.util.List;

import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;

/**
 * 热搜分类服务
 * 通过 AI 分析热搜标题，自动划分到经济、社会、文娱等主题
 */
public interface CategoryClassificationService {

    /**
     * 对单条热搜进行分类
     *
     * @param title 热搜标题
     * @return 分类结果（如“经济”“社会”“文娱”等）
     */
    String classifyTitle(String title);

    /**
     * 批量分类热搜项目
     *
     * @param items 热搜列表
     * @return 已附加分类结果的热搜列表
     */
    List<HotSearchItem> classifyItems(List<HotSearchItem> items);
}
