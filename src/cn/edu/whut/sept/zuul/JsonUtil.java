/**
 * 简单的JSON处理工具类
 * 用于序列化和反序列化JSON，避免外部依赖
 * 
 * @author 扩展功能实现
 * @version 1.0
 */
package cn.edu.whut.sept.zuul;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class JsonUtil {
    /**
     * 将Map转换为JSON字符串
     */
    public static String toJson(Map<String, Object> map) {
        if (map == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 将值转换为JSON字符串
     */
    private static String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return toJson(map);
        } else if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return listToJson(list);
        } else {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
    }
    
    /**
     * 将List转换为JSON字符串
     */
    private static String listToJson(List<Object> list) {
        if (list == null) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append(valueToJson(item));
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 转义JSON字符串中的特殊字符
     */
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
    
    /**
     * 解析简单的JSON对象（仅支持字符串值）
     * 支持 {"key": "value"} 格式
     */
    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> result = new HashMap<>();
        
        if (json == null || json.trim().isEmpty()) {
            return result;
        }
        
        // 移除空白字符
        json = json.trim();
        
        // 移除花括号
        if (json.startsWith("{")) {
            json = json.substring(1).trim();
        }
        if (json.endsWith("}")) {
            json = json.substring(0, json.length() - 1).trim();
        }
        
        // 简单的键值对解析（处理转义字符）
        int start = 0;
        while (start < json.length()) {
            // 查找键的开始（引号）
            int keyStart = json.indexOf('"', start);
            if (keyStart == -1) break;
            
            // 查找键的结束
            int keyEnd = json.indexOf('"', keyStart + 1);
            if (keyEnd == -1) break;
            
            String key = json.substring(keyStart + 1, keyEnd);
            
            // 查找冒号
            int colonPos = json.indexOf(':', keyEnd + 1);
            if (colonPos == -1) break;
            
            // 查找值的开始（引号）
            int valueStart = json.indexOf('"', colonPos + 1);
            if (valueStart == -1) break;
            
            // 查找值的结束
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1) break;
            
            String value = json.substring(valueStart + 1, valueEnd);
            result.put(key, value);
            
            // 查找下一个逗号或结束
            start = valueEnd + 1;
            int commaPos = json.indexOf(',', start);
            if (commaPos == -1) break;
            start = commaPos + 1;
        }
        
        return result;
    }
}

