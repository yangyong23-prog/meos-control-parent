package com.persagy.apccontrol.basic.utils.systemgraph;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.Pair;

public class FieldUtils {

    public static Pair<Object, Object> getRegion(JSONObject obj) {
        Object gt = obj.get("$gte");
        if (gt == null) {
            gt = obj.get("$gt");
        }

        Object lt = obj.get("$lte");
        if (lt == null) {
            lt = obj.get("$lt");
        }
        return Pair.of(gt, lt);
    }

}
