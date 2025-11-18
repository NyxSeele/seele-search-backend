package org.xiaobuding.hotsearchaiplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.exception.*;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.service.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    private final AICoreService aiCoreService;
    private final HotSearchCollectorService hotSearchCollectorService;
    private final DataValidationService dataValidationService;
    private final ObjectMapper objectMapper;

    public AIServiceImpl(AICoreService aiCoreService,
                         HotSearchCollectorService hotSearchCollectorService,
                         DataValidationService dataValidationService) {
        this.aiCoreService = aiCoreService;
        this.hotSearchCollectorService = hotSearchCollectorService;
        this.dataValidationService = dataValidationService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @Cacheable(value = "aiSummary", key = "'global'")
    public TrendSummary analyzeGlobalTrends() {
        logger.info("Analyzing global trends");
        try {
            List<HotSearchItem> allItems = hotSearchCollectorService.collectAll(false);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一个专业的热搜数据分析师。请分析以下来自不同平台的热搜数据，提供深度分析报告。\n\n");
            
            Map<PlatformType, List<HotSearchItem>> grouped = allItems.stream()
                    .limit(100)
                    .collect(Collectors.groupingBy(HotSearchItem::getPlatform));
            
            prompt.append("=== 各平台热搜数据 ===\n\n");
            
            for (Map.Entry<PlatformType, List<HotSearchItem>> entry : grouped.entrySet()) {
                PlatformType platform = entry.getKey();
                List<HotSearchItem> items = entry.getValue();
                
                prompt.append("【").append(getPlatformName(platform)).append("】\n");
                prompt.append("平台特点：").append(getPlatformCharacteristic(platform)).append("\n");
                prompt.append("TOP10热搜：\n");
                
                items.stream().limit(10).forEach(item -> {
                    prompt.append(String.format("  %d. %s (热度: %s, 分类: %s)\n", 
                        item.getRank(), 
                        item.getTitle(), 
                        formatHeat(item.getHeat()),
                        item.getCategory()));
                });
                prompt.append("\n");
            }
            
            prompt.append("\n=== 分析要求 ===\n");
            prompt.append("请以JSON格式返回分析结果，包含以下字段：\n");
            prompt.append("1. summary: 全局趋势总结（150-200字，分析当前热点事件的主要方向、社会关注焦点、各平台内容差异）\n");
            prompt.append("2. coreTopics: 核心话题数组（3-5个），每个话题包含：\n");
            prompt.append("   - topic: 话题名称（简洁明了）\n");
            prompt.append("   - description: 话题描述（50-80字，说明为什么重要、涉及哪些平台、影响范围）\n");
            prompt.append("   - platforms: 涉及的平台数组（如[\"WEIBO\", \"DOUYIN\"]）\n");
            prompt.append("   - heatLevel: 热度等级（HIGH/MEDIUM/LOW）\n");
            prompt.append("3. crossPlatformInsights: 跨平台洞察数组（3-5条，每条30-50字，分析不同平台对同一事件的关注差异）\n");
            prompt.append("\n注意：\n");
            prompt.append("- 必须返回有效的JSON格式\n");
            prompt.append("- 所有字段都必须填写，不能为空\n");
            prompt.append("- 分析要具体、有深度，避免泛泛而谈\n");
            prompt.append("- 核心话题必须从实际热搜数据中提取，不能编造\n");
            
            String aiResponse = aiCoreService.callDashScopeAPI(prompt.toString());
            TrendSummary summary = parseGlobalTrendResponse(aiResponse);
            summary.setGeneratedAt(LocalDateTime.now());
            return summary;
        } catch (Exception e) {
            logger.error("Failed to analyze global trends", e);
            return createFallbackSummary();
        }
    }

    @Override
    @Cacheable(value = "evaluation", key = "#platform.name() + ':' + #itemId")
    public EvaluationResult evaluateSingleItem(PlatformType platform, String itemId) {
        logger.info("Evaluating item: platform={}, itemId={}", platform, itemId);
        try {
            boolean exists = dataValidationService.isItemExists(platform, itemId);
            if (!exists) {
                throw new ItemNotFoundException("Item not found: " + itemId, itemId, platform.name());
            }

            String title = dataValidationService.getItemTitle(platform, itemId);
            Long heat = dataValidationService.getItemHeat(platform, itemId);

            String prompt = String.format(
                "Evaluate this hot search item:\nPlatform: %s\nTitle: %s\nHeat: %d\n\n" +
                "Provide JSON format response with 'background' and 'impactAssessment' fields.",
                platform.name(), title, heat);
            
            String aiResponse = aiCoreService.callDashScopeAPI(prompt);
            EvaluationResult result = parseEvaluationResponse(aiResponse, platform, itemId, title);
            result.setEvaluatedAt(LocalDateTime.now());
            return result;
        } catch (ItemNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to evaluate item", e);
            throw new AIServiceException("Evaluation failed", true);
        }
    }

    @Override
    public QNAResponse answerQuestion(String question, String conversationId, String platformFilter) {
        logger.info("Processing QNA: conversationId={}, question={}", conversationId, question);
        try {
            List<HotSearchItem> relevantItems = collectRelevantItems(question, platformFilter);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("Question: ").append(question).append("\n\nRelevant hot searches:\n");
            relevantItems.stream().limit(10).forEach(item ->
                    prompt.append("- ").append(item.getTitle()).append(" (").append(item.getPlatform()).append(")\n"));
            prompt.append("\nPlease answer in JSON format with 'answer' field.");
            
            String aiResponse = aiCoreService.callDashScopeAPI(prompt.toString());
            QNAResponse response = parseQNAResponse(aiResponse, conversationId);
            response.setStatus("SUCCESS");
            return response;
        } catch (Exception e) {
            logger.error("QNA processing failed", e);
            return createFallbackQNAResponse(question, conversationId);
        }
    }

    private TrendSummary parseGlobalTrendResponse(String aiResponse) {
        try {
            String cleaned = cleanJsonResponse(aiResponse);
            JsonNode root = objectMapper.readTree(cleaned);
            
            TrendSummary summary = new TrendSummary();
            summary.setSummary(root.has("summary") ? root.get("summary").asText() : "Unable to generate summary");
            
            if (root.has("coreTopics") && root.get("coreTopics").isArray()) {
                List<TrendSummary.CoreTopic> topics = new ArrayList<>();
                for (JsonNode node : root.get("coreTopics")) {
                    TrendSummary.CoreTopic topic = new TrendSummary.CoreTopic();
                    topic.setTopic(node.path("topic").asText());
                    topic.setDescription(node.path("description").asText());
                    topics.add(topic);
                }
                summary.setCoreTopics(topics);
            }
            return summary;
        } catch (Exception e) {
            logger.error("Failed to parse trend response", e);
            return createFallbackSummary();
        }
    }

    private EvaluationResult parseEvaluationResponse(String aiResponse, PlatformType platform, String itemId, String title) {
        try {
            String cleaned = cleanJsonResponse(aiResponse);
            JsonNode root = objectMapper.readTree(cleaned);
            
            EvaluationResult result = new EvaluationResult();
            result.setItemId(itemId);
            result.setPlatform(platform);
            result.setTitle(title);
            result.setBackground(root.path("background").asText("No background info"));
            result.setImpactAssessment(root.path("impactAssessment").asText("No impact assessment"));
            return result;
        } catch (Exception e) {
            return createFallbackEvaluation(platform, itemId, title);
        }
    }

    private QNAResponse parseQNAResponse(String aiResponse, String conversationId) {
        try {
            String cleaned = cleanJsonResponse(aiResponse);
            JsonNode root = objectMapper.readTree(cleaned);
            
            QNAResponse response = new QNAResponse();
            response.setConversationId(conversationId);
            response.setAnswer(root.path("answer").asText("Unable to generate answer"));
            return response;
        } catch (Exception e) {
            return createFallbackQNAResponse("", conversationId);
        }
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null) return "{}";
        String cleaned = raw.trim();
        if (cleaned.startsWith("\uFEFF")) cleaned = cleaned.substring(1);
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private List<HotSearchItem> collectRelevantItems(String question, String platformFilter) {
        try {
            if (platformFilter != null && !platformFilter.isEmpty()) {
                try {
                    PlatformType platform = PlatformType.valueOf(platformFilter.toUpperCase());
                    return hotSearchCollectorService.collectByPlatform(platform, false);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
            return hotSearchCollectorService.collectAll(false);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private TrendSummary createFallbackSummary() {
        TrendSummary summary = new TrendSummary();
        summary.setSummary("AI service temporarily unavailable");
        summary.setCoreTopics(new ArrayList<>());
        summary.setGeneratedAt(LocalDateTime.now());
        return summary;
    }

    private EvaluationResult createFallbackEvaluation(PlatformType platform, String itemId, String title) {
        EvaluationResult result = new EvaluationResult();
        result.setItemId(itemId);
        result.setPlatform(platform);
        result.setTitle(title);
        result.setBackground("Unable to get background info");
        result.setImpactAssessment("Unable to assess impact");
        result.setEvaluatedAt(LocalDateTime.now());
        return result;
    }

    private QNAResponse createFallbackQNAResponse(String question, String conversationId) {
        QNAResponse response = new QNAResponse();
        response.setConversationId(conversationId);
        response.setAnswer("AI service temporarily unavailable, please try again later.");
        response.setStatus("FALLBACK");
        return response;
    }

    private String getPlatformName(PlatformType platform) {
        switch (platform) {
            case WEIBO: return "微博";
            case TOUTIAO: return "今日头条";
            case BILIBILI: return "B站";
            case DOUYIN: return "抖音";
            default: return platform.name();
        }
    }

    private String getPlatformCharacteristic(PlatformType platform) {
        switch (platform) {
            case WEIBO: return "社交媒体，用户基数大，热点传播快，娱乐、社会、政治话题活跃";
            case TOUTIAO: return "资讯平台，新闻时效性强，社会民生、国际时事关注度高";
            case BILIBILI: return "年轻用户聚集地，二次元文化、游戏、科技内容丰富";
            case DOUYIN: return "短视频平台，流量最大，娱乐、生活、社会话题覆盖广";
            default: return "综合平台";
        }
    }

    private String formatHeat(Long heat) {
        if (heat == null || heat == 0) return "0";
        if (heat >= 100000000) return String.format("%.1f亿", heat / 100000000.0);
        if (heat >= 10000) return String.format("%.1f万", heat / 10000.0);
        return heat.toString();
    }
}