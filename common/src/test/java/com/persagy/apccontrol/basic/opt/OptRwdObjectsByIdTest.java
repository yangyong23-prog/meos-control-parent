package com.persagy.apccontrol.basic.opt;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OptRwdObjectsByIdTest {

    private static JSONObject space(String id, int valid) {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("classCode", "space");
        o.put("valid", valid);
        return o;
    }

    /** 模拟 RWD 空间表：1200 个空间，其中每 7 个有一个 valid=0 */
    private static final List<JSONObject> ALL = new ArrayList<>();

    static {
        for (int i = 0; i < 1200; i++) {
            ALL.add(space("Sp" + i, i % 7 == 0 ? 0 : 1));
        }
    }

    /** 与 queryObjectList(classCode=space, valid=1) 相同的筛选，可再加 id 条件 */
    private static List<JSONObject> rwd(List<String> ids) {
        return ALL.stream().filter(o -> o.getIntValue("valid") == 1 && (ids == null || ids.contains(o.getString("id"))))
                .collect(Collectors.toList());
    }

    /** convertEquipmentInfo 中 space_info 的用法：按设备 → 空间关系逐个取，首次出现加入 spaceList */
    private static List<String> spaceList(Map<String, String> objectSpace, Map<String, JSONObject> spaceInfo) {
        List<String> out = new ArrayList<>();
        for (String spaceId : objectSpace.values()) {
            if (spaceInfo.containsKey(spaceId) && !out.contains(spaceId)) {
                out.add(spaceId);
            }
        }
        return out;
    }

    private static Map<String, JSONObject> index(List<JSONObject> rows) {
        Map<String, JSONObject> m = new HashMap<>();
        rows.forEach(o -> m.putIfAbsent(o.getString("id"), o));
        return m;
    }

    @Test
    void byIdGivesSameSpacesAsFullList() {
        Map<String, String> objectSpace = new LinkedHashMap<>();
        for (int i = 0; i < 900; i++) {
            objectSpace.put("Eq" + i, "Sp" + (i * 3 % 1300));   // 有重复、有 valid=0、有不存在的空间
        }
        List<String> before = spaceList(objectSpace, index(rwd(null)));
        List<Integer> batchSizes = new ArrayList<>();
        Function<List<String>, List<JSONObject>> byId = ids -> {
            batchSizes.add(ids.size());
            return rwd(ids);
        };
        List<String> after = spaceList(objectSpace, index(OptRwdObjectsById.batched(objectSpace.values(), byId)));
        Assertions.assertEquals(before, after, "改造前后 spaceList 一致");
        Assertions.assertTrue(batchSizes.stream().allMatch(n -> n <= OptRwdObjectsById.BATCH));
    }

    @Test
    void emptyIdsDoNotCall() {
        Assertions.assertTrue(OptRwdObjectsById.batched(Collections.emptyList(), ids -> {
            throw new AssertionError("不应调用");
        }).isEmpty());
        Assertions.assertEquals(1, OptRwdObjectsById.batched(Arrays.asList("Sp1", "Sp1"), ids -> {
            Assertions.assertEquals(1, ids.size(), "去重");
            return rwd(ids);
        }).size());
    }
}
