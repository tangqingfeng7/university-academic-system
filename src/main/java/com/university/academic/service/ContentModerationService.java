package com.university.academic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 内容审核服务类
 * 提供敏感词检测、内容审核等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
public class ContentModerationService {

    /**
     * 敏感词列表
     * 实际应用中应从数据库或配置文件加载
     */
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            // 政治敏感词
            "反动", "造反", "颠覆",
            // 暴力相关
            "暴力", "杀人", "血腥",
            // 色情相关
            "色情", "淫秽", "裸体",
            // 辱骂词汇
            "傻逼", "操你", "滚蛋", "垃圾", "废物", "白痴",
            "智障", "弱智", "脑残", "SB", "傻X",
            // 歧视词汇
            "歧视", "种族主义",
            // 违法相关
            "贩毒", "吸毒", "走私",
            // 其他不当内容
            "抄袭", "作弊", "舞弊"
    ));

    /**
     * 敏感词变体映射（处理谐音、变形等）
     */
    private static final Map<String, String> WORD_VARIANTS = new HashMap<>();
    
    static {
        // 数字谐音
        WORD_VARIANTS.put("5B", "傻逼");
        WORD_VARIANTS.put("sb", "傻逼");
        WORD_VARIANTS.put("cao", "操");
        WORD_VARIANTS.put("nmsl", "你妈死了");
        // 可以添加更多变体
    }

    /**
     * 审核结果类
     */
    public static class ModerationResult {
        private final boolean flagged;
        private final String note;
        private final List<String> detectedWords;

        public ModerationResult(boolean flagged, String note, List<String> detectedWords) {
            this.flagged = flagged;
            this.note = note;
            this.detectedWords = detectedWords;
        }

        public boolean isFlagged() {
            return flagged;
        }

        public String getNote() {
            return note;
        }

        public List<String> getDetectedWords() {
            return detectedWords;
        }
    }

    /**
     * 检测内容是否包含敏感词
     *
     * @param content 待检测内容
     * @return 审核结果
     */
    public ModerationResult moderateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ModerationResult(false, null, Collections.emptyList());
        }

        List<String> detectedWords = new ArrayList<>();
        String normalizedContent = normalizeContent(content);

        // 1. 检测直接匹配的敏感词
        for (String word : SENSITIVE_WORDS) {
            if (normalizedContent.contains(word.toLowerCase())) {
                detectedWords.add(word);
            }
        }

        // 2. 检测变体词汇
        for (Map.Entry<String, String> entry : WORD_VARIANTS.entrySet()) {
            if (normalizedContent.contains(entry.getKey().toLowerCase())) {
                detectedWords.add(entry.getValue() + "(变体: " + entry.getKey() + ")");
            }
        }

        // 3. 检测重复字符（如：垃垃垃圾圾圾）
        if (hasRepetitivePattern(content)) {
            detectedWords.add("重复字符模式");
        }

        // 4. 生成审核结果
        if (!detectedWords.isEmpty()) {
            String note = "检测到敏感词: " + String.join(", ", detectedWords);
            log.warn("内容审核标记: {}", note);
            return new ModerationResult(true, note, detectedWords);
        }

        return new ModerationResult(false, null, Collections.emptyList());
    }

    /**
     * 内容标准化处理
     * 转小写、去除空格、去除特殊字符等
     *
     * @param content 原始内容
     * @return 标准化后的内容
     */
    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }

        // 转小写
        String normalized = content.toLowerCase();
        
        // 去除空格、下划线、连字符等
        normalized = normalized.replaceAll("[\\s_\\-]", "");
        
        // 去除常见的混淆字符
        normalized = normalized.replace("0", "o")
                              .replace("1", "i")
                              .replace("3", "e")
                              .replace("4", "a")
                              .replace("5", "s")
                              .replace("7", "t")
                              .replace("8", "b");

        return normalized;
    }

    /**
     * 检测是否有重复字符模式
     * 例如：垃垃垃圾圾圾、哈哈哈哈哈（超过5个）
     *
     * @param content 内容
     * @return true-有重复模式，false-无
     */
    private boolean hasRepetitivePattern(String content) {
        if (content == null || content.length() < 6) {
            return false;
        }

        // 检测连续重复字符（超过5个相同字符）
        Pattern pattern = Pattern.compile("(.)\\1{5,}");
        return pattern.matcher(content).find();
    }

    /**
     * 过滤敏感词（替换为*）
     *
     * @param content 原始内容
     * @return 过滤后的内容
     */
    public String filterSensitiveWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        String filtered = content;
        
        // 替换敏感词为星号
        for (String word : SENSITIVE_WORDS) {
            if (filtered.contains(word)) {
                String replacement = "*".repeat(word.length());
                filtered = filtered.replace(word, replacement);
            }
        }

        // 替换变体词汇
        for (String variant : WORD_VARIANTS.keySet()) {
            if (filtered.toLowerCase().contains(variant.toLowerCase())) {
                String replacement = "*".repeat(variant.length());
                filtered = filtered.replaceAll("(?i)" + Pattern.quote(variant), replacement);
            }
        }

        return filtered;
    }

    /**
     * 检查内容长度是否合法
     *
     * @param content   内容
     * @param maxLength 最大长度
     * @return true-合法，false-超长
     */
    public boolean validateLength(String content, int maxLength) {
        if (content == null) {
            return true;
        }
        return content.length() <= maxLength;
    }

    /**
     * 检测是否为垃圾评论（全是无意义字符）
     *
     * @param content 内容
     * @return true-是垃圾评论，false-正常
     */
    public boolean isSpam(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        String trimmed = content.trim();
        
        // 检测是否全是标点符号
        if (trimmed.matches("[\\p{Punct}\\s]+")) {
            return true;
        }

        // 检测是否全是数字
        if (trimmed.matches("\\d+")) {
            return true;
        }

        // 检测是否只有单个字符重复
        if (trimmed.matches("(.)\\1+") && trimmed.length() > 10) {
            return true;
        }

        return false;
    }

    /**
     * 获取敏感词列表（用于管理）
     *
     * @return 敏感词集合
     */
    public Set<String> getSensitiveWords() {
        return new HashSet<>(SENSITIVE_WORDS);
    }

    /**
     * 添加敏感词（动态添加，实际应存储到数据库）
     *
     * @param word 敏感词
     */
    public void addSensitiveWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            SENSITIVE_WORDS.add(word.trim());
            log.info("添加敏感词: {}", word);
        }
    }

    /**
     * 移除敏感词
     *
     * @param word 敏感词
     */
    public void removeSensitiveWord(String word) {
        if (SENSITIVE_WORDS.remove(word)) {
            log.info("移除敏感词: {}", word);
        }
    }
}

