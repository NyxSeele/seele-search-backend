package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * AI 服务调用失败或降级时抛出的异常
 */
public class AIServiceException extends RuntimeException {
    private boolean isDegraded;

    public AIServiceException(String message) {
        super(message);
        this.isDegraded = false;
    }

    public AIServiceException(String message, boolean isDegraded) {
        super(message);
        this.isDegraded = isDegraded;
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.isDegraded = false;
    }

    public boolean isDegraded() {
        return isDegraded;
    }
}
