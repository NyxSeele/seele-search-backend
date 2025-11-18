package org.xiaobuding.hotsearchaiplatform.util;

import org.xiaobuding.hotsearchaiplatform.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * 输入验证工具类
 * 提供空值检查、敏感词过滤等功能
 */
public class InputValidator {

    // 敏感词列表（可以扩展或从配置文件读取）
    private static final String[] SENSITIVE_WORDS = {
            "恐怖", "暴力", "色情", "赌博", "毒品", "诈骗",
            "传销", "邪教", "分裂", "颠覆", "反动"
    };

    // SQL注入检测模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i).*('|(\\-\\-)|(;)|(\\|\\|)|(\\*)|(\\bor\\b)|(\\band\\b)|(\\bunion\\b)|(\\bselect\\b)|(\\binsert\\b)|(\\bupdate\\b)|(\\bdelete\\b)|(\\bdrop\\b)).*"
    );

    // XSS检测模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i).*(<|>|script|iframe|onclick|onerror|javascript:|eval).*"
    );

    /**
     * 验证字符串不为空
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + "不能为空", fieldName);
        }
    }

    /**
     * 验证字符串长度
     */
    public static void validateLength(String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + "不能为空", fieldName);
        }
        int length = value.trim().length();
        if (length < minLength || length > maxLength) {
            throw new ValidationException(
                    String.format("%s长度必须在%d-%d之间，当前长度: %d", fieldName, minLength, maxLength, length),
                    fieldName
            );
        }
    }

    /**
     * 检查敏感词
     */
    public static void validateNoSensitiveWords(String value, String fieldName) {
        if (value == null) {
            return;
        }
        for (String word : SENSITIVE_WORDS) {
            if (value.contains(word)) {
                throw new ValidationException(
                        fieldName + "包含不允许的内容",
                        fieldName
                );
            }
        }
    }

    /**
     * 检查SQL 注入
     */
    public static void validateNoSQLInjection(String value, String fieldName) {
        if (value == null) {
            return;
        }
        if (SQL_INJECTION_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                    fieldName + "包含非法字符",
                    fieldName
            );
        }
    }

    /**
     * 检查XSS 攻击
     */
    public static void validateNoXSS(String value, String fieldName) {
        if (value == null) {
            return;
        }
        if (XSS_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                    fieldName + "包含非法字符",
                    fieldName
            );
        }
    }

    /**
     * 综合验证（推荐使用）
     */
    public static void validateQuestion(String question) {
        validateNotEmpty(question, "question");
        validateLength(question, 1, 500, "question");
        validateNoSensitiveWords(question, "question");
        validateNoSQLInjection(question, "question");
        validateNoXSS(question, "question");
    }

    /**
     * 验证 itemId
     */
    public static void validateItemId(String itemId) {
        validateNotEmpty(itemId, "itemId");
        validateLength(itemId, 1, 100, "itemId");
        validateNoSQLInjection(itemId, "itemId");
        validateNoXSS(itemId, "itemId");
    }

    /**
     * 清理输入（移除潜在危险字符）
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim()
                .replaceAll("[<>\"'%;()&+]", "")
                .replaceAll("\\s+", " ");
    }

    /**
     * 验证平台类型
     */
    public static void validatePlatformType(String platform) {
        validateNotEmpty(platform, "platform");
        String upper = platform.toUpperCase();
        if (!upper.equals("WEIBO") && !upper.equals("TOUTIAO") && !upper.equals("BILIBILI")) {
            throw new ValidationException(
                    "无效的平台类型: " + platform + "。有效值为: WEIBO, TOUTIAO, BILIBILI",
                    "platform"
            );
        }
    }
}
