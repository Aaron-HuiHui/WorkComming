package com.iwantjob.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 千问响应 JSON 提取工具。
 * <p>
 * 大模型输出常被 markdown 代码块包裹或附带说明文字，
 * 这里负责从原始输出中定位并解析第一个 JSON 对象。
 * 解析失败返回 null，由调用方降级处理。
 */
@Slf4j
final class AiJsonExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AiJsonExtractor() {
    }

    /**
     * 从模型原始输出中提取第一个 JSON 对象；提取或解析失败返回 null
     */
    static JsonNode extractObject(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        String candidate = raw.substring(start, end + 1);
        try {
            JsonNode node = MAPPER.readTree(candidate);
            return node.isObject() ? node : null;
        } catch (Exception e) {
            log.warn("AI 响应 JSON 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 读取字符串字段，缺失或类型不符时返回默认值
     */
    static String text(JsonNode node, String field, String defaultVal) {
        if (node == null || node.get(field) == null) {
            return defaultVal;
        }
        JsonNode v = node.get(field);
        return v.isTextual() ? v.asText() : v.toString();
    }

    /**
     * 读取整数字段，缺失或非法时返回默认值
     */
    static Integer intVal(JsonNode node, String field, Integer defaultVal) {
        if (node == null || node.get(field) == null) {
            return defaultVal;
        }
        try {
            return node.get(field).asInt();
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
