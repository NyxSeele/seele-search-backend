package org.xiaobuding.hotsearchaiplatform.exception;

/**
 * 楠岃瘉寮傚父
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
