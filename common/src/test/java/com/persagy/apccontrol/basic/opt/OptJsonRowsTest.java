package com.persagy.apccontrol.basic.opt;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class OptJsonRowsTest {

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    private static JSONObject row(String id) {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("classCode", "ACCCCC");
        return o;
    }

    /** 模拟 ShellConfigServiceImpl：同一关系被多个配置项查询，调用方会 removeIf 并改字段 */
    private static List<List<String>> run(boolean on, AtomicInteger calls) {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.shellConfigRelationMemo", String.valueOf(on)));
        OptCallMemo<String, Object> memo = OptCallMemo.create("t", OptSwitch.SHELL_CONFIG_RELATION_MEMO);
        Function<String, List<JSONObject>> rwd = k -> {
            calls.incrementAndGet();
            List<JSONObject> l = new ArrayList<>();
            l.add(row("C1"));
            l.add(row("C2"));
            l.add(row("C3"));
            return l;
        };
        List<List<String>> seen = new ArrayList<>();
        for (int item = 0; item < 4; item++) {
            @SuppressWarnings("unchecked")
            List<JSONObject> rows = memo.enabled() ? OptJsonRows.copyOf((List<JSONObject>) memo.get("rel", rwd::apply)) : rwd.apply("rel");
            List<String> ids = new ArrayList<>();
            for (JSONObject r : rows) {
                ids.add(r.getString("id") + ":" + r.getString("mark"));
            }
            seen.add(ids);
            final int n = item;
            rows.removeIf(r -> r.getString("id").equals("C" + (n % 3 + 1)));
            rows.forEach(r -> r.put("mark", "x"));
        }
        return seen;
    }

    @Test
    void reusedResultsBehaveLikeFreshQueries() {
        AtomicInteger off = new AtomicInteger();
        AtomicInteger on = new AtomicInteger();
        Assertions.assertEquals(run(false, off), run(true, on), "调用方修改不影响后续配置项看到的结果");
        Assertions.assertEquals(4, off.get());
        Assertions.assertEquals(1, on.get());
    }

    @Test
    void nullStaysNull() {
        Assertions.assertNull(OptJsonRows.copyOf(null));
    }
}
