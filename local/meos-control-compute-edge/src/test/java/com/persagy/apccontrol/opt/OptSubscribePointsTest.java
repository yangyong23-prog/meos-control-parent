package com.persagy.apccontrol.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class OptSubscribePointsTest {

    private static Set<String> set(String... s) {
        return new TreeSet<>(Arrays.asList(s));
    }

    @Test
    void classWithoutConfigStaysAllAndIsNotNarrowedByRefs() {
        Map<String, Set<String>> own = new HashMap<>();
        own.put("A", set("x"));
        Map<String, Set<String>> refs = new HashMap<>();
        refs.put("A", set("y"));
        refs.put("B", set("z"));
        refs.put("C", set("w"));
        Map<String, Set<String>> r = OptSubscribePoints.merge(set("A", "B"), own, refs);
        Assertions.assertEquals(set("x", "y"), r.get("A"));
        Assertions.assertTrue(r.get("B").isEmpty(), "B 没有配置：保持全部点");
        Assertions.assertEquals(set("w"), r.get("C"), "引用到类集合外的类：按点订阅");
    }

    @Test
    void collectsNestedRefsWithClassOverride() {
        Map<String, Object> variable = new LinkedHashMap<>();
        variable.put("classCode", "OTHER&1");
        variable.put("infoCode", "v1");
        Map<String, Object> formula = new LinkedHashMap<>();
        formula.put("triggerInfoCode", "t1");
        formula.put("variables", Collections.singletonList(variable));
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("triggerFormulaInfo", formula);
        ext.put("mergeInfoCodes", Collections.singletonMap("infoCodes", Arrays.asList("m1", "m2")));
        Map<String, Set<String>> out = new HashMap<>();
        OptSubscribePoints.collectRefs(ext, "SELF", out);
        Assertions.assertEquals(set("t1", "m1", "m2"), out.get("SELF"));
        Assertions.assertEquals(set("v1"), out.get("OTHER"));
    }

    @Test
    void collectsFromListSource() {
        Map<String, Object> force = new HashMap<>();
        force.put("classCode", "B");
        force.put("infoCode", "f1");
        List<Object> forceInfo = Collections.singletonList(force);
        Map<String, Set<String>> out = new HashMap<>();
        OptSubscribePoints.collectRefs(forceInfo, "A", out);
        OptSubscribePoints.collectRefs(null, "A", out);
        Assertions.assertEquals(Collections.singletonMap("B", set("f1")), out);
    }

    @Test
    void parsesExtraPoints() {
        Map<String, Set<String>> r = OptSubscribePoints.parsePoints(" A:x, A:y ,B&2:z,bad,:q,C: ");
        Assertions.assertEquals(set("x", "y"), r.get("A"));
        Assertions.assertEquals(set("z"), r.get("B"));
        Assertions.assertEquals(new HashSet<>(Arrays.asList("A", "B")), r.keySet());
        Assertions.assertTrue(OptSubscribePoints.parsePoints("").isEmpty());
    }
}
