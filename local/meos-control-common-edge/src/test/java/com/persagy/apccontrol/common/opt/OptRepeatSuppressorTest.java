package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.opt.OptSwitch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptRepeatSuppressorTest {

    private final OptRepeatSuppressor suppressor = new OptRepeatSuppressor();

    @BeforeEach
    void on() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.lightingSendDiff", "true"));
        suppressor.setResendRounds(3);
    }

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    private static Map<String, Object> order(String id, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("objectId", id);
        m.put("value", value);
        return m;
    }

    private List<Map<String, Object>> round(List<Map<String, Object>> orders) {
        return suppressor.filter("space", orders, o -> (String) o.get("objectId"));
    }

    @Test
    void sameOrderIsSuppressedAndForcedEveryNRounds() {
        List<Map<String, Object>> orders = Arrays.asList(order("Sp1", 1), order("Sp2", 2));
        Assertions.assertEquals(2, round(orders).size());
        Assertions.assertTrue(round(orders).isEmpty());
        Assertions.assertTrue(round(orders).isEmpty());
        Assertions.assertEquals(2, round(orders).size(), "距上次下发满 3 轮强制重发");
    }

    @Test
    void changedOrClearOrderIsSentImmediately() {
        round(Arrays.asList(order("Sp1", 1), order("Sp2", 2)));
        List<Map<String, Object>> r = round(Arrays.asList(order("Sp1", 1), order("Sp2", null)));
        Assertions.assertEquals(Collections.singletonList(order("Sp2", null)), r);
    }

    @Test
    void forgetMakesNextRoundResend() {
        List<Map<String, Object>> orders = Collections.singletonList(order("Sp1", 1));
        round(orders);
        suppressor.forget("space", orders, o -> (String) o.get("objectId"));
        Assertions.assertEquals(1, round(orders).size());
    }

    @Test
    void offOrOtherScopeIsUnaffected() {
        List<Map<String, Object>> orders = Collections.singletonList(order("Sp1", 1));
        round(orders);
        Assertions.assertEquals(1, suppressor.filter("circuit", orders, o -> (String) o.get("objectId")).size());
        OptSwitch.setEnvironmentForTest(null);
        Assertions.assertSame(orders, round(orders));
    }
}
