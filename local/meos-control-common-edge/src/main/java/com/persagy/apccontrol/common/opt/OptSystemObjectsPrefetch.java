package com.persagy.apccontrol.common.opt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptRelationBatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * opt 分支 B10（switch.opt.clientSystemRelationBatch）：ClientServerHandler 按系统逐个查“系统的空间 / 管网 / 竖井”，
 * 改为对所有系统各查一次，再按 relObjIds 拆回各系统。
 * <p>
 * 每行拆分时复制一份，relObjIds 只保留对应的系统（或管网），与按单个系统查询时的返回一致。
 * 某一类无法归属或查询失败时，该类返回 null，调用方对每个系统退回原来的单独查询。
 */
public final class OptSystemObjectsPrefetch {

    /** 与 BizDtpRwdService#queryObjectList(groupCode, projectId, classCode, graphCode, relCode, objFrom, objTo) 相同 */
    @FunctionalInterface
    public interface RelationQuery {
        List<JSONObject> query(List<String> classCodes, String graphCode, String relCode, List<String> objFrom, List<String> objTo);
    }

    static final int BATCH = 200;

    private Map<String, List<JSONObject>> spaces;
    private Map<String, List<JSONObject>> meshes;
    private Map<String, List<JSONObject>> shafts;
    private RuntimeException failure;

    private OptSystemObjectsPrefetch() {
    }

    /**
     * @param systemIds     要处理的系统
     * @param spaceClass    空间类编码（Sp2Sy，objTo=系统）
     * @param meshCodes     管网类编码（Sy2Eq，objFrom=系统）；为 null 时不预取管网和竖井
     * @param shaftMeshCode 用于查竖井的管网类（Eq2Sh，objFrom=该类管网）
     */
    public static OptSystemObjectsPrefetch load(RelationQuery query, Collection<String> systemIds, String spaceClass,
                                                Collection<String> meshCodes, String shaftMeshCode) {
        OptSystemObjectsPrefetch p = new OptSystemObjectsPrefetch();
        List<String> systems = new ArrayList<>(new LinkedHashSet<>(systemIds));
        try {
            p.spaces = batch(query, systems, Lists.newArrayList(spaceClass), "ArchforMech", "Sp2Sy", false);
            if (meshCodes != null) {
                p.meshes = batch(query, systems, new ArrayList<>(meshCodes), "MechSubset", "Sy2Eq", true);
                if (p.meshes != null) {
                    p.shafts = shafts(query, p.meshes, shaftMeshCode);
                }
            }
        } catch (RuntimeException e) {
            OptMetrics.incAlways("clientRelation.batch.error");
            p.failure = e;
        }
        if (p.spaces == null || (meshCodes != null && (p.meshes == null || p.shafts == null))) {
            OptMetrics.incAlways("clientRelation.batch.fallback");
        }
        return p;
    }

    /** @return 该系统的空间；null 表示未预取，按原逻辑查询 */
    public List<JSONObject> spaces(String systemId) {
        return spaces == null ? null : spaces.get(systemId);
    }

    public List<JSONObject> meshes(String systemId) {
        return meshes == null ? null : meshes.get(systemId);
    }

    /** 竖井依赖管网：只有管网和竖井都预取成功才返回 */
    public List<JSONObject> shafts(String systemId) {
        return meshes == null || shafts == null ? null : shafts.get(systemId);
    }

    public RuntimeException failure() {
        return failure;
    }

    private static Map<String, List<JSONObject>> batch(RelationQuery query, List<String> sources, List<String> classCodes,
                                                       String graph, String rel, boolean sourceIsFrom) {
        Map<String, List<JSONObject>> out = new HashMap<>();
        for (List<String> part : Lists.partition(sources, BATCH)) {
            List<String> ids = new ArrayList<>(part);
            List<JSONObject> rows = sourceIsFrom ? query.query(classCodes, graph, rel, ids, null)
                    : query.query(classCodes, graph, rel, null, ids);
            Map<String, List<JSONObject>> split = OptRelationBatch.splitBySource(ids, rows,
                    OptSystemObjectsPrefetch::relIds, OptSystemObjectsPrefetch::copyFor);
            if (split == null) {
                return null;
            }
            out.putAll(split);
        }
        OptMetrics.add("clientRelation.batch.saved", sources.size() - (long) ((sources.size() + BATCH - 1) / BATCH));
        return out;
    }

    /**
     * 原逻辑：每个系统用自己的该类管网 id 查一次竖井（同一竖井只返回一次）。
     * 这里用全部管网 id 查一次，再按竖井的 relObjIds 归到各系统，每个系统内按行顺序去重。
     */
    private static Map<String, List<JSONObject>> shafts(RelationQuery query, Map<String, List<JSONObject>> meshesBySystem,
                                                        String shaftMeshCode) {
        Map<String, Set<String>> meshToSystems = new LinkedHashMap<>();
        Map<String, List<JSONObject>> out = new HashMap<>();
        meshesBySystem.forEach((systemId, list) -> {
            out.put(systemId, new ArrayList<>());
            for (JSONObject m : list) {
                if (shaftMeshCode.equals(m.getString("classCode")) && m.getString("id") != null) {
                    meshToSystems.computeIfAbsent(m.getString("id"), k -> new LinkedHashSet<>()).add(systemId);
                }
            }
        });
        if (meshToSystems.isEmpty()) {
            return out;
        }
        Map<String, Set<String>> seen = new HashMap<>();
        for (List<String> part : Lists.partition(new ArrayList<>(meshToSystems.keySet()), BATCH)) {
            List<JSONObject> rows = query.query(Lists.newArrayList("shaft"), "MechInArch", "Eq2Sh", new ArrayList<>(part), null);
            if (rows == null) {
                continue;
            }
            for (JSONObject row : rows) {
                Set<String> rel = relIds(row);
                if (rel == null || rel.isEmpty()) {
                    return null;
                }
                for (String meshId : rel) {
                    for (String systemId : meshToSystems.getOrDefault(meshId, Collections.emptySet())) {
                        if (seen.computeIfAbsent(systemId, k -> new java.util.HashSet<>()).add(row.getString("id"))) {
                            out.get(systemId).add(row);
                        }
                    }
                }
            }
        }
        return out;
    }

    static Set<String> relIds(JSONObject row) {
        JSONArray arr = row.getJSONArray("relObjIds");
        if (arr == null) {
            return null;
        }
        Set<String> s = new LinkedHashSet<>();
        for (Object o : arr) {
            if (o != null) {
                s.add(o.toString());
            }
        }
        return s;
    }

    static JSONObject copyFor(JSONObject row, String source) {
        JSONObject copy = (JSONObject) row.clone();
        copy.put("relObjIds", new JSONArray(Lists.newArrayList(source)));
        return copy;
    }
}
