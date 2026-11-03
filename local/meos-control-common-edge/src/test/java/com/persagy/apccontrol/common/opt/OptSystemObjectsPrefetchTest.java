package com.persagy.apccontrol.common.opt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class OptSystemObjectsPrefetchTest {

    /** 模拟 RWD：对象 id → 类编码；关系 (rel, from, to) */
    private final Map<String, String> classOf = new LinkedHashMap<>();
    private final List<String[]> edges = new ArrayList<>();
    private final AtomicInteger calls = new AtomicInteger();

    private void obj(String id, String cls) {
        classOf.put(id, cls);
    }

    private void edge(String rel, String from, String to) {
        edges.add(new String[]{rel, from, to});
    }

    /** 与 queryObjectList 相同的语义：按 objFrom 查 to 端对象或按 objTo 查 from 端对象，同一对象一行，relObjIds 为命中的对端 */
    private List<JSONObject> query(List<String> classCodes, String graph, String rel, List<String> objFrom, List<String> objTo) {
        calls.incrementAndGet();
        Map<String, Set<String>> hits = new LinkedHashMap<>();
        for (String id : classOf.keySet()) {
            if (!classCodes.contains(classOf.get(id))) {
                continue;
            }
            for (String[] e : edges) {
                if (!e[0].equals(rel)) {
                    continue;
                }
                if (objFrom != null && objFrom.contains(e[1]) && e[2].equals(id)) {
                    hits.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(e[1]);
                }
                if (objTo != null && objTo.contains(e[2]) && e[1].equals(id)) {
                    hits.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(e[2]);
                }
            }
        }
        List<JSONObject> rows = new ArrayList<>();
        hits.forEach((id, rel2) -> {
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("classCode", classOf.get(id));
            o.put("relObjIds", new JSONArray(new ArrayList<>(rel2)));
            rows.add(o);
        });
        return rows;
    }

    private static final Set<String> MESH = new LinkedHashSet<>(Arrays.asList("ACPPCS", "ACPPCR"));

    private void sample() {
        obj("Sp1", "space");
        obj("Sp2", "space");
        obj("M1", "ACPPCS");
        obj("M2", "ACPPCS");
        obj("M3", "ACPPCR");
        obj("Sh1", "shaft");
        obj("Sh2", "shaft");
        edge("Sp2Sy", "Sp1", "S1");
        edge("Sp2Sy", "Sp2", "S1");
        edge("Sp2Sy", "Sp2", "S2");   // 空间同时服务两个系统
        edge("Sy2Eq", "S1", "M1");
        edge("Sy2Eq", "S2", "M1");    // 管网同时属于两个系统
        edge("Sy2Eq", "S2", "M2");
        edge("Sy2Eq", "S2", "M3");
        edge("Eq2Sh", "M1", "Sh1");
        edge("Eq2Sh", "M2", "Sh1");   // 同一竖井被同系统两根管连接，只出现一次
        edge("Eq2Sh", "M2", "Sh2");
    }

    @Test
    void prefetchEqualsPerSystemQueries() {
        sample();
        List<String> systems = Arrays.asList("S1", "S2", "S3");
        OptSystemObjectsPrefetch p = OptSystemObjectsPrefetch.load(this::query, systems, "space", MESH, "ACPPCS");
        Assertions.assertNull(p.failure());
        int batchCalls = calls.get();
        calls.set(0);
        for (String s : systems) {
            List<JSONObject> spaces = query(Collections.singletonList("space"), "ArchforMech", "Sp2Sy", null, Collections.singletonList(s));
            List<JSONObject> meshes = query(new ArrayList<>(MESH), "MechSubset", "Sy2Eq", Collections.singletonList(s), null);
            List<String> ccs = new ArrayList<>();
            for (JSONObject m : meshes) {
                if ("ACPPCS".equals(m.getString("classCode"))) {
                    ccs.add(m.getString("id"));
                }
            }
            Assertions.assertEquals(spaces, p.spaces(s), "空间 " + s);
            Assertions.assertEquals(meshes, p.meshes(s), "管网 " + s);
            if (!ccs.isEmpty()) {
                List<JSONObject> shafts = query(Collections.singletonList("shaft"), "MechInArch", "Eq2Sh", ccs, null);
                Assertions.assertEquals(ids(shafts), ids(p.shafts(s)), "竖井 " + s);
            }
        }
        Assertions.assertEquals(3, batchCalls, "空间、管网、竖井各一次");
        Assertions.assertTrue(calls.get() > batchCalls);
    }

    @Test
    void unattributableRowsFallBack() {
        OptSystemObjectsPrefetch p = OptSystemObjectsPrefetch.load((c, g, r, from, to) -> {
            JSONObject o = new JSONObject();
            o.put("id", "X");
            return Collections.singletonList(o);
        }, Arrays.asList("S1", "S2"), "space", MESH, "ACPPCS");
        Assertions.assertNull(p.spaces("S1"));
        Assertions.assertNull(p.meshes("S1"));
        Assertions.assertNull(p.shafts("S1"));
    }

    @Test
    void failureIsCapturedAndFallsBack() {
        IllegalStateException boom = new IllegalStateException("rwd down");
        OptSystemObjectsPrefetch p = OptSystemObjectsPrefetch.load((c, g, r, from, to) -> {
            throw boom;
        }, Arrays.asList("S1", "S2"), "space", null, null);
        Assertions.assertSame(boom, p.failure());
        Assertions.assertNull(p.spaces("S1"));
    }

    private static List<String> ids(List<JSONObject> rows) {
        List<String> r = new ArrayList<>();
        for (JSONObject o : rows) {
            r.add(o.getString("id"));
        }
        return r;
    }
}
