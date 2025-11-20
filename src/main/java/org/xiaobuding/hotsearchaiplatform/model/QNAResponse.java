package org.xiaobuding.hotsearchaiplatform.model;

import java.util.List;

public class QNAResponse {
    private String conversationId;
    private String answer;
    private String status;
    private List<RelatedHotSearch> relatedHotSearches;

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<RelatedHotSearch> getRelatedHotSearches() { return relatedHotSearches; }
    public void setRelatedHotSearches(List<RelatedHotSearch> relatedHotSearches) { this.relatedHotSearches = relatedHotSearches; }

    public static class RelatedHotSearch {
        private Long id;
        private String title;
        private String platform;
        private Long heat;
        private Integer rank;
        private String category;
        private String url;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }

        public Long getHeat() { return heat; }
        public void setHeat(Long heat) { this.heat = heat; }

        public Integer getRank() { return rank; }
        public void setRank(Integer rank) { this.rank = rank; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
