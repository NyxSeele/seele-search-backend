package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * 骞冲彴绫诲瀷杞崲寮傚父
 */
public class InvalidPlatformException extends RuntimeException {
    private String validPlatforms;

    public InvalidPlatformException(String message) {
        super(message);
    }

    public InvalidPlatformException(String message, String validPlatforms) {
        super(message);
        this.validPlatforms = validPlatforms;
    }

    public String getValidPlatforms() {
        return validPlatforms;
    }
}
