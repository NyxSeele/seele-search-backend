package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * 平台类型转换失败时抛出的异常
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
