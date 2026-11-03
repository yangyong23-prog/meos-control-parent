package com.persagy.apccontrol.opt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * opt 分支 B3：“应订阅点”的纯计算逻辑（无 Spring 依赖，单测覆盖），由 {@link OptSubscribePointRegistry} 调用。
 */
final class OptSubscribePoints {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<String> POINT_FIELDS = new HashSet<>(Arrays.asList("infoCode", "triggerInfoCode"));
    private static final String POINT_LIST_FIELD = "infoCodes";

    private OptSubscribePoints() {
    }

    /**
     * @param classes 原按类订阅的类集合
     * @param own     有信息点配置的类及其配置点：这些类改为按点订阅
     * @param refs    引用 / 补充点：只并入按点订阅的类或不在类集合里的类，不会把“全部点”的类收窄
     */
    static Map<String, Set<String>> merge(Set<String> classes, Map<String, Set<String>> own, Map<String, Set<String>> refs) {
        Map<String, Set<String>> result = new TreeMap<>();
        for (String c : classes) {
            result.put(c, new TreeSet<>());
        }
        for (Map.Entry<String, Set<String>> e : own.entrySet()) {
            if (!e.getValue().isEmpty()) {
                result.put(e.getKey(), new TreeSet<>(e.getValue()));
            }
        }
        for (Map.Entry<String, Set<String>> e : refs.entrySet()) {
            Set<String> current = result.get(e.getKey());
            if (current == null) {
                result.put(e.getKey(), new TreeSet<>(e.getValue()));
            } else if (!current.isEmpty()) {
                current.addAll(e.getValue());
            }
        }
        return result;
    }

    /** 遍历任意 JSON（extendInfo / forceInfo），收集引用的信息点；对象自身带 classCode 时归到该类 */
    static void collectRefs(Object source, String defaultClass, Map<String, Set<String>> out) {
        if (source == null) {
            return;
        }
        try {
            walk(MAPPER.valueToTree(source), defaultClass, out);
        } catch (IllegalArgumentException e) {
            // 无法序列化的结构忽略：漏订的点由自动补订阅兜底
        }
    }

    private static void walk(JsonNode node, String classCode, Map<String, Set<String>> out) {
        if (node == null) {
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                walk(child, classCode, out);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        JsonNode cls = node.get("classCode");
        String current = cls != null && cls.isTextual() && !isBlank(cls.asText())
                ? normalizeClass(cls.asText()) : classCode;
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> f = fields.next();
            JsonNode v = f.getValue();
            if (POINT_FIELDS.contains(f.getKey()) && v.isTextual()) {
                add(out, current, v.asText());
            } else if (POINT_LIST_FIELD.equals(f.getKey()) && v.isArray()) {
                for (JsonNode item : v) {
                    if (item.isTextual()) {
                        add(out, current, item.asText());
                    }
                }
            } else if (v.isContainerNode()) {
                walk(v, current, out);
            }
        }
    }

    /** "classCode:infoCode,classCode:infoCode" */
    static Map<String, Set<String>> parsePoints(String text) {
        Map<String, Set<String>> out = new HashMap<>();
        if (isBlank(text)) {
            return out;
        }
        for (String item : text.split(",")) {
            String[] parts = item.trim().split(":", 2);
            if (parts.length == 2) {
                add(out, normalizeClass(parts[0].trim()), parts[1].trim());
            }
        }
        return out;
    }

    static String normalizeClass(String classCode) {
        if (classCode == null) {
            return "";
        }
        // 与 MqttSubscribeInitServiceImpl 一致：“类&xxx”只取类编码
        int amp = classCode.indexOf('&');
        return (amp >= 0 ? classCode.substring(0, amp) : classCode).trim();
    }

    private static void add(Map<String, Set<String>> out, String classCode, String infoCode) {
        if (isBlank(classCode) || isBlank(infoCode)) {
            return;
        }
        out.computeIfAbsent(classCode, k -> new TreeSet<>()).add(infoCode.trim());
    }

    static void mergeInto(Map<String, Set<String>> target, Map<String, Set<String>> source) {
        source.forEach((k, v) -> target.computeIfAbsent(k, x -> new TreeSet<>()).addAll(v));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
