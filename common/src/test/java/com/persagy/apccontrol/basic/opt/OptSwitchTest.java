package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class OptSwitchTest {

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    @Test
    void defaultsToOffWhenUnsetOrNoEnvironment() {
        Assertions.assertFalse(OptSwitch.on("anything"));
        OptSwitch.setEnvironmentForTest(new MockEnvironment());
        Assertions.assertFalse(OptSwitch.on("anything"));
        Assertions.assertEquals("off", OptSwitch.value("mode", "off"));
        Assertions.assertEquals(7L, OptSwitch.number("n", 7L));
    }

    @Test
    void readsLiveEnvironmentValues() {
        MockEnvironment env = new MockEnvironment();
        new OptSwitch(env);
        env.setProperty("switch.opt.metrics", "true");
        env.setProperty("switch.opt.mode", " verify ");
        env.setProperty("switch.opt.n", "42");
        env.setProperty("switch.opt.bad", "x");
        Assertions.assertTrue(OptSwitch.on(OptSwitch.METRICS));
        Assertions.assertEquals("verify", OptSwitch.value("mode", "off"));
        Assertions.assertEquals(42L, OptSwitch.number("n", 1L));
        Assertions.assertEquals(1L, OptSwitch.number("bad", 1L));
        // 刷新后立即生效
        env.setProperty("switch.opt.metrics", "false");
        Assertions.assertFalse(OptSwitch.on(OptSwitch.METRICS));
    }
}
