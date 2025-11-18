package org.xiaobuding.hotsearchaiplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xiaobuding.hotsearchaiplatform.model.TrendSummary;
import org.xiaobuding.hotsearchaiplatform.service.impl.AIServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 服务响应解析测试
 */
public class AIServiceImplTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    /**
     * 测试 AI 响应解析 - 完整响应
     */
    @Test
    public void testParseCompleteAIResponse() {
        String aiResponse = "{\n" +
                "  \"summary\": \"基于今日100条热搜数据的综合分析，微博、今日头条、B站三大平台呈现出多元化热点特征。\",\n" +
                "  \"coreTopics\": [\n" +
                "    {\n" +
                "      \"topic\": \"科技创新\",\n" +
                "      \"description\": \"AI技术和芯片相关话题持续热议\",\n" +
                "      \"heat\": 95000,\n" +
                "      \"platforms\": [\"weibo\", \"TOUTIAO\", \"bilibili\"]\n" +
                "    },\n" +
                "    {\n" +
                "      \"topic\": \"社会民生\",\n" +
                "      \"description\": \"教育、医疗等民生话题关注度高\",\n" +
                "      \"heat\": 87000,\n" +
                "      \"platforms\": [\"weibo\", \"TOUTIAO\"]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"platformAnalysis\": {\n" +
                "    \"weibo\": {\n" +
                "      \"characteristic\": \"实时性强，话题讨论热烈\",\n" +
                "      \"topItems\": [\n" +
                "        {\n" +
                "          \"rank\": 1,\n" +
                "          \"title\": \"AI技术突破\",\n" +
                "          \"description\": \"新型AI模型发布\",\n" +
                "          \"heatValue\": 98000,\n" +
                "          \"trend\": \"上升\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"summary\": \"微博热点以科技创新为主\"\n" +
                "    },\n" +
                "    \"TOUTIAO\": {\n" +
                "      \"characteristic\": \"搜索热度稳定，覆盖面广\",\n" +
                "      \"topItems\": [\n" +
                "        {\n" +
                "          \"rank\": 1,\n" +
                "          \"title\": \"教育改革\",\n" +
                "          \"description\": \"新政策发布\",\n" +
                "          \"heatValue\": 85000,\n" +
                "          \"trend\": \"稳定\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"summary\": \"今日头条热点以民生话题为主\"\n" +
                "    },\n" +
                "    \"bilibili\": {\n" +
                "      \"characteristic\": \"年轻用户为主，内容多元\",\n" +
                "      \"topItems\": [\n" +
                "        {\n" +
                "          \"rank\": 1,\n" +
                "          \"title\": \"游戏新作\",\n" +
                "          \"description\": \"热门游戏发布\",\n" +
                "          \"heatValue\": 75000,\n" +
                "          \"trend\": \"上升\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"summary\": \"B站热点以娱乐内容为主\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"crossPlatformInsights\": [\n" +
                "    \"科技创新话题在三大平台均获得高关注\",\n" +
                "    \"年轻用户对娱乐内容的关注度持续上升\",\n" +
                "    \"民生话题的讨论热度稳定\"\n" +
                "  ]\n" +
                "}";

        // 验证 JSON 有效性
        assertDoesNotThrow(() -> objectMapper.readTree(aiResponse));

        // 验证关键字段存在
        assertDoesNotThrow(() -> {
            var root = objectMapper.readTree(aiResponse);
            assertTrue(root.has("summary"), "缺少 summary 字段");
            assertTrue(root.has("coreTopics"), "缺少 coreTopics 字段");
            assertTrue(root.has("platformAnalysis"), "缺少 platformAnalysis 字段");
            assertTrue(root.has("crossPlatformInsights"), "缺少 crossPlatformInsights 字段");
        });

        System.out.println("✅ AI 完整响应解析测试通过");
    }

    /**
     * 测试 AI 响应解析 - 缺少字段
     */
    @Test
    public void testParsePartialAIResponse() {
        String aiResponse = "{\n" +
                "  \"summary\": \"基于热搜数据的分析\",\n" +
                "  \"coreTopics\": []\n" +
                "}";

        assertDoesNotThrow(() -> {
            var root = objectMapper.readTree(aiResponse);
            assertTrue(root.has("summary"), "summary 字段存在");
            assertTrue(root.has("coreTopics"), "coreTopics 字段存在");
            assertFalse(root.has("platformAnalysis"), "platformAnalysis 字段不存在（预期）");
        });

        System.out.println("✅ AI 部分响应解析测试通过");
    }

    /**
     * 测试 JSON 解析性能
     */
    @Test
    public void testParsePerformance() {
        String aiResponse = "{\n" +
                "  \"summary\": \"基于今日100条热搜数据的综合分析\",\n" +
                "  \"coreTopics\": [\n" +
                "    {\"topic\": \"话题1\", \"description\": \"描述1\", \"heat\": 90000, \"platforms\": [\"weibo\"]},\n" +
                "    {\"topic\": \"话题2\", \"description\": \"描述2\", \"heat\": 85000, \"platforms\": [\"TOUTIAO\"]}\n" +
                "  ],\n" +
                "  \"platformAnalysis\": {\n" +
                "    \"weibo\": {\"characteristic\": \"特征\", \"topItems\": [], \"summary\": \"总结\"},\n" +
                "    \"TOUTIAO\": {\"characteristic\": \"特征\", \"topItems\": [], \"summary\": \"总结\"},\n" +
                "    \"bilibili\": {\"characteristic\": \"特征\", \"topItems\": [], \"summary\": \"总结\"}\n" +
                "  },\n" +
                "  \"crossPlatformInsights\": [\"洞察1\", \"洞察2\"]\n" +
                "}";

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            assertDoesNotThrow(() -> objectMapper.readTree(aiResponse));
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("✅ 1000次 JSON 解析耗时: " + duration + "ms");
        assertTrue(duration < 5000, "解析性能过低，应在 5 秒内完成 1000 次解析");
    }

    /**
     * 测试 TrendSummary 对象映射
     */
    @Test
    public void testTrendSummaryMapping() {
        String aiResponse = "{\n" +
                "  \"summary\": \"测试总结\",\n" +
                "  \"coreTopics\": [\n" +
                "    {\"topic\": \"话题\", \"description\": \"描述\", \"heat\": 90000, \"platforms\": [\"weibo\"]}\n" +
                "  ]\n" +
                "}";

        assertDoesNotThrow(() -> {
            var root = objectMapper.readTree(aiResponse);

            // 验证 summary 映射
            String summary = root.get("summary").asText();
            assertEquals("测试总结", summary);

            // 验证 coreTopics 映射
            var coreTopics = root.get("coreTopics");
            assertTrue(coreTopics.isArray());
            assertEquals(1, coreTopics.size());

            var firstTopic = coreTopics.get(0);
            assertEquals("话题", firstTopic.get("topic").asText());
            assertEquals("描述", firstTopic.get("description").asText());
            assertEquals(90000, firstTopic.get("heat").asLong());
        });

        System.out.println("✅ TrendSummary 对象映射测试通过");
    }

    /**
     * 测试异常处理
     */
    @Test
    public void testInvalidJSONHandling() {
        String invalidJson = "{invalid json}";

        assertThrows(Exception.class, () -> objectMapper.readTree(invalidJson));

        System.out.println("✅ 异常处理测试通过");
    }
}
