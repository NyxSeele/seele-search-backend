package org.xiaobuding.hotsearchaiplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AICoreService {
    private static final Logger logger = LoggerFactory.getLogger(AICoreService.class);
    
    @Value("${dashscope.api.key:}")
    private String apiKey;
    @Value("${dashscope.api.base-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String baseUrl;
    @Value("${dashscope.api.model:deepseek-v3.2-exp}")
    private String model;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public AICoreService() {
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String callDashScopeAPI(String prompt) {
        return callDashScopeAPI(prompt, null);
    }

    public String callDashScopeAPI(String prompt, String searchQuery) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("Qianwen API key not configured, returning mock response");
            return generateMockResponse(prompt);
        }
        
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            
            ObjectNode input = requestBody.putObject("input");
            ArrayNode messages = input.putArray("messages");
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a helpful assistant.");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            if (searchQuery != null && !searchQuery.isBlank()) {
                ObjectNode search = input.putObject("web_search");
                search.put("enable", true);
                search.put("query", searchQuery);
            }
            
            ObjectNode parameters = requestBody.putObject("parameters");
            parameters.put("result_format", "message");
            parameters.put("enable_thinking", false);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("Qianwen API call successful");
                return extractResponseText(response.body());
            } else {
                logger.error("Qianwen API error: {}", response.statusCode());
                return generateMockResponse(prompt);
            }
        } catch (Exception e) {
            logger.error("Failed to call Qianwen API", e);
            return generateMockResponse(prompt);
        }
    }
    
    private String extractResponseText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode output = root.path("output");
            if (output.has("text")) {
                return output.path("text").asText();
            }
            JsonNode choices = output.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                if (message.has("content")) {
                    return message.path("content").asText();
                }
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }
    
    private String generateMockResponse(String prompt) {
        if (prompt.contains("trend") || prompt.contains("summary")) {
            return "{\"summary\": \"当前热门话题包括科技、娱乐和社会问题。\", \"coreTopics\": [{\"topic\": \"科技\", \"description\": \"人工智能与创新\"}, {\"topic\": \"娱乐\", \"description\": \"电影与明星\"}]}";
        } else if (prompt.contains("evaluate")) {
            return "{\"background\": \"这是一个热门话题\", \"impactAssessment\": \"公众关注度较高\"}";
        } else {
            return "{\"answer\": \"抱歉，AI服务暂时不可用。请检查API密钥配置或稍后重试。\"}";
        }
    }
}