package org.xiaobuding.hotsearchaiplatform.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.service.AIService;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {
    private static final Logger logger = LoggerFactory.getLogger(AIController.class);
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/summary/global")
    public ResponseEntity<ApiResponse<TrendSummary>> getGlobalSummary() {
        logger.info("Get global summary");
        try {
            TrendSummary summary = aiService.analyzeGlobalTrends();
            return ResponseEntity.ok(ApiResponse.success("Success", summary));
        } catch (Exception e) {
            logger.error("Failed to get global summary", e);
            return ResponseEntity.status(503)
                    .body(ApiResponse.serverError("AI service temporarily unavailable"));
        }
    }

    @GetMapping("/summary/platform/{platform}")
    public ResponseEntity<ApiResponse<TrendSummary>> getPlatformSummary(@PathVariable String platform) {
        logger.info("Get platform summary: {}", platform);
        try {
            // For now, return global summary (can be enhanced later)
            TrendSummary summary = aiService.analyzeGlobalTrends();
            return ResponseEntity.ok(ApiResponse.success("Success", summary));
        } catch (Exception e) {
            logger.error("Failed to get platform summary", e);
            return ResponseEntity.status(503)
                    .body(ApiResponse.serverError("AI service temporarily unavailable"));
        }
    }

    @PostMapping("/qna")
    public ResponseEntity<ApiResponse<QNAResponse>> askQuestion(@RequestBody QNARequest request) {
        logger.info("QNA request: {}", request.getQuestion());
        try {
            QNAResponse response = aiService.answerQuestion(
                    request.getQuestion(),
                    request.getConversationId(),
                    request.getPlatformFilter()
            );
            return ResponseEntity.ok(ApiResponse.success("Success", response));
        } catch (Exception e) {
            logger.error("QNA failed", e);
            return ResponseEntity.status(503)
                    .body(ApiResponse.serverError("AI service temporarily unavailable"));
        }
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<EvaluationResult>> evaluateHotSearch(@RequestBody EvaluateRequest request) {
        logger.info("Evaluate request: platform={}, itemId={}", request.getPlatform(), request.getItemId());
        try {
            PlatformType platformType = PlatformType.valueOf(request.getPlatform().toUpperCase());
            EvaluationResult result = aiService.evaluateSingleItem(platformType, request.getItemId());
            return ResponseEntity.ok(ApiResponse.success("Success", result));
        } catch (Exception e) {
            logger.error("Evaluation failed", e);
            return ResponseEntity.status(503)
                    .body(ApiResponse.serverError("AI service temporarily unavailable"));
        }
    }

    public static class QNARequest {
        private String question;
        private String conversationId;
        private String platformFilter;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public String getPlatformFilter() { return platformFilter; }
        public void setPlatformFilter(String platformFilter) { this.platformFilter = platformFilter; }
    }

    public static class EvaluateRequest {
        private String platform;
        private String itemId;

        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }
    }
}