package org.xiaobuding.hotsearchaiplatform.model;
import java.time.LocalDateTime;
import java.util.Map;
public class LastUpdateInfo {
    private LocalDateTime lastUpdate;
    private Map<String, LocalDateTime> platforms;
    private Map<String, LocalDateTime> categories;
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    public Map<String, LocalDateTime> getPlatforms() { return platforms; }
    public void setPlatforms(Map<String, LocalDateTime> platforms) { this.platforms = platforms; }
    public Map<String, LocalDateTime> getCategories() { return categories; }
    public void setCategories(Map<String, LocalDateTime> categories) { this.categories = categories; }
}
