package org.xiaobuding.hotsearchaiplatform.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xiaobuding.hotsearchaiplatform.model.*;
import java.time.LocalDateTime;
import java.util.*;
@Repository
public interface HotSearchRepository extends JpaRepository<HotSearchItem, Long> {
    List<HotSearchItem> findByPlatformOrderByRankAsc(PlatformType platform);
    List<HotSearchItem> findByPlatformAndCapturedAtAfter(PlatformType platform, LocalDateTime capturedAt);
    List<HotSearchItem> findByCapturedAtAfter(LocalDateTime capturedAt);
    
    @Modifying
    void deleteByPlatform(PlatformType platform);
    
    @Modifying
    long deleteByCapturedAtBefore(LocalDateTime capturedAt);
    @Query("SELECT MAX(h.capturedAt) FROM HotSearchItem h")
    Optional<LocalDateTime> findLatestUpdateTime();
    @Query("SELECT MAX(h.capturedAt) FROM HotSearchItem h WHERE h.platform = ?1")
    Optional<LocalDateTime> findLatestUpdateTimeByPlatform(PlatformType platform);
    @Query("SELECT MAX(h.capturedAt) FROM HotSearchItem h WHERE h.category = ?1")
    Optional<LocalDateTime> findLatestUpdateTimeByCategory(String category);
    @Query("SELECT DISTINCT h.category FROM HotSearchItem h WHERE h.category IS NOT NULL")
    List<String> findAllCategories();
}
