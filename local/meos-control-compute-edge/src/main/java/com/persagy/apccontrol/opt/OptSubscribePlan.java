package com.persagy.apccontrol.opt;

import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * B3：按点订阅规则的差量计算（纯逻辑）。
 *
 * <p>Twin 规则语义（dtp-twin-server-edge TwinSubscRuleServiceImpl.saveBatch:178-215）：同一 (topic, 类) 已有“全部点”规则时，
 * 新加的具体点会被忽略；新加“全部点”会删除该类的具体点规则；重复添加同一点幂等。
 * 因此从“全部点”改为具体点时必须先删“全部点”再加具体点。
 *
 * <p>期望集合中某类的信息点集合为空，表示该类订阅全部点。
 */
@Getter
public class OptSubscribePlan {

    /** 删除：类 → 信息点；信息点集合为空表示删除该类的“全部点”规则 */
    private final Map<String, Set<String>> toDelete = new TreeMap<>();
    /** 添加：类 → 信息点；信息点集合为空表示添加“全部点”规则 */
    private final Map<String, Set<String>> toAdd = new TreeMap<>();

    /**
     * @param expected 期望：类 → 信息点（空集合 = 全部点）
     * @param existing 现有规则：每条为 (类, 信息点)，信息点为 null 表示“全部点”
     */
    public static OptSubscribePlan diff(Map<String, Set<String>> expected, Collection<Rule> existing) {
        Map<String, ExistingClass> current = new LinkedHashMap<>();
        for (Rule r : existing) {
            if (r == null || r.getClassCode() == null) {
                continue;
            }
            ExistingClass c = current.computeIfAbsent(r.getClassCode(), k -> new ExistingClass());
            if (r.getInfoCode() == null || r.getInfoCode().isEmpty()) {
                c.all = true;
            } else {
                c.points.add(r.getInfoCode());
            }
        }

        OptSubscribePlan plan = new OptSubscribePlan();
        for (Map.Entry<String, Set<String>> e : expected.entrySet()) {
            String classCode = e.getKey();
            Set<String> want = e.getValue() == null ? Collections.emptySet() : e.getValue();
            ExistingClass have = current.getOrDefault(classCode, new ExistingClass());
            if (want.isEmpty()) {
                if (!have.all) {
                    // 加“全部点”时 Twin 会删除该类具体点规则
                    plan.toAdd.put(classCode, new TreeSet<>());
                }
                continue;
            }
            if (have.all) {
                plan.toDelete.put(classCode, new TreeSet<>());
                plan.toAdd.put(classCode, new TreeSet<>(want));
                continue;
            }
            Set<String> extra = new TreeSet<>(have.points);
            extra.removeAll(want);
            if (!extra.isEmpty()) {
                plan.toDelete.put(classCode, extra);
            }
            Set<String> missing = new TreeSet<>(want);
            missing.removeAll(have.points);
            if (!missing.isEmpty()) {
                plan.toAdd.put(classCode, missing);
            }
        }
        for (Map.Entry<String, ExistingClass> e : current.entrySet()) {
            if (expected.containsKey(e.getKey())) {
                continue;
            }
            ExistingClass have = e.getValue();
            // Twin 加“全部点”时会删掉该类具体点，所以两者不会同时存在
            plan.toDelete.put(e.getKey(), have.all ? new TreeSet<>() : new TreeSet<>(have.points));
        }
        return plan;
    }

    public boolean isEmpty() {
        return toDelete.isEmpty() && toAdd.isEmpty();
    }

    public int addedPointCount() {
        int n = 0;
        for (Set<String> s : toAdd.values()) {
            n += s.isEmpty() ? 1 : s.size();
        }
        return n;
    }

    private static final class ExistingClass {
        boolean all;
        final Set<String> points = new LinkedHashSet<>();
    }

    @Getter
    public static final class Rule {
        private final String classCode;
        private final String infoCode;

        public Rule(String classCode, String infoCode) {
            this.classCode = classCode;
            this.infoCode = infoCode;
        }
    }
}
