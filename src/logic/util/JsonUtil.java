package logic.util;

import java.util.*;

/**
 * 轻量级JSON工具类，支持将Map序列化为JSON字符串，以及反向解析。
 * 不依赖第三方库，专门为本项目的数据模型设计。
 *
 * @author Yuan
 * @version 1.0
 */
public class JsonUtil {

    /**
     * 将Map序列化为JSON字符串
     * @param map 键值对
     * @return JSON字符串
     */
    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            appendValue(sb, entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    /** 追加JSON值 */
    private static void appendValue(StringBuilder sb, Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            sb.append("\"").append(escape((String) value)).append("\"");
        } else if (value instanceof Integer || value instanceof Long) {
            sb.append(value);
        } else if (value instanceof Double || value instanceof Float) {
            sb.append(String.format("%.2f", (Double) value));
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof int[]) {
            int[] arr = (int[]) value;
            sb.append("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(arr[i]);
            }
            sb.append("]");
        } else if (value instanceof long[]) {
            long[] arr = (long[]) value;
            sb.append("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(arr[i]);
            }
            sb.append("]");
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                appendValue(sb, list.get(i));
            }
            sb.append("]");
        } else {
            sb.append("\"").append(escape(value.toString())).append("\"");
        }
    }

    /**
     * 解析JSON字符串为Map
     * @param json JSON字符串
     * @return 键值对Map
     */
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return map;
        // 去掉外层大括号
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        List<String> pairs = splitJson(json);
        for (String pair : pairs) {
            int colonIdx = findColon(pair);
            if (colonIdx < 0) continue;
            String key = parseString(pair.substring(0, colonIdx).trim());
            String valueStr = pair.substring(colonIdx + 1).trim();
            Object value = parseValue(valueStr);
            map.put(key, value);
        }
        return map;
    }

    /**
     * 解析JSON数组字符串为int[]
     * @param jsonArray JSON数组，如 "[1,2,3]"
     * @return int数组
     */
    public static int[] parseIntArray(String jsonArray) {
        if (jsonArray == null || jsonArray.trim().isEmpty()) return new int[0];
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[")) jsonArray = jsonArray.substring(1);
        if (jsonArray.endsWith("]")) jsonArray = jsonArray.substring(0, jsonArray.length() - 1);
        if (jsonArray.trim().isEmpty()) return new int[0];
        String[] parts = jsonArray.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    /** 按逗号分割JSON键值对（不分割字符串内的逗号） */
    private static List<String> splitJson(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        boolean inString = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                current.append(c);
                if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                    current.append(c);
                } else if (c == '{' || c == '[') {
                    depth++;
                    current.append(c);
                } else if (c == '}' || c == ']') {
                    depth--;
                    current.append(c);
                } else if (c == ',' && depth == 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }

    /** 查找冒号位置（不在字符串内） */
    private static int findColon(String s) {
        boolean inString = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (c == ':' && !inString) {
                return i;
            }
        }
        return -1;
    }

    /** 解析JSON字符串值（去掉引号，处理转义） */
    private static String parseString(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return unescape(s.substring(1, s.length() - 1));
        }
        return unescape(s);
    }

    /** 解析JSON值 */
    private static Object parseValue(String valueStr) {
        if (valueStr == null || valueStr.isEmpty()) return "";
        if (valueStr.equals("null")) return null;
        if (valueStr.equals("true")) return true;
        if (valueStr.equals("false")) return false;
        if (valueStr.startsWith("\"")) return parseString(valueStr);
        if (valueStr.startsWith("[")) {
            // 返回int数组
            return parseIntArray(valueStr);
        }
        if (valueStr.startsWith("{")) {
            return parseObject(valueStr);
        }
        // 数字
        try {
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Long.parseLong(valueStr);
            }
        } catch (NumberFormatException e) {
            return valueStr;
        }
    }

    /** 字符串转义 */
    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 字符串反转义 */
    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"':  sb.append('"');  i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case 'n':  sb.append('\n'); i++; break;
                    case 'r':  sb.append('\r'); i++; break;
                    case 't':  sb.append('\t'); i++; break;
                    default:   sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
