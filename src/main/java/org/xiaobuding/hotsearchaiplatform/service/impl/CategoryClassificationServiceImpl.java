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
            "会见", "访问", "会谈", "谈判", "发言人", "回应", "表态", "声明", "建交", "断交",
            "国际关系", "外交部长", "外交官", "大使", "领事馆", "大使馆", "外交政策",
            "国际会议", "峰会", "首脑", "国会", "议员", "国际组织", "联合国"
        });
        
        // 2. 军事 - 国防、武器、军演、战争、军队
        CATEGORY_KEYWORDS.put("military", new String[]{
            "军事", "战争", "导弹", "军队", "国防", "航母", "福建舰", "军演", "武器", "坦克", "战斗机",
            "舰队", "演习", "部队", "士兵", "将军", "核武", "军备", "防空", "战略", "战术",
            "军舰", "战舰", "潜艇", "军机", "战机", "军事基地", "军区", "战区", "军事演习",
            "军事行动", "战役", "战斗", "军事冲突", "武装力量", "军事实力"
        });
        
        // 3. 经济 - 股市、金融、房地产、企业、财经、商业
        CATEGORY_KEYWORDS.put("economy", new String[]{
            "经济", "股市", "金融", "房地产", "企业", "财经", "A股", "涨跌", "市场", "投资", "银行",
            "上市", "IPO", "债券", "汇率", "GDP", "通胀", "贸易", "关税", "税收", "楼市", "房价",
            "融资", "基金", "证券", "期货", "商业", "公司", "集团", "董事", "CEO", "总裁", "营收",
            "利润", "亏损", "破产", "并购", "收购", "股东", "股价", "股票", "港股", "美股", "创业板",
            "科创板", "新三板", "估值", "市值", "财报", "业绩", "营业额", "销售额", "电商", "零售",
            "消费", "物价", "CPI", "PPI", "降息", "加息", "货币", "外汇", "人民币", "美元", "欧元",
            "石油", "黄金", "大宗商品", "期权", "衍生品", "风投", "天使投资", "私募", "对冲基金",
            "财富", "富豪", "首富", "身家", "资产", "负债", "现金流", "资本", "产业", "制造业",
            "服务业", "第三产业", "供应链", "产能", "订单", "合同", "招标", "中标", "项目", "工程"
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
            "婚礼", "离婚", "恋情", "绯闻", "八卦", "网红", "直播", "短视频",
            "影视", "影星", "艺人", "艺能", "演艺圈", "娱乐圈", "明星嘉宾",
            "电影节", "音乐会", "演唱会", "粉丝见面会", "影视作品", "电影作品"
        });
        
        // 6. 体育 - 足球、篮球、奥运、比赛、运动员
        CATEGORY_KEYWORDS.put("sports", new String[]{
            "体育", "足球", "篮球", "奥运", "比赛", "运动员", "乒乓球", "全运会", "选手", "冠军",
            "联赛", "世界杯", "欧洲杯", "亚洲杯", "NBA", "CBA", "球队", "球员", "教练", "进球",
            "得分", "冠军", "亚军", "金牌", "银牌", "铜牌", "破纪录", "夺冠", "淘汰",
            "体育赛事", "运动会", "体育项目", "体育明星", "体育界", "体育竞技",
            "球赛", "赛场", "体育场", "运动员退役", "运动员复出"
        });
        
        // 7. 文化 - 文艺、历史、传统、艺术、文学（严格精简关键词）
        CATEGORY_KEYWORDS.put("culture", new String[]{
            "文化遗产", "文化交流", "文化活动", "文化艺术", "文化传承", "文化现象",
            "历史人物", "历史事件", "历史文化", "古代历史", "朝代", "皇帝",
            "艺术家", "艺术界", "艺术作品", "书法", "绘画", "雕塑",
            "文学作品", "文学界", "作家", "诗人", "诗歌", "小说",
            "博物馆", "展览", "文物", "遗产", "古迹", "考古", "发掘",
            "非遗", "民俗", "传统文化", "传统艺术", "传统节日",
            "教育改革", "教育政策", "学术研究", "学术成果", "学术界"
        });
        
        // 8. 社会 - 民生、事故、犯罪、社会热点、公益（作为兜底分类）
        CATEGORY_KEYWORDS.put("society", new String[]{
            "社会事件", "公共安全", "社会问题", "民生工程", "公共事件",
            "事故", "犯罪", "火灾", "地震", "洪水", "灾害", "救援",
            "公益", "慈善", "志愿", "捐款", "医疗", "健康",
            "疫情", "病毒", "治疗", "药物", "食品安全", "环保", "污染",
            "交通", "气温", "天气", "降温", "人民生活"
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
        
        // 3. 使用增强的关键词匹配进行分类（加权评分机制）
        // 按照优先级顺序匹配：军事、政治、科技、经济、体育、娱乐、文化、社会
        String category = null;
        double maxScore = 0;
        
        // 定义优先级顺序和权重（严格分类，均衡分布）
        String[] priorityOrder = {"military", "politics", "tech", "economy", "sports", "entertainment", "culture", "society"};
        Map<String, Double> categoryWeights = new HashMap<>();
        categoryWeights.put("military", 2.5);  // 军事最高优先级
        categoryWeights.put("politics", 2.5);  // 政治最高优先级
        categoryWeights.put("tech", 2.0);      // 科技高优先级
        categoryWeights.put("economy", 1.8);   // 经济较高优先级
        categoryWeights.put("sports", 1.6);    // 体育中等优先级
        categoryWeights.put("entertainment", 1.6);  // 娱乐中等优先级
        categoryWeights.put("culture", 1.4);   // 文化中低优先级
        categoryWeights.put("society", 0.8);   // 社会作为兜底
        
        // 多关键词匹配机制：统计每个分类匹配到的关键词数量并计算加权分数
        for (String cat : priorityOrder) {
            String[] keywords = CATEGORY_KEYWORDS.get(cat);
            if (keywords != null) {
                int matchCount = 0;
                StringBuilder matchedKeywords = new StringBuilder();
                
                for (String keyword : keywords) {
                    if (title.contains(keyword)) {
                        matchCount++;
                        if (matchedKeywords.length() > 0) {
                            matchedKeywords.append(", ");
                        }
                        matchedKeywords.append(keyword);
                    }
                }
                
                // 计算加权分数：匹配数量 * 分类权重
                double score = matchCount * categoryWeights.getOrDefault(cat, 1.0);
                
                // 如果当前分类得分更高，则选择该分类
                if (score > maxScore) {
                    maxScore = score;
                    category = cat;
                    logger.debug("Classified '{}' as '{}' (matched {} keywords, score: {}, keywords: {})", 
                        title, category, matchCount, score, matchedKeywords.toString());
                }
                
                // 如果已经匹配到3个以上关键词，且是最高优先级分类，直接返回
                if (matchCount >= 3 && (cat.equals("military") || cat.equals("politics"))) {
                    break;
                }
                // 如果已经匹配到2个以上关键词，且是高优先级分类，直接返回
                if (matchCount >= 2 && (cat.equals("tech") || cat.equals("economy"))) {
                    break;
                }
            }
        }
        
        // 4. 如果没有匹配到任何关键词，使用严格的智能推断
        if (category == null || maxScore < 1.0) {
            // 根据标题特征进行严格推断
            if (title.matches(".*[0-9]+.*") && title.length() < 15) {
                // 短标题且包含数字，更可能是科技或经济类
                category = "tech";
                logger.debug("Numeric short title '{}' inferred as 'tech'", title);
            } else if (title.length() > 25) {
                // 超长标题更可能是娱乐类
                category = "entertainment";
                logger.debug("Very long title '{}' inferred as 'entertainment'", title);
            } else {
                // 其他情况归类到社会
                category = "society";
                logger.debug("No keyword matched for '{}', default to 'society'", title);
            }
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
        // 如果太长，只取前20个字符（增加缓存精度）
        if (core.length() > 20) {
            core = core.substring(0, 20);
        }
        return core;
    }
}
