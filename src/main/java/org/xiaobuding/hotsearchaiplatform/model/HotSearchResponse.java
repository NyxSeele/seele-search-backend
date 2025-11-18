package org.xiaobuding.hotsearchaiplatform.model;
import java.util.List;
public class HotSearchResponse {
    private List<HotSearchItem> items;
    private int total;
    private String message;
    public List<HotSearchItem> getItems() { return items; }
    public void setItems(List<HotSearchItem> items) { this.items = items; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
