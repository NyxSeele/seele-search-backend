package org.xiaobuding.hotsearchaiplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.exception.*;
import org.xiaobuding.hotsearchaiplatform.model.*;
import org.xiaobuding.hotsearchaiplatform.service.AICoreService;
import org.xiaobuding.hotsearchaiplatform.service.AIService;
import org.xiaobuding.hotsearchaiplatform.service.DataValidationService;
import org.xiaobuding.hotsearchaiplatform.service.HotSearchCollectorService;
import org.xiaobuding.hotsearchaiplatform.service.SearchService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    private final AICoreService aiCoreService;
    private final HotSearchCollectorService hotSearchCollectorService;
    private final SearchService searchService;
    private final DataValidationService dataValidationService;
    private final ObjectMapper objectMapper;
    private static final List<String> HOT_QUESTION_KEYWORDS = Arrays.asList(
            "热搜", "热榜", "热点", "热度", "榜单", "排名", "舆论", "话题", "上榜", "趋势"
    );
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[\\s,.;，。！？!?:：/\\\\]+");
    private static final List<String> TIME_KEYWORDS = Arrays.asList("时间", "几点", "date", "time", "现在几号", "今天几号", "星期几", "周几");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    public AIServiceImpl(AICoreService aiCoreService,
                         HotSearchCollectorService hotSearchCollectorService,
                         SearchService searchService,
                         DataValidationService dataValidationService) {
        this.aiCoreService = aiCoreService;
        this.hotSearchCollectorService = hotSearchCollectorService;
        this.searchService = searchService;
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
            prompt.append("1. summary: 全局趋势总结（必须分点说明，使用\\n分隔，每点以\"1. \"、\"2. \"、\"3. \"开头，共分为3-5点，每点30-50字）\n");
            prompt.append("2. coreTopics: 核心话题数组（3-5个），每个话题包含：\n");
            prompt.append("   - topic: 话题名称（简洁明了）\n");
            prompt.append("   - description: 话题描述（必须分点说明，使用\\n分隔，每点以\"· \"开头，共分为2-3点，每点20-30字）\n");
            prompt.append("   - platforms: 涉及的平台数组（如[\"WEIBO\", \"DOUYIN\"]）\n");
            prompt.append("   - heatLevel: 热度等级（HIGH/MEDIUM/LOW）\n");
            prompt.append("3. crossPlatformInsights: 跨平台洞察数组（3-5条，每条必须是一个完整的观点，30-50字）\n");
            prompt.append("\n注意：\n");
            prompt.append("- 必须返回有效的JSON格式\n");
            prompt.append("- 所有字段都必须填写，不能为空\n");
            prompt.append("- summary和description必须分点列出，不能是一整段文字\n");
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
            if (isTimeQuestion(question)) {
                return buildLocalTimeAnswer(conversationId);
            }

            List<HotSearchItem> keywordMatches = findDatabaseMatches(question, platformFilter);
            boolean looksLikeHotSearch = isHotSearchQuestion(question);

            if (!keywordMatches.isEmpty()) {
                return buildDatabaseAnswer(question, conversationId, keywordMatches);
            }

            if (looksLikeHotSearch) {
                List<HotSearchItem> relevantItems = collectRelevantItems(question, platformFilter);
                if (!relevantItems.isEmpty()) {
                    return buildInsightAnswer(question, conversationId, relevantItems);
                }
            }

            return buildWebSearchAnswer(question, conversationId);
        } catch (Exception e) {
            logger.error("QNA processing failed", e);
            return createFallbackQNAResponse(question, conversationId);
        }
    }

    private QNAResponse buildDatabaseAnswer(String question, String conversationId, List<HotSearchItem> matches) {
        Comparator<HotSearchItem> byTime = Comparator.comparing(
                HotSearchItem::getCapturedAt,
                Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<HotSearchItem> byRank = Comparator.comparing(
                HotSearchItem::getRank,
                Comparator.nullsLast(Comparator.naturalOrder()));

        List<HotSearchItem> topItems = matches.stream()
                .sorted(byTime.reversed().thenComparing(byRank))
                .limit(5)
                .collect(Collectors.toList());

        QNAResponse response = new QNAResponse();
        response.setConversationId(conversationId);
        response.setAnswer(formatDataDrivenAnswer(question, topItems));
        response.setStatus("DATA_MATCH");
        response.setRelatedHotSearches(convertRelatedItems(topItems));
        return response;
    }

    private QNAResponse buildInsightAnswer(String question, String conversationId, List<HotSearchItem> relevantItems) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一个专业的热搜分析师。请结合下面的热搜数据，用中文回答用户的问题。\n");
            prompt.append("用户问题：").append(question).append("\n\n");
            prompt.append("相关热搜（仅供参考，请确保回答建立在这些数据上）：\n");
            relevantItems.stream().limit(12).forEach(item -> prompt
                    .append("- 平台: ").append(item.getPlatform())
                    .append("，标题: ").append(item.getTitle())
                    .append("，热度: ").append(formatHeat(item.getHeat()))
                    .append("，分类: ").append(Optional.ofNullable(item.getCategory()).orElse("未分类"))
                    .append("\n"));
            prompt.append("\n回答要求：\n");
            prompt.append("1. 先用一句话概括整体态势；\n");
            prompt.append("2. 然后用分点形式给出2-4条关键判断；\n");
            prompt.append("3. 每条判断控制在25-40字；\n");
            prompt.append("4. 如果数据不足，请直说而不是编造；\n");
            prompt.append("5. 输出格式必须是 JSON 对象：{\"answer\": \"...\"}，不要包含 markdown 或额外字段。\n");

            String aiResponse = aiCoreService.callDashScopeAPI(prompt.toString());
            QNAResponse response = parseQNAResponse(aiResponse, conversationId);
            response.setStatus("AI_HOTSEARCH");
            response.setRelatedHotSearches(convertRelatedItems(relevantItems.stream().limit(5).collect(Collectors.toList())));
            return response;
        } catch (Exception e) {
            logger.warn("AI insight generation failed, fallback to database answer", e);
            return buildDatabaseAnswer(question, conversationId, relevantItems);
        }
    }

    private QNAResponse buildWebSearchAnswer(String question, String conversationId) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("你是一个实时联网的中文助手。你可以访问最新的互联网搜索结果。");
            prompt.append("请严格按照下面格式输出：\n");
            prompt.append("{\"answer\":\"(用一到两段中文直接回答，禁止出现markdown、列表、星号或额外字段)\"}\n");
            prompt.append("要求：\n");
            prompt.append("1. 首句给出明确结论；\n");
            prompt.append("2. 若引用事实，可在句末用括号标注来源网站名；\n");
            prompt.append("3. 若搜索无结果，请写“未检索到相关信息”；\n");
            prompt.append("4. 严禁出现中文冒号或“answer：”这类前缀，必须保持 JSON 键为英文冒号。\n");
            prompt.append("用户问题：").append(question).append("\n");

            String aiResponse = aiCoreService.callDashScopeAPI(prompt.toString(), question);
            QNAResponse response = parseQNAResponse(aiResponse, conversationId);
            response.setStatus("WEB_SEARCH");
            response.setRelatedHotSearches(Collections.emptyList());
            return response;
        } catch (Exception e) {
            logger.error("Web search assistant failed", e);
            return createFallbackQNAResponse(question, conversationId);
        }
    }

    private boolean isTimeQuestion(String question) {
        if (question == null) return false;
        String normalized = question.toLowerCase(Locale.ROOT);
        return TIME_KEYWORDS.stream().anyMatch(keyword -> normalized.contains(keyword.toLowerCase(Locale.ROOT)));
    }

    private QNAResponse buildLocalTimeAnswer(String conversationId) {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm:ss"));
        String weekday = now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.CHINA);
        QNAResponse response = new QNAResponse();
        response.setConversationId(conversationId);
        response.setAnswer("当前北京时间为 " + formatted + "，" + weekday + "。");
        response.setStatus("LOCAL_TIME");
        response.setRelatedHotSearches(Collections.emptyList());
        return response;
    }

    private boolean isHotSearchQuestion(String question) {
        if (question == null) {
            return false;
        }
        String normalized = question.toLowerCase(Locale.ROOT);
        return HOT_QUESTION_KEYWORDS.stream().anyMatch(keyword -> normalized.contains(keyword));
    }

    private List<HotSearchItem> findDatabaseMatches(String question, String platformFilter) {
        if (question == null || question.isBlank()) {
            return Collections.emptyList();
        }

        Set<Long> seenIds = new LinkedHashSet<>();
        List<HotSearchItem> matches = new ArrayList<>();
        List<String> keywords = extractKeywords(question);
        if (keywords.isEmpty()) {
            keywords = Collections.singletonList(question.trim());
        }

        PlatformType platform = null;
        if (platformFilter != null && !platformFilter.isBlank()) {
            try {
                platform = PlatformType.valueOf(platformFilter.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        for (String keyword : keywords) {
            if (keyword.length() < 2) {
                continue;
            }
            List<HotSearchItem> found = searchService.searchByKeyword(keyword);
            for (HotSearchItem item : found) {
                if (platform != null && item.getPlatform() != platform) {
                    continue;
                }
                if (item.getId() == null || !seenIds.add(item.getId())) {
                    continue;
                }
                matches.add(item);
            }
            if (matches.size() >= 8) {
                break;
            }
        }
        return matches;
    }

    private List<String> extractKeywords(String text) {
        if (text == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(WORD_SPLIT_PATTERN.split(text))
                .map(String::trim)
                .filter(token -> token.length() >= 2)
                .limit(6)
                .collect(Collectors.toList());
    }

    private List<QNAResponse.RelatedHotSearch> convertRelatedItems(List<HotSearchItem> items) {
        return items.stream().map(item -> {
            QNAResponse.RelatedHotSearch related = new QNAResponse.RelatedHotSearch();
            related.setId(item.getId());
            related.setTitle(item.getTitle());
            related.setPlatform(item.getPlatform() != null ? item.getPlatform().name() : null);
            related.setHeat(item.getHeat());
            related.setRank(item.getRank());
            related.setCategory(item.getCategory());
            related.setUrl(item.getUrl());
            return related;
        }).collect(Collectors.toList());
    }

    private String formatDataDrivenAnswer(String question, List<HotSearchItem> items) {
        if (items.isEmpty()) {
            return "暂未在数据库中找到与「" + question + "」直接匹配的热搜记录。";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("根据数据库记录，「").append(question).append("」相关的热搜如下：\n");
        for (int i = 0; i < items.size(); i++) {
            HotSearchItem item = items.get(i);
            builder.append(i + 1).append(". ")
                    .append(getPlatformName(item.getPlatform()))
                    .append(" · ").append(Optional.ofNullable(item.getCategory()).orElse("未分类"))
                    .append(" · ").append(item.getTitle());
            if (item.getHeat() != null) {
                builder.append("（热度 ").append(formatHeat(item.getHeat())).append("）");
            }
            builder.append("\n");
        }
        LocalDateTime latestTime = items.stream()
                .map(HotSearchItem::getCapturedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (latestTime != null) {
            builder.append("数据更新：").append(latestTime.format(TIME_FORMATTER));
        }
        return builder.toString().trim();
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
        QNAResponse response = new QNAResponse();
        response.setConversationId(conversationId);

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            response.setAnswer("AI service temporarily unavailable, please try again later.");
            response.setStatus("EMPTY_RESPONSE");
            return response;
        }

        logger.debug("Raw AI response: {}", aiResponse);
        String cleaned = cleanJsonResponse(aiResponse);

        // Attempt direct JSON parsing first
        try {
            JsonNode root = objectMapper.readTree(cleaned);
            response.setAnswer(root.path("answer").asText("Unable to generate answer"));
            response.setStatus("STRUCTURED");
            return response;
        } catch (Exception ignored) {
            // Fallback if cleaned text is still not JSON
        }

        // Try to normalize patterns like "answer：xxx" or "答复: xxx"
        String normalized = cleaned;
        normalized = normalized
                .replaceAll("(?i)answer\\s*[:：]", "")
                .replaceAll("(?i)答复\\s*[:：]", "")
                .trim();

        if (!normalized.isEmpty()) {
            response.setAnswer(normalized);
            response.setStatus("RAW_TEXT");
            return response;
        }

        response.setAnswer("AI service temporarily unavailable, please try again later.");
        response.setStatus("EMPTY_RESPONSE");
        return response;
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
        response.setRelatedHotSearches(Collections.emptyList());
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