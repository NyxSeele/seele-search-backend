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
 * 微博热搜专用爬取服务
 * 针对微博平台优化的高速爬取实现
 */
@Service
public class WeiboHotSearchService {
    private static final Logger logger = LoggerFactory.getLogger(WeiboHotSearchService.class);
    // 主API（API1）- 使用用户指定的URL
    private static final String WEIBO_URL = "https://mini.itunes123.com/c/J7jYiuVjrR/";
    // 备用API（API2 ~ API4），优先级从高到低
    private static final String[] BACKUP_WEIBO_URLS = {
        "https://rebang.today/?tab=weibo",          // 备用API2
        "https://www.entobit.cn/hot-search/desktop", // 备用API3
        "https://www.weibotop.cn/2.0/"              // 备用API4
    };
    
    public List<HotSearchItem> fetchHotSearch() {
        long startTime = System.currentTimeMillis();
        List<HotSearchItem> items = new ArrayList<>();
        
        try {
            logger.info("========== 开始爬取微博热搜 ==========");
            
            // 第1步：先访问第三方首页，建立Session和Cookie
            logger.info("微博: 步骤1 - 访问第三方首页建立Session");
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
            
            logger.info("微博: 首页访问成功，获得Cookie: {}", homepageResponse.cookies());
            
            // 第2步：使用首页的Cookie访问微博热搜页面
            logger.info("微博: 步骤2 - 访问微博热搜页面");
            Document doc = Jsoup.connect(WEIBO_URL)
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
            logger.info("微博: 选择器'li.c-text'找到 {} 个元素", listItems.size());
            
            if (listItems.isEmpty()) {
                logger.info("微博: 尝试备用选择器'li:has(a)'");
                listItems = doc.select("li:has(a)");
                logger.info("微博: 备用选择器找到 {} 个元素", listItems.size());
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
                hotItem.setPlatform(PlatformType.WEIBO);
                hotItem.setTitle(title);
                hotItem.setHeat(heat);
                hotItem.setRank(rank);
                hotItem.setUrl(url);
                hotItem.setCapturedAt(LocalDateTime.now());
                hotItem.setCategory("pending");
                items.add(hotItem);
                
                if (rank <= 3) {
                    logger.debug("微博 rank {}: title='{}', heat={}", rank, title, heat);
                }
                rank++;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("========== 微博爬取完成: {} 条数据, 耗时 {}ms ==========", items.size(), duration);
            
            // 如果主API没有数据，尝试备用API
            if (items.isEmpty()) {
                logger.warn("微博主API返回0条数据，尝试切换到备用API");
                try {
                    items = fetchFromBackupAPI();
                } catch (Exception backupError) {
                    logger.error("微博备用API也失败: {}", backupError.getMessage(), backupError);
                }
            }
            
        } catch (Exception e) {
            logger.error("微博主API爬取异常: {}", e.getMessage());
            logger.warn("微博: 尝试切换到备用API");
            try {
                items = fetchFromBackupAPI();
            } catch (Exception backupError) {
                logger.error("微博备用API也失败: {}", backupError.getMessage(), backupError);
            }
        }
        
        return items;
    }
    
    /**
     * 从备用API爬取数据（尝试多个备用源）
     */
    private List<HotSearchItem> fetchFromBackupAPI() throws Exception {
        logger.info("========== 微博: 尝试备用API ==========");
        
        // 按优先级尝试每个备用API
        for (int i = 0; i < BACKUP_WEIBO_URLS.length; i++) {
            String backupUrl = BACKUP_WEIBO_URLS[i];
            String apiName = "备用API" + (i + 1);
            
            try {
                logger.info("微博: 尝试{} - {}", apiName, backupUrl);
                List<HotSearchItem> items = fetchFromSingleBackupAPI(backupUrl, apiName);
                
                if (!items.isEmpty()) {
                    logger.info("========== 微博{}爬取成功: {} 条数据 ==========", apiName, items.size());
                    return items;
                }
                
                logger.warn("微博{}: 返回0条数据，尝试下一个", apiName);
                
            } catch (Exception e) {
                logger.error("微博{}: 爬取失败 - {}", apiName, e.getMessage());
                if (i == BACKUP_WEIBO_URLS.length - 1) {
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
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(10000)
                .ignoreHttpErrors(true)
                .get();
        
        // 针对weibotop.cn，需要找到"微博热搜榜"区域
        if (url.contains("weibotop.cn")) {
            return fetchFromWeibotop(doc, apiName);
        }
        
        // 尝试多个选择器
        Elements listItems = doc.select("tbody tr");  // 官方API表格结构
        logger.info("微博{}: 选择器'tbody tr'找到 {} 个元素", apiName, listItems.size());
        
        if (listItems.isEmpty()) {
            listItems = doc.select("div.item, li.item, div.hot-item");
            logger.info("微博{}: 选择器'div.item'找到 {} 个元素", apiName, listItems.size());
        }
        
        if (listItems.isEmpty()) {
            listItems = doc.select("a[href*='weibo'], a[title]");
            logger.info("微博{}: 选择器'a'找到 {} 个元素", apiName, listItems.size());
        }
        
        int rank = 1;
        for (Element item : listItems) {
            if (rank > 50) break;
            
            Element link = item.tagName().equals("a") ? item : item.selectFirst("a");
            if (link == null) continue;
            
            String title = link.text().trim();
            if (title.isEmpty() || title.length() < 3) continue;
            
            // 过滤掉"排名"、"热搜榜"等非热搜标题
            if (title.matches("^\\d+$") || title.contains("排名") || title.contains("热搜榜")) {
                continue;
            }
            
            String itemFullText = item.text();
            String heatStr = extractHeat(title, itemFullText);
            long heat = parseHeat(heatStr);
            
            String itemUrl = generateUrl(title);
            
            HotSearchItem hotItem = new HotSearchItem();
            hotItem.setPlatform(PlatformType.WEIBO);
            hotItem.setTitle(title);
            hotItem.setHeat(heat);
            hotItem.setRank(rank);
            hotItem.setUrl(itemUrl);
            hotItem.setCapturedAt(LocalDateTime.now());
            hotItem.setCategory("pending");
            items.add(hotItem);
            
            if (rank <= 3) {
                logger.debug("微博{} rank {}: title='{}', heat={}", apiName, rank, title, heat);
            }
            rank++;
        }
        
        return items;
    }
    
    /**
     * 从weibotop.cn爬取 - 只解析微博热搜榜下的数据
     */
    private List<HotSearchItem> fetchFromWeibotop(Document doc, String apiName) {
        List<HotSearchItem> items = new ArrayList<>();
        
        // 查找包含"微博热搜榜"标题的区域
        Element weiboSection = null;
        
        // 方法1: 查找包含"微博热搜榜"文本的元素
        Elements headings = doc.select("h1, h2, h3, h4, .title, .header, div[class*='title'], div[class*='header']");
        for (Element heading : headings) {
            String headingText = heading.text();
            if (headingText.contains("微博热搜榜") || headingText.contains("微博热搜")) {
                // 找到标题后的列表区域
                Element parent = heading.parent();
                if (parent != null) {
                    // 查找父容器中的表格或列表
                    Elements listItems = parent.select("tbody tr, table tr");
                    if (listItems.size() > 5) {
                        weiboSection = parent;
                        logger.info("微博{}: 通过标题找到微博热搜榜区域，包含 {} 个列表项", apiName, listItems.size());
                        break;
                    }
                }
                // 或者查找下一个兄弟元素
                Element nextSibling = heading.nextElementSibling();
                if (nextSibling != null) {
                    Elements listItems = nextSibling.select("tbody tr, table tr");
                    if (listItems.size() > 5) {
                        weiboSection = nextSibling;
                        logger.info("微博{}: 通过兄弟元素找到微博热搜榜区域", apiName);
                        break;
                    }
                }
            }
        }
        
        // 方法2: 如果没找到，查找包含"榜单时间"的元素（这是微博热搜榜的特征）
        if (weiboSection == null) {
            Elements timeElements = doc.select("*:contains(榜单时间), *:contains(实时更新)");
            for (Element timeEl : timeElements) {
                Element parent = timeEl.parent();
                if (parent != null) {
                    Elements listItems = parent.select("tbody tr, table tr");
                    if (listItems.size() > 5) {
                        weiboSection = parent;
                        logger.info("微博{}: 通过'榜单时间'找到微博热搜榜区域", apiName);
                        break;
                    }
                }
            }
        }
        
        Elements listItems;
        if (weiboSection != null) {
            // 在微博热搜榜区域内查找列表项
            listItems = weiboSection.select("tbody tr");
            logger.info("微博{}: 在微博热搜榜区域内找到 {} 个tr元素", apiName, listItems.size());
        } else {
            // 如果找不到明确区域，尝试全局查找表格行
            listItems = doc.select("tbody tr");
            logger.warn("微博{}: 未找到明确的微博热搜榜区域，使用全局查找", apiName);
        }
        
        int rank = 1;
        for (Element item : listItems) {
            if (rank > 50) break;
            
            // 查找标题链接
            Element link = item.selectFirst("a");
            if (link == null) continue;
            
            String title = link.text().trim();
            if (title.isEmpty() || title.length() < 3) continue;
            
            // 过滤掉非热搜标题
            if (title.matches("^\\d+$") || title.contains("排名") || title.contains("热搜榜") || 
                title.contains("跳转") || title.contains("在榜") || title.contains("今日最高排名")) {
                continue;
            }
            
            // 提取热度 - 从表格中查找热度数据
            String heatStr = "";
            Elements heatElements = item.select("td");
            for (Element td : heatElements) {
                String tdText = td.text().trim();
                if (tdText.matches(".*\\d+.*")) {
                    // 尝试提取热度数字
                    Pattern pattern = Pattern.compile("(\\d+)");
                    Matcher matcher = pattern.matcher(tdText);
                    if (matcher.find()) {
                        String num = matcher.group(1);
                        // 如果数字大于1000，可能是热度值
                        if (num.length() >= 4) {
                            heatStr = tdText;
                            break;
                        }
                    }
                }
            }
            
            long heat = parseHeat(heatStr);
            
            // 提取链接URL
            String itemUrl = link.attr("href");
            if (itemUrl == null || itemUrl.isEmpty() || !itemUrl.startsWith("http")) {
                itemUrl = generateUrl(title);
            }
            
            HotSearchItem hotItem = new HotSearchItem();
            hotItem.setPlatform(PlatformType.WEIBO);
            hotItem.setTitle(title);
            hotItem.setHeat(heat);
            hotItem.setRank(rank);
            hotItem.setUrl(itemUrl);
            hotItem.setCapturedAt(LocalDateTime.now());
            hotItem.setCategory("pending");
            items.add(hotItem);
            
            if (rank <= 3) {
                logger.debug("微博{} rank {}: title='{}', heat={}", apiName, rank, title, heat);
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
            return "https://s.weibo.com/weibo?q=" + enc;
        } catch (Exception e) {
            return "";
        }
    }
}
