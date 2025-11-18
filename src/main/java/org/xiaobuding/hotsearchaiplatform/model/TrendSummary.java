package org.xiaobuding.hotsearchaiplatform.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class TrendSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDateTime generatedAt;
    private String summary;
    private List<CoreTopic> coreTopics;
    private PlatformAnalysis platformAnalysis;
    private List<String> crossPlatformInsights;
    private DataStatus dataStatus;

    public TrendSummary() {
    }

    // Getters and setters
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<CoreTopic> getCoreTopics() {
        return coreTopics;
    }

    public void setCoreTopics(List<CoreTopic> coreTopics) {
        this.coreTopics = coreTopics;
    }

    public PlatformAnalysis getPlatformAnalysis() {
        return platformAnalysis;
    }

    public void setPlatformAnalysis(PlatformAnalysis platformAnalysis) {
        this.platformAnalysis = platformAnalysis;
    }

    public List<String> getCrossPlatformInsights() {
        return crossPlatformInsights;
    }

    public void setCrossPlatformInsights(List<String> crossPlatformInsights) {
        this.crossPlatformInsights = crossPlatformInsights;
    }

    public DataStatus getDataStatus() {
        return dataStatus;
    }

    public void setDataStatus(DataStatus dataStatus) {
        this.dataStatus = dataStatus;
    }

    public static class CoreTopic implements Serializable {
        private static final long serialVersionUID = 1L;
        private String topic;
        private String description;
        private List<String> platforms;
        private String heatLevel;

        public CoreTopic() {
        }

        public CoreTopic(String topic, String description, List<String> platforms, String heatLevel) {
            this.topic = topic;
            this.description = description;
            this.platforms = platforms;
            this.heatLevel = heatLevel;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getPlatforms() {
            return platforms;
        }

        public void setPlatforms(List<String> platforms) {
            this.platforms = platforms;
        }

        public String getHeatLevel() {
            return heatLevel;
        }

        public void setHeatLevel(String heatLevel) {
            this.heatLevel = heatLevel;
        }
    }

    public static class PlatformAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;
        private PlatformDetail weibo;
        private PlatformDetail TOUTIAO;
        private PlatformDetail bilibili;

        public PlatformAnalysis() {
        }

        public PlatformDetail getWeibo() {
            return weibo;
        }

        public void setWeibo(PlatformDetail weibo) {
            this.weibo = weibo;
        }

        public PlatformDetail getTOUTIAO() {
            return TOUTIAO;
        }

        public void setTOUTIAO(PlatformDetail TOUTIAO) {
            this.TOUTIAO = TOUTIAO;
        }

        public PlatformDetail getBilibili() {
            return bilibili;
        }

        public void setBilibili(PlatformDetail bilibili) {
            this.bilibili = bilibili;
        }
    }

    public static class PlatformDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        private String characteristic;
        private List<TopItem> topItems;
        private String summary;

        public PlatformDetail() {
        }

        public String getCharacteristic() {
            return characteristic;
        }

        public void setCharacteristic(String characteristic) {
            this.characteristic = characteristic;
        }

        public List<TopItem> getTopItems() {
            return topItems;
        }

        public void setTopItems(List<TopItem> topItems) {
            this.topItems = topItems;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }

    public static class TopItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private int rank;
        private String title;
        private String description;
        private long heatValue;
        private String trend;

        public TopItem() {
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public long getHeatValue() {
            return heatValue;
        }

        public void setHeatValue(long heatValue) {
            this.heatValue = heatValue;
        }

        public String getTrend() {
            return trend;
        }

        public void setTrend(String trend) {
            this.trend = trend;
        }
    }

    public static class DataStatus implements Serializable {
        private static final long serialVersionUID = 1L;
        private String weibo;
        private String TOUTIAO;
        private String bilibili;

        public DataStatus() {
        }

        public DataStatus(String weibo, String TOUTIAO, String bilibili) {
            this.weibo = weibo;
            this.TOUTIAO = TOUTIAO;
            this.bilibili = bilibili;
        }

        public String getWeibo() {
            return weibo;
        }

        public void setWeibo(String weibo) {
            this.weibo = weibo;
        }

        public String getTOUTIAO() {
            return TOUTIAO;
        }

        public void setTOUTIAO(String TOUTIAO) {
            this.TOUTIAO = TOUTIAO;
        }

        public String getBilibili() {
            return bilibili;
        }

        public void setBilibili(String bilibili) {
            this.bilibili = bilibili;
        }
    }
}
