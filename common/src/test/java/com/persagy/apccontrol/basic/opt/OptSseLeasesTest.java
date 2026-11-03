package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class OptSseLeasesTest {

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final OptSseLeases leases = new OptSseLeases(now::get);

    @Test
    void expiresOnlyKeysThatStoppedPinging() {
        leases.ping("a");
        leases.ping("b");
        now.addAndGet(40_000);
        leases.ping("b");
        now.addAndGet(30_000);
        Assertions.assertEquals(Collections.singletonList("a"), leases.drainExpired(60_000));
        Assertions.assertFalse(leases.isTracked("a"), "过期后移除，避免重复回收");
        Assertions.assertTrue(leases.isTracked("b"));
        Assertions.assertTrue(leases.drainExpired(60_000).isEmpty());
    }

    @Test
    void untrackedKeysAndDisabledLeaseNeverExpire() {
        leases.ping("a");
        leases.ping(null);
        leases.ping("");
        now.addAndGet(3_600_000);
        Assertions.assertTrue(leases.drainExpired(0).isEmpty(), "≤0 不启用");
        Assertions.assertEquals(1, leases.size());
        leases.forget("a");
        Assertions.assertTrue(leases.drainExpired(60_000).isEmpty());
    }
}
