package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataOnMomentDTO;
import com.persagy.apccontrol.basic.opt.OptMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * opt 分支 B10：循环里“每个 (对象, 信息点) 调一次 Twin 当前值”改为先登记、最后一次查完再回填。
 * <p>
 * 每个对象只带它自己登记过的信息点（不做对象 × 信息点的笛卡尔积），≤200 个对象一片。
 * 同一 (对象, 信息点) 在结果中出现多条时取第一条，与单点查询取 get(0) 一致；查不到的回填 null。
 * 查询抛出的异常原样抛出（原逻辑在第一次单点查询失败时同样抛出，方法整体失败）。
 */
public final class OptPointBatch<T> {

    static final int OBJECTS_PER_CALL = 200;

    private final List<String> objectIds = new ArrayList<>();
    private final List<String> infoCodes = new ArrayList<>();
    private final List<T> targets = new ArrayList<>();

    public void add(String objectId, String infoCode, T target) {
        objectIds.add(objectId);
        infoCodes.add(infoCode);
        targets.add(target);
    }

    public int size() {
        return targets.size();
    }

    /**
     * @param fetch      一片查询 → 有效记录
     * @param objectIdOf 记录的对象 id
     * @param infoCodeOf 记录的信息点
     * @param apply      (登记的目标, 对应记录或 null)，按登记顺序回调
     */
    public <V> void flush(String projectId, Function<QueryTwinDataDTO, List<V>> fetch,
                          Function<V, String> objectIdOf, Function<V, String> infoCodeOf, BiConsumer<T, V> apply) {
        if (targets.isEmpty()) {
            return;
        }
        Map<String, Set<String>> byObject = new LinkedHashMap<>();
        for (int i = 0; i < targets.size(); i++) {
            byObject.computeIfAbsent(objectIds.get(i), k -> new LinkedHashSet<>()).add(infoCodes.get(i));
        }
        Map<String, V> found = new HashMap<>();
        List<QueryTwinDataOnMomentDTO> chunk = new ArrayList<>();
        int calls = 0;
        for (Map.Entry<String, Set<String>> e : byObject.entrySet()) {
            chunk.add(new QueryTwinDataOnMomentDTO(projectId, e.getKey(), new LinkedHashSet<>(e.getValue())));
            if (chunk.size() == OBJECTS_PER_CALL) {
                collect(fetch.apply(new QueryTwinDataDTO(chunk)), objectIdOf, infoCodeOf, found);
                chunk = new ArrayList<>();
                calls++;
            }
        }
        if (!chunk.isEmpty()) {
            collect(fetch.apply(new QueryTwinDataDTO(chunk)), objectIdOf, infoCodeOf, found);
            calls++;
        }
        OptMetrics.add("pointBatch.saved", targets.size() - (long) calls);
        for (int i = 0; i < targets.size(); i++) {
            apply.accept(targets.get(i), found.get(key(objectIds.get(i), infoCodes.get(i))));
        }
    }

    private static <V> void collect(List<V> rows, Function<V, String> objectIdOf, Function<V, String> infoCodeOf, Map<String, V> found) {
        if (rows == null) {
            return;
        }
        for (V v : rows) {
            if (v != null) {
                found.putIfAbsent(key(objectIdOf.apply(v), infoCodeOf.apply(v)), v);
            }
        }
    }

    private static String key(String objectId, String infoCode) {
        return objectId + "\n" + infoCode;
    }
}
