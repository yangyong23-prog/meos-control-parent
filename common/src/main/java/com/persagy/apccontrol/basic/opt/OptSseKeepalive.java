package com.persagy.apccontrol.basic.opt;

import com.persagy.apccontrol.basic.sse.SseEmitters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * opt 分支 B9（switch.opt.sseKeepalive，默认关）：SSE 连接保活与回收。
 * <ul>
 *     <li>新建 emitter 使用 {@code opt.sse.emitter-timeout-minutes}（默认 30）超时，替代永不超时；浏览器 EventSource 超时后自动重连；</li>
 *     <li>每 {@code opt.sse.heartbeat-seconds}（默认 30）秒给每个连接发一条 SSE 注释心跳，写失败即回收；</li>
 *     <li>前端调用 {@code GET /server-event/sse/ping?eventKey=} 后进入租约管理，超过 {@code opt.sse.lease-seconds}（默认 60）
 *     秒没再 ping 的连接被回收（拔网线场景）。不 ping 的老前端不受租约影响。</li>
 * </ul>
 * 开关关闭时：emitter 仍为永不超时，心跳任务直接返回，ping 不记录。
 */
@Slf4j
@Component
public class OptSseKeepalive {

    private static final long NO_TIMEOUT = 0L;
    private static volatile long emitterTimeoutMillis = TimeUnit.MINUTES.toMillis(30);

    private final OptSseLeases leases;

    @Lazy
    @Resource
    private SseEmitters sseEmitters;

    @Value("${opt.sse.lease-seconds:60}")
    private long leaseSeconds;

    public OptSseKeepalive() {
        this(new OptSseLeases(System::currentTimeMillis));
    }

    OptSseKeepalive(OptSseLeases leases) {
        this.leases = leases;
    }

    @Value("${opt.sse.emitter-timeout-minutes:30}")
    public void setEmitterTimeoutMinutes(long minutes) {
        emitterTimeoutMillis = TimeUnit.MINUTES.toMillis(Math.max(minutes, 0));
    }

    /**
     * 新建 emitter 的超时毫秒数；开关关闭时为 0（永不超时，与原行为一致）。
     */
    public static long emitterTimeout() {
        return OptSwitch.on(OptSwitch.SSE_KEEPALIVE) ? emitterTimeoutMillis : NO_TIMEOUT;
    }

    public void ping(String eventKey) {
        if (OptSwitch.on(OptSwitch.SSE_KEEPALIVE)) {
            leases.ping(eventKey);
        }
    }

    @Scheduled(initialDelayString = "${opt.sse.heartbeat-seconds:30}000", fixedDelayString = "${opt.sse.heartbeat-seconds:30}000")
    public void heartbeat() {
        if (!OptSwitch.on(OptSwitch.SSE_KEEPALIVE)) {
            return;
        }
        Map<String, ? extends List<SseEmitter>> all = sseEmitters.getMultiEmitters();
        reapExpired(all);
        // 同一个 emitter 可能绑定多个 eventKey（POST 多订阅），每个连接只发一次
        Set<SseEmitter> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Object[]> failed = new ArrayList<>();
        all.forEach((eventKey, emitters) -> {
            for (SseEmitter emitter : emitters) {
                if (!seen.add(emitter)) {
                    continue;
                }
                try {
                    emitter.send(SseEmitter.event().comment("hb"));
                    OptMetrics.inc("sse.heartbeat");
                } catch (Exception e) {
                    failed.add(new Object[]{eventKey, emitter, e});
                }
            }
        });
        for (Object[] f : failed) {
            close((String) f[0], (SseEmitter) f[1], (Exception) f[2]);
            OptMetrics.incAlways("sse.heartbeatFail");
        }
    }

    private void reapExpired(Map<String, ? extends List<SseEmitter>> all) {
        for (String eventKey : leases.drainExpired(TimeUnit.SECONDS.toMillis(leaseSeconds))) {
            List<SseEmitter> emitters = all.get(eventKey);
            if (emitters == null) {
                continue;
            }
            for (SseEmitter emitter : new ArrayList<>(emitters)) {
                close(eventKey, emitter, null);
                OptMetrics.incAlways("sse.leaseExpired");
            }
        }
    }

    private void close(String eventKey, SseEmitter emitter, Exception cause) {
        try {
            if (cause == null) {
                emitter.complete();
            } else {
                emitter.completeWithError(cause);
            }
        } catch (Exception ignore) {
            // 连接已断开时 complete 可能抛异常，后面仍要从集合移除
            log.debug("[opt] 关闭 SSE 连接异常: {}", ignore.toString());
        }
        sseEmitters.removeSseEmitter(eventKey, emitter);
    }
}
