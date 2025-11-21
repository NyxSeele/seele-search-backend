package org.xiaobuding.hotsearchaiplatform.service.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 崩坏3公告爬取服务
 */
@Service
public class HonkaiHotSearchService {
    private static final Logger logger = LoggerFactory.getLogger(HonkaiHotSearchService.class);
    private static final String HONKAI_API = "https://bbs-api.miyoushe.com/post/wapi/userPost?uid=73565430&page_size=%d&page=%d";
    private static final int PAGE_SIZE = 20;
    private static final int MAX_ITEMS = 50;
    private static final int MAX_PAGES = 5;
    private static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern BRACKET_CATEGORY = Pattern.compile("【([^】]+)】");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HonkaiHotSearchService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<HotSearchItem> fetchHotSearch() {
        long startTime = System.currentTimeMillis();
        List<HotSearchItem> items = new ArrayList<>();

        try {
            logger.info("========== 开始爬取崩坏3公告 ==========");
            for (int page = 1; page <= MAX_PAGES && items.size() < MAX_ITEMS; page++) {
                String url = String.format(HONKAI_API, PAGE_SIZE, page);
                JsonNode listNode = fetchPageData(url);
                if (listNode == null || !listNode.isArray() || listNode.isEmpty()) {
                    break;
                }

                for (JsonNode node : listNode) {
                    if (items.size() >= MAX_ITEMS) {
                        break;
                    }
                    HotSearchItem item = buildHotSearchItem(node, items.size() + 1);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            logger.info("========== 崩坏3爬取完成: {} 条数据, 耗时 {}ms ==========", items.size(), duration);
        } catch (Exception e) {
            logger.error("崩坏3爬取异常: {}", e.getMessage(), e);
        }

        return items;
    }

    private JsonNode fetchPageData(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + ",text/plain,*/*");
            headers.set("Referer", "https://www.miyoushe.com/bh3/accountCenter/postList?id=73565430");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                logger.warn("崩坏3接口返回异常: status={}, url={}", response.getStatusCode(), url);
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("retcode").asInt(-1) != 0) {
                logger.warn("崩坏3接口 retcode 非0: {}, url={}", root.path("retcode").asInt(), url);
                return null;
            }
            return root.path("data").path("list");
        } catch (Exception e) {
            logger.warn("崩坏3接口请求失败: {}", e.getMessage());
            return null;
        }
    }

    private HotSearchItem buildHotSearchItem(JsonNode entry, int rank) {
        JsonNode postNode = entry.path("post");
        JsonNode statNode = entry.path("stat");

        if (postNode.isMissingNode()) {
            return null;
        }

        String title = postNode.path("subject").asText("").trim();
        if (title.isEmpty()) {
            return null;
        }

        String postId = postNode.path("post_id").asText("");
        String url = postId.isEmpty()
                ? "https://www.miyoushe.com/bh3/accountCenter/postList?id=73565430"
                : "https://www.miyoushe.com/bh3/article/" + postId;

        long heat = statNode.path("view_num").asLong(0);
        if (heat <= 0) {
            heat = statNode.path("like_num").asLong(0);
        }

        long createdAt = postNode.path("created_at").asLong(0);
        LocalDateTime capturedAt = createdAt > 0
                ? LocalDateTime.ofInstant(Instant.ofEpochSecond(createdAt), CHINA_ZONE)
                : LocalDateTime.now(CHINA_ZONE);

        HotSearchItem hotItem = new HotSearchItem();
        hotItem.setPlatform(PlatformType.WEIBO); // 仍沿用占位符，前端以描述区分
        hotItem.setTitle(title);
        hotItem.setHeat(heat > 0 ? heat : 0);
        hotItem.setRank(rank);
        hotItem.setUrl(url);
        hotItem.setCapturedAt(capturedAt);
        hotItem.setCategory(extractCategoryFromTitle(title));
        hotItem.setActualSource("MIYOUSHE");
        return hotItem;
    }

    private String extractCategoryFromTitle(String title) {
        if (title == null) {
            return "pending";
        }
        Matcher matcher = BRACKET_CATEGORY.matcher(title);
        if (matcher.find()) {
            String category = matcher.group(1).trim();
            if (!category.isEmpty()) {
                return category;
            }
        }
        return "pending";
    }
}
