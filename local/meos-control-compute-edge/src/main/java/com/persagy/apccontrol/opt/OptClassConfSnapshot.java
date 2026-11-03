package com.persagy.apccontrol.opt;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * opt 分支 B8：InnerSchdule 设备频率配置（saveClassConfs）的快照维护，按 (classCode, paramCode) 合并。
 */
public final class OptClassConfSnapshot {

    private OptClassConfSnapshot() {
    }

    /** 上一版快照 + 本轮写入，同一 (classCode, paramCode) 以本轮为准 */
    public static List<JSONObject> merge(Collection<JSONObject> previous, Collection<JSONObject> written) {
        Map<String, JSONObject> byKey = new LinkedHashMap<>();
        if (previous != null) {
            previous.forEach(o -> put(byKey, o));
        }
        if (written != null) {
            written.forEach(o -> put(byKey, o));
        }
        return new ArrayList<>(byKey.values());
    }

    /** 去掉已删除的 (classCode, paramCode) */
    public static List<JSONObject> remove(Collection<JSONObject> snapshot, Collection<JSONObject> deleted) {
        Map<String, JSONObject> byKey = new LinkedHashMap<>();
        if (snapshot != null) {
            snapshot.forEach(o -> put(byKey, o));
        }
        if (deleted != null) {
            deleted.forEach(o -> byKey.remove(key(o)));
        }
        return new ArrayList<>(byKey.values());
    }

    private static void put(Map<String, JSONObject> byKey, JSONObject o) {
        if (o != null) {
            byKey.put(key(o), o);
        }
    }

    private static String key(JSONObject o) {
        return o.getString("classCode") + "|" + o.getString("paramCode");
    }
}
