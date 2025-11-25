package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * 输入或参数校验失败时抛出的异常
 */
public class ValidationException extends RuntimeException {
    private String fieldName;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
