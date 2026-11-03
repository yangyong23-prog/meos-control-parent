package com.persagy.apccontrol.common.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class OptRecentKeysTest {

    @Test
    void marksAndExpires() throws InterruptedException {
        OptRecentKeys keys = new OptRecentKeys(100, TimeUnit.MILLISECONDS, 10);
        String k = OptRecentKeys.key("trace", "obj", "info");
        Assertions.assertEquals("trace|obj|info", k);
        Assertions.assertFalse(keys.contains(k));
        keys.mark(k);
        Assertions.assertTrue(keys.contains(k));
        Thread.sleep(250);
        Assertions.assertFalse(keys.contains(k));
    }
}
