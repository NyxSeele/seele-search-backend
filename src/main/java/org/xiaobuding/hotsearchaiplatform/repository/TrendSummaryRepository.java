package org.xiaobuding.hotsearchaiplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.xiaobuding.hotsearchaiplatform.model.TrendSummaryEntity;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 趋势总结相关的数据访问层
 */
@Repository
public interface TrendSummaryRepository extends JpaRepository<TrendSummaryEntity, Long> {

    /**
     * 获取最新生成的趋势总结
     */
    @Query(value = "SELECT * FROM trend_summaries ORDER BY generated_at DESC LIMIT 1", nativeQuery = true)
    Optional<TrendSummaryEntity> findLatest();

    /**
     * 获取指定时间之后的趋势总结
     */
    Optional<TrendSummaryEntity> findByGeneratedAtAfter(LocalDateTime generatedAt);
}
