package org.xiaobuding.hotsearchaiplatform.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class EvaluationResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private String itemId;
    private PlatformType platform;
    private String title;
    private LocalDateTime evaluatedAt;
    private String background;
    private String impactAssessment;
    private List<String> perspectives;
    private List<String> futureOutlook;

    public EvaluationResult() {
    }

    public EvaluationResult(String itemId, PlatformType platform, String title, LocalDateTime evaluatedAt,
                            String background, String impactAssessment, List<String> perspectives, List<String> futureOutlook) {
        this.itemId = itemId;
        this.platform = platform;
        this.title = title;
        this.evaluatedAt = evaluatedAt;
        this.background = background;
        this.impactAssessment = impactAssessment;
        this.perspectives = perspectives;
        this.futureOutlook = futureOutlook;
    }

    // Getters and setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public PlatformType getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformType platform) {
        this.platform = platform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getImpactAssessment() {
        return impactAssessment;
    }

    public void setImpactAssessment(String impactAssessment) {
        this.impactAssessment = impactAssessment;
    }

    public List<String> getPerspectives() {
        return perspectives;
    }

    public void setPerspectives(List<String> perspectives) {
        this.perspectives = perspectives;
    }

    public List<String> getFutureOutlook() {
        return futureOutlook;
    }

    public void setFutureOutlook(List<String> futureOutlook) {
        this.futureOutlook = futureOutlook;
    }
}
