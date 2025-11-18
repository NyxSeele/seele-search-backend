package org.xiaobuding.hotsearchaiplatform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "hot_search_items")
public class HotSearchItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformType platform;

    @Column(nullable = false)
    private Long heat;

    @Column(name = "`rank`", nullable = false)
    private Integer rank;

    @Column(name = "captured_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime capturedAt;

    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String actualSource;

    @Column(length = 100)
    private String degradedReason;

    @Column(length = 500)
    private String url;

    public HotSearchItem() {
    }

    public HotSearchItem(String title, PlatformType platform, Long heat, Integer rank, LocalDateTime capturedAt) {
        this.title = title;
        this.platform = platform;
        this.heat = heat;
        this.rank = rank;
        this.capturedAt = capturedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformType platform) {
        this.platform = platform;
    }

    public Long getHeat() {
        return heat;
    }

    public void setHeat(Long heat) {
        this.heat = heat;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(LocalDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getActualSource() {
        return actualSource;
    }

    public void setActualSource(String actualSource) {
        this.actualSource = actualSource;
    }

    public String getDegradedReason() {
        return degradedReason;
    }

    public void setDegradedReason(String degradedReason) {
        this.degradedReason = degradedReason;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

