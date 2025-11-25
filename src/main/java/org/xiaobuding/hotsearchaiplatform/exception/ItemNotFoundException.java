package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * 热搜条目不存在时抛出的异常
 */
public class ItemNotFoundException extends RuntimeException {
    private String itemId;
    private String platform;

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, String itemId, String platform) {
        super(message);
        this.itemId = itemId;
        this.platform = platform;
    }

    public String getItemId() {
        return itemId;
    }

    public String getPlatform() {
        return platform;
    }
}
