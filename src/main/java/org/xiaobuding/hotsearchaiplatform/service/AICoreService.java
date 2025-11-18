package org.xiaobuding.hotsearchaiplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public AICoreService() {
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }
    
    public String callDashScopeAPI(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("Qianwen API key not configured, returning mock response");
            return generateMockResponse(prompt);
        }
        
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "qwen-turbo");
            
            ObjectNode input = requestBody.putObject("input");
            input.put("prompt", prompt);
            
            ObjectNode parameters = requestBody.putObject("parameters");
            parameters.put("result_format", "text");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"))
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
            var root = objectMapper.readTree(responseBody);
            return root.path("output").path("text").asText();
        } catch (Exception e) {
            return responseBody;
        }
    }
    
    private String generateMockResponse(String prompt) {
        if (prompt.contains("trend") || prompt.contains("summary")) {
            return "{\"summary\": \"Current trending topics include technology, entertainment, and social issues.\", \"coreTopics\": [{\"topic\": \"Technology\", \"description\": \"AI and innovation\"}, {\"topic\": \"Entertainment\", \"description\": \"Movies and celebrities\"}]}";
        } else if (prompt.contains("evaluate")) {
            return "{\"background\": \"This is a trending topic\", \"impactAssessment\": \"High public interest\"}";
        } else {
            return "{\"answer\": \"Based on current hot search data, the main trends are...\"}";
        }
    }
}