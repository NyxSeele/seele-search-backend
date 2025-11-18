-- 数据库优化脚本 - 数据去重和性能优化
-- 用途：解决数据重复问题，提升查询性能

-- ============================================
-- 1. 清理重复数据（在添加索引前）
-- ============================================

-- 1.1 查看当前重复数据统计
SELECT 
    platform,
    title,
    COUNT(*) as duplicate_count
FROM hot_search_items
GROUP BY platform, title
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC
LIMIT 20;

-- 1.2 删除重复数据（保留ID最小的，即最早的记录）
DELETE t1 FROM hot_search_items t1
INNER JOIN hot_search_items t2 
WHERE 
    t1.id > t2.id 
    AND t1.platform = t2.platform 
    AND t1.title = t2.title;

-- 1.3 删除降级数据
DELETE FROM hot_search_items 
WHERE title LIKE '【降级数据】%' 
   OR category = '降级数据';

-- 1.4 删除过期数据（超过7天的数据）
DELETE FROM hot_search_items 
WHERE captured_at < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- ============================================
-- 2. 添加索引优化查询性能
-- ============================================

-- 2.1 添加复合索引：平台+标题（用于去重和查询）
-- 注意：这不是唯一索引，因为标题可能会随时间重复出现
ALTER TABLE hot_search_items 
ADD INDEX idx_platform_title (platform, title(100));

-- 2.2 添加复合索引：平台+排名（用于按平台查询和排序）
ALTER TABLE hot_search_items 
ADD INDEX idx_platform_rank (platform, `rank`);

-- 2.3 添加复合索引：平台+采集时间（用于查询最新数据）
ALTER TABLE hot_search_items 
ADD INDEX idx_platform_captured (platform, captured_at);

-- 2.4 添加索引：采集时间（用于时间范围查询和清理旧数据）
ALTER TABLE hot_search_items 
ADD INDEX idx_captured_at (captured_at);

-- 2.5 添加索引：分类（用于按分类查询）
ALTER TABLE hot_search_items 
ADD INDEX idx_category (category);

-- 2.6 添加复合索引：分类+采集时间（用于分类统计）
ALTER TABLE hot_search_items 
ADD INDEX idx_category_captured (category, captured_at);

-- ============================================
-- 3. 查看索引效果
-- ============================================

-- 3.1 查看表的所有索引
SHOW INDEX FROM hot_search_items;

-- 3.2 查看表的统计信息
SELECT 
    COUNT(*) as total_count,
    COUNT(DISTINCT platform, title) as unique_items,
    COUNT(*) - COUNT(DISTINCT platform, title) as duplicates
FROM hot_search_items;

-- 3.3 按平台统计
SELECT 
    platform,
    COUNT(*) as count,
    COUNT(DISTINCT title) as unique_titles,
    MIN(captured_at) as earliest,
    MAX(captured_at) as latest
FROM hot_search_items
GROUP BY platform
ORDER BY platform;

-- ============================================
-- 4. 优化建议的查询语句
-- ============================================

-- 4.1 使用 DISTINCT 查询（防止重复）
-- 注意：这个查询可能比较慢，主要用于诊断
SELECT DISTINCT ON (title) 
    title, platform, heat, `rank`, captured_at, category
FROM hot_search_items
WHERE platform = 'WEIBO'
  AND category = '娱乐'
ORDER BY title, captured_at DESC;

-- MySQL 不支持 DISTINCT ON，使用以下替代方案：
SELECT h1.*
FROM hot_search_items h1
INNER JOIN (
    SELECT platform, title, MAX(captured_at) as max_captured
    FROM hot_search_items
    GROUP BY platform, title
) h2 ON h1.platform = h2.platform 
    AND h1.title = h2.title 
    AND h1.captured_at = h2.max_captured
WHERE h1.platform = 'WEIBO'
ORDER BY h1.`rank`;

-- ============================================
-- 5. 定期清理脚本（建议在定时任务中执行）
-- ============================================

-- 5.1 清理7天前的数据
CREATE EVENT IF NOT EXISTS cleanup_old_data
ON SCHEDULE EVERY 1 DAY
DO
    DELETE FROM hot_search_items 
    WHERE captured_at < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- 5.2 清理重复数据
CREATE EVENT IF NOT EXISTS cleanup_duplicates
ON SCHEDULE EVERY 1 HOUR
DO
    DELETE t1 FROM hot_search_items t1
    INNER JOIN hot_search_items t2 
    WHERE t1.id > t2.id 
      AND t1.platform = t2.platform 
      AND t1.title = t2.title
      AND DATE(t1.captured_at) = DATE(t2.captured_at);

-- 5.3 查看定时任务
SHOW EVENTS;

-- ============================================
-- 6. 回滚脚本（如果需要）
-- ============================================

-- 删除添加的索引
-- ALTER TABLE hot_search_items DROP INDEX idx_platform_title;
-- ALTER TABLE hot_search_items DROP INDEX idx_platform_rank;
-- ALTER TABLE hot_search_items DROP INDEX idx_platform_captured;
-- ALTER TABLE hot_search_items DROP INDEX idx_captured_at;
-- ALTER TABLE hot_search_items DROP INDEX idx_category;
-- ALTER TABLE hot_search_items DROP INDEX idx_category_captured;

-- 删除定时任务
-- DROP EVENT IF EXISTS cleanup_old_data;
-- DROP EVENT IF EXISTS cleanup_duplicates;

-- ============================================
-- 7. 验证优化效果
-- ============================================

-- 7.1 测试查询性能（使用 EXPLAIN）
EXPLAIN SELECT * FROM hot_search_items 
WHERE platform = 'WEIBO' 
  AND captured_at > DATE_SUB(NOW(), INTERVAL 30 MINUTE)
ORDER BY `rank`;

-- 7.2 测试去重查询
EXPLAIN SELECT h1.*
FROM hot_search_items h1
INNER JOIN (
    SELECT platform, title, MAX(id) as max_id
    FROM hot_search_items
    WHERE captured_at > DATE_SUB(NOW(), INTERVAL 30 MINUTE)
    GROUP BY platform, title
) h2 ON h1.id = h2.max_id
WHERE h1.platform = 'WEIBO'
ORDER BY h1.`rank`;

-- ============================================
-- 8. 数据一致性检查
-- ============================================

-- 8.1 检查是否还有重复数据
SELECT 
    platform, 
    title, 
    captured_at,
    COUNT(*) as count
FROM hot_search_items
WHERE captured_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY platform, title, DATE(captured_at)
HAVING COUNT(*) > 1;

-- 8.2 检查降级数据
SELECT 
    platform,
    COUNT(*) as degraded_count
FROM hot_search_items
WHERE title LIKE '【降级数据】%' OR category = '降级数据'
GROUP BY platform;

-- ============================================
-- 使用说明：
-- 1. 执行前请备份数据库
-- 2. 先执行第1部分清理重复数据
-- 3. 再执行第2部分添加索引
-- 4. 使用第7部分验证优化效果
-- 5. 可选：启用第5部分的定时清理任务
-- ============================================
