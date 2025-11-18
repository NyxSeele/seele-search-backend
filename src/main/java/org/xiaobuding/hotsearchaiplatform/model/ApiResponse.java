package org.xiaobuding.hotsearchaiplatform.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 缁熶竴鐨?API 鍝嶅簲鏍煎紡
 */
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String traceId;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ApiResponse(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message);
    }

    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, message);
    }

    public static <T> ApiResponse<T> timeout(String message) {
        return new ApiResponse<>(504, message);
    }

    // Getters and setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
