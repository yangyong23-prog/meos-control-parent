package com.persagy.apccontrol.basic.opt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * opt 分支 B10：把“每个源对象查一次关系”合并为“一批源对象查一次”后，再按源对象拆回去。
 * <p>
 * 批量结果每一行的关系对端（relObjIds）标明它属于哪些源对象。拆分时按行的原顺序放入各源对象的列表，
 * 并通过 copyForSource 为每个源对象生成独立副本（关系对端只保留该源对象），与单独查询时的返回一致。
 * 只要有一行的对端为空、无法归属，就返回 null，调用方应退回逐个查询。
 */
public final class OptRelationBatch {

    private OptRelationBatch() {
    }

    /**
     * @param sources       本批的源对象 id
     * @param rows          批量查询结果
     * @param relIdsOf      取一行的关系对端
     * @param copyForSource 为某个源对象生成该行的副本
     * @return 源对象 id → 行列表（每个源对象都有条目，可能为空）；无法归属时返回 null
     */
    public static <T> Map<String, List<T>> splitBySource(Collection<String> sources, List<T> rows,
                                                         Function<T, Set<String>> relIdsOf,
                                                         BiFunction<T, String, T> copyForSource) {
        Map<String, List<T>> out = new LinkedHashMap<>();
        for (String s : sources) {
            out.put(s, new ArrayList<>());
        }
        if (rows == null) {
            return out;
        }
        for (T row : rows) {
            Set<String> rel = relIdsOf.apply(row);
            if (rel == null || rel.isEmpty()) {
                return null;
            }
            boolean attributed = false;
            for (String s : rel) {
                List<T> list = out.get(s);
                if (list != null) {
                    list.add(copyForSource.apply(row, s));
                    attributed = true;
                }
            }
            if (!attributed) {
                return null;
            }
        }
        return out;
    }
}
