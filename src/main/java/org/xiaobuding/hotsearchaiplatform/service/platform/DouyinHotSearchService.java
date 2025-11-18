package org.xiaobuding.hotsearchaiplatform.service.platform;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.model.PlatformType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抖音热搜专用爬取服务
 * 针对抖音平台优化的高速爬取实现
 */
@Service
public class DouyinHotSearchService {
    private static final Logger logger = LoggerFactory.getLogger(DouyinHotSearchService.class);
    // 主API
    private static final String DOUYIN_URL = "https://mini.itunes123.com/node/23zziBmEFZ/";
    // 备用API（优先级从高到低）
    private static final String[] BACKUP_DOUYIN_URLS = {
        "https://www.36jxs.com/hot/7.html",       // 备用1
        "https://www.remenla.com/hot/douyin"      // 备用2
    };
    
    public List<HotSearchItem> fetchHotSearch() {
        long startTime = System.currentTimeMillis();
        List<HotSearchItem> items = new ArrayList<>();
        
        try {
            logger.info("========== 开始爬取抖音热搜 ==========");
            
            // 第1步：先访问第三方首页，建立Session和Cookie
            logger.info("抖音: 步骤1 - 访问第三方首页建立Session");
            org.jsoup.Connection.Response homepageResponse = Jsoup.connect("https://mini.itunes123.com/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Cache-Control", "max-age=0")
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .maxBodySize(0)
                    .method(org.jsoup.Connection.Method.GET)
                    .execute();
            
            logger.info("抖音: 首页访问成功，获得Cookie: {}", homepageResponse.cookies());
            
            // 第2步：使用首页的Cookie访问抖音热搜页面
            logger.info("抖音: 步骤2 - 访问抖音热搜页面");
            Document doc = Jsoup.connect(DOUYIN_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Cache-Control", "max-age=0")
                    .referrer("https://mini.itunes123.com/")
                    .cookies(homepageResponse.cookies())
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .maxBodySize(0)
                    .get();
            
            // 根据HTML源码，使用li.c-text选择器
            Elements listItems = doc.select("li.c-text");
            logger.info("抖音: 选择器'li.c-text'找到 {} 个元素", listItems.size());
            
            if (listItems.isEmpty()) {
                logger.warn("抖音: 未找到任何数据，尝试备用选择器");
                listItems = doc.select("li a[href*='/n/']").parents();
            }
            
            int rank = 1;
            for (Element item : listItems) {
                if (rank > 50) break;
                
                Element link = item.selectFirst("a[href*='/n/']");
                if (link == null) continue;
                
                String title = link.text().trim();
                if (title.isEmpty()) continue;
                
                // 提取热度
                String itemFullText = item.text();
                String heatStr = extractHeat(title, itemFullText);
                long heat = parseHeat(heatStr);
                
                // 生成URL
                String url = generateUrl(title);
                
                HotSearchItem hotItem = new HotSearchItem();
                hotItem.setPlatform(PlatformType.DOUYIN);
                hotItem.setTitle(title);
                hotItem.setHeat(heat);
                hotItem.setRank(rank);
                hotItem.setUrl(url);
                hotItem.setCapturedAt(LocalDateTime.now());
                hotItem.setCategory("pending");
                items.add(hotItem);
                
                if (rank <= 3) {
                    logger.debug("抖音 rank {}: title='{}', heat={}", rank, title, heat);
                }
                rank++;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("========== 抖音爬取完成: {} 条数据, 耗时 {}ms ==========", items.size(), duration);
            
            // 如果主API没有数据，尝试备用API
            if (items.isEmpty()) {
                logger.warn("抖音主API返回0条数据，尝试切换到备用API");
                try {
                    items = fetchFromBackupAPI();
                } catch (Exception backupError) {
                    logger.error("抖音备用API也失败: {}", backupError.getMessage(), backupError);
                }
            }
            
        } catch (Exception e) {
            logger.error("抖音主API爬取异常: {}", e.getMessage());
            logger.warn("抖音: 尝试切换到备用API");
            try {
                items = fetchFromBackupAPI();
            } catch (Exception backupError) {
                logger.error("抖音备用API也失败: {}", backupError.getMessage(), backupError);
            }
        }
        
        return items;
    }
    
    /**
     * 从备用API爬取数据（尝试多个备用源）
     */
    private List<HotSearchItem> fetchFromBackupAPI() throws Exception {
        logger.info("========== 抖音: 尝试备用API ==========");
        
        for (int i = 0; i < BACKUP_DOUYIN_URLS.length; i++) {
            String backupUrl = BACKUP_DOUYIN_URLS[i];
            String apiName = "备用API" + (i + 1);
            
            try {
                logger.info("抖音: 尝试{} - {}", apiName, backupUrl);
                List<HotSearchItem> items = fetchFromSingleBackupAPI(backupUrl, apiName);
                
                if (!items.isEmpty()) {
                    logger.info("========== 抖音{}爬取成功: {} 条数据 ==========", apiName, items.size());
                    return items;
                }
                
                logger.warn("抖音{}: 返回0条数据，尝试下一个", apiName);
                
            } catch (Exception e) {
                logger.error("抖音{}: 爬取失败 - {}", apiName, e.getMessage());
                if (i == BACKUP_DOUYIN_URLS.length - 1) {
                    throw new Exception("所有备用API都失败", e);
                }
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 从单个备用API爬取
     */
    private List<HotSearchItem> fetchFromSingleBackupAPI(String url, String apiName) throws Exception {
        List<HotSearchItem> items = new ArrayList<>();
        
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .ignoreHttpErrors(true)
                .get();
        
        Elements listItems = doc.select("tbody tr");
        logger.info("抖音{}: 选择器'tbody tr'找到 {} 个元素", apiName, listItems.size());
        
        if (listItems.isEmpty()) {
            listItems = doc.select("div.item, li.item, tr");
            logger.info("抖音{}: 选择器'div.item'找到 {} 个元素", apiName, listItems.size());
        }
        
        if (listItems.isEmpty()) {
            listItems = doc.select("a[href*='/hot/douyin/'], a[href*='douyin']");
            logger.info("抖音{}: 选择器'a'找到 {} 个元素", apiName, listItems.size());
        }
        
        int rank = 1;
        for (Element item : listItems) {
            if (rank > 50) break;
            
            Element link = item.tagName().equals("a") ? item : item.selectFirst("a");
            if (link == null) continue;
            
            String title = link.text().trim();
            if (title.isEmpty() || title.length() < 3) continue;
            
            if (title.matches("^\\d+$") || title.contains("排名") || title.contains("热搜榜")) {
                continue;
            }
            
            String itemFullText = item.text();
            String heatStr = extractHeat(title, itemFullText);
            long heat = parseHeat(heatStr);
            
            String itemUrl = "https://www.douyin.com/search/" + URLEncoder.encode(title, StandardCharsets.UTF_8);
            
            HotSearchItem hotItem = new HotSearchItem();
            hotItem.setPlatform(PlatformType.DOUYIN);
            hotItem.setTitle(title);
            hotItem.setHeat(heat);
            hotItem.setRank(rank);
            hotItem.setUrl(itemUrl);
            hotItem.setCapturedAt(LocalDateTime.now());
            hotItem.setCategory("pending");
            items.add(hotItem);
            
            if (rank <= 3) {
                logger.debug("抖音{} rank {}: title='{}', heat={}", apiName, rank, title, heat);
            }
            rank++;
        }
        
        return items;
    }
    
    private String extractHeat(String title, String fullText) {
        String heatPart = fullText.replace(title, "").trim();
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)[万千亿]\\s*$");
        Matcher matcher = pattern.matcher(heatPart);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)[万千亿]");
        matcher = pattern.matcher(heatPart);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        return "";
    }
    
    private long parseHeat(String str) {
        if (str == null || str.isEmpty()) return 0;
        try {
            String numStr = str.replaceAll("[^0-9.]", "");
            if (numStr.isEmpty()) return 0;
            
            double val = Double.parseDouble(numStr);
            
            if (str.contains("亿")) {
                val *= 100000000;
            } else if (str.contains("万")) {
                val *= 10000;
            } else if (str.contains("千")) {
                val *= 1000;
            }
            
            return (long) val;
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String generateUrl(String title) {
        try {
            String enc = URLEncoder.encode(title, StandardCharsets.UTF_8);
            return "https://www.douyin.com/search/" + enc;
        } catch (Exception e) {
            return "";
        }
    }
}
