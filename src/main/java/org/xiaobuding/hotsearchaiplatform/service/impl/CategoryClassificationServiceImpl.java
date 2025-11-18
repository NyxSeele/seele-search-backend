package org.xiaobuding.hotsearchaiplatform.service.impl;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.xiaobuding.hotsearchaiplatform.model.HotSearchItem;
import org.xiaobuding.hotsearchaiplatform.service.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class CategoryClassificationServiceImpl implements CategoryClassificationService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryClassificationServiceImpl.class);
    
    // 分类结果缓存：标题核心词 -> 分类
    private static final Map<String, String> CLASSIFICATION_CACHE = new ConcurrentHashMap<>();
    
    // 关键词分类映射
    private static final Map<String, String[]> CATEGORY_KEYWORDS = new HashMap<>();
    
    static {
        // 严格按照前端定义的8个分类配置关键词
        // 优先级从高到低排列，避免误判
        
        // 1. 政治 - 国内外政治、政府政策、外交、选举
        CATEGORY_KEYWORDS.put("politics", new String[]{
            "政府", "外交", "官方", "中央", "国务院", "外交部", "外长", "召见", "政治", "选举", "议会", "政策",
            "日本", "美国", "中日", "中美", "台湾", "朝鲜", "韩国", "领导人", "主席", "总理", "总统",
            "会见", "访问", "会谈", "谈判", "发言人", "回应", "表态", "声明", "建交", "断交"
        });
        
        // 2. 军事 - 国防、武器、军演、战争、军队
        CATEGORY_KEYWORDS.put("military", new String[]{
            "军事", "战争", "导弹", "军队", "国防", "航母", "福建舰", "军演", "武器", "坦克", "战斗机",
            "舰队", "演习", "部队", "士兵", "将军", "核武", "军备", "防空", "战略", "战术"
        });
        
        // 3. 经济 - 股市、金融、房地产、企业、财经
        CATEGORY_KEYWORDS.put("economy", new String[]{
            "经济", "股市", "金融", "房地产", "企业", "财经", "A股", "涨跌", "市场", "投资", "银行",
            "上市", "IPO", "债券", "汇率", "GDP", "通胀", "贸易", "关税", "税收", "楼市", "房价",
            "融资", "基金", "证券", "期货", "原副省长", "被判", "受贿", "贪腐"
        });
        
        // 4. 科技 - AI、互联网、手机、科学发现、创新
        CATEGORY_KEYWORDS.put("tech", new String[]{
            "科技", "AI", "手机", "华为", "苹果", "小米", "互联网", "芯片", "麒麟", "技术", "5G", "6G",
            "人工智能", "机器人", "算法", "程序", "软件", "硬件", "电脑", "网络", "云计算", "大数据",
            "区块链", "虚拟现实", "元宇宙", "科学", "研究", "发明", "专利", "创新"
        });
        
        // 5. 娱乐 - 明星、电影、音乐、综艺、八卦
        CATEGORY_KEYWORDS.put("entertainment", new String[]{
            "明星", "电影", "音乐", "综艺", "娱乐", "演员", "歌手", "导演", "粉丝", "爱豆", "偶像",
            "歌曲", "电视剧", "节目", "演唱会", "演出", "首映", "票房", "获奖", "提名", "红毯",
            "婚礼", "离婚", "恋情", "绯闻", "八卦", "网红", "直播", "短视频"
        });
        
        // 6. 体育 - 足球、篮球、奥运、比赛、运动员
        CATEGORY_KEYWORDS.put("sports", new String[]{
            "体育", "足球", "篮球", "奥运", "比赛", "运动员", "乒乓球", "全运会", "选手", "冠军",
            "联赛", "世界杯", "欧洲杯", "亚洲杯", "NBA", "CBA", "球队", "球员", "教练", "进球",
            "得分", "冠军", "亚军", "金牌", "银牌", "铜牌", "破纪录", "夺冠", "淘汰"
        });
        
        // 7. 文化 - 文艺、历史、传统、艺术、文学
        CATEGORY_KEYWORDS.put("culture", new String[]{
            "文化", "历史", "艺术", "文学", "传统", "教育", "扶灵", "许绍雄", "博物馆", "展览",
            "书法", "绘画", "雕塑", "诗歌", "小说", "作家", "诗人", "古迹", "文物", "遗产",
            "非遗", "民俗", "节日", "春节", "中秋", "端午", "学校", "大学", "考试", "高考"
        });
        
        // 8. 社会 - 民生、事故、犯罪、社会热点、公益（作为兜底分类）
        CATEGORY_KEYWORDS.put("society", new String[]{
            "社会", "民生", "事故", "犯罪", "热点", "楼市", "房价", "气温", "降温", "天气",
            "交通", "火灾", "地震", "洪水", "灾害", "救援", "公益", "慈善", "志愿", "捐款",
            "医疗", "健康", "疫情", "病毒", "治疗", "药物", "食品", "安全", "环保", "污染"
        });
    }
    @Override
    public List<HotSearchItem> classifyItems(List<HotSearchItem> items) {
        logger.info("Start classification, total: {}", items.size());
        int classified = 0;
        for (HotSearchItem item : items) {
            if (item.getCategory() == null || "pending".equals(item.getCategory()) || "other".equals(item.getCategory())) {
                String category = classifyTitle(item.getTitle());
                item.setCategory(category);
                classified++;
            }
        }
        logger.info("Classification completed: {}/{} items classified to defined categories", classified, items.size());
        return items;
    }
    
    @Override
    public String classifyTitle(String title) {
        if (title == null || title.isEmpty()) {
            // 空标题默认归类到社会
            return "society";
        }
        
        // 1. 提取标题核心词（用于缓存复用）
        String coreWords = extractCoreWords(title);
        
        // 2. 检查缓存，相似标题直接复用
        String cachedCategory = CLASSIFICATION_CACHE.get(coreWords);
        if (cachedCategory != null) {
            logger.debug("Cache hit for '{}': {}", title, cachedCategory);
            return cachedCategory;
        }
        
        // 3. 使用关键词匹配进行分类
        // 按照优先级顺序匹配：政治、军事、经济、科技、娱乐、体育、文化、社会
        String category = null;
        
        // 定义优先级顺序（某些关键词可能同时匹配多个分类，优先选择更具体的）
        String[] priorityOrder = {"politics", "military", "economy", "tech", "entertainment", "sports", "culture", "society"};
        
        for (String cat : priorityOrder) {
            String[] keywords = CATEGORY_KEYWORDS.get(cat);
            if (keywords != null) {
                for (String keyword : keywords) {
                    if (title.contains(keyword)) {
                        category = cat;
                        logger.debug("Classified '{}' as '{}' (keyword: '{}')", title, category, keyword);
                        break;
                    }
                }
                if (category != null) break;
            }
        }
        
        // 4. 如果没有匹配到任何关键词，默认归类到"社会"（兜底分类）
        if (category == null) {
            category = "society";
            logger.debug("No keyword matched for '{}', default to 'society'", title);
        }
        
        // 5. 缓存结果供相似标题复用
        CLASSIFICATION_CACHE.put(coreWords, category);
        
        return category;
    }
    
    /**
     * 提取标题核心词，用于缓存key
     * 去除数字、标点等，保留主要关键词
     */
    private String extractCoreWords(String title) {
        // 移除数字、标点、空格
        String core = title.replaceAll("[0-9\\s\\p{Punct}]", "");
        // 如果太长，只取前15个字符
        if (core.length() > 15) {
            core = core.substring(0, 15);
        }
        return core;
    }
}
