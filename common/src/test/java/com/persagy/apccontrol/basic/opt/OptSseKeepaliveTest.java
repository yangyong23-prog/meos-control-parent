package com.persagy.apccontrol.basic.opt;

import com.persagy.apccontrol.basic.sse.SseEmitters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OptSseKeepaliveTest {

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final SseEmitters sseEmitters = new SseEmitters();
    private final OptSseKeepalive keepalive = new OptSseKeepalive(new OptSseLeases(now::get));

    OptSseKeepaliveTest() {
        ReflectionTestUtils.setField(keepalive, "sseEmitters", sseEmitters);
        ReflectionTestUtils.setField(keepalive, "leaseSeconds", 60L);
        keepalive.setEmitterTimeoutMinutes(30);
    }

    @AfterEach
    void reset() {
        OptSwitch.setEnvironmentForTest(null);
    }

    private static void on() {
        OptSwitch.setEnvironmentForTest(new MockEnvironment().withProperty("switch.opt.sseKeepalive", "true"));
    }

    /** 记录 send 次数；broken 时抛 IOException 模拟连接已断 */
    static class FakeEmitter extends SseEmitter {
        final AtomicInteger sends = new AtomicInteger();
        final boolean broken;

        FakeEmitter(boolean broken) {
            this.broken = broken;
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            sends.incrementAndGet();
            if (broken) {
                throw new IOException("Broken pipe");
            }
        }

        @Override
        public void send(Object object) throws IOException {
            send(event().data(object));
        }

        @Override
        public synchronized void completeWithError(Throwable ex) {
        }

        @Override
        public synchronized void complete() {
        }
    }

    private void bind(String key, SseEmitter e) {
        sseEmitters.getMultiEmitters().computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(e);
    }

    @Test
    void offMeansNoTimeoutAndNoHeartbeat() {
        FakeEmitter e = new FakeEmitter(false);
        bind("k", e);
        Assertions.assertEquals(0L, OptSseKeepalive.emitterTimeout());
        keepalive.heartbeat();
        keepalive.ping("k");
        Assertions.assertEquals(0, e.sends.get());
    }

    @Test
    void heartbeatOncePerConnectionAndRemovesBroken() {
        on();
        Assertions.assertEquals(30 * 60_000L, OptSseKeepalive.emitterTimeout());
        FakeEmitter shared = new FakeEmitter(false);
        FakeEmitter dead = new FakeEmitter(true);
        bind("k1", shared);
        bind("k2", shared);
        bind("k2", dead);
        keepalive.heartbeat();
        Assertions.assertEquals(1, shared.sends.get(), "多订阅共用的连接只发一次");
        Set<String> keys = sseEmitters.getMultiEmitters().keySet();
        Assertions.assertTrue(keys.contains("k1") && keys.contains("k2"));
        Assertions.assertFalse(sseEmitters.getMultiEmitters().get("k2").contains(dead));
    }

    @Test
    void expiredLeaseClosesOnlyPingingClients() {
        on();
        bind("pinging", new FakeEmitter(false));
        bind("legacy", new FakeEmitter(false));
        keepalive.ping("pinging");
        now.addAndGet(30_000);
        keepalive.heartbeat();
        Assertions.assertTrue(sseEmitters.getMultiEmitters().containsKey("pinging"), "租约内保留");
        now.addAndGet(40_000);
        keepalive.heartbeat();
        Assertions.assertFalse(sseEmitters.getMultiEmitters().containsKey("pinging"), "超过 60 秒未 ping 回收");
        Assertions.assertTrue(sseEmitters.getMultiEmitters().containsKey("legacy"), "不 ping 的老前端不受影响");
    }
}
