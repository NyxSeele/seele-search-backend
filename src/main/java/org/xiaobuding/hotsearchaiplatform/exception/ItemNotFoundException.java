package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * 鐑悳椤圭洰涓嶅瓨鍦ㄥ紓甯?
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
