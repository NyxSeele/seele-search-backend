package org.xiaobuding.hotsearchaiplatform.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.xiaobuding.hotsearchaiplatform.model.ApiResponse;
import org.xiaobuding.hotsearchaiplatform.exception.InvalidPlatformException;
import org.xiaobuding.hotsearchaiplatform.exception.ItemNotFoundException;
import org.xiaobuding.hotsearchaiplatform.exception.AIServiceException;
import org.xiaobuding.hotsearchaiplatform.exception.ValidationException;

import java.util.UUID;

/**
 * 全局异常处理器
 * 统一处理所有异常并返回标准化的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理平台类型转换异常
     */
    @ExceptionHandler(InvalidPlatformException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidPlatformException(
            InvalidPlatformException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        logger.warn("[{}] 平台类型转换失败: {}", traceId, ex.getMessage());

        ApiResponse<?> response = ApiResponse.badRequest(ex.getMessage());
        response.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理热搜项目不存在异常
     */
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleItemNotFoundException(
            ItemNotFoundException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        logger.warn("[{}] 热搜项目不存在: {}", traceId, ex.getMessage());

        ApiResponse<?> response = ApiResponse.notFound(ex.getMessage());
        response.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理 AI 服务异常
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleAIServiceException(
            AIServiceException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] AI服务异常: {}", traceId, ex.getMessage(), ex);

        ApiResponse<?> response = new ApiResponse<>(503, ex.getMessage());
        response.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            ValidationException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        logger.warn("[{}] 验证失败: {}", traceId, ex.getMessage());

        ApiResponse<?> response = ApiResponse.badRequest(ex.getMessage());
        response.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理方法参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        BindingResult bindingResult = ex.getBindingResult();
        String message = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数验证失败");

        logger.warn("[{}] 参数验证失败: {}", traceId, message);

        ApiResponse<?> response = ApiResponse.badRequest(message);
        response.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        logger.error("[{}] 未预期的异常: {}", traceId, ex.getMessage(), ex);

        ApiResponse<?> response = new ApiResponse<>(500, "服务器内部错误，请稍后重试");
        response.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
