package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

public class OptMetricsTest {

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
        OptMetrics.drain();
    }

    @Test
    void countsOnlyWhenMetricsSwitchOn() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        OptMetrics.drain();
        OptMetrics.inc("a");
        Assertions.assertFalse(OptMetrics.drain().containsKey("a"));

        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.metrics", "true"));
        OptMetrics.inc("a");
        OptMetrics.add("a", 2);
        Map<String, Long> snapshot = OptMetrics.drain();
        Assertions.assertEquals(3L, snapshot.get("a"));
        // drain 后清零
        Assertions.assertFalse(OptMetrics.drain().containsKey("a"));
    }

    @Test
    void incAlwaysIgnoresSwitch() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        OptMetrics.drain();
        OptMetrics.incAlways("x.drop");
        Assertions.assertEquals(1L, OptMetrics.drain().get("x.drop"));
    }
}
