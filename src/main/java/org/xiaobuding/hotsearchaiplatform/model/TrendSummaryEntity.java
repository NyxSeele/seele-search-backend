package org.xiaobuding.hotsearchaiplatform.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "trend_summary")
public class TrendSummaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String summary;
    private LocalDateTime generatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
