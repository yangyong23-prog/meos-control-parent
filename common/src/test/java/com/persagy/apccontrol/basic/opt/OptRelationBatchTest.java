package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OptRelationBatchTest {

    /** 简化的 RWD 行：id + 关系对端 */
    static final class Row {
        final String id;
        final Set<String> rel;

        Row(String id, String... rel) {
            this.id = id;
            this.rel = new LinkedHashSet<>(Arrays.asList(rel));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Row && ((Row) o).id.equals(id) && ((Row) o).rel.equals(rel);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id + rel;
        }
    }

    /** 模拟 RWD 关系表：源 → 有序的对端列表 */
    private static final Map<String, List<String>> EDGES = new HashMap<>();

    static {
        EDGES.put("S1", Arrays.asList("A", "B", "C"));
        EDGES.put("S2", Arrays.asList("B", "D"));
        EDGES.put("S3", Collections.emptyList());
    }

    /** 单个源查询：返回的行只带该源 */
    private static List<Row> single(String source) {
        List<Row> r = new ArrayList<>();
        for (String id : EDGES.get(source)) {
            r.add(new Row(id, source));
        }
        return r;
    }

    /** 批量查询：同一对象只返回一行，对端为所有相关源 */
    private static List<Row> batch(List<String> sources) {
        Map<String, Row> byId = new java.util.LinkedHashMap<>();
        for (String s : sources) {
            for (String id : EDGES.get(s)) {
                byId.computeIfAbsent(id, Row::new).rel.add(s);
            }
        }
        return new ArrayList<>(byId.values());
    }

    @Test
    void splitEqualsPerSourceQueries() {
        List<String> sources = Arrays.asList("S1", "S2", "S3");
        Map<String, List<Row>> split = OptRelationBatch.splitBySource(sources, batch(sources),
                r -> r.rel, (r, s) -> new Row(r.id, s));
        Assertions.assertNotNull(split);
        for (String s : sources) {
            Assertions.assertEquals(single(s), split.get(s), "源 " + s + " 改造前后一致");
        }
    }

    @Test
    void unattributableRowMeansFallback() {
        List<Row> rows = Arrays.asList(new Row("A", "S1"), new Row("X"));
        Assertions.assertNull(OptRelationBatch.splitBySource(Collections.singletonList("S1"), rows, r -> r.rel, (r, s) -> r));
        List<Row> foreign = Collections.singletonList(new Row("A", "S9"));
        Assertions.assertNull(OptRelationBatch.splitBySource(Collections.singletonList("S1"), foreign, r -> r.rel, (r, s) -> r));
    }
}
