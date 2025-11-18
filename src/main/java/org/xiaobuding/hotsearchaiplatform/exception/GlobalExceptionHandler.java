package org.xiaobuding.hotsearchaiplatform.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xiaobuding.hotsearchaiplatform.model.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        logger.error("Global exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.serverError("Server Error"));
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleItemNotFound(ItemNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound(ex.getMessage()));
    }

    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleAIServiceException(AIServiceException ex) {
        logger.error("AI service exception", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.serverError("AI Service Unavailable"));
    }
}
