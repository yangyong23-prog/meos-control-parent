package com.persagy.apccontrol.basic.opt;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * opt 分支 B10：复用查询结果时给调用方独立副本。
 * <p>
 * 列表新建（调用方可 add/removeIf），每行 JSONObject 浅拷贝（调用方可 put/remove 顶层字段），
 * 与每次重新查询得到的新对象在调用方看来没有区别。
 */
public final class OptJsonRows {

    private OptJsonRows() {
    }

    public static List<JSONObject> copyOf(List<JSONObject> rows) {
        if (rows == null) {
            return null;
        }
        List<JSONObject> out = new ArrayList<>(rows.size());
        for (JSONObject o : rows) {
            out.add(o == null ? null : (JSONObject) o.clone());
        }
        return out;
    }
}
