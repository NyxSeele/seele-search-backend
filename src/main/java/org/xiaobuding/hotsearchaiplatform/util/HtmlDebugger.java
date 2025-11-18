package org.xiaobuding.hotsearchaiplatform.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * HTML调试工具
 * 用于诊断选择器问题
 */
public class HtmlDebugger {
    
    public static void main(String[] args) {
        try {
            System.out.println("========== 测试B站爬取 ==========");
            
            // 第1步：访问首页
            System.out.println("步骤1: 访问首页");
            org.jsoup.Connection.Response homepageResponse = Jsoup.connect("https://mini.itunes123.com/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .execute();
            
            System.out.println("首页Cookie: " + homepageResponse.cookies());
            
            // 第2步：访问B站页面
            System.out.println("\n步骤2: 访问B站页面");
            Document doc = Jsoup.connect("https://mini.itunes123.com/node/7FjvUyuEl3/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .referrer("https://mini.itunes123.com/")
                    .cookies(homepageResponse.cookies())
                    .timeout(15000)
                    .ignoreHttpErrors(true)
                    .get();
            
            System.out.println("页面标题: " + doc.title());
            System.out.println("页面Body长度: " + doc.body().text().length());
            
            // 测试各种选择器
            System.out.println("\n========== 测试选择器 ==========");
            
            // 选择器1: li:has(a[href*='/n/'])
            Elements selector1 = doc.select("li:has(a[href*='/n/'])");
            System.out.println("选择器1 'li:has(a[href*='/n/'])': " + selector1.size() + " 个元素");
            
            // 选择器2: li
            Elements selector2 = doc.select("li");
            System.out.println("选择器2 'li': " + selector2.size() + " 个元素");
            
            // 选择器3: a[href*='/n/']
            Elements selector3 = doc.select("a[href*='/n/']");
            System.out.println("选择器3 'a[href*='/n/']': " + selector3.size() + " 个元素");
            
            // 选择器4: .hot-item (可能的class名)
            Elements selector4 = doc.select(".hot-item");
            System.out.println("选择器4 '.hot-item': " + selector4.size() + " 个元素");
            
            // 选择器5: tr (可能是表格)
            Elements selector5 = doc.select("tr");
            System.out.println("选择器5 'tr': " + selector5.size() + " 个元素");
            
            // 选择器6: div.item
            Elements selector6 = doc.select("div.item");
            System.out.println("选择器6 'div.item': " + selector6.size() + " 个元素");
            
            // 搜索关键字
            System.out.println("\n========== 搜索关键字 ==========");
            String bodyText = doc.body().text();
            if (bodyText.contains("遇到险境")) {
                System.out.println("✅ 找到'遇到险境' - 页面是静态HTML");
            } else {
                System.out.println("❌ 未找到'遇到险境' - 页面可能是JS动态加载");
            }
            
            if (bodyText.contains("252.3万")) {
                System.out.println("✅ 找到'252.3万' - 热度值在HTML中");
            } else {
                System.out.println("❌ 未找到'252.3万' - 热度值可能是JS加载");
            }
            
            // 打印前500个字符
            System.out.println("\n========== HTML前500字符 ==========");
            String html = doc.html();
            System.out.println(html.substring(0, Math.min(500, html.length())));
            
            // 如果找到a标签，打印第一个
            if (selector3.size() > 0) {
                System.out.println("\n========== 第一个a标签 ==========");
                Element firstA = selector3.first();
                System.out.println("href: " + firstA.attr("href"));
                System.out.println("text: " + firstA.text());
                System.out.println("parent: " + firstA.parent().tagName());
                System.out.println("HTML: " + firstA.outerHtml());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
