package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.opt.OptSwitch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
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

    private final List<List<Map<String, Object>>> twinCalls = new ArrayList<>();

    /** 模拟一轮照明下发：Twin 成功与否由 twinOk 决定 */
    private boolean sendRound(List<Map<String, Object>> orders, boolean twinOk) {
        return suppressor.send("space", orders, o -> (String) o.get("objectId"),
                twinCalls::add, batch -> {
                    twinCalls.add(batch);
                    return twinOk;
                });
    }

    @Test
    void twinFailureMakesNextRoundResend() {
        List<Map<String, Object>> changed = Collections.singletonList(order("Sp1", 1));
        Assertions.assertFalse(sendRound(changed, false), "Twin 返回失败");
        Assertions.assertTrue(sendRound(changed, true));
        Assertions.assertEquals(2, twinCalls.size(), "失败后下一轮立即重发，不等满 N 轮");
        Assertions.assertTrue(sendRound(changed, true));
        Assertions.assertEquals(2, twinCalls.size(), "成功后恢复不重发");
    }

    @Test
    void twinExceptionMakesNextRoundResendAndPropagates() {
        List<Map<String, Object>> changed = Collections.singletonList(order("Sp1", 1));
        Assertions.assertThrows(IllegalStateException.class, () -> suppressor.send("space", changed,
                o -> (String) o.get("objectId"), b -> { }, b -> {
                    throw new IllegalStateException("twin down");
                }));
        Assertions.assertTrue(sendRound(changed, true));
        Assertions.assertEquals(1, twinCalls.size());
    }

    @Test
    void switchOffSendsEverythingThroughPlainSender() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        List<Map<String, Object>> orders = Collections.singletonList(order("Sp1", 1));
        List<String> plain = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            suppressor.send("space", orders, o -> (String) o.get("objectId"), b -> plain.add("plain"), b -> {
                throw new AssertionError("开关关闭时不走新路径");
            });
        }
        Assertions.assertEquals(Arrays.asList("plain", "plain"), plain);
        suppressor.send("space", Collections.<Map<String, Object>>emptyList(), o -> (String) o.get("objectId"), b -> plain.add("empty"), b -> true);
        Assertions.assertEquals(2, plain.size(), "空指令与原逻辑一样不调 Twin");
    }
}
