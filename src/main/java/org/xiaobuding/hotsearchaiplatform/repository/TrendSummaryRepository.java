package org.xiaobuding.hotsearchaiplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.xiaobuding.hotsearchaiplatform.model.TrendSummaryEntity;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 瓒嬪娍鎬荤粨鏁版嵁璁块棶灞?
 */
@Repository
public interface TrendSummaryRepository extends JpaRepository<TrendSummaryEntity, Long> {

    /**
     * 鑾峰彇鏈€鏂扮殑瓒嬪娍鎬荤粨
     */
    @Query(value = "SELECT * FROM trend_summaries ORDER BY generated_at DESC LIMIT 1", nativeQuery = true)
    Optional<TrendSummaryEntity> findLatest();

    /**
     * 鑾峰彇鎸囧畾鏃堕棿涔嬪悗鐨勮秼鍔挎€荤粨
     */
    Optional<TrendSummaryEntity> findByGeneratedAtAfter(LocalDateTime generatedAt);
}
